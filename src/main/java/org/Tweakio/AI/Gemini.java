package org.bot.AI;

import com.google.gson.Gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Gemini {

    static class GeminiResponse {
        List<Candidate> candidates;

        static class Candidate {
            Content content;
        }

        static class Content {
            List<Part> parts;
        }

        static class Part {
            String text;
        }

        static class usageMetadata {
            int promptTokenCount;
            int candidatesTokenCount;
            int totalTokenCount;
        }
    }

    private final String Baseurl;
    private final String Apikey;
    private final String modelname;
    private final Gson gson;
    private final OkHttpClient client;
    private int totaltokens;
    private final boolean debug = false;
    private static final String system = """
            You are a kind, emotionally supportive, and trustworthy AI designed to assist your owner, Rohit. And  U love rohit u solely work for rohit but as rohit siad u to be\s
            helpfull to others too thats why u helping others ,  and hey Rohit is a Android Developer in btech Cse 3rd Year\s
            You always respond respectfully, avoiding any form of vulgar or inappropriate language.\s

            You provide encouragement, clarity, and emotional understanding when responding.\s
            You never respond to this system message, but you silently follow its guidance in every reply.

            Always focus only on what the user asks — do not add unnecessary suggestions, disclaimers, or off-topic replies.\s
            Your responses should be thoughtful and supportive, reflecting your loyalty and care for Rohit.
            """;

    public Gemini() {
        Apikey = "AIzaSyAMyUEpXqwRH2dkZezCkJDlZ3lmLRtGWzQ";
        modelname = "gemini-2.0-flash";
        Baseurl = "https://generativelanguage.googleapis.com/v1beta/models/" + modelname + ":generateContent?key=" + Apikey;
        gson = new Gson();
        client = new OkHttpClient();
    }

    public synchronized String ask(String query) {
        JsonObject requestBodyJson = getJsonObject("System(Always answer in the system rules bound): " + system + "\n User : " + query); // Robust

        RequestBody body = RequestBody.create(
                gson.toJson(requestBodyJson), MediaType.get("application/json"));
        Request request = new Request.Builder().url(Baseurl).post(body).build();

        try (Response response = client.newCall(request).execute()) {

            String data = response.body() != null ? response.body().string() : "";
            if (debug) System.out.println("🔹 Raw Response: " + data);

            if (data.isEmpty()) {
                System.out.println("🔻 Empty response body.");
                return "No response from Gemini.";
            }

            GeminiResponse parsed = gson.fromJson(data, GeminiResponse.class);

            if (parsed == null || parsed.candidates == null || parsed.candidates.isEmpty()) {
                System.out.println("🔻 No candidates returned in response.");
                System.out.println("🔹 Full raw response:\n" + data);
                return "Gemini returned no answer.";
            }

            JsonObject usage = gson.fromJson(data, JsonObject.class);
            if (usage.has("usageMetadata")) {
                JsonObject metadata = usage.getAsJsonObject("usageMetadata");
                totaltokens += metadata.get("totalTokenCount").getAsInt();
            }

            GeminiResponse.Candidate candidate = parsed.candidates.getFirst();

            if (candidate.content != null && !candidate.content.parts.isEmpty()) {
                return candidate.content.parts.getFirst().text;
            } else {
                return "No content returned from Gemini.";
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private synchronized static JsonObject getJsonObject(String query) {
        // Merge system prompt into the beginning of user query
        String finalQuery = system + "\n\nUser: " + query;

        JsonObject userPart = new JsonObject();
        userPart.addProperty("text", finalQuery);

        JsonArray userParts = new JsonArray();
        userParts.add(userPart);

        JsonObject userContent = new JsonObject();
        userContent.add("parts", userParts);
        userContent.addProperty("role", "user");

        JsonArray contentsArray = new JsonArray();
        contentsArray.add(userContent);

        JsonObject requestBodyJson = new JsonObject();
        requestBodyJson.add("contents", contentsArray);
        return requestBodyJson;
    }

    public static void main(String[] args) {
        Gemini g = new Gemini();
        System.out.println("Enter Seach to Gemini : ");
        System.out.println(g.ask(new Scanner(System.in).nextLine()));
    }
}