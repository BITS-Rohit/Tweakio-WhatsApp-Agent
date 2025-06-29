package org.Tweakio.AI.ImageAIs;

import okhttp3.*;
import org.Tweakio.UserSettings.user;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dall_E_3 {
    private static final OkHttpClient client = new OkHttpClient();
    public Dall_E_3() {
    }

    /**
     * Sends `prompt` to Agent.ai via the async webhook and returns the raw image URL.
     * Strips any HTML wrapper.
     */
    public String get_Pic_Url_With_Prompt(String prompt) {
        final String agentId   = user.AgentID;
        final String webhookId = user.webhook;
        final String base      = user.base + agentId + "/webhook/" + webhookId;

        try {
            // 1) Start run
            JSONObject payload = new JSONObject().put("user_input", prompt);
            RequestBody body = RequestBody.create(
                    payload.toString(),
                    MediaType.get("application/json")
            );
            Request startReq = new Request.Builder()
                    .url(base + "/async")
                    .post(body)
                    .build();

            String runId;
            try (Response r = client.newCall(startReq).execute()) {
                if (!r.isSuccessful() || r.body() == null) {
                    System.err.println("❌ Start run failed: HTTP " + r.code());
                    return null;
                }
                JSONObject js = new JSONObject(r.body().string());
                runId = js.optString("run_id", "");
                if (runId.isEmpty()) {
                    System.err.println("❌ Empty run_id");
                    return null;
                }
            }

            // 2) Poll
            String statusUrl = base + "/status/" + runId;
            while (true) {
                Request poll = new Request.Builder().url(statusUrl).get().build();
                try (Response pr = client.newCall(poll).execute()) {
                    int code = pr.code();
                    if (code == 204) {
                        Thread.sleep(500);
                        continue;
                    }
                    if (code == 200 && pr.body() != null) {
                        String resp = pr.body().string();
                        JSONObject result = new JSONObject(resp);
                        String raw = result.optString("response", null);
                        if (raw == null) return null;
                        return extractSrc(raw);
                    }
                    System.err.println("❌ Polling error: HTTP " + code);
                    return null;
                }
            }
        } catch (IOException e) {
            System.err.println("❌ I/O error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("❌ Interrupted: " + e.getMessage());
        }
        return null;
    }

    /**
     * If input is an <img> tag, extracts the src URL; otherwise returns input as-is.
     */
    private String extractSrc(String htmlOrUrl) {
        // quick check: starts with "<img"
        if (!htmlOrUrl.trim().toLowerCase().startsWith("<img")) {
            return htmlOrUrl;
        }
        // regex to grab src="…"
        Pattern p = Pattern.compile("src\\s*=\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(htmlOrUrl);
        if (m.find()) {
            return m.group(1);
        }
        // fallback
        return htmlOrUrl;
    }

    /**
     * Download from "<image-url>##<filename>" into
     * src/main/java/org/Tweakio/FilesSaved/Image/, preserving quality.
     */
    public Path downloadImageFromInput(String combined) {
        if (combined == null || !combined.contains("##")) {
            System.err.println("❌ Input must be '<url>##<filename>'");
            return null;
        }
        String[] parts = combined.split("##", 2);
        String imageUrl = parts[0].trim();
        String filename = parts[1].trim();

        if (!imageUrl.startsWith("http")) {
            System.err.println("❌ Invalid URL: " + imageUrl);
            return null;
        }

        // infer extension
        String ext = "png";
        int dot = imageUrl.lastIndexOf('.');
        if (dot > 0) {
            String tmp = imageUrl.substring(dot + 1).split("[?#]")[0];
            if (tmp.matches("(?i)png|jpg|jpeg|webp|gif")) {
                ext = tmp.toLowerCase();
            }
        }

        try {
            Path dir = Paths.get("src/main/java/org/Tweakio/FilesSaved/Image");
            Files.createDirectories(dir);
            Path output = dir.resolve(filename + "." + ext);

            Request req = new Request.Builder().url(imageUrl).get().build();
            try (Response res = client.newCall(req).execute()) {
                if (!res.isSuccessful() || res.body() == null) {
                    System.err.println("❌ Download failed: HTTP " + res.code());
                    return null;
                }
                try (InputStream in = res.body().byteStream()) {
                    Files.copy(in, output, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            System.out.println("✅ Image saved to: " + output.toAbsolutePath());
            return output;
        } catch (IOException e) {
            System.err.println("❌ I/O Error: " + e.getMessage());
        }
        return null;
    }

//    public static void main(String[] args) {
//        Dall_E_3 ai = new Dall_E_3();
//
//        // 1) Generate & extract URL
//        long start = System.currentTimeMillis();
//        String url = ai.get_Pic_Url_With_Prompt(
//                "Crescent moon  neon light with shiny dark night universe and a person with hoodie staring at the secnario"
//        );
//        if (url == null) {
//            System.err.println("❌ Generation failed.");
//            return;
//        }
//        System.out.println("➡️ Image URL: " + url);
//
//        // 2) Download
//        Path saved = ai.downloadImageFromInput(url + "##cosmic_scene");
//        if (saved != null) {
//            System.out.println("✅ Final saved path: " + saved.toAbsolutePath());
//        } else {
//            System.err.println("❌ Download/save failed.");
//        }
//        System.out.println("Total Time : "+ Extras.Time(start)+ "Sec");
//    }
}
