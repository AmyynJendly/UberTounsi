package com.covoitdark.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generic JSON serializer using Java Reflection.
 *
 * Demonstrates:
 *  - Java Reflection: dynamically reads class fields at runtime via getDeclaredMethods()
 *  - Java Generics: works for ANY model class (User, Trip, Car, etc.)
 *  - Stream API: used to map and join JSON key-value pairs
 *  - Collections: handles List fields automatically
 *
 * @param <T> the model type to serialize
 */
public class JsonMapper<T> {

    private final Class<T> type;

    public JsonMapper(Class<T> type) {
        this.type = type;
    }

    /**
     * Serializes a single object to a JSON string using reflection.
     * Reads all public getter methods (getXxx / isXxx) and builds a JSON object.
     */
    public String toJson(T obj) {
        if (obj == null) return "null";

        // Use Reflection to get all declared methods of T
        Method[] methods = type.getMethods();

        // Stream: filter getter methods, map to "key":value pairs
        List<String> pairs = Arrays.stream(methods)
                .filter(m -> (m.getName().startsWith("get") || m.getName().startsWith("is"))
                        && m.getParameterCount() == 0
                        && !m.getName().equals("getClass"))
                .map(m -> {
                    try {
                        String key = methodToKey(m.getName());
                        Object value = m.invoke(obj);
                        return "\"" + key + "\":" + valueToJson(value);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return "{" + String.join(",", pairs) + "}";
    }

    /**
     * Serializes a List of objects to a JSON array.
     * Uses Stream to map each element through toJson().
     */
    public String toJsonArray(List<T> items) {
        if (items == null || items.isEmpty()) return "[]";

        String body = items.stream()
                .map(this::toJson)
                .collect(Collectors.joining(","));

        return "[" + body + "]";
    }

    /** Convert a getter name to a camelCase JSON key. e.g. getFullName → fullName, isBlocked → blocked */
    private String methodToKey(String methodName) {
        String stripped = methodName.startsWith("is")
                ? methodName.substring(2)
                : methodName.substring(3);
        return Character.toLowerCase(stripped.charAt(0)) + stripped.substring(1);
    }

    /** Convert a Java value to its JSON representation. */
    private String valueToJson(Object value) {
        if (value == null) return "null";
        if (value instanceof String s) {
            // Escape special characters to prevent JSON injection
            String escaped = s.replace("\\", "\\\\")
                               .replace("\"", "\\\"")
                               .replace("\n", "\\n")
                               .replace("\r", "\\r");
            return "\"" + escaped + "\"";
        }
        if (value instanceof Boolean || value instanceof Number) return value.toString();
        if (value instanceof Enum<?> e) return "\"" + e.name() + "\"";
        if (value instanceof List<?> list) {
            // Recursively handle List fields (e.g. languages)
            String inner = list.stream()
                    .map(item -> "\"" + item.toString() + "\"")
                    .collect(Collectors.joining(","));
            return "[" + inner + "]";
        }
        // Temporal types — use toString
        return "\"" + value.toString().replace("\"", "\\\"") + "\"";
    }
}
