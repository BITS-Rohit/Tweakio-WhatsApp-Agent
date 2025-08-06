package org.Tweakio.WhatsappWeb;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public final class MapSerializer {
    private static final Path FILE_PATH =
            Paths.get("src/main/java/org/Tweakio/FilesSaved/saved_data.ser");

    private MapSerializer() {
        // no instances
    }

    public static void serialize(Map<String, Set<String>> map) {
        try {
            // make sure directory exists
            Files.createDirectories(FILE_PATH.getParent());

            try (var oos = new ObjectOutputStream(Files.newOutputStream(FILE_PATH))) {
                oos.writeObject(map);
                System.out.println("✅ Map serialized to file.");
            }
        } catch (IOException e) {
            logError("Error during serialization", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Set<String>> deserialize() {
        if (!Files.exists(FILE_PATH)) {
            System.out.println("ℹ️ No previous data file found. Returning empty map.");
            return new HashMap<>();
        }
        try (var ois = new ObjectInputStream(Files.newInputStream(FILE_PATH))) {
            var obj = ois.readObject();
            if (obj instanceof Map<?, ?>) {
                return (Map<String, Set<String>>) obj;
            } else {
                logError("Deserialized object is not a Map", null);
            }
        } catch (IOException | ClassNotFoundException e) {
            logError("Error during deserialization", e);
        }
        return new HashMap<>();
    }

    private static void logError(String message, Exception e) {
        var full = message + (e != null ? ": " + e.getMessage() : "");
        System.err.println("❌ " + full);
        Extras.logwriter("MapSerializer // " + full);
    }
}
