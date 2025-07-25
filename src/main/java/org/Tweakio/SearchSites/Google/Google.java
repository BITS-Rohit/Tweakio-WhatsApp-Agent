package org.Tweakio.SearchSites.Google;

import com.google.api.services.gmail.model.Profile;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.Tweakio.UserSettings.user;
import org.Tweakio.WhatsappWeb.Extras;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Google {

    private final String API_KEY;
    private final String CSE_ID;
    private static final String BASE_URL = "https://www.googleapis.com/customsearch/v1";
    private static final int MAX_RESULTS = 10;

    private final OkHttpClient client;
    private final Gson gson;

    public Google() {
        this.API_KEY = user.GOOGLE_API_KEY;
        this.CSE_ID = user.CSE_ID;
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    public String google(String query) {
        if (API_KEY.isEmpty() || CSE_ID.isEmpty()) {
            return "❌ Google API key or CSE ID not set in profile: " ;
        }

        String url = BASE_URL +
                "?key=" + API_KEY +
                "&cx=" + CSE_ID +
                "&q=" + URLEncoder.encode(query, StandardCharsets.UTF_8) +
                "&num=" + MAX_RESULTS;

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                Extras.logwriter("Google API http error //google "+response.code());
                return "⚠️ Google API HTTP " + response.code();
            }

            JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);

            if (json.has("error")) {
                JsonObject err = json.getAsJsonObject("error");
                String msg = err.has("message") ? err.get("message").getAsString() : "Unknown error";
                Extras.logwriter("Google API  error //google "+msg);
                return "⚠️ Google API error: " + msg;
            }

            JsonObject searchInfo = json.has("searchInformation")
                    ? json.getAsJsonObject("searchInformation")
                    : null;
            if (searchInfo == null) {
                Extras.logwriter("No Search information found in Google API ");
                return "⚠️ No search information available.";
            }

            JsonArray items = json.has("items") ? json.getAsJsonArray("items") : null;
            if (items == null || items.isEmpty()) {
                Extras.logwriter("No results found for "+query+" in Google API");
                return "⚠️ No results found for \"" + query + "\".";
            }

            StringBuilder result = new StringBuilder();
            result.append("🔍 Search Time: ")
                    .append(searchInfo.has("searchTime")
                            ? searchInfo.get("searchTime").getAsString()
                            : "N/A")
                    .append(" seconds\n")
                    .append("📦 Total Results: ")
                    .append(searchInfo.has("formattedTotalResults")
                            ? searchInfo.get("formattedTotalResults").getAsString()
                            : "N/A")
                    .append("\n\n");

            for (int i = 0; i < items.size(); i++) {
                JsonObject item = items.get(i).getAsJsonObject();
                String title = item.has("title") ? item.get("title").getAsString() : "No title";
                String link = item.has("link") ? item.get("link").getAsString() : "No link";
                String snippet = item.has("snippet") ? item.get("snippet").getAsString() : "No description.";

                String thumbnail = null;
                if (item.has("pagemap")) {
                    JsonObject pageMap = item.getAsJsonObject("pagemap");
                    if (pageMap.has("cse_thumbnail")) {
                        JsonArray thumbs = pageMap.getAsJsonArray("cse_thumbnail");
                        if (!thumbs.isEmpty()) {
                            JsonObject thumbObj = thumbs.get(0).getAsJsonObject();
                            if (thumbObj.has("src")) {
                                thumbnail = thumbObj.get("src").getAsString();
                            }
                        }
                    }
                }

                result.append("🔸 Title      : ").append(title).append("\n")
                        .append("🔗 Link       : ").append(link).append("\n")
                        .append("📝 Description: ").append(snippet).append("\n");

                if (thumbnail != null) {
                    result.append("🖼️ Thumbnail  : ").append(thumbnail).append("\n");
                }

                result.append("---------------------------------------\n");
            }

            return result.toString();

        } catch (IOException e) {
            Extras.logwriter("Network error //google : "+e.getMessage());
            return "💥 Network error: " + e.getMessage();
        } catch (Exception e) {
            Extras.logwriter("Error in google : "+e.getMessage());
            return "⚠️ Unexpected error: " + e.getMessage();
        }
    }

    // ✅ Optional test main
//    public static void main(String[] args) {
//        Google g = new Google("Me_2");
//        System.out.println(g.google("genshin character skirk data set"));
//    }
}
