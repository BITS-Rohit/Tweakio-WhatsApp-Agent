package org.Tweakio.SearchSites.Youtube;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.Tweakio.UserSettings.user;
import org.Tweakio.WhatsappWeb.Extras;
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
import java.util.*;

import static java.lang.System.in;

public class YoutubeAPI {
    private final String BaseURL;
    private final String key;
    private final OkHttpClient client;
    private final String maxResults = "10";
    private final boolean debugmode = true;
    private final static String ytDlpPath = "C:\\Users\\rohit\\Downloads\\yt-dlp.exe";

    public YoutubeAPI( ){
        BaseURL = "https://www.googleapis.com/youtube/v3/search?part=snippet&q=";
        key = user.YOUTUBE_API_KEY; // Consider externalizing this
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
                Extras.logwriter("Youtube api error //search : " + msg);
                return "⚠️ YouTube API error: " + msg;
            }

            // 2) Safely grab items:
            JsonArray results = json.getAsJsonArray("items");

            if (results == null || results.isEmpty()) {
                Extras.logwriter("No results // seach / youtube for : "+query);
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
                Extras.logwriter("No videos found // youtube  //search : " + query);
                return "⚠️ No videos found in search results.";
            }
            videoIds.setLength(videoIds.length() - 1);

            // 3) Fetch details, same guard logic there…
            return fetchVideoDetails(videoIds.toString(), results);

        } catch (IOException e) {
            Extras.logwriter("Network error //search //youtube : " + e.getMessage());
            return "⚠️ Network error: " + e.getMessage();
        } catch (Exception e) {
            Extras.logwriter("Error in Youtube API //search : " + e.getMessage());
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
        Extras.logwriter("Thumbnail URL not found //youtube ");
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
    public boolean ytdownloadfromUrls(String vidurl, String[] res, String[] name) {
        File saveDir = Paths.get(System.getProperty("user.dir"), "src", "main", "java", "org", "Tweakio", "FilesSaved").toFile();
        if (!saveDir.exists() && !saveDir.mkdirs()) {
            System.out.println("❌ Failed to create directory: " + saveDir.getAbsolutePath());
            Extras.logwriter("Failed to create directory //youtube : " + saveDir.getAbsolutePath());
            return false;
        }

        String outputTemplate = saveDir.getAbsolutePath() + File.separator + "%(title)s.%(ext)s";
        List<String> command = new ArrayList<>(Arrays.asList(
                "yt-dlp",
                "-f", "bestaudio",
                "-x",
                "--audio-format", "mp3",
                "--restrict-filenames",
                "-o", outputTemplate,
                vidurl
        ));

        // Add ffmpeg location if OS is Windows
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            String ffmpegPath = "C:\\Users\\rohit\\Downloads\\ffmpeg-7.1.1-full_build\\ffmpeg-7.1.1-full_build\\bin";
            command.addAll(Arrays.asList("--ffmpeg-location", ffmpegPath));
        }

        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String finalFileName = null;
            String previousLine = null;

            while ((line = reader.readLine()) != null) {
                if (debugmode) System.out.println(line);

                if (line.contains("[ExtractAudio] Destination:") || line.contains("[download] Destination:")) {
                    finalFileName = line.substring(line.indexOf(":") + 1).trim();
                } else if (line.contains("file is already in target format mp3") && previousLine != null && previousLine.contains("[ExtractAudio] Not converting")) {
                    int start = previousLine.indexOf("audio ") + 6;
                    int end = previousLine.indexOf("; file is already");
                    if (start > 0 && end > start) {
                        finalFileName = previousLine.substring(start, end).trim();
                    }
                }

                previousLine = line;
            }

            int exitCode = process.waitFor();

            // Fallback if filename was never parsed
            if (finalFileName == null) {
                File[] mp3s = saveDir.listFiles((dir, name1) -> name1.toLowerCase().endsWith(".mp3"));
                if (mp3s != null && mp3s.length > 0) {
                    Arrays.sort(mp3s, Comparator.comparingLong(File::lastModified).reversed());
                    finalFileName = mp3s[0].getAbsolutePath();
                }
            }

            if (exitCode == 0 && finalFileName != null) {
                File downloadedFile = new File(finalFileName);
                if (downloadedFile.exists()) {
                    res[0] = downloadedFile.getAbsolutePath();
                    name[0] = downloadedFile.getName();
                    System.out.println("✅✅ Downloaded successfully to: " + res[0]);
                    return true;
                } else {
                    System.err.println("❌ Download reported success, but file not found: " + finalFileName);
                    Extras.logwriter("Download reported success, but file not found //youtube: " + finalFileName);
                }
            } else {
                System.out.println("🔻🔻 Nope, Not this time. Downloading Error");
                Extras.logwriter("Downloading Error //youtube ");
            }

        } catch (IOException e) {
            System.out.println("❌ Reader Error: " + e.getMessage());
            Extras.logwriter("Reader Error //youtube: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("❌ Interrupted: " + e.getMessage());
            Extras.logwriter("Interrupted //youtube: " + e.getMessage());
            Thread.currentThread().interrupt();
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
                        Extras.logwriter("Invalid JSON line skipped //youtube : " + line);
                    }
                } else {
                    System.out.println("ℹ️ Non-JSON Output: " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("❌ yt-dlp command failed.");
                Extras.logwriter("yt-dlp command failed //youtube  ");
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
            Extras.logwriter("Error executing yt-dlp: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("⚠️ Interrupted: " + e.getMessage());
            Extras.logwriter("Interrupted executing yt-dlp: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

//    public static void main(String[] args) {
//        YoutubeAPI a = new YoutubeAPI();
//        System.out.println(a.ytdownloadfromUrls("https://www.youtube.com/watch?v=TU2Hrw10YLk", new String[1]));
//    }
}

