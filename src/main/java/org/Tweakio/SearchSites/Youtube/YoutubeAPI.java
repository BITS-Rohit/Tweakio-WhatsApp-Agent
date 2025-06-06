package org.bot.SearchSites.Youtube;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bot.UserSettings.user;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static java.lang.System.in;

public class YoutubeAPI {
    private final String BaseURL;
    private final String key;
    private final OkHttpClient client;
    private final String maxResults = "10";
    private final boolean debugmode = true;
    private final static String ytDlpPath = "C:\\Users\\rohit\\Downloads\\yt-dlp.exe";
    static user u = new user();

    public YoutubeAPI() {
        BaseURL = "https://www.googleapis.com/youtube/v3/search?part=snippet&q=";
        key = u.youtubeapikey; // Consider externalizing this
        client = new OkHttpClient();
    }

    private void getQuery() {
        System.out.print("Enter query: ");
        String query = new Scanner(in).nextLine().trim().replace(" ", "%20");
    }

    public synchronized String search(String query) {
        System.out.println("🛠️ Received query: \"" + query + "\"");
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        String searchURL = BaseURL
                + encodedQuery
                + "&key=" + key
                + "&type=video"
                + "&maxResults=" + maxResults;
        System.out.println("🛠️ Calling URL: " + searchURL);
        Request request = new Request.Builder().url(searchURL).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return "⚠️ YouTube API request failed with HTTP " + response.code() + "\n May be ur quota has exceeded";
            }

            JsonObject json = JsonParser.parseString(response.body().string())
                    .getAsJsonObject();

            // 1) If the API returned an error object, surface it:
            if (json.has("error")) {
                JsonObject err = json.getAsJsonObject("error");
                String msg = err.has("message")
                        ? err.get("message").getAsString()
                        : "Unknown error";
                return "⚠️ YouTube API error: " + msg;
            }

            // 2) Safely grab items:
            JsonArray results = json.getAsJsonArray("items");

            if (results == null || results.isEmpty()) {
                return "⚠️ No results found for \"" + query + "\".";
            }

            // build comma-separated list of IDs
            StringBuilder videoIds = new StringBuilder();
            for (int i = 0; i < results.size(); i++) {
                JsonObject idObj = results.get(i)
                        .getAsJsonObject()
                        .getAsJsonObject("id");
                if (idObj.has("videoId")) {
                    videoIds.append(idObj.get("videoId").getAsString())
                            .append(",");
                }
            }
            if (videoIds.isEmpty()) {
                System.out.println(response);
                return "⚠️ No videos found in search results.";
            }
            videoIds.setLength(videoIds.length() - 1);

            // 3) Fetch details, same guard logic there…
            return fetchVideoDetails(videoIds.toString(), results);

        } catch (IOException e) {
            return "⚠️ Network error: " + e.getMessage();
        } catch (Exception e) {
            return "⚠️ Unexpected parsing error: " + e.getMessage();
        }
    }

    private synchronized String fetchVideoDetails(String videoIds, JsonArray firstCall) throws IOException {
        String detailsURL = "https://www.googleapis.com/youtube/v3/videos?part=snippet,statistics,contentDetails&id=" + videoIds + "&key=" + key;
        Request detailsRequest = new Request.Builder().url(detailsURL).build();

        try (Response detailsResponse = client.newCall(detailsRequest).execute()) {
            assert detailsResponse.body() != null;
            String metadata = detailsResponse.body().string();
            JsonObject detailsJson = JsonParser.parseString(metadata).getAsJsonObject();
            JsonArray items = detailsJson.getAsJsonArray("items");

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < items.size(); i++) {
                JsonObject video = items.get(i).getAsJsonObject();
                JsonObject snippet = video.getAsJsonObject("snippet");
                JsonObject content = video.getAsJsonObject("contentDetails");
                JsonObject stats = video.has("statistics") ? video.getAsJsonObject("statistics") : null;

                String title = snippet.has("title") ? snippet.get("title").getAsString() : "N/A";
                String description = firstCall.get(i).getAsJsonObject().getAsJsonObject("snippet").has("description") ?
                        firstCall.get(i).getAsJsonObject().getAsJsonObject("snippet").get("description").getAsString() : "No description.";
                String videoId = video.get("id").getAsString();
                String channelName = snippet.has("channelTitle") ? snippet.get("channelTitle").getAsString() : "Unknown Channel";
                String thumbnail = getThumbnailUrl(snippet.getAsJsonObject("thumbnails"));
                String publishTime = snippet.has("publishedAt") ? formatPublishedTime(snippet.get("publishedAt").getAsString()) : "N/A";
                String duration = content.has("duration") ? formatDuration(content.get("duration").getAsString()) : "N/A";
                String quality = content.has("definition") ? content.get("definition").getAsString() : "N/A";
                String license = content.has("licensedContent") ? String.valueOf(content.get("licensedContent").getAsBoolean()) : "false";
                String views = stats != null && stats.has("viewCount") ? stats.get("viewCount").getAsString() : "N/A";
                String likes = stats != null && stats.has("likeCount") ? stats.get("likeCount").getAsString() : "N/A";
                String comments = stats != null && stats.has("commentCount") ? stats.get("commentCount").getAsString() : "N/A";

                sb.append("🎥 Title: ").append(title).append("\n")
                        .append("📺 *Channel* : ").append(channelName).append("\n")
                        .append("🗓️ *Published* : ").append(publishTime).append("\n")
                        .append("📝 *Description* : ").append(description).append("\n")
                        .append("📹 *Video ID* : ").append(videoId).append("\n")
                        .append("🔗 *Link* : https://www.youtube.com/watch?v=").append(videoId).append("\n")
                        .append("🖼️ *Thumbnail* : ").append(thumbnail).append("\n")
                        .append("⏱️ *Duration* : ").append(duration).append("\n")
                        .append("📽️ *Quality* : ").append(quality).append("\n")
                        .append("🔐 *Licensed* : ").append(license).append("\n")
                        .append("👁️ *Views* : ").append(views).append("\n")
                        .append("👍 *Likes* : ").append(likes).append("\n")
                        .append("💬 *Comments* : ").append(comments).append("\n")
                        .append("---------------------------------------------------\n");
            }
            return sb.toString();
        }
    }

    private synchronized String getThumbnailUrl(JsonObject thumbnails) {
        if (thumbnails.has("maxres")) {
            return thumbnails.getAsJsonObject("maxres").get("url").getAsString();
        } else if (thumbnails.has("high")) {
            return thumbnails.getAsJsonObject("high").get("url").getAsString();
        } else if (thumbnails.has("default")) {
            return thumbnails.getAsJsonObject("default").get("url").getAsString();
        }
        return "No thumbnail available.";
    }

    private synchronized String formatDuration(String isoDuration) {
        Duration duration = Duration.parse(isoDuration);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0 || sb.isEmpty()) sb.append(seconds).append("s");

        return sb.toString().trim();
    }

    private String formatPublishedTime(String isoDate) {
        Instant instant = Instant.parse(isoDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
                .withZone(ZoneId.systemDefault());
        return formatter.format(instant);
    }

    //Utility -------------------------->
    public synchronized boolean ytdownloadfromUrls(String vidurl, String[] res) {
        File saveDir = new File(Paths.get(System.getProperty("user.dir"), "src", "main", "java", "org", "bot", "FilesSaved").toString());
        if (!saveDir.exists() && !saveDir.mkdirs()) {
            System.out.println("❌ Failed to create directory: " + saveDir.getAbsolutePath());
            return false;
        }

        String outputTemplate = saveDir.getAbsolutePath() + File.separator + "%(title)s.%(ext)s";

        List<String> command = Arrays.asList(
                "yt-dlp",
                "-f", "bestaudio",
                "-x",
                "--audio-format", "mp3",
                "--restrict-filenames",
                "--ffmpeg-location", "C:\\Users\\rohit\\Downloads\\ffmpeg-7.1.1-full_build\\ffmpeg-7.1.1-full_build\\bin",
                "-o", outputTemplate,
                vidurl
        );

        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (debugmode) System.out.println(line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                String fileName = outputTemplate.replace("%(title)s.%(ext)s", "").trim();  // Assuming filename pattern follows this
                File downloadedFile = new File(fileName); // Get file path
                res[0] = downloadedFile.getAbsolutePath();
                System.out.println("✅✅ Downloaded successfully");
                return true;
            } else {
                System.out.println("🔻🔻 Nope, Not this time. Downloading Error");
            }

        } catch (IOException e) {
            System.out.println("Reader Error: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("Exit Code error: " + e.getMessage());
            Thread.currentThread().interrupt(); // reset interrupted state
        }

        return false;
    }

    public static void ytdlpsearch() {
        String searchQuery = "ytsearch5: Nvidia ";  // fetch top 5 results

        // Build command
        List<String> command = Arrays.asList(
                ytDlpPath,
                "-j",
                searchQuery
        );

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true); // Merge stderr with stdout
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            List<JSONObject> results = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("{")) {
                    try {
                        JSONObject obj = new JSONObject(line);
                        results.add(obj);
                    } catch (Exception e) {
                        System.out.println("⚠️ Invalid JSON line skipped: " + line);
                    }
                } else {
                    System.out.println("ℹ️ Non-JSON Output: " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("❌ yt-dlp command failed.");
                return;
            }

            for (JSONObject video : results) {
                String title = video.optString("title", "No Title");
                String url = video.optString("webpage_url", "No URL");
                String thumbnail = video.optString("thumbnail", "No Thumbnail");

                System.out.println("🎬 Title     : " + title);
                System.out.println("🔗 URL       : " + url);
                System.out.println("🖼️ Thumbnail : " + thumbnail);
                System.out.println("------------");
            }

        } catch (IOException e) {
            System.out.println("❌ Error executing yt-dlp: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("⚠️ Interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        YoutubeAPI a = new YoutubeAPI();
        System.out.println(a.ytdownloadfromUrls("https://www.youtube.com/watch?v=TU2Hrw10YLk", new String[1]));
    }
}

