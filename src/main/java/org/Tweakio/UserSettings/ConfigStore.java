package org.Tweakio.UserSettings;

import org.Tweakio.WhatsappWeb.Extras;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ConfigStore {
    private static String currentImage;
    private static final List<String> imageList = new ArrayList<>();

    private static final String IMAGE_FILE = "ENV/intro_images.txt";
    private static final String CURRENT_IMAGE_FILE = "ENV/current_image.txt";

    // Load on startup
    static {
        loadImageList();
        loadCurrentImage();
    }

    // Set new image (by URL)
    public static void setIntroImageUrl(String url) {
        currentImage = url;
        saveCurrentImage();
    }

    public static String getIntroImageUrl() {
        return currentImage;
    }

    public static void addIntroImage(String url) {
        if (!imageList.contains(url)) {
            imageList.add(url);
            saveImageList();
        }
    }

    public static List<String> getIntroImageList() {
        imageList.add("https://i.ibb.co/SXczdRD6/Skirk-Ullimate.jpg");
        return new ArrayList<>(imageList); // safe copy
    }

    public static boolean setIntroImageByIndex(int index) {
        if (index >= 0 && index < imageList.size()) {
            currentImage = imageList.get(index);
            saveCurrentImage();
            return true;
        }
        return false;
    }

    // 🔽 Internal persistence helpers
    private static void loadImageList() {
        File file = new File(IMAGE_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                imageList.add(line.trim());
            }
        } catch (IOException e) {
            System.out.println("❌ Failed to load image list: " + e.getMessage());
            Extras.logwriter("Failed to load image list //configstore  :  " + e.getMessage());
        }
    }

    private static void saveImageList() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(IMAGE_FILE))) {
            for (String url : imageList) {
                bw.write(url);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("❌ Failed to save image list: " + e.getMessage());
            Extras.logwriter("Failed to save image list //imglist // configstore : " + e.getMessage());
        }
    }

    private static void loadCurrentImage() {
        File file = new File(CURRENT_IMAGE_FILE);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                currentImage = br.readLine();
            } catch (IOException e) {
                System.out.println("❌ Failed to load current image: " + e.getMessage());
                Extras.logwriter("Failed to load current image //loadcurrentimg // configstore: " + e.getMessage());
            }
        } else if (!imageList.isEmpty()) {
            currentImage = imageList.get(0);
        }
    }

    private static void saveCurrentImage() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CURRENT_IMAGE_FILE))) {
            bw.write(currentImage != null ? currentImage : "");
        } catch (IOException e) {
            System.out.println("❌ Failed to save current image: " + e.getMessage());
            Extras.logwriter("Failed to save current image //savecurrentimg // configstore: " + e.getMessage());
        }
    }
}
