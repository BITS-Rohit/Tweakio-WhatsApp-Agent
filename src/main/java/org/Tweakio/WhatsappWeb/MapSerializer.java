package org.Tweakio.WhatsappWeb;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapSerializer {

    private static final Path FILE_PATH = Paths.get("src/main/java/org/Tweakio/FilesSaved/saved_data.ser");

    public static void serialize(Map<String, Set<String>> map) {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(FILE_PATH))) {
            oos.writeObject(map);
            System.out.println("✅ Map serialized to file.");
        } catch (IOException e) {
            System.err.println("❌ Error during serialization: " + e.getMessage());
            Extras.logwriter("Error during serialization //mapserialzier // serialize : " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Set<String>> deserialize() {
        if (!Files.exists(FILE_PATH)) {
            System.out.println("ℹ️ No previous data file found. Returning empty map.");
            return new HashMap<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(FILE_PATH))) {
            Object obj = ois.readObject();
            if (obj instanceof Map<?, ?>) {
                return (Map<String, Set<String>>) obj;
            }
            else {
                System.err.println("❌ Deserialized object is not a Map.");
                Extras.logwriter("Error ---> Deserialized object is not a Map. //mapserialzier // deserialize ");
                return new HashMap<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ Error during deserialization: " + e.getMessage());
            Extras.logwriter("Error during deserialization // mapserailzer // deserilaize : " + e.getMessage());
            return new HashMap<>();
        }
    }
}