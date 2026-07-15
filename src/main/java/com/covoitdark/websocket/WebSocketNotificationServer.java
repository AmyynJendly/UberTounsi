package com.covoitdark.websocket;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Feature 14 – Pure-Java WebSocket server for live push notifications.
 *
 * Implements the WebSocket handshake (RFC 6455) and text-frame sending
 * without any external library — uses only java.net.ServerSocket.
 *
 * Demonstrates:
 *  - Collections framework: ConcurrentHashMap to track connected clients per user
 *  - Map: Map<Integer, List<PrintWriter>> userId → open sockets
 *  - Stream: broadcast uses stream().forEach()
 *  - SOLID / Single Responsibility: only handles WebSocket lifecycle
 *  - Open/Closed Principle: message format is extensible via sendToUser()
 *
 * Usage: call WebSocketNotificationServer.getInstance().start() once from App.main().
 * After NotificationDAO.create(), call WebSocketNotificationServer.push(userId, json).
 */
public class WebSocketNotificationServer {

    private static final int WS_PORT = 8081;
    private static WebSocketNotificationServer INSTANCE;

    /** Map of userId → list of active WebSocket output streams. */
    private final Map<Integer, List<DataOutputStream>> clients = new ConcurrentHashMap<>();

    private WebSocketNotificationServer() {}

    public static synchronized WebSocketNotificationServer getInstance() {
        if (INSTANCE == null) INSTANCE = new WebSocketNotificationServer();
        return INSTANCE;
    }

    /** Start the WebSocket listener in a daemon thread. */
    public void start() {
        Thread t = new Thread(this::listenLoop, "ws-notification-server");
        t.setDaemon(true);
        t.start();
        System.out.println("🔌 WebSocket notification server on ws://localhost:" + WS_PORT);
    }

    /**
     * Push a JSON notification string to all open connections for userId.
     * The front-end connects to ws://localhost:8081?userId=<id>
     */
    public void push(int userId, String json) {
        List<DataOutputStream> streams = clients.get(userId);
        if (streams == null || streams.isEmpty()) return;
        // Stream: send frame to every open connection
        List<DataOutputStream> dead = streams.stream()
                .filter(out -> !sendFrame(out, json))
                .collect(Collectors.toList());
        streams.removeAll(dead);
    }

    /** Broadcast to all connected clients (admin broadcast, etc.). */
    public void broadcast(String json) {
        clients.values().stream()
                .flatMap(Collection::stream)
                .forEach(out -> sendFrame(out, json));
    }

    private void listenLoop() {
        try (ServerSocket server = new ServerSocket(WS_PORT)) {
            while (true) {
                Socket socket = server.accept();
                Thread handler = new Thread(() -> handleConnection(socket), "ws-client");
                handler.setDaemon(true);
                handler.start();
            }
        } catch (IOException e) {
            System.err.println("WebSocket server error: " + e.getMessage());
        }
    }

    private void handleConnection(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // Read HTTP upgrade request and extract userId query param
            Map<String, String> headers = new LinkedHashMap<>();
            String requestLine = in.readLine();
            String userId = extractUserId(requestLine);
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                int colon = line.indexOf(':');
                if (colon > 0) headers.put(line.substring(0, colon).trim(), line.substring(colon + 1).trim());
            }

            // Perform WebSocket handshake
            String key = headers.get("Sec-WebSocket-Key");
            if (key == null || userId == null) { socket.close(); return; }
            String acceptKey = generateAcceptKey(key);
            String response = "HTTP/1.1 101 Switching Protocols\r\n"
                    + "Upgrade: websocket\r\n"
                    + "Connection: Upgrade\r\n"
                    + "Sec-WebSocket-Accept: " + acceptKey + "\r\n\r\n";
            out.write(response.getBytes(StandardCharsets.UTF_8));
            out.flush();

            // Register connection
            int uid = Integer.parseInt(userId);
            clients.computeIfAbsent(uid, k -> new CopyOnWriteArrayList<>()).add(out);

            // Keep-alive read loop (consume ping frames, detect close)
            InputStream raw = socket.getInputStream();
            while (!socket.isClosed()) {
                int firstByte = raw.read();
                if (firstByte == -1) break;
                int secondByte = raw.read();
                int payloadLen = secondByte & 0x7F;
                // Skip masking key + payload (client→server frames are masked)
                byte[] mask = new byte[4];
                raw.read(mask);
                if (payloadLen > 0) raw.readNBytes(payloadLen);
                int opcode = firstByte & 0x0F;
                if (opcode == 0x8) break; // close frame
            }

            clients.getOrDefault(uid, List.of()).remove(out);
            socket.close();
        } catch (Exception ignored) {}
    }

    /** Send a WebSocket text frame (RFC 6455 §5.2). */
    private boolean sendFrame(DataOutputStream out, String text) {
        try {
            byte[] payload = text.getBytes(StandardCharsets.UTF_8);
            out.write(0x81);  // FIN=1, opcode=1 (text)
            if (payload.length <= 125) {
                out.write(payload.length);
            } else if (payload.length <= 65535) {
                out.write(126);
                out.writeShort(payload.length);
            } else {
                out.write(127);
                out.writeLong(payload.length);
            }
            out.write(payload);
            out.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private String extractUserId(String requestLine) {
        if (requestLine == null) return null;
        int q = requestLine.indexOf("userId=");
        if (q == -1) return null;
        int end = requestLine.indexOf(' ', q);
        String param = end == -1 ? requestLine.substring(q + 7) : requestLine.substring(q + 7, end);
        return param.replaceAll("[^0-9]", "");
    }

    private String generateAcceptKey(String key) throws Exception {
        String magic = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] digest = sha1.digest(magic.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(digest);
    }
}
