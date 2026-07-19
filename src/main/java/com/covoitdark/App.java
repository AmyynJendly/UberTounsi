package com.covoitdark;

import com.covoitdark.controllers.AuthController;
import com.covoitdark.websocket.WebSocketNotificationServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App {

    private static final int PORT = 8080;
    private static final AuthController authController = new AuthController(); // Ensure admin setup

    public static void main(String[] args) {
        try {
            // Seed DB with admin if not present
            authController.checkAdminDefault();

            // Feature 14: start WebSocket notification server on port 8081
            WebSocketNotificationServer.getInstance().start();

            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            // API Routes
            server.createContext("/api/auth/login", new LoginHandler());
            server.createContext("/api/auth/register", new RegisterHandler());
            server.createContext("/api/auth/current-user", new CurrentUserHandler());
            server.createContext("/api/auth/logout", new LogoutHandler());
            server.createContext("/api/wallet/add-funds", new AddFundsHandler());
            server.createContext("/api/wallet/withdraw", new WithdrawFundsHandler());
            server.createContext("/api/trips/book", new BookTripHandler());
            server.createContext("/api/admin/users", new AdminUsersHandler());
            server.createContext("/api/admin/stats", new AdminStatsHandler());
            server.createContext("/api/admin/block-user", new AdminBlockUserHandler());
            server.createContext("/api/admin/broadcast", new AdminBroadcastHandler());
            server.createContext("/api/admin/reports", new AdminReportsHandler());
            server.createContext("/api/admin/dismiss-report", new AdminDismissReportHandler());
            server.createContext("/api/reports/submit", new SubmitReportHandler());
            server.createContext("/api/trips/all", new FetchTripsHandler());
            server.createContext("/api/trips/search", new SearchTripsHandler());
            server.createContext("/api/trips/publish", new PublishTripHandler());
            server.createContext("/api/trips/my", new MyTripsHandler());
            server.createContext("/api/bookings/my", new MyBookingsHandler());
            server.createContext("/api/bookings/requests", new DriverRequestsHandler());
            server.createContext("/api/bookings/respond", new RespondBookingHandler());
            server.createContext("/api/bookings/verify", new VerifyCodeHandler());
            server.createContext("/api/cars/list", new ListCarsHandler());
            server.createContext("/api/cars/add", new AddCarHandler());
            server.createContext("/api/cars/update", new UpdateCarHandler());
            server.createContext("/api/cars/delete", new DeleteCarHandler());
            server.createContext("/api/bookings/delete", new DeleteBookingHandler());
            server.createContext("/api/bookings/delete-archived", new DeleteArchivedBookingsHandler());
            server.createContext("/api/notifications/list", new ListNotificationsHandler());
            server.createContext("/api/notifications/read", new NotificationReadHandler());
            server.createContext("/api/notifications/read-all", new NotificationReadAllHandler());
            server.createContext("/api/notifications/delete", new NotificationDeleteHandler());
            server.createContext("/api/notifications/delete-all", new NotificationDeleteAllHandler());
            server.createContext("/api/messages/history", new MessageHistoryHandler());
            server.createContext("/api/messages/send", new SendMessageHandler());
            server.createContext("/api/stats/my", new FetchStatsHandler());
            server.createContext("/api/ratings/add", new AddRatingHandler());
            server.createContext("/api/ratings/list", new ListRatingsHandler());
            server.createContext("/api/bookings/cancel", new CancelBookingHandler());
            server.createContext("/api/users/update-profile", new UpdateProfileHandler());
            server.createContext("/api/users/list", new ListUsersHandler());
        server.createContext("/api/trips/complete", new CompleteTripHandler());
            server.createContext("/api/trips/cancel", new CancelTripHandler());
            server.createContext("/api/trips/accepted-passengers", new AcceptedPassengersHandler());
            server.createContext("/api/searches/save", new SaveSearchHandler());
            server.createContext("/api/searches/my", new MySavedSearchesHandler());
            server.createContext("/api/searches/delete", new DeleteSavedSearchHandler());
            server.createContext("/api/badges/my", new MyBadgesHandler());
            server.createContext("/api/users/block", new ToggleBlockHandler());
            server.createContext("/api/users/is-blocked", new IsBlockedHandler());
            server.createContext("/api/users/delete-account", new DeleteAccountHandler());
            server.createContext("/api/trips/history", new TripHistoryHandler());
            server.createContext("/api/admin/delete-user", new AdminDeleteUserHandler());
            server.createContext("/api/admin/audit-log", new AuditLogHandler());
            server.createContext("/api/admin/sparkline", new SparklineHandler());
            server.createContext("/api/users/upload-id-doc", new UploadIdDocHandler());
            server.createContext("/api/admin/get-id-doc", new GetIdDocHandler());
            server.createContext("/api/admin/request-id-resubmit", new RequestIdResubmitHandler());

            // ── New Feature Routes ────────────────────────────────────────────
            // Feature 1 – Trip Recurrence
            server.createContext("/api/trips/occurrences", new TripOccurrencesHandler());
            // Feature 2 – Boarding Code Verification
            server.createContext("/api/bookings/verify-code", new VerifyBoardingCodeHandler());
            // Feature 3 – Trip Group Chat
            server.createContext("/api/chat/send", new TripChatSendHandler());
            server.createContext("/api/chat/messages", new TripChatMessagesHandler());
            // Feature 4 – OTP Verification
            server.createContext("/api/otp/generate", new OtpGenerateHandler());
            server.createContext("/api/otp/verify", new OtpVerifyHandler());
            // Feature 5 – ID Verification
            server.createContext("/api/admin/id-verifications", new IdVerificationListHandler());
            server.createContext("/api/admin/id-verify-approve", new IdVerifyApproveHandler());
            server.createContext("/api/admin/id-verify-reject", new IdVerifyRejectHandler());
            // Feature 7 – Favorite Drivers
            server.createContext("/api/favorites/add", new AddFavoriteHandler());
            server.createContext("/api/favorites/remove", new RemoveFavoriteHandler());
            server.createContext("/api/favorites/my", new MyFavoritesHandler());
            server.createContext("/api/favorites/is-favorite", new IsFavoriteHandler());
            // Feature 8 – Price Negotiation
            server.createContext("/api/bookings/counter-offer", new CounterOfferHandler());
            server.createContext("/api/bookings/accept-offer", new AcceptOfferHandler());
            server.createContext("/api/bookings/reject-offer", new RejectOfferHandler());
            // Feature 10 – Driver Earnings
            server.createContext("/api/stats/earnings", new DriverEarningsHandler());
            // Feature 12 – Promo Codes
            server.createContext("/api/promo/validate", new PromoValidateHandler());
            server.createContext("/api/promo/create", new PromoCreateHandler());
            server.createContext("/api/promo/list", new PromoListHandler());
            server.createContext("/api/promo/deactivate", new PromoDeactivateHandler());
            // Feature 13 – Disputes
            server.createContext("/api/disputes/file", new FileDisputeHandler());
            server.createContext("/api/disputes/list", new DisputeListHandler());
            server.createContext("/api/disputes/resolve", new DisputeResolveHandler());
            server.createContext("/api/disputes/dismiss", new DisputeDismissHandler());

            // Payment Methods
            server.createContext("/api/payments/list", new ListPaymentMethodsHandler());
            server.createContext("/api/payments/add", new AddPaymentMethodHandler());
            server.createContext("/api/payments/delete", new DeletePaymentMethodHandler());

            // Static Files
            server.createContext("/", new StaticFileHandler("web_ui", "/web_ui"));

            server.setExecutor(null); // default executor
            server.start();
            System.out.println("UberTounsi Web Server running strictly native on http://localhost:" + PORT);
            System.out.println("   -> Open http://localhost:" + PORT + "/auth.html to view Web APP!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ListPaymentMethodsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            com.covoitdark.dao.MoyenPaiementDAO dao = new com.covoitdark.dao.MoyenPaiementDAO();
            java.util.List<com.covoitdark.models.MoyenPaiement> list = dao.listByUser(userId);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                com.covoitdark.models.MoyenPaiement m = list.get(i);
                sb.append(String.format("{\"id\":%d,\"userId\":%d,\"type\":\"%s\",\"last4\":\"%s\",\"brand\":\"%s\",\"exp\":\"%s\"}",
                    m.getId(), m.getUserId(), m.getType(), m.getLastFour(), m.getBrand(), m.getExpiry()));
                if (i < list.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJsonResponse(exchange, sb.toString(), 200);
        }
    }

    static class AddPaymentMethodHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String type = extractJsonValue(input, "type");
            String last4 = extractJsonValue(input, "last4");
            String brand = extractJsonValue(input, "brand");
            String exp = extractJsonValue(input, "exp");
            com.covoitdark.models.MoyenPaiement mp = new com.covoitdark.models.MoyenPaiement(0, userId, type, last4, brand, exp);
            com.covoitdark.dao.MoyenPaiementDAO dao = new com.covoitdark.dao.MoyenPaiementDAO();
            if (dao.add(mp)) {
                sendJsonResponse(exchange, "{\"success\":true}", 200);
            } else {
                sendJsonResponse(exchange, "{\"error\":\"Erreur lors de l'ajout\"}", 500);
            }
        }
    }

    static class DeletePaymentMethodHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            int id = Integer.parseInt(extractJsonValue(input, "id"));
            com.covoitdark.dao.MoyenPaiementDAO dao = new com.covoitdark.dao.MoyenPaiementDAO();
            if (dao.delete(id, userId)) {
                sendJsonResponse(exchange, "{\"success\":true}", 200);
            } else {
                sendJsonResponse(exchange, "{\"error\":\"Erreur lors de la suppression\"}", 500);
            }
        }
    }

    static class DeleteBookingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int bookingId = Integer.parseInt(extractJsonValue(input, "id"));
                com.covoitdark.dao.RequestDAO rdao = new com.covoitdark.dao.RequestDAO();
                com.covoitdark.models.Request req = rdao.findById(bookingId);
                if (req == null || req.getPassengerId() != userId) {
                    sendJsonResponse(exchange, "{\"error\":\"Réservation non trouvée ou non autorisée\"}", 403);
                    return;
                }
                if (req.getStatus() == com.covoitdark.models.Request.Status.PENDING || req.getStatus() == com.covoitdark.models.Request.Status.ACCEPTED) {
                    sendJsonResponse(exchange, "{\"error\":\"Impossible de supprimer une réservation active\"}", 400);
                    return;
                }
                if (rdao.delete(bookingId)) sendJsonResponse(exchange, "{\"success\":true}", 200);
                else sendJsonResponse(exchange, "{\"error\":\"Erreur lors de la suppression\"}", 500);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Requête invalide\"}", 400); }
        }
    }

    static class DeleteArchivedBookingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            try {
                com.covoitdark.dao.RequestDAO rdao = new com.covoitdark.dao.RequestDAO();
                if (rdao.deleteArchivedByPassenger(userId)) sendJsonResponse(exchange, "{\"success\":true}", 200);
                else sendJsonResponse(exchange, "{\"error\":\"Aucune réservation archivée à supprimer\"}", 404);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Erreur DB\"}", 500); }
        }
    }

    // --- Notifications API ---

    private static void enableCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String identifier = extractJsonValue(input, "email");
                String password = extractJsonValue(input, "password");

                com.covoitdark.dao.UserDAO uDao = new com.covoitdark.dao.UserDAO();
                com.covoitdark.models.User userRef = uDao.findByEmailOrPhone(identifier);
                
                if (userRef != null && userRef.isBlocked()) {
                    sendJsonResponse(exchange, "{\"success\":false,\"error\":\"Ce compte a été bloqué par l'administrateur.\"}", 403);
                    return;
                }

                boolean success = authController.authentifier(identifier, password);
                String response = success ? "{\"success\":true}" : "{\"success\":false,\"error\":\"Email/Numéro ou mot de passe incorrect.\"}";
                
                sendJsonResponse(exchange, response, success ? 200 : 401);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String name = extractJsonValue(input, "name");
                String email = extractJsonValue(input, "email");
                String password = extractJsonValue(input, "password");
                String roleStr = extractJsonValue(input, "role");
                com.covoitdark.models.User.Role role = roleStr.equals("DRIVER") ? com.covoitdark.models.User.Role.DRIVER : com.covoitdark.models.User.Role.PASSENGER;

                boolean success = authController.creerCompte(name, email, password, "N/A", role);
                String response = success ? "{\"success\":true}" : "{\"success\":false,\"error\":\"Email déjà utilisé ou mot de passe non conforme.\"}";
                
                sendJsonResponse(exchange, response, success ? 200 : 400);
            }
        }
    }

    static class CurrentUserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                com.covoitdark.models.User user = com.covoitdark.utils.SessionManager.getInstance().getCurrentUser();
                if (user == null) {
                    sendJsonResponse(exchange, "{\"user\":null}", 200);
                    return;
                }
                com.covoitdark.dao.RequestDAO rdao = new com.covoitdark.dao.RequestDAO();
                double reserved = rdao.getReservedAmount(user.getId());
                
                String response = "{\"user\":{" +
                    "\"id\":" + user.getId() +
                    ",\"name\":\"" + escapeJson(user.getFullName()) + "\"" +
                    ",\"email\":\"" + escapeJson(user.getEmail()) + "\"" +
                    ",\"phone\":\"" + escapeJson(user.getPhone() != null ? user.getPhone() : "") + "\"" +
                    ",\"role\":\"" + user.getRole() + "\"" +
                    ",\"balance\":" + user.getBalance() +
                    ",\"reservedBalance\":" + reserved +
                    ",\"isVerified\":" + user.isVerified() +
                    ",\"idVerified\":" + user.isIdVerified() +
                    ",\"bio\":\"" + escapeJson(user.getBio() != null ? user.getBio() : "") + "\"" +
                    ",\"avatar\":\"" + escapeJson(user.getAvatar() != null ? user.getAvatar() : "") + "\"" +
                    "}}";
                sendJsonResponse(exchange, response, 200);
            }
        }
    }

    static class LogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            authController.deconnecter();
            sendJsonResponse(exchange, "{\"success\":true}", 200);
        }
    }

    private static String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    private static String extractJsonValue(String input, String key) {
        String keyMarker = "\"" + key + "\"";
        int keyIndex = input.indexOf(keyMarker);
        if (keyIndex == -1) return "";

        int valueStart = input.indexOf(":", keyIndex) + 1;
        while (valueStart < input.length() && (Character.isWhitespace(input.charAt(valueStart)) || input.charAt(valueStart) == '"')) {
            valueStart++;
        }

        int valueEnd;
        if (valueStart > 0 && input.charAt(valueStart - 1) == '"') {
            valueEnd = input.indexOf("\"", valueStart);
        } else {
            valueEnd = valueStart;
            while (valueEnd < input.length() && !Character.isWhitespace(input.charAt(valueEnd)) && input.charAt(valueEnd) != ',' && input.charAt(valueEnd) != '}') {
                valueEnd++;
            }
        }

        if (valueStart >= 0 && valueEnd > valueStart) {
            String val = input.substring(valueStart, valueEnd);
            return val.replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return "";
    }

    private static void sendJsonResponse(HttpExchange exchange, String response, int code) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    static class AddFundsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) {
                sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401);
                return;
            }

            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String body = br.readLine();
            
            try {
                double amount = Double.parseDouble(extractJsonValue(body, "amount"));
                if (amount <= 0 || amount > 10000) {
                    sendJsonResponse(exchange, "{\"error\":\"Montant invalide (1-10000 TND)\"}", 400);
                    return;
                }
                com.covoitdark.dao.UserDAO userDAO = new com.covoitdark.dao.UserDAO();
                com.covoitdark.models.User u = userDAO.findByIdDirect(userId);

                if(u != null) {
                    double newBal = u.getBalance() + amount;
                    if(userDAO.updateBalance(userId, newBal)) {
                        u.setBalance(newBal);
                        com.covoitdark.utils.SessionManager.getInstance().setCurrentUser(u);
                        sendJsonResponse(exchange, "{\"success\":true,\"balance\":" + newBal + "}", 200);
                    } else {
                        sendJsonResponse(exchange, "{\"error\":\"Erreur DB\"}", 500);
                    }
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, "{\"error\":\"Requête invalide\"}", 400);
            }
        }
    }

    static class WithdrawFundsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                double amount = Double.parseDouble(extractJsonValue(body, "amount"));
                com.covoitdark.dao.UserDAO userDAO = new com.covoitdark.dao.UserDAO();
                com.covoitdark.dao.RequestDAO reqDAO = new com.covoitdark.dao.RequestDAO();
                
                com.covoitdark.models.User u = userDAO.findByIdDirect(userId);
                double reserved = reqDAO.getReservedAmount(userId);
                double available = u.getBalance() - reserved;

                if (amount > available) {
                    sendJsonResponse(exchange, "{\"error\":\"Montant supérieur au solde disponible (" + reserved + " TND sont réservés pour trajets actifs)\"}", 400);
                    return;
                }

                double newBal = u.getBalance() - amount;
                if (userDAO.updateBalance(userId, newBal)) {
                    u.setBalance(newBal);
                    com.covoitdark.utils.SessionManager.getInstance().setCurrentUser(u);
                    sendJsonResponse(exchange, "{\"success\":true, \"balance\":" + newBal + "}", 200);
                } else {
                    sendJsonResponse(exchange, "{\"error\":\"Erreur lors de la mise à jour\"}", 500);
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, "{\"error\":\"Requête invalide\"}", 400);
            }
        }
    }

    static class BookTripHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int tripId = Integer.parseInt(extractJsonValue(body, "tripId"));
                int qty = 1;
                try { qty = Integer.parseInt(extractJsonValue(body, "qty")); } catch(Exception e){}
                if (qty < 1 || qty > 8) {
                    sendJsonResponse(exchange, "{\"error\":\"Quantité invalide (1-8)\"}", 400);
                    return;
                }

                com.covoitdark.dao.TripDAO tripDAO = new com.covoitdark.dao.TripDAO();
                com.covoitdark.dao.UserDAO userDAO = new com.covoitdark.dao.UserDAO();
                com.covoitdark.dao.RequestDAO reqDAO = new com.covoitdark.dao.RequestDAO();
                com.covoitdark.dao.NotificationDAO nDAO = new com.covoitdark.dao.NotificationDAO();

                com.covoitdark.models.Trip trip = tripDAO.findByIdDirect(tripId);
                com.covoitdark.models.User user = userDAO.findByIdDirect(userId);

                if (trip == null || user == null) {
                    sendJsonResponse(exchange, "{\"error\":\"Trajet ou Utilisateur introuvable\"}", 404);
                    return;
                }

                if (trip.getDriverId() == userId) {
                    sendJsonResponse(exchange, "{\"error\":\"Vous ne pouvez pas réserver votre propre trajet\"}", 400);
                    return;
                }

                if (!trip.isActive()) {
                    sendJsonResponse(exchange, "{\"error\":\"Trajet non disponible\"}", 400);
                    return;
                }

                if (reqDAO.existsForPassengerAndTrip(userId, tripId)) {
                    sendJsonResponse(exchange, "{\"error\":\"Vous avez déjà une demande pour ce trajet\"}", 400);
                    return;
                }

                if (trip.getAvailableSeats() < qty) {
                    sendJsonResponse(exchange, "{\"error\":\"Pas assez de places\"}", 400);
                    return;
                }

                double total = trip.getPrice() * qty;
                if (user.getBalance() < total) {
                    sendJsonResponse(exchange, "{\"error\":\"Solde insuffisant\"}", 400);
                    return;
                }

                // Create Pending Request
                com.covoitdark.models.Request req = new com.covoitdark.models.Request();
                req.setTripId(tripId);
                req.setPassengerId(userId);
                req.setQuantity(qty);
                req.setTotalCost(total);
                req.setStatus(com.covoitdark.models.Request.Status.PENDING);
                
                // Generate secure PIN Code
                String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // No I, O, 0, 1 for readability
                StringBuilder code = new StringBuilder();
                java.util.Random rnd = new java.util.Random();
                for (int i = 0; i < 4; i++) {
                    code.append(chars.charAt(rnd.nextInt(chars.length())));
                }
                req.setSecretCode(code.toString());
                
                if (reqDAO.create(req)) {
                    // Notify Driver
                    com.covoitdark.models.Notification n = new com.covoitdark.models.Notification();
                    n.setUserId(trip.getDriverId());
                    n.setTitle("Nouvelle réservation");
                    n.setMessage(user.getFullName() + " a demandé " + qty + " place(s) pour votre trajet " + trip.getDeparture() + " -> " + trip.getArrival());
                    nDAO.create(n);
                    
                    // Notify Passenger
                    com.covoitdark.models.Notification n2 = new com.covoitdark.models.Notification();
                    n2.setUserId(userId);
                    n2.setTitle("Demande envoyée");
                    n2.setMessage("Votre demande pour le trajet " + trip.getDeparture() + " -> " + trip.getArrival() + " est en attente de confirmation.");
                    nDAO.create(n2);
                    
                    sendJsonResponse(exchange, "{\"success\":true, \"requestId\":" + req.getId() + "}", 200);
                } else {
                    sendJsonResponse(exchange, "{\"error\":\"Impossible de créer la demande\"}", 500);
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(exchange, "{\"error\":\"Requête invalide\"}", 400);
            }
        }
    }

    static class AdminUsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!com.covoitdark.utils.SessionManager.getInstance().isAdmin()) {
                sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 403);
                return;
            }
            
            com.covoitdark.dao.UserDAO userDAO = new com.covoitdark.dao.UserDAO();
            java.util.List<com.covoitdark.models.User> users = userDAO.findAll();
            
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < users.size(); i++) {
                com.covoitdark.models.User u = users.get(i);
                sb.append("{\"id\":").append(u.getId())
                  .append(",\"name\":\"").append(u.getFullName()).append("\"")
                  .append(",\"email\":\"").append(u.getEmail()).append("\"")
                  .append(",\"role\":\"").append(u.getRole()).append("\"")
                  .append(",\"balance\":").append(u.getBalance())
                  .append(",\"isBlocked\":").append(u.isBlocked())
                  .append(",\"isVerified\":").append(u.isVerified())
                  .append(",\"phone\":\"").append(u.getPhone() != null ? u.getPhone() : "").append("\"")
                  .append("}");
                if (i < users.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJsonResponse(exchange, sb.toString(), 200);
        }
    }

    static class ListUsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            
            com.covoitdark.dao.UserDAO userDAO = new com.covoitdark.dao.UserDAO();
            java.util.List<com.covoitdark.models.User> users = userDAO.findRelevantContacts(userId);
            
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < users.size(); i++) {
                com.covoitdark.models.User u = users.get(i);
                sb.append("{\"id\":").append(u.getId())
                  .append(",\"name\":\"").append(u.getFullName()).append("\"")
                  .append(",\"role\":\"").append(u.getRole()).append("\"")
                  .append(",\"avatar\":\"").append(u.getAvatar() != null ? u.getAvatar() : "").append("\"")
                  .append(",\"phone\":\"").append(u.getPhone() != null ? u.getPhone() : "").append("\"")
                  .append("}");
                if (i < users.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJsonResponse(exchange, sb.toString(), 200);
        }
    }

    static class AdminStatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!com.covoitdark.utils.SessionManager.getInstance().isAdmin()) {
                sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 403);
                return;
            }
            
            com.covoitdark.dao.UserDAO userDAO = new com.covoitdark.dao.UserDAO();
            com.covoitdark.dao.TripDAO tripDAO = new com.covoitdark.dao.TripDAO();
            com.covoitdark.dao.RequestDAO reqDAO = new com.covoitdark.dao.RequestDAO();
            java.util.List<com.covoitdark.models.User> allUsers = userDAO.findAll();
            java.util.List<com.covoitdark.models.Trip> allTrips = tripDAO.findAll();
            java.util.List<com.covoitdark.models.Request> allReqs = reqDAO.findAll();
            int totalUsers = allUsers.size();
            int totalTrips = allTrips.size();
            long activeTrips = allTrips.stream().filter(t -> t.getStatus() == com.covoitdark.models.Trip.Status.ACTIVE).count();
            long completedTrips = allTrips.stream().filter(t -> t.getStatus() == com.covoitdark.models.Trip.Status.COMPLETE).count();
            long cancelledTrips = allTrips.stream().filter(t -> t.getStatus() == com.covoitdark.models.Trip.Status.CANCELLED).count();
            long drivers = allUsers.stream().filter(u -> u.getRole() == com.covoitdark.models.User.Role.DRIVER).count();
            long passengers = allUsers.stream().filter(u -> u.getRole() == com.covoitdark.models.User.Role.PASSENGER).count();
            long blocked = allUsers.stream().filter(com.covoitdark.models.User::isBlocked).count();
            long activeBookings = allReqs.stream().filter(r -> r.getStatus() == com.covoitdark.models.Request.Status.ACCEPTED).count();
            double totalRevenue = allReqs.stream()
                .filter(r -> r.getStatus() == com.covoitdark.models.Request.Status.COMPLETE || r.getStatus() == com.covoitdark.models.Request.Status.ACCEPTED)
                .mapToDouble(com.covoitdark.models.Request::getTotalCost).sum();
            String response = "{\"totalUsers\":" + totalUsers
                + ",\"totalTrips\":" + totalTrips
                + ",\"activeTrips\":" + activeTrips
                + ",\"completedTrips\":" + completedTrips
                + ",\"cancelledTrips\":" + cancelledTrips
                + ",\"drivers\":" + drivers
                + ",\"passengers\":" + passengers
                + ",\"blockedUsers\":" + blocked
                + ",\"totalRevenue\":" + String.format("%.2f", totalRevenue)
                + ",\"activeBookings\":" + activeBookings + "}";
            sendJsonResponse(exchange, response, 200);
        }
    }

    static class AdminBlockUserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!com.covoitdark.utils.SessionManager.getInstance().isAdmin()) {
                sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 403);
                return;
            }
            
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String body = br.readLine();
            
            try {
                int userId = Integer.parseInt(extractJsonValue(body, "userId"));
                boolean block = Boolean.parseBoolean(extractJsonValue(body, "block"));
                
                com.covoitdark.dao.UserDAO userDAO = new com.covoitdark.dao.UserDAO();
                // Note: userDAO.setBlocked should exist
                if(userDAO.setBlocked(userId, block)) {
                    int adminId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
                    com.covoitdark.models.User target = userDAO.findByIdDirect(userId);
                    com.covoitdark.utils.AuditLogger.getInstance().log(adminId,
                        block ? "BLOCK_USER" : "UNBLOCK_USER", "USER", userId,
                        target != null ? target.getFullName() : null);
                    sendJsonResponse(exchange, "{\"success\":true}", 200);
                } else {
                    sendJsonResponse(exchange, "{\"error\":\"Erreur DB\"}", 500);
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, "{\"error\":\"Requête invalide\"}", 400);
            }
        }
    }

    static class SubmitReportHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int reportedId = Integer.parseInt(extractJsonValue(input, "reportedId"));
                String reason = extractJsonValue(input, "reason");
                com.covoitdark.dao.ReportDAO reportDAO = new com.covoitdark.dao.ReportDAO();
                com.covoitdark.models.Report report = new com.covoitdark.models.Report(0, userId, reportedId, reason);
                if(reportDAO.create(report)) sendJsonResponse(exchange, "{\"success\":true}", 200);
                else sendJsonResponse(exchange, "{\"error\":\"Erreur DB\"}", 500);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Requête invalide\"}", 400); }
        }
    }

    static class AdminReportsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!com.covoitdark.utils.SessionManager.getInstance().isAdmin()) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 403); return; }
            com.covoitdark.dao.ReportDAO reportDAO = new com.covoitdark.dao.ReportDAO();
            java.util.List<com.covoitdark.models.Report> reports = reportDAO.findAll();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < reports.size(); i++) {
                com.covoitdark.models.Report r = reports.get(i);
                sb.append("{\"id\":").append(r.getId())
                  .append(",\"reporterId\":").append(r.getReporterId())
                  .append(",\"reportedId\":").append(r.getReportedId())
                  .append(",\"reason\":\"").append(r.getReason() != null ? r.getReason().replace("\"", "'") : "").append("\"")
                  .append(",\"date\":\"").append(r.getCreatedAt()).append("\"}");
                if (i < reports.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJsonResponse(exchange, sb.toString(), 200);
        }
    }

    static class AdminDismissReportHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            if (!com.covoitdark.utils.SessionManager.getInstance().isAdmin()) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 403); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int id = Integer.parseInt(extractJsonValue(input, "id"));
                com.covoitdark.dao.ReportDAO reportDAO = new com.covoitdark.dao.ReportDAO();
                if(reportDAO.delete(id)) sendJsonResponse(exchange, "{\"success\":true}", 200);
                else sendJsonResponse(exchange, "{\"error\":\"Erreur DB\"}", 500);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Requête invalide\"}", 400); }
        }
    }

    static class FetchTripsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                enableCORS(exchange);
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
                com.covoitdark.dao.TripDAO tripDAO = new com.covoitdark.dao.TripDAO();
                java.util.List<com.covoitdark.models.Trip> trips = tripDAO.findAllActive();
                sendJsonResponse(exchange, tripsToJson(trips), 200);
            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(exchange, "[]", 200); // Return empty instead of crashing
            }
        }
    }

    static class SearchTripsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String dep = extractJsonValue(input, "dep");
            String arr = extractJsonValue(input, "arr");
            String dateStr = extractJsonValue(input, "date");
            
            String acStr      = extractJsonValue(input, "ac");
            String musicStr   = extractJsonValue(input, "music");
            String smokingStr = extractJsonValue(input, "smoking");
            String flexStr    = extractJsonValue(input, "flex");
            String womenStr   = extractJsonValue(input, "women");
            String petsStr    = extractJsonValue(input, "pets");
            String luggageStr = extractJsonValue(input, "luggage");
            String minSeatsStr= extractJsonValue(input, "minSeats");
            String priceMinStr= extractJsonValue(input, "priceMin");
            String priceMaxStr= extractJsonValue(input, "priceMax");

            java.time.LocalDate date = dateStr.isEmpty() ? java.time.LocalDate.now() : java.time.LocalDate.parse(dateStr);

            java.util.List<com.covoitdark.dao.filters.TripFilter> filters = new java.util.ArrayList<>();
            if ("true".equals(acStr))      filters.add(new com.covoitdark.dao.filters.AcFilter());
            if ("true".equals(musicStr))   filters.add(new com.covoitdark.dao.filters.MusicFilter());
            if ("true".equals(smokingStr)) filters.add(new com.covoitdark.dao.filters.SmokingFilter());
            if ("true".equals(flexStr))    filters.add(new com.covoitdark.dao.filters.FlexibleFilter());
            if ("true".equals(womenStr))   filters.add(new com.covoitdark.dao.filters.WomenOnlyFilter());
            if ("true".equals(petsStr))    filters.add(new com.covoitdark.dao.filters.PetsFilter());
            if (!luggageStr.isEmpty())     filters.add(new com.covoitdark.dao.filters.LuggageFilter(luggageStr));
            if (!minSeatsStr.isEmpty()) {
                int ms = Integer.parseInt(minSeatsStr);
                if (ms > 1) filters.add(new com.covoitdark.dao.filters.MinSeatsFilter(ms));
            }
            if (!priceMinStr.isEmpty() || !priceMaxStr.isEmpty()) {
                double pMin = priceMinStr.isEmpty() ? 0 : Double.parseDouble(priceMinStr);
                double pMax = priceMaxStr.isEmpty() ? 9999 : Double.parseDouble(priceMaxStr);
                filters.add(new com.covoitdark.dao.filters.PriceRangeFilter(pMin, pMax));
            }
            
            com.covoitdark.dao.TripDAO tripDAO = new com.covoitdark.dao.TripDAO();
            java.util.List<com.covoitdark.models.Trip> trips = tripDAO.search(dep, arr, date, filters);
            
            sendJsonResponse(exchange, tripsToJson(trips), 200);
        }
    }

    static class PublishTripHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                com.covoitdark.models.Trip t = new com.covoitdark.models.Trip();
                t.setDriverId(userId);
                String carIdStr = extractJsonValue(input, "carId");
                if (carIdStr.isEmpty() || carIdStr.equals("0")) {
                    sendJsonResponse(exchange, "{\"error\":\"Veuillez sélectionner un véhicule\"}", 400);
                    return;
                }
                t.setCarId(Integer.parseInt(carIdStr));
                t.setDeparture(extractJsonValue(input, "dep"));
                t.setArrival(extractJsonValue(input, "arr"));
                String depTimeStr = extractJsonValue(input, "depTime");
                t.setDepartureTime(depTimeStr.isEmpty() ? java.time.LocalTime.now() : java.time.LocalTime.parse(depTimeStr));
                String dateStr = extractJsonValue(input, "date");
                t.setStartDate(dateStr.isEmpty() ? java.time.LocalDate.now() : java.time.LocalDate.parse(dateStr));
                // Recurrence
                String endDateStr = extractJsonValue(input, "endDate");
                if (!endDateStr.isEmpty()) t.setEndDate(java.time.LocalDate.parse(endDateStr));
                String repeatDaysStr = extractJsonValue(input, "repeatDays");
                if (!repeatDaysStr.isEmpty()) {
                    java.util.List<String> days = new java.util.ArrayList<>();
                    for (String d : repeatDaysStr.split(",")) { String s = d.trim(); if (!s.isEmpty()) days.add(s); }
                    t.setRepeatDays(days);
                }
                String seatsStr = extractJsonValue(input, "seats");
                t.setAvailableSeats(seatsStr.isEmpty() ? 4 : Integer.parseInt(seatsStr));
                String priceStr = extractJsonValue(input, "price");
                t.setPrice(priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr));
                t.setStatus(com.covoitdark.models.Trip.Status.ACTIVE);

                // Extract Preferences
                t.setAcAvailable(extractJsonValue(input, "ac").equals("true"));
                t.setSmokingAllowed(extractJsonValue(input, "smoking").equals("true"));
                t.setFlexiblePickup(extractJsonValue(input, "pickup").equals("true") || extractJsonValue(input, "flex").equals("true"));
                t.setWomenOnly(extractJsonValue(input, "women").equals("true"));
                t.setPetsAllowed(extractJsonValue(input, "pets").equals("true"));
                
                String musicPref = extractJsonValue(input, "music");
                if (musicPref.equals("true")) t.setMusicPreference(com.covoitdark.models.Trip.MusicPreference.ANY);
                else t.setMusicPreference(com.covoitdark.models.Trip.MusicPreference.NONE);

                String luggagePref = extractJsonValue(input, "luggage");
                if (luggagePref.equals("true") || luggagePref.equals("LARGE")) t.setLuggagePolicy(com.covoitdark.models.Trip.LuggagePolicy.LARGE);
                else t.setLuggagePolicy(com.covoitdark.models.Trip.LuggagePolicy.SMALL);
                
                com.covoitdark.dao.TripDAO tripDAO = new com.covoitdark.dao.TripDAO();
                if (tripDAO.create(t)) {
                    // Smart Route Matching: Notify passengers with saved searches
                    com.covoitdark.dao.SavedSearchDAO searchDAO = new com.covoitdark.dao.SavedSearchDAO();
                    java.util.List<Integer> matchingPassengers = searchDAO.findMatchingPassengers(t.getDeparture(), t.getArrival());
                    if (!matchingPassengers.isEmpty()) {
                        com.covoitdark.dao.NotificationDAO notifDAO = new com.covoitdark.dao.NotificationDAO();
                        for (int passengerId : matchingPassengers) {
                            com.covoitdark.models.Notification n = new com.covoitdark.models.Notification();
                            n.setUserId(passengerId);
                            n.setTitle("Nouveau trajet trouvé !");
                            n.setMessage("Un nouveau trajet " + t.getDeparture() + " ➔ " + t.getArrival() + " vient d'être publié. Réservez vite !");
                            notifDAO.create(n);
                        }
                    }
                    sendJsonResponse(exchange, "{\"success\":true,\"tripId\":" + t.getId() + "}", 200);
                } else {
                    sendJsonResponse(exchange, "{\"error\":\"Erreur lors de la création\"}", 500);
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(exchange, "{\"error\":\"Données invalides\"}", 400);
            }
        }
    }

    static class AddCarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                enableCORS(exchange);
                if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
                int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
                if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
                
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String brand = extractJsonValue(body, "brand");
                String model = extractJsonValue(body, "model");
                String color = extractJsonValue(body, "color");
                String seatsStr = extractJsonValue(body, "seats");
                int seats = seatsStr.isEmpty() ? 4 : Integer.parseInt(seatsStr);
                String plate = extractJsonValue(body, "plate");
                String image = extractJsonValue(body, "image");
                if (plate.isEmpty()) plate = "TND-000";
                
                com.covoitdark.models.Car car = new com.covoitdark.models.Car(0, userId, brand, model, color, seats, plate, image);
                System.out.println("DEBUG: Attempting to add car for user " + userId + ": " + brand + " " + model);
                com.covoitdark.dao.CarDAO carDAO = new com.covoitdark.dao.CarDAO();
                if (carDAO.create(car)) {
                    System.out.println("DEBUG: Car successfully added to database with ID: " + car.getId());
                    sendJsonResponse(exchange, "{\"success\":true}", 200);
                } else {
                    System.out.println("DEBUG: CarDAO.create failed for unknown reason.");
                    sendJsonResponse(exchange, "{\"error\":\"Erreur lors de la sauvegarde\"}", 500);
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(exchange, "{\"error\":\"Données invalides\"}", 400);
            }
        }
    }

    static class DeleteCarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int carId = Integer.parseInt(extractJsonValue(body, "carId"));
                com.covoitdark.dao.CarDAO carDAO = new com.covoitdark.dao.CarDAO();
                if (carDAO.delete(carId)) {
                    sendJsonResponse(exchange, "{\"success\":true}", 200);
                } else {
                    sendJsonResponse(exchange, "{\"error\":\"Echec\"}", 500);
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400);
            }
        }
    }

    static class ListCarsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            
            com.covoitdark.dao.CarDAO carDAO = new com.covoitdark.dao.CarDAO();
            java.util.List<com.covoitdark.models.Car> cars = carDAO.findByDriver(userId);
            
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < cars.size(); i++) {
                com.covoitdark.models.Car c = cars.get(i);
                sb.append("{\"id\":").append(c.getId())
                  .append(",\"brand\":\"").append(c.getBrand()).append("\"")
                  .append(",\"model\":\"").append(c.getModel()).append("\"")
                  .append(",\"color\":\"").append(c.getColor()).append("\"")
                  .append(",\"seats\":").append(c.getSeats())
                  .append(",\"plate\":\"").append(c.getLicensePlate()).append("\"")
                  .append(",\"image\":\"").append(c.getImageBase64() != null ? c.getImageBase64() : "").append("\"")
                  .append("}");
                if (i < cars.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJsonResponse(exchange, sb.toString(), 200);
        }
    }

    private static String tripsToJson(java.util.List<com.covoitdark.models.Trip> trips) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < trips.size(); i++) {
            com.covoitdark.models.Trip t = trips.get(i);
            sb.append("{\"id\":").append(t.getId())
              .append(",\"driverId\":").append(t.getDriverId())
              .append(",\"carId\":").append(t.getCarId())
              .append(",\"departure\":\"").append(t.getDeparture()).append("\"")
              .append(",\"dep\":\"").append(t.getDeparture()).append("\"")
              .append(",\"arrival\":\"").append(t.getArrival()).append("\"")
              .append(",\"arr\":\"").append(t.getArrival()).append("\"")
              .append(",\"departureTime\":\"").append(t.getDepartureTime()).append("\"")
              .append(",\"time\":\"").append(t.getDepartureTime()).append("\"")
              .append(",\"startDate\":\"").append(t.getStartDate()).append("\"")
              .append(",\"date\":\"").append(t.getStartDate()).append("\"")
              .append(",\"availableSeats\":").append(t.getAvailableSeats())
              .append(",\"totalSeats\":").append(t.getTotalSeats() > 0 ? t.getTotalSeats() : t.getAvailableSeats())
              .append(",\"seats\":").append(t.getAvailableSeats())
              .append(",\"price\":").append(t.getPrice())
              .append(",\"status\":\"").append(t.getStatus()).append("\"")
              .append(",\"driverName\":\"").append(escapeJson(t.getDriverName() != null ? t.getDriverName() : "Inconnu")).append("\"")
              .append(",\"rating\":").append(t.getDriverRating())
              .append(",\"avatar\":\"").append(escapeJson(t.getDriverAvatar() != null ? t.getDriverAvatar() : "")).append("\"")
              .append(",\"carInfo\":\"").append(escapeJson(t.getCarInfo() != null ? t.getCarInfo() : "Véhicule")).append("\"")
              .append(",\"carImage\":\"").append(escapeJson(t.getCarImage() != null ? t.getCarImage() : "")).append("\"")
              .append(",\"carColor\":\"").append(escapeJson(t.getCarColor() != null ? t.getCarColor() : "")).append("\"")
              .append(",\"carPlate\":\"").append(escapeJson(t.getCarPlate() != null ? t.getCarPlate() : "")).append("\"")
              .append(",\"prefs\":{")
              .append("\"ac\":").append(t.isAcAvailable())
              .append(",\"music\":").append(t.getMusicPreference() != com.covoitdark.models.Trip.MusicPreference.NONE)
              .append(",\"smoking\":").append(t.isSmokingAllowed())
              .append(",\"flex\":").append(t.isFlexiblePickup())
              .append(",\"women\":").append(t.isWomenOnly())
              .append(",\"pets\":").append(t.isPetsAllowed())
              .append(",\"luggage\":\"").append(t.getLuggagePolicy()).append("\"")
              .append("}")
              .append("}");
            if (i < trips.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    static class FetchStatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                enableCORS(exchange);
                int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
                if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
                com.covoitdark.dao.StatsDAO sDAO = new com.covoitdark.dao.StatsDAO();
                com.covoitdark.models.Stats s = sDAO.findByUser(userId);
                if (s == null) { 
                    sDAO.create(userId); 
                    s = sDAO.findByUser(userId); 
                    if (s == null) s = new com.covoitdark.models.Stats(); // Ultimate fallback
                }
                String json = String.format(java.util.Locale.US, "{\"totalTrips\":%d,\"moneySaved\":%.2f,\"co2Saved\":%.2f,\"trees\":%.2f}", 
                    s.getTotalTrips(), s.getMoneySaved(), s.getCo2Saved(), s.getTreesEquivalent());
                sendJsonResponse(exchange, json, 200);
            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(exchange, "{\"error\":\"Erreur interne\"}", 500);
            }
        }
    }

    static class StaticFileHandler implements HttpHandler {
        private final String baseDir;

        public StaticFileHandler(String... baseDirCandidates) {
            // Pick the first candidate directory that actually exists on disk.
            // This lets us support both local dev (relative "web_ui") and Docker ("/web_ui").
            String resolved = baseDirCandidates[0];
            for (String candidate : baseDirCandidates) {
                if (new java.io.File(candidate).isDirectory()) {
                    resolved = candidate;
                    break;
                }
            }
            this.baseDir = resolved;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String pathStr = exchange.getRequestURI().getPath();
            if (pathStr.equals("/")) pathStr = "/welcome.html";
            // Strip leading slash: on Linux, Paths.get("web_ui", "/img/x.png") returns
            // the absolute path /img/x.png (ignoring baseDir). We need a relative segment.
            String relativePath = pathStr.startsWith("/") ? pathStr.substring(1) : pathStr;
            Path filePath = Paths.get(baseDir, relativePath).normalize();

            // Security: ensure the resolved path stays within the base directory
            if (!filePath.startsWith(Paths.get(baseDir).normalize())) {
                exchange.sendResponseHeaders(403, -1);
                return;
            }

            File file = filePath.toFile();
            if (!file.exists() || file.isDirectory()) {
                String response = "404 (Not Found)\n";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            String contentType = getContentType(file.getName());
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, file.length());

            try (OutputStream os = exchange.getResponseBody();
                 FileInputStream fs = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int count;
                while ((count = fs.read(buffer)) >= 0) {
                    os.write(buffer, 0, count);
                }
            }
        }

        private String getContentType(String filename) {
            if (filename.endsWith(".html")) return "text/html; charset=UTF-8";
            if (filename.endsWith(".css")) return "text/css; charset=UTF-8";
            if (filename.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (filename.endsWith(".json")) return "application/json; charset=UTF-8";
            if (filename.endsWith(".png")) return "image/png";
            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
            if (filename.endsWith(".svg")) return "image/svg+xml; charset=UTF-8";
            return "text/plain; charset=UTF-8";
        }
    }

    static class MyTripsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            com.covoitdark.dao.TripDAO tripDAO = new com.covoitdark.dao.TripDAO();
            java.util.List<com.covoitdark.models.Trip> trips = tripDAO.findByDriver(userId);
            sendJsonResponse(exchange, tripsToJson(trips), 200);
        }
    }

    static class MyBookingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            com.covoitdark.dao.RequestDAO requestDAO = new com.covoitdark.dao.RequestDAO();
            com.covoitdark.dao.TripDAO tripDAO = new com.covoitdark.dao.TripDAO();
            java.util.List<com.covoitdark.models.Request> requests = requestDAO.findByPassenger(userId);
            StringBuilder sb = new StringBuilder("[");
            for(int i=0; i<requests.size(); i++) {
                com.covoitdark.models.Request r = requests.get(i);
                com.covoitdark.models.Trip trip = tripDAO.findByIdDirect(r.getTripId());
                String dep = trip != null ? trip.getDeparture() : "?";
                String arr = trip != null ? trip.getArrival() : "?";
                String driverName = trip != null ? (trip.getDriverName() != null ? trip.getDriverName() : "?") : "?";
                String avatar = trip != null ? (trip.getDriverAvatar() != null ? trip.getDriverAvatar() : "") : "";
                sb.append("{\"id\":").append(r.getId())
                  .append(",\"tripId\":").append(r.getTripId())
                  .append(",\"departure\":\"").append(dep).append("\"")
                  .append(",\"arrival\":\"").append(arr).append("\"")
                  .append(",\"driverName\":\"").append(driverName).append("\"")
                  .append(",\"driverAvatar\":\"").append(avatar).append("\"")
                  .append(",\"qty\":").append(r.getQuantity())
                  .append(",\"totalCost\":").append(r.getTotalCost())
                  .append(",\"status\":\"").append(r.getStatus()).append("\"")
                  .append(",\"counterPrice\":").append(r.getCounterPrice())
                  .append(",\"secretCode\":\"").append(r.getSecretCode() != null ? r.getSecretCode() : "").append("\"")
                  .append(",\"date\":\"").append(r.getCreatedAt()).append("\"")
                  .append("}");
                if(i < requests.size() -1) sb.append(",");
            }
            sb.append("]");
            sendJsonResponse(exchange, sb.toString(), 200);
        }
    }

    static class DriverRequestsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            com.covoitdark.dao.RequestDAO requestDAO = new com.covoitdark.dao.RequestDAO();
            java.util.List<com.covoitdark.models.Request> requests = requestDAO.findByDriver(userId);
            StringBuilder sb = new StringBuilder("[");
            for(int i=0; i<requests.size(); i++) {
                com.covoitdark.models.Request r = requests.get(i);
                sb.append("{\"id\":").append(r.getId())
                  .append(",\"tripId\":").append(r.getTripId())
                  .append(",\"userId\":").append(r.getPassengerId())
                  .append(",\"userName\":\"").append(r.getPassengerName() != null ? r.getPassengerName() : "Passager").append("\"")
                  .append(",\"qty\":").append(r.getQuantity())
                  .append(",\"totalCost\":").append(r.getTotalCost())
                  .append(",\"status\":\"").append(r.getStatus()).append("\"")
                  .append(",\"counterPrice\":").append(r.getCounterPrice())
                  .append(",\"secretCode\":\"").append(r.getSecretCode() != null ? r.getSecretCode() : "").append("\"")
                  .append(",\"date\":\"").append(r.getCreatedAt()).append("\"")
                  .append("}");
                if(i < requests.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJsonResponse(exchange, sb.toString(), 200);
        }
    }

    static class RespondBookingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int requestId = Integer.parseInt(extractJsonValue(input, "requestId"));
                String status = extractJsonValue(input, "status");
                com.covoitdark.dao.RequestDAO reqDAO = new com.covoitdark.dao.RequestDAO();
                com.covoitdark.dao.TripDAO tripDAO = new com.covoitdark.dao.TripDAO();
                com.covoitdark.dao.UserDAO userDAO = new com.covoitdark.dao.UserDAO();
                com.covoitdark.dao.NotificationDAO nDAO = new com.covoitdark.dao.NotificationDAO();
                
                com.covoitdark.models.Request req = reqDAO.findById(requestId);
                if (req == null) { sendJsonResponse(exchange, "{\"error\":\"Demande introuvable\"}", 404); return; }
                
                com.covoitdark.models.Trip trip = tripDAO.findByIdDirect(req.getTripId());
                com.covoitdark.models.User passenger = userDAO.findByIdDirect(req.getPassengerId());
                
                if ("ACCEPTED".equals(status)) {
                    // Check seats and balance final check
                    if (trip.getAvailableSeats() < req.getQuantity()) { sendJsonResponse(exchange, "{\"error\":\"Plus de places dispo\"}", 400); return; }
                    if (passenger.getBalance() < req.getTotalCost()) { sendJsonResponse(exchange, "{\"error\":\"Solde passager insuffisant\"}", 400); return; }
                    
                    // Perform transaction: Subtract money from passenger, Add to driver, decrement seats
                    double newBal = passenger.getBalance() - req.getTotalCost();
                    com.covoitdark.models.User driver = userDAO.findByIdDirect(trip.getDriverId());
                    double newBalDriver = (driver != null ? driver.getBalance() : 0) + req.getTotalCost();
                    
                    if (userDAO.updateBalance(passenger.getId(), newBal) && 
                        (driver == null || userDAO.updateBalance(driver.getId(), newBalDriver)) &&
                        tripDAO.decrementSeats(trip.getId())) {
                        reqDAO.updateStatus(requestId, com.covoitdark.models.Request.Status.ACCEPTED);
                        // Notify Passenger
                        com.covoitdark.models.Notification n = new com.covoitdark.models.Notification();
                        n.setUserId(passenger.getId());
                        n.setTitle("Réservation acceptée !");
                        n.setMessage("Votre demande pour " + trip.getDeparture() + " -> " + trip.getArrival() + " a été acceptée.");
                        nDAO.create(n);
                        sendJsonResponse(exchange, "{\"success\":true}", 200);
                    } else { sendJsonResponse(exchange, "{\"error\":\"Echec transaction\"}", 500); }
                } else if ("REJECTED".equals(status)) {
                    reqDAO.updateStatus(requestId, com.covoitdark.models.Request.Status.REJECTED);
                    com.covoitdark.models.Notification n = new com.covoitdark.models.Notification();
                    n.setUserId(passenger.getId());
                    n.setTitle("Réservation refusée");
                    n.setMessage("Malheureusement, votre demande pour " + trip.getDeparture() + " a été refusée.");
                    nDAO.create(n);
                    sendJsonResponse(exchange, "{\"success\":true}", 200);
                }
            } catch (Exception e) { e.printStackTrace(); sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    static class VerifyCodeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int driverId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (driverId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int requestId = Integer.parseInt(extractJsonValue(input, "requestId"));
                String code = extractJsonValue(input, "code");
                
                com.covoitdark.dao.RequestDAO reqDAO = new com.covoitdark.dao.RequestDAO();
                com.covoitdark.models.Request req = reqDAO.findById(requestId);
                
                if (req == null) { sendJsonResponse(exchange, "{\"error\":\"Demande introuvable\"}", 404); return; }
                if (req.getStatus() != com.covoitdark.models.Request.Status.ACCEPTED) { sendJsonResponse(exchange, "{\"error\":\"Action impossible pour ce statut\"}", 400); return; }
                
                com.covoitdark.dao.TripDAO tripDAO = new com.covoitdark.dao.TripDAO();
                com.covoitdark.models.Trip trip = tripDAO.findByIdDirect(req.getTripId());
                if (trip.getDriverId() != driverId) { sendJsonResponse(exchange, "{\"error\":\"Vous n'êtes pas le conducteur de ce trajet\"}", 403); return; }
                
                if (req.getSecretCode() != null && req.getSecretCode().equalsIgnoreCase(code)) {
                    reqDAO.updateStatus(requestId, com.covoitdark.models.Request.Status.COMPLETE);
                    sendJsonResponse(exchange, "{\"success\":true}", 200);
                } else {
                    sendJsonResponse(exchange, "{\"error\":\"Code invalide\"}", 400);
                }
            } catch (Exception e) { e.printStackTrace(); sendJsonResponse(exchange, "{\"error\":\"Requête Invalide\"}", 400); }
        }
    }

    static class ListNotificationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            com.covoitdark.dao.NotificationDAO nDAO = new com.covoitdark.dao.NotificationDAO();
            java.util.List<com.covoitdark.models.Notification> notes = nDAO.findByUser(userId);
            StringBuilder sb = new StringBuilder("[");
            for(int i=0; i<notes.size(); i++) {
                com.covoitdark.models.Notification n = notes.get(i);
                sb.append("{\"id\":").append(n.getId())
                  .append(",\"title\":\"").append(n.getTitle()).append("\"")
                  .append(",\"message\":\"").append(n.getMessage().replace("\n"," ")).append("\"")
                  .append(",\"isRead\":").append(n.isRead())
                  .append(",\"date\":\"").append(n.getCreatedAt()).append("\"")
                  .append("}");
                if(i < notes.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJsonResponse(exchange, sb.toString(), 200);
        }
    }

    static class MessageHistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            String query = exchange.getRequestURI().getQuery();
            int otherId = query != null && query.contains("otherId=") ? Integer.parseInt(query.split("otherId=")[1].split("&")[0]) : 0;
            com.covoitdark.dao.MessageDAO mDAO = new com.covoitdark.dao.MessageDAO();
            java.util.List<com.covoitdark.models.Message> msgs = mDAO.findConversation(userId, otherId);
            StringBuilder sb = new StringBuilder("[");
            for(int i=0; i<msgs.size(); i++) {
                com.covoitdark.models.Message m = msgs.get(i);
                sb.append("{\"id\":").append(m.getId())
                  .append(",\"senderId\":").append(m.getSenderId())
                  .append(",\"content\":\"").append(m.getContent().replace("\"","'")).append("\"")
                  .append(",\"sentAt\":\"").append(m.getSentAt()).append("\"")
                  .append("}");
                if(i < msgs.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJsonResponse(exchange, sb.toString(), 200);
        }
    }

    static class SendMessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int senderId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (senderId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int receiverId = Integer.parseInt(extractJsonValue(input, "receiverId"));
                String content = extractJsonValue(input, "content");
                com.covoitdark.models.Message m = new com.covoitdark.models.Message(0, 0, senderId, receiverId, content, false);
                com.covoitdark.dao.MessageDAO mDAO = new com.covoitdark.dao.MessageDAO();
                if(mDAO.create(m)) sendJsonResponse(exchange, "{\"success\":true}", 200);
                else sendJsonResponse(exchange, "{\"error\":\"Echec\"}", 500);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    static class NotificationReadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            String query = exchange.getRequestURI().getQuery();
            try {
                int notifId = query != null && query.contains("id=") ? Integer.parseInt(query.split("id=")[1].split("&")[0]) : 0;
                com.covoitdark.dao.NotificationDAO nDAO = new com.covoitdark.dao.NotificationDAO();
                nDAO.markRead(notifId);
                sendJsonResponse(exchange, "{\"success\":true}", 200);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    static class NotificationReadAllHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            com.covoitdark.dao.NotificationDAO nDAO = new com.covoitdark.dao.NotificationDAO();
            nDAO.markAllRead(userId);
            sendJsonResponse(exchange, "{\"success\":true}", 200);
        }
    }

    static class NotificationDeleteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            String query = exchange.getRequestURI().getQuery();
            try {
                int notifId = query != null && query.contains("id=") ? Integer.parseInt(query.split("id=")[1].split("&")[0]) : 0;
                com.covoitdark.dao.NotificationDAO nDAO = new com.covoitdark.dao.NotificationDAO();
                nDAO.delete(notifId);
                sendJsonResponse(exchange, "{\"success\":true}", 200);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    static class NotificationDeleteAllHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            com.covoitdark.dao.NotificationDAO nDAO = new com.covoitdark.dao.NotificationDAO();
            nDAO.deleteAll(userId);
            sendJsonResponse(exchange, "{\"success\":true}", 200);
        }
    }

    static class CancelBookingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int bookingId = Integer.parseInt(extractJsonValue(input, "bookingId"));
                com.covoitdark.dao.RequestDAO reqDAO = new com.covoitdark.dao.RequestDAO();
                if (reqDAO.updateStatus(bookingId, com.covoitdark.models.Request.Status.CANCELLED)) {
                    sendJsonResponse(exchange, "{\"success\":true,\"message\":\"Réservation annulée.\"}", 200);
                } else {
                    sendJsonResponse(exchange, "{\"error\":\"Impossible d'annuler\"}", 500);
                }
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    static class UpdateProfileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                String name = extractJsonValue(input, "name");
                String email = extractJsonValue(input, "email");
                String password = extractJsonValue(input, "password");
                String bio = extractJsonValue(input, "bio");
                String avatar = extractJsonValue(input, "avatar");
                com.covoitdark.dao.UserDAO uDAO = new com.covoitdark.dao.UserDAO();
                com.covoitdark.models.User u = uDAO.findByIdDirect(userId);
                if (u == null) { sendJsonResponse(exchange, "{\"error\":\"Utilisateur introuvable\"}", 404); return; }
                if (!name.isEmpty()) u.setFullName(name);
                if (!email.isEmpty()) u.setEmail(email);
                if (bio != null) u.setBio(bio);
                if (!password.isEmpty()) {
                    String salt = com.covoitdark.utils.PasswordUtils.generateSalt();
                    u.setSalt(salt);
                    u.setPassword(com.covoitdark.utils.PasswordUtils.hash(password, salt));
                    uDAO.updatePassword(userId, u.getPassword(), salt);
                }
                if (u != null && avatar != null && !avatar.isEmpty()) u.setAvatar(avatar);
                if (uDAO.update(u)) {
                    com.covoitdark.utils.SessionManager.getInstance().setCurrentUser(u);
                    sendJsonResponse(exchange, "{\"success\":true}", 200);
                } else {
                    sendJsonResponse(exchange, "{\"error\":\"Erreur de mise à jour\"}", 500);
                }
            } catch (Exception e) { e.printStackTrace(); sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    static class DeleteAccountHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            
            try {
                com.covoitdark.dao.UserDAO uDAO = new com.covoitdark.dao.UserDAO();
                if (uDAO.delete(userId)) {
                    com.covoitdark.utils.SessionManager.getInstance().logout();
                    sendJsonResponse(exchange, "{\"success\":true}", 200);
                } else {
                    sendJsonResponse(exchange, "{\"error\":\"Erreur lors de la suppression\"}", 500);
                }
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Erreur serveur\"}", 500); }
        }
    }

    static class CompleteTripHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int tripId = Integer.parseInt(extractJsonValue(input, "tripId"));
                com.covoitdark.dao.TripDAO tripDAO = new com.covoitdark.dao.TripDAO();
                com.covoitdark.models.Trip trip = tripDAO.findByIdDirect(tripId);
                if (trip == null || trip.getDriverId() != userId) {
                    sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 403);
                    return;
                }
                if (tripDAO.updateStatus(tripId, com.covoitdark.models.Trip.Status.COMPLETE)) {
                    sendJsonResponse(exchange, "{\"success\":true}", 200);
                } else {
                    sendJsonResponse(exchange, "{\"error\":\"Erreur lors de la mise à jour\"}", 500);
                }
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    static class CancelTripHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int tripId = Integer.parseInt(extractJsonValue(input, "tripId"));
                com.covoitdark.controllers.TripController tripController = new com.covoitdark.controllers.TripController();
                String message = tripController.annulerTrajet(tripId);
                
                if (message != null && !message.equals("Non autorisé.")) {
                    sendJsonResponse(exchange, "{\"success\":true,\"message\":\"" + message + "\"}", 200);
                } else {
                    sendJsonResponse(exchange, "{\"error\":\"" + (message != null ? message : "Echec") + "\"}", 403);
                }
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    static class AcceptedPassengersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            String query = exchange.getRequestURI().getQuery();
            try {
                int tripId = query != null && query.contains("tripId=") ? Integer.parseInt(query.split("tripId=")[1].split("&")[0]) : 0;
                com.covoitdark.dao.RequestDAO reqDAO = new com.covoitdark.dao.RequestDAO();
                java.util.List<com.covoitdark.models.Request> reqs = reqDAO.findByTrip(tripId);
                StringBuilder sb = new StringBuilder("[");
                int count = 0;
                for (com.covoitdark.models.Request r : reqs) {
                    if (r.getStatus() == com.covoitdark.models.Request.Status.ACCEPTED) {
                        if (count > 0) sb.append(",");
                        sb.append("{\"id\":").append(r.getPassengerId())
                          .append(",\"name\":\"").append(r.getPassengerName()).append("\"}");
                        count++;
                    }
                }
                sb.append("]");
                sendJsonResponse(exchange, sb.toString(), 200);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    static class AddRatingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int raterId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (raterId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int tripId = Integer.parseInt(extractJsonValue(input, "tripId"));
                int ratedId = Integer.parseInt(extractJsonValue(input, "ratedId"));
                int score = Integer.parseInt(extractJsonValue(input, "score"));
                String comment = extractJsonValue(input, "comment");

                com.covoitdark.models.Rating r = new com.covoitdark.models.Rating(0, tripId, raterId, ratedId, score, comment);
                com.covoitdark.dao.RatingDAO rDAO = new com.covoitdark.dao.RatingDAO();
                if (rDAO.create(r)) {
                    // Update user reputation
                    double avg = rDAO.getAverageScore(ratedId);
                    new com.covoitdark.dao.UserDAO().updateReputationScore(ratedId, avg);
                    sendJsonResponse(exchange, "{\"success\":true}", 200);
                } else {
                    sendJsonResponse(exchange, "{\"error\":\"Erreur lors de la sauvegarde\"}", 500);
                }
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    static class ListRatingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            String query = exchange.getRequestURI().getQuery();
            try {
                int userId = query != null && query.contains("userId=") ? Integer.parseInt(query.split("userId=")[1].split("&")[0]) : 0;
                com.covoitdark.dao.RatingDAO rDAO = new com.covoitdark.dao.RatingDAO();
                java.util.List<com.covoitdark.models.Rating> ratings = rDAO.findByRatedUser(userId);
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < ratings.size(); i++) {
                    com.covoitdark.models.Rating r = ratings.get(i);
                    sb.append("{\"id\":").append(r.getId())
                      .append(",\"raterName\":\"").append(r.getRaterName()).append("\"")
                      .append(",\"score\":").append(r.getScore())
                      .append(",\"comment\":\"").append(r.getComment().replace("\"","'")).append("\"")
                      .append(",\"date\":\"").append(r.getCreatedAt()).append("\"")
                      .append("}");
                    if (i < ratings.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendJsonResponse(exchange, sb.toString(), 200);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }
    static class AdminBroadcastHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            if (!com.covoitdark.utils.SessionManager.getInstance().isAdmin()) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 403); return; }
            
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                String messageText = extractJsonValue(input, "message");
                if (messageText.isEmpty()) { sendJsonResponse(exchange, "{\"error\":\"Message vide\"}", 400); return; }
                
                com.covoitdark.dao.UserDAO uDAO = new com.covoitdark.dao.UserDAO();
                com.covoitdark.dao.MessageDAO mDAO = new com.covoitdark.dao.MessageDAO();
                java.util.List<com.covoitdark.models.User> allUsers = uDAO.findAll();
                int adminId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
                
                for (com.covoitdark.models.User u : allUsers) {
                    if (u.getId() == adminId) continue;
                    com.covoitdark.models.Message msg = new com.covoitdark.models.Message();
                    msg.setSenderId(adminId);
                    msg.setReceiverId(u.getId());
                    msg.setContent("[DIFFUSION ADMIN] " + messageText);
                    mDAO.create(msg);
                }
                
                sendJsonResponse(exchange, "{\"success\":true}", 200);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    // ── SAVED SEARCHES ───────────────────────────────────────────────────────
    static class SaveSearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                com.covoitdark.models.SavedSearch s = new com.covoitdark.models.SavedSearch();
                s.setPassengerId(userId);
                s.setName(extractJsonValue(input, "name"));
                s.setDeparture(extractJsonValue(input, "dep"));
                s.setArrival(extractJsonValue(input, "arr"));
                s.setFrequency("OCCASIONAL"); // Default
                com.covoitdark.dao.SavedSearchDAO sDAO = new com.covoitdark.dao.SavedSearchDAO();
                if (sDAO.create(s)) sendJsonResponse(exchange, "{\"success\":true}", 200);
                else sendJsonResponse(exchange, "{\"error\":\"Erreur DB\"}", 500);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    static class MySavedSearchesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            com.covoitdark.dao.SavedSearchDAO sDAO = new com.covoitdark.dao.SavedSearchDAO();
            java.util.List<com.covoitdark.models.SavedSearch> searches = sDAO.findByPassenger(userId);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < searches.size(); i++) {
                com.covoitdark.models.SavedSearch s = searches.get(i);
                sb.append("{\"id\":").append(s.getId())
                  .append(",\"name\":\"").append(s.getName() != null ? s.getName() : "").append("\"")
                  .append(",\"dep\":\"").append(s.getDeparture()).append("\"")
                  .append(",\"arr\":\"").append(s.getArrival()).append("\"")
                  .append(",\"frequency\":\"").append(s.getFrequency()).append("\"")
                  .append("}");
                if (i < searches.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJsonResponse(exchange, sb.toString(), 200);
        }
    }

    static class DeleteSavedSearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int searchId = Integer.parseInt(extractJsonValue(input, "id"));
                com.covoitdark.dao.SavedSearchDAO sDAO = new com.covoitdark.dao.SavedSearchDAO();
                if (sDAO.delete(searchId)) sendJsonResponse(exchange, "{\"success\":true}", 200);
                else sendJsonResponse(exchange, "{\"error\":\"Erreur DB\"}", 500);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    // ── BADGES ───────────────────────────────────────────────────────────────
    static class MyBadgesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            
            // Trigger auto-awards
            com.covoitdark.utils.BadgeManager bm = new com.covoitdark.utils.BadgeManager();
            com.covoitdark.dao.UserDAO uDAO = new com.covoitdark.dao.UserDAO();
            com.covoitdark.dao.TripDAO tDAO = new com.covoitdark.dao.TripDAO();
            com.covoitdark.dao.RequestDAO rDAO = new com.covoitdark.dao.RequestDAO();
            com.covoitdark.models.User u = uDAO.findByIdDirect(userId);
            int tripsDriven = tDAO.findByDriver(userId).size();
            int tripsRiden = rDAO.findByPassenger(userId).size();
            if (u != null) bm.checkAndAwardBadges(userId, u.getReputationScore(), tripsDriven, tripsRiden);

            com.covoitdark.dao.BadgeDAO bDAO = new com.covoitdark.dao.BadgeDAO();
            java.util.List<com.covoitdark.models.Badge> badges = bDAO.findByUser(userId);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < badges.size(); i++) {
                com.covoitdark.models.Badge b = badges.get(i);
                sb.append("{\"id\":").append(b.getId())
                  .append(",\"name\":\"").append(b.getBadgeName()).append("\"}");
                if (i < badges.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJsonResponse(exchange, sb.toString(), 200);
        }
    }

    // ── BLOCKS ───────────────────────────────────────────────────────────────
    static class UpdateCarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int carId = Integer.parseInt(extractJsonValue(input, "id"));
                String brand = extractJsonValue(input, "brand");
                String model = extractJsonValue(input, "model");
                String plate = extractJsonValue(input, "licensePlate");
                String color = extractJsonValue(input, "color");
                int seats = Integer.parseInt(extractJsonValue(input, "seats"));
                String base64 = extractJsonValue(input, "imageBase64");
                
                com.covoitdark.dao.CarDAO cdao = new com.covoitdark.dao.CarDAO();
                com.covoitdark.models.Car car = cdao.findById(carId);
                if (car == null || car.getDriverId() != userId) {
                    sendJsonResponse(exchange, "{\"error\":\"Véhicule non trouvé\"}", 403);
                    return;
                }
                
                if (brand != null) car.setBrand(brand);
                if (model != null) car.setModel(model);
                if (color != null) car.setColor(color);
                if (seats > 0) car.setSeats(seats);
                if (plate != null) car.setLicensePlate(plate);
                if (base64 != null && !base64.isEmpty() && !base64.equals("null")) car.setImageBase64(base64);
                
                if (cdao.update(car)) sendJsonResponse(exchange, "{\"success\":true}", 200);
                else sendJsonResponse(exchange, "{\"error\":\"Erreur DB\"}", 500);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Champs invalides\"}", 400); }
        }
    }

    // ── BLOCKS ───────────────────────────────────────────────────────────────
    static class ToggleBlockHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int blockerId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (blockerId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int blockedId = Integer.parseInt(extractJsonValue(input, "blockedId"));
                com.covoitdark.dao.BlockDAO bDAO = new com.covoitdark.dao.BlockDAO();
                boolean currentlyBlocked = bDAO.isBlocked(blockerId, blockedId);
                if (currentlyBlocked) {
                    bDAO.delete(blockerId, blockedId);
                    sendJsonResponse(exchange, "{\"success\":true,\"blocked\":false}", 200);
                } else {
                    com.covoitdark.models.Block b = new com.covoitdark.models.Block(0, blockerId, blockedId);
                    bDAO.create(b);
                    sendJsonResponse(exchange, "{\"success\":true,\"blocked\":true}", 200);
                }
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    static class IsBlockedHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int currentId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (currentId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            String query = exchange.getRequestURI().getQuery();
            try {
                int otherId = query != null && query.contains("otherId=") ? Integer.parseInt(query.split("otherId=")[1].split("&")[0]) : 0;
                com.covoitdark.dao.BlockDAO bDAO = new com.covoitdark.dao.BlockDAO();
                boolean blockedSelf = bDAO.isBlocked(currentId, otherId);
                boolean blockedOther = bDAO.isBlocked(otherId, currentId);
                boolean isBlocked = blockedSelf || blockedOther;
                sendJsonResponse(exchange, "{\"blocked\":" + isBlocked + "}", 200);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    // ── Trip History (all users) ──────────────────────────────────────────────
    /**
     * GET /api/trips/history
     *
     * Returns every COMPLETE trip the current user participated in,
     * with rich co-traveller data for each trip:
     *  - driver info (as passenger's view) or passenger list (as driver's view)
     *  - each person's: id, name, phone, bio, reputationScore, isVerified, idVerified, avatar
     *  - plus two social flags: isFavorite, isBlocked (relative to the caller)
     */
    static class TripHistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }

            com.covoitdark.dao.TripDAO tripDAO         = new com.covoitdark.dao.TripDAO();
            com.covoitdark.dao.RequestDAO requestDAO   = new com.covoitdark.dao.RequestDAO();
            com.covoitdark.dao.UserDAO userDAO         = new com.covoitdark.dao.UserDAO();
            com.covoitdark.dao.BlockDAO blockDAO       = new com.covoitdark.dao.BlockDAO();
            com.covoitdark.dao.DriverFavoriteDAO favDAO = new com.covoitdark.dao.DriverFavoriteDAO();
            com.covoitdark.dao.RatingDAO ratingDAO     = new com.covoitdark.dao.RatingDAO();

            // Completed trips where current user was a passenger
            java.util.List<com.covoitdark.models.Request> myRequests = requestDAO.findByPassenger(userId)
                    .stream()
                    .filter(r -> r.getStatus() == com.covoitdark.models.Request.Status.COMPLETE
                              || r.getStatus() == com.covoitdark.models.Request.Status.ACCEPTED)
                    .collect(java.util.stream.Collectors.toList());

            // Completed trips where current user was the driver
            java.util.List<com.covoitdark.models.Trip> myDrivenTrips = tripDAO.findByDriver(userId)
                    .stream()
                    .filter(t -> t.getStatus() == com.covoitdark.models.Trip.Status.COMPLETE)
                    .collect(java.util.stream.Collectors.toList());

            StringBuilder sb = new StringBuilder("[");
            boolean first = true;

            // ── As passenger ──────────────────────────────────────────────────
            for (com.covoitdark.models.Request req : myRequests) {
                com.covoitdark.models.Trip trip = tripDAO.findByIdDirect(req.getTripId());
                if (trip == null) continue;

                com.covoitdark.models.User driver = userDAO.findByIdDirect(trip.getDriverId());
                // co-passengers on same trip (excluding self)
                java.util.List<com.covoitdark.models.Request> coReqs = requestDAO.findByTrip(trip.getId())
                        .stream()
                        .filter(r -> r.getPassengerId() != userId
                                && (r.getStatus() == com.covoitdark.models.Request.Status.COMPLETE
                                 || r.getStatus() == com.covoitdark.models.Request.Status.ACCEPTED))
                        .collect(java.util.stream.Collectors.toList());

                if (!first) sb.append(",");
                first = false;
                sb.append("{");
                sb.append("\"tripId\":").append(trip.getId()).append(",");
                sb.append("\"departure\":\"").append(escapeJson(trip.getDeparture())).append("\",");
                sb.append("\"arrival\":\"").append(escapeJson(trip.getArrival())).append("\",");
                sb.append("\"date\":\"").append(trip.getStartDate()).append("\",");
                sb.append("\"price\":").append(req.getTotalCost()).append(",");
                sb.append("\"role\":\"PASSENGER\",");
                sb.append("\"driver\":").append(personJson(driver, userId, blockDAO, favDAO)).append(",");
                sb.append("\"coPassengers\":[");
                boolean fp = true;
                for (com.covoitdark.models.Request cr : coReqs) {
                    com.covoitdark.models.User p = userDAO.findByIdDirect(cr.getPassengerId());
                    if (!fp) sb.append(",");
                    fp = false;
                    sb.append(personJson(p, userId, blockDAO, favDAO));
                }
                sb.append("]}");
            }

            // ── As driver ────────────────────────────────────────────────────
            for (com.covoitdark.models.Trip trip : myDrivenTrips) {
                java.util.List<com.covoitdark.models.Request> accepted = requestDAO.findByTrip(trip.getId())
                        .stream()
                        .filter(r -> r.getStatus() == com.covoitdark.models.Request.Status.COMPLETE
                                  || r.getStatus() == com.covoitdark.models.Request.Status.ACCEPTED)
                        .collect(java.util.stream.Collectors.toList());

                if (!first) sb.append(",");
                first = false;
                sb.append("{");
                sb.append("\"tripId\":").append(trip.getId()).append(",");
                sb.append("\"departure\":\"").append(escapeJson(trip.getDeparture())).append("\",");
                sb.append("\"arrival\":\"").append(escapeJson(trip.getArrival())).append("\",");
                sb.append("\"date\":\"").append(trip.getStartDate()).append("\",");
                sb.append("\"price\":").append(trip.getPrice()).append(",");
                sb.append("\"role\":\"DRIVER\",");
                sb.append("\"driver\":null,");
                sb.append("\"coPassengers\":[");
                boolean fp = true;
                for (com.covoitdark.models.Request r : accepted) {
                    com.covoitdark.models.User p = userDAO.findByIdDirect(r.getPassengerId());
                    if (!fp) sb.append(",");
                    fp = false;
                    sb.append(personJson(p, userId, blockDAO, favDAO));
                }
                sb.append("]}");
            }

            sb.append("]");
            sendJsonResponse(exchange, sb.toString(), 200);
        }

        private String personJson(com.covoitdark.models.User u, int viewerId,
                                   com.covoitdark.dao.BlockDAO blockDAO,
                                   com.covoitdark.dao.DriverFavoriteDAO favDAO) {
            if (u == null) return "null";
            boolean blocked  = blockDAO.isBlocked(viewerId, u.getId());
            boolean favorite = u.isDriver() && favDAO.isFavorite(viewerId, u.getId());
            return "{\"id\":" + u.getId()
                + ",\"name\":\"" + escapeJson(u.getFullName()) + "\""
                + ",\"phone\":\"" + escapeJson(u.getPhone() != null ? u.getPhone() : "") + "\""
                + ",\"bio\":\"" + escapeJson(u.getBio() != null ? u.getBio() : "") + "\""
                + ",\"rating\":" + u.getReputationScore()
                + ",\"isVerified\":" + u.isVerified()
                + ",\"idVerified\":" + u.isIdVerified()
                + ",\"role\":\"" + u.getRole().name() + "\""
                + ",\"avatar\":\"" + escapeJson(u.getAvatar() != null ? u.getAvatar() : "") + "\""
                + ",\"isBlocked\":" + blocked
                + ",\"isFavorite\":" + favorite
                + "}";
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // NEW FEATURE HANDLERS
    // ══════════════════════════════════════════════════════════════════════════

    // Feature 1 – Trip Recurrences
    static class TripOccurrencesHandler implements HttpHandler {
        private final com.covoitdark.controllers.RecurrenceController rc = new com.covoitdark.controllers.RecurrenceController();
        private final com.covoitdark.dao.TripDAO tripDAO = new com.covoitdark.dao.TripDAO();
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            String query = exchange.getRequestURI().getQuery();
            try {
                int tripId = Integer.parseInt(query.split("tripId=")[1].split("&")[0]);
                com.covoitdark.models.Trip trip = tripDAO.findByIdDirect(tripId);
                if (trip == null) { sendJsonResponse(exchange, "{\"error\":\"Trajet introuvable\"}", 404); return; }
                java.util.List<java.time.LocalDate> dates = rc.getUpcomingOccurrences(trip);
                String json = "[" + dates.stream().map(d -> "\"" + d + "\"").collect(java.util.stream.Collectors.joining(",")) + "]";
                sendJsonResponse(exchange, json, 200);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    // Feature 2 – Boarding Code Verification
    static class VerifyBoardingCodeHandler implements HttpHandler {
        private final com.covoitdark.controllers.RequestController rc = new com.covoitdark.controllers.RequestController();
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int requestId = Integer.parseInt(extractJsonValue(input, "requestId"));
                String code = extractJsonValue(input, "code");
                boolean valid = rc.verifierCodeEmbarquement(requestId, code);
                sendJsonResponse(exchange, "{\"valid\":" + valid + "}", 200);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    // Feature 3 – Trip Group Chat
    static class TripChatSendHandler implements HttpHandler {
        private final com.covoitdark.controllers.TripChatController cc = new com.covoitdark.controllers.TripChatController();
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            if (com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId() == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int tripId = Integer.parseInt(extractJsonValue(input, "tripId"));
                String content = extractJsonValue(input, "content");
                String result = cc.sendMessage(tripId, content);
                sendJsonResponse(exchange, "{\"message\":\"" + escapeJson(result) + "\"}", 200);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    static class TripChatMessagesHandler implements HttpHandler {
        private final com.covoitdark.controllers.TripChatController cc = new com.covoitdark.controllers.TripChatController();
        private final com.covoitdark.utils.JsonMapper<com.covoitdark.models.TripChatMessage> mapper = new com.covoitdark.utils.JsonMapper<>(com.covoitdark.models.TripChatMessage.class);
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            String query = exchange.getRequestURI().getQuery();
            try {
                int tripId = Integer.parseInt(query.split("tripId=")[1].split("&")[0]);
                sendJsonResponse(exchange, mapper.toJsonArray(cc.getMessages(tripId)), 200);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    // Feature 4 – OTP Verification
    static class OtpGenerateHandler implements HttpHandler {
        private final com.covoitdark.controllers.OtpController oc = new com.covoitdark.controllers.OtpController();
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            if (com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId() == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            String code = oc.generateOtp();
            sendJsonResponse(exchange, code != null ? "{\"code\":\"" + code + "\"}" : "{\"error\":\"Erreur\"}", code != null ? 200 : 500);
        }
    }

    static class OtpVerifyHandler implements HttpHandler {
        private final com.covoitdark.controllers.OtpController oc = new com.covoitdark.controllers.OtpController();
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String code = extractJsonValue(input, "code");
            System.out.println("[API] POST /api/otp/verify | code: " + code);
            boolean ok = oc.verifyOtp(code);
            sendJsonResponse(exchange, "{\"success\":" + ok + "}", ok ? 200 : 400);
        }
    }

    // Feature 5 – ID Verification (admin)
    static class IdVerificationListHandler implements HttpHandler {
        private final com.covoitdark.controllers.IdVerificationController ivc = new com.covoitdark.controllers.IdVerificationController();
        private final com.covoitdark.utils.JsonMapper<com.covoitdark.models.User> mapper = new com.covoitdark.utils.JsonMapper<>(com.covoitdark.models.User.class);
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!com.covoitdark.utils.SessionManager.getInstance().isAdmin()) { sendJsonResponse(exchange, "{\"error\":\"Accès refusé\"}", 403); return; }
            sendJsonResponse(exchange, mapper.toJsonArray(ivc.getPendingVerifications()), 200);
        }
    }

    static class IdVerifyApproveHandler implements HttpHandler {
        private final com.covoitdark.controllers.IdVerificationController ivc = new com.covoitdark.controllers.IdVerificationController();
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int driverId = Integer.parseInt(extractJsonValue(input, "driverId"));
                boolean ok = ivc.approveIdVerification(driverId);
                if (ok) com.covoitdark.utils.AuditLogger.getInstance().log(
                    com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId(),
                    "APPROVE_ID", "USER", driverId);
                sendJsonResponse(exchange, "{\"success\":" + ok + "}", ok ? 200 : 403);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    static class IdVerifyRejectHandler implements HttpHandler {
        private final com.covoitdark.controllers.IdVerificationController ivc = new com.covoitdark.controllers.IdVerificationController();
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int driverId = Integer.parseInt(extractJsonValue(input, "driverId"));
                boolean ok = ivc.rejectIdVerification(driverId);
                if (ok) com.covoitdark.utils.AuditLogger.getInstance().log(
                    com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId(),
                    "REJECT_ID", "USER", driverId);
                sendJsonResponse(exchange, "{\"success\":" + ok + "}", ok ? 200 : 403);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    // Feature 7 – Favorite Drivers
    static class AddFavoriteHandler implements HttpHandler {
        private final com.covoitdark.controllers.DriverFavoriteController dfc = new com.covoitdark.controllers.DriverFavoriteController();
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int driverId = Integer.parseInt(extractJsonValue(input, "driverId"));
                boolean ok = dfc.addFavorite(driverId);
                sendJsonResponse(exchange, "{\"success\":" + ok + "}", 200);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    static class RemoveFavoriteHandler implements HttpHandler {
        private final com.covoitdark.controllers.DriverFavoriteController dfc = new com.covoitdark.controllers.DriverFavoriteController();
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int driverId = Integer.parseInt(extractJsonValue(input, "driverId"));
                boolean ok = dfc.removeFavorite(driverId);
                sendJsonResponse(exchange, "{\"success\":" + ok + "}", 200);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    static class MyFavoritesHandler implements HttpHandler {
        private final com.covoitdark.controllers.DriverFavoriteController dfc = new com.covoitdark.controllers.DriverFavoriteController();
        private final com.covoitdark.utils.JsonMapper<com.covoitdark.models.DriverFavorite> mapper = new com.covoitdark.utils.JsonMapper<>(com.covoitdark.models.DriverFavorite.class);
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            sendJsonResponse(exchange, mapper.toJsonArray(dfc.getMyFavorites()), 200);
        }
    }

    static class IsFavoriteHandler implements HttpHandler {
        private final com.covoitdark.controllers.DriverFavoriteController dfc = new com.covoitdark.controllers.DriverFavoriteController();
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            String query = exchange.getRequestURI().getQuery();
            try {
                int driverId = Integer.parseInt(query.split("driverId=")[1].split("&")[0]);
                sendJsonResponse(exchange, "{\"favorite\":" + dfc.isFavorite(driverId) + "}", 200);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    // Feature 8 – Price Negotiation
    static class CounterOfferHandler implements HttpHandler {
        private final com.covoitdark.controllers.NegotiationController nc = new com.covoitdark.controllers.NegotiationController();
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int requestId = Integer.parseInt(extractJsonValue(input, "requestId"));
                double price = Double.parseDouble(extractJsonValue(input, "price"));
                String result = nc.proposeCounterOffer(requestId, price);
                sendJsonResponse(exchange, "{\"message\":\"" + escapeJson(result) + "\"}", 200);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    static class AcceptOfferHandler implements HttpHandler {
        private final com.covoitdark.controllers.NegotiationController nc = new com.covoitdark.controllers.NegotiationController();
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int requestId = Integer.parseInt(extractJsonValue(input, "requestId"));
                String result = nc.acceptCounterOffer(requestId);
                sendJsonResponse(exchange, "{\"message\":\"" + escapeJson(result) + "\"}", 200);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    static class RejectOfferHandler implements HttpHandler {
        private final com.covoitdark.controllers.NegotiationController nc = new com.covoitdark.controllers.NegotiationController();
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int requestId = Integer.parseInt(extractJsonValue(input, "requestId"));
                String result = nc.rejectCounterOffer(requestId);
                sendJsonResponse(exchange, "{\"message\":\"" + escapeJson(result) + "\"}", 200);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    // Feature 10 – Driver Earnings
    static class DriverEarningsHandler implements HttpHandler {
        private final com.covoitdark.controllers.StatsController sc = new com.covoitdark.controllers.StatsController();
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId() == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            double earned = sc.getDriverEarnings();
            double rate = sc.getCancellationRate();
            sendJsonResponse(exchange, "{\"moneyEarned\":" + earned + ",\"cancellationRate\":" + String.format("%.2f", rate) + "}", 200);
        }
    }

    // Feature 12 – Promo Codes
    static class PromoValidateHandler implements HttpHandler {
        private final com.covoitdark.controllers.PromoCodeController pcc = new com.covoitdark.controllers.PromoCodeController();
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            String query = exchange.getRequestURI().getQuery();
            try {
                String code = query.split("code=")[1].split("&")[0];
                double base = query.contains("price=") ? Double.parseDouble(query.split("price=")[1].split("&")[0]) : 0;
                com.covoitdark.models.PromoCode p = pcc.validate(code);
                if (p == null) { sendJsonResponse(exchange, "{\"valid\":false}", 200); return; }
                double after = pcc.applyCode(code, base);
                sendJsonResponse(exchange, "{\"valid\":true,\"discountedPrice\":" + String.format("%.2f", after) + "}", 200);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    static class PromoCreateHandler implements HttpHandler {
        private final com.covoitdark.controllers.PromoCodeController pcc = new com.covoitdark.controllers.PromoCodeController();
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                String code = extractJsonValue(input, "code");
                double pct = Double.parseDouble(extractJsonValue(input, "discountPct"));
                double fixed = Double.parseDouble(extractJsonValue(input, "discountFixed"));
                int maxUses = Integer.parseInt(extractJsonValue(input, "maxUses"));
                String result = pcc.createCode(code, pct, fixed, maxUses, null);
                sendJsonResponse(exchange, "{\"message\":\"" + escapeJson(result) + "\"}", 200);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    static class PromoListHandler implements HttpHandler {
        private final com.covoitdark.controllers.PromoCodeController pcc = new com.covoitdark.controllers.PromoCodeController();
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!com.covoitdark.utils.SessionManager.getInstance().isAdmin()) { sendJsonResponse(exchange, "{\"error\":\"Accès refusé\"}", 403); return; }
            java.util.List<String> summary = pcc.getSummary();
            String json = "[" + summary.stream().map(s -> "\"" + escapeJson(s) + "\"").collect(java.util.stream.Collectors.joining(",")) + "]";
            sendJsonResponse(exchange, json, 200);
        }
    }

    static class PromoDeactivateHandler implements HttpHandler {
        private final com.covoitdark.controllers.PromoCodeController pcc = new com.covoitdark.controllers.PromoCodeController();
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int promoId = Integer.parseInt(extractJsonValue(input, "id"));
                boolean ok = pcc.deactivate(promoId);
                sendJsonResponse(exchange, "{\"success\":" + ok + "}", 200);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    // Feature 13 – Disputes
    static class FileDisputeHandler implements HttpHandler {
        private final com.covoitdark.controllers.DisputeController dc = new com.covoitdark.controllers.DisputeController();
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int tripId = Integer.parseInt(extractJsonValue(input, "tripId"));
                String reason = extractJsonValue(input, "reason");
                String result = dc.fileDispute(tripId, reason);
                sendJsonResponse(exchange, "{\"message\":\"" + escapeJson(result) + "\"}", 200);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    static class DisputeListHandler implements HttpHandler {
        private final com.covoitdark.controllers.DisputeController dc = new com.covoitdark.controllers.DisputeController();
        private final com.covoitdark.utils.JsonMapper<com.covoitdark.models.Dispute> mapper = new com.covoitdark.utils.JsonMapper<>(com.covoitdark.models.Dispute.class);
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!com.covoitdark.utils.SessionManager.getInstance().isAdmin()) { sendJsonResponse(exchange, "{\"error\":\"Accès refusé\"}", 403); return; }
            sendJsonResponse(exchange, mapper.toJsonArray(dc.getOpenDisputes()), 200);
        }
    }

    static class DisputeResolveHandler implements HttpHandler {
        private final com.covoitdark.controllers.DisputeController dc = new com.covoitdark.controllers.DisputeController();
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int disputeId = Integer.parseInt(extractJsonValue(input, "disputeId"));
                String note = extractJsonValue(input, "note");
                double refund = Double.parseDouble(extractJsonValue(input, "refund"));
                String result = dc.resolve(disputeId, note, refund);
                sendJsonResponse(exchange, "{\"message\":\"" + escapeJson(result) + "\"}", 200);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    static class DisputeDismissHandler implements HttpHandler {
        private final com.covoitdark.controllers.DisputeController dc = new com.covoitdark.controllers.DisputeController();
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int disputeId = Integer.parseInt(extractJsonValue(input, "disputeId"));
                String note = extractJsonValue(input, "note");
                String result = dc.dismiss(disputeId, note);
                sendJsonResponse(exchange, "{\"message\":\"" + escapeJson(result) + "\"}", 200);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    // ── Admin: delete any user account ───────────────────────────────────────
    static class AdminDeleteUserHandler implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int adminId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (adminId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            com.covoitdark.dao.UserDAO userDAO = new com.covoitdark.dao.UserDAO();
            com.covoitdark.models.User admin = userDAO.findByIdDirect(adminId);
            if (admin == null || !admin.isAdmin()) { sendJsonResponse(exchange, "{\"error\":\"Réservé à l'administrateur\"}", 403); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int targetId = Integer.parseInt(extractJsonValue(input, "userId"));
                com.covoitdark.models.User target = userDAO.findByIdDirect(targetId);
                String name = target != null ? target.getFullName() : "#" + targetId;
                boolean ok = userDAO.delete(targetId);
                if (ok) {
                    com.covoitdark.utils.AuditLogger.getInstance().log(adminId, "DELETE_USER", "USER", targetId, name);
                    sendJsonResponse(exchange, "{\"success\":true}", 200);
                } else {
                    sendJsonResponse(exchange, "{\"error\":\"Suppression échouée\"}", 500);
                }
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    // ── Audit log ────────────────────────────────────────────────────────────
    static class AuditLogHandler implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            int adminId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (adminId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            com.covoitdark.dao.UserDAO userDAO = new com.covoitdark.dao.UserDAO();
            com.covoitdark.models.User admin = userDAO.findByIdDirect(adminId);
            if (admin == null || !admin.isAdmin()) { sendJsonResponse(exchange, "{\"error\":\"Réservé à l'administrateur\"}", 403); return; }
            String q = exchange.getRequestURI().getQuery();
            int limit = 50;
            try { if (q != null && q.contains("limit=")) limit = Integer.parseInt(q.split("limit=")[1].split("&")[0]); } catch (Exception ignored) {}
            java.util.List<java.util.Map<String,Object>> entries =
                com.covoitdark.utils.AuditLogger.getInstance().getRecentEntries(limit);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < entries.size(); i++) {
                if (i > 0) sb.append(",");
                java.util.Map<String,Object> e = entries.get(i);
                sb.append("{")
                  .append("\"id\":").append(e.get("id")).append(",")
                  .append("\"actorId\":").append(e.get("actorId")).append(",")
                  .append("\"actorName\":\"").append(escapeJson(String.valueOf(e.get("actorName")))).append("\",")
                  .append("\"action\":\"").append(escapeJson(String.valueOf(e.get("action")))).append("\",")
                  .append("\"targetType\":\"").append(escapeJson(String.valueOf(e.get("targetType")))).append("\",")
                  .append("\"targetId\":").append(e.get("targetId")).append(",")
                  .append("\"detail\":").append(e.get("detail") == null ? "null" : "\"" + escapeJson(String.valueOf(e.get("detail"))) + "\"").append(",")
                  .append("\"createdAt\":\"").append(escapeJson(String.valueOf(e.get("createdAt")))).append("\"")
                  .append("}");
            }
            sb.append("]");
            sendJsonResponse(exchange, sb.toString(), 200);
        }
    }

    // ── Sparkline: trip counts per day for last 7 days ───────────────────────
    static class SparklineHandler implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            // Returns daily trip completion counts for last 7 days as [{date, count}]
            try {
                String sql = "SELECT DATE(created_at) as day, COUNT(*) as cnt " +
                             "FROM trips WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                             "GROUP BY DATE(created_at) ORDER BY day ASC";
                java.sql.Statement st = com.covoitdark.dao.DatabaseConnection.getInstance().getConnection().createStatement();
                java.sql.ResultSet rs = st.executeQuery(sql);
                StringBuilder sb = new StringBuilder("[");
                boolean first = true;
                while (rs.next()) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append("{\"date\":\"").append(rs.getString("day")).append("\",\"count\":").append(rs.getInt("cnt")).append("}");
                }
                sb.append("]");
                sendJsonResponse(exchange, sb.toString(), 200);
            } catch (Exception e) { sendJsonResponse(exchange, "[]", 200); }
        }
    }

    // ── Upload ID document (driver self-upload) ───────────────────────────────
    static class UploadIdDocHandler implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int userId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (userId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String doc = extractJsonValue(input, "document");
            if (doc == null || doc.isBlank()) { sendJsonResponse(exchange, "{\"error\":\"Document manquant\"}", 400); return; }
            com.covoitdark.dao.UserDAO userDAO = new com.covoitdark.dao.UserDAO();
            // Reset id_verified so admin sees it as pending again
            userDAO.setIdVerified(userId, false);
            boolean ok = userDAO.uploadIdDocument(userId, doc);
            sendJsonResponse(exchange, "{\"success\":" + ok + "}", ok ? 200 : 500);
        }
    }

    // ── Admin: fetch a driver's ID document image ────────────────────────────
    static class GetIdDocHandler implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            int adminId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (adminId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            com.covoitdark.dao.UserDAO userDAO = new com.covoitdark.dao.UserDAO();
            com.covoitdark.models.User admin = userDAO.findByIdDirect(adminId);
            if (admin == null || !admin.isAdmin()) { sendJsonResponse(exchange, "{\"error\":\"Réservé à l'administrateur\"}", 403); return; }
            String q = exchange.getRequestURI().getQuery();
            try {
                int driverId = Integer.parseInt(q.split("userId=")[1].split("&")[0]);
                String doc = userDAO.getIdDocument(driverId);
                sendJsonResponse(exchange, "{\"document\":" + (doc != null ? "\"" + escapeJson(doc) + "\"" : "null") + "}", 200);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }

    // ── Admin: ask driver to resubmit ID photo ───────────────────────────────
    static class RequestIdResubmitHandler implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { exchange.sendResponseHeaders(405, -1); return; }
            int adminId = com.covoitdark.utils.SessionManager.getInstance().getCurrentUserId();
            if (adminId == -1) { sendJsonResponse(exchange, "{\"error\":\"Non autorisé\"}", 401); return; }
            com.covoitdark.dao.UserDAO userDAO = new com.covoitdark.dao.UserDAO();
            com.covoitdark.models.User admin = userDAO.findByIdDirect(adminId);
            if (admin == null || !admin.isAdmin()) { sendJsonResponse(exchange, "{\"error\":\"Réservé à l'administrateur\"}", 403); return; }
            String input = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                int driverId = Integer.parseInt(extractJsonValue(input, "driverId"));
                // Clear the doc so driver can upload a fresh one
                userDAO.uploadIdDocument(driverId, null);
                com.covoitdark.dao.NotificationDAO notif = new com.covoitdark.dao.NotificationDAO();
                com.covoitdark.models.Notification n = new com.covoitdark.models.Notification();
                n.setUserId(driverId);
                n.setTitle("Nouvelle photo requise");
                n.setMessage("L'administrateur vous demande de soumettre à nouveau votre pièce d'identité dans votre profil.");
                notif.create(n);
                com.covoitdark.utils.AuditLogger.getInstance().log(adminId, "REQUEST_ID_RESUBMIT", "USER", driverId);
                sendJsonResponse(exchange, "{\"success\":true}", 200);
            } catch (Exception e) { sendJsonResponse(exchange, "{\"error\":\"Invalide\"}", 400); }
        }
    }
}
