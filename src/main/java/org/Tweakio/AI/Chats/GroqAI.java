package org.Tweakio.AI.Chats;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.Tweakio.AI.HistoryManager.ChatMemory;
import org.Tweakio.UserSettings.user;
import org.Tweakio.WhatsappWeb.Extras;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;


class GroqResponse {
    List<Choice> choices;

    public static class Choice {
        Message message;
    }

    public static class Message {
        String role;
        String content;
    }


    public static class usage {
        int prompt_tokens;
        int completion_tokens;
        int total_tokens;
        int total_time;
    }
}

public class GroqAI {
    private final String groqapikey;
    private final String baseurl;
    private final String modelname;
    private final Gson gson;
    private final OkHttpClient client;
    private int totaltokens;
    ChatMemory memory = new ChatMemory();

    public GroqAI() {
        totaltokens = 0;
        modelname = "llama-3.3-70b-versatile";
        client = new OkHttpClient();
        gson = new Gson();
        baseurl = "https://api.groq.com/openai/v1/chat/completions";
        groqapikey = user.GROQ_API_KEY;
    }

    public synchronized String chat(String ask) {

        JsonObject payload = getJsonObject(ask);

        RequestBody body = RequestBody.create(
                gson.toJson(payload), MediaType.get("application/json"));

        Request req = new Request.Builder().url(baseurl)
                .addHeader("Authorization", "Bearer " + groqapikey)
                .post(body)
                .build();

        try (Response res = client.newCall(req).execute()) {
            String json = res.body() != null ? res.body().string() : "";

            JsonObject js = gson.fromJson(json, JsonObject.class);
            if (js == null || js.isEmpty()) {
                Extras.logwriter("Empty response from groq //chat //1 : " + json);
                return "⚠️ Empty response from Groq.";
            }

            GroqResponse response = gson.fromJson(json, GroqResponse.class);
            if (response.choices == null || response.choices.isEmpty()) {
                Extras.logwriter("Empty response from groq //chat //2 : " + json);
                return "⚠️ AI returned nothing.";
            }

            GroqResponse.Message message = response.choices.get(0).message;
            String reply = message != null ? message.content : "No message returned.";

            if(reply.equals("No message returned."))Extras.logwriter("No message returned. //groq");

            // 👇 Save to file
            memory.writeToFile("User : " + ask, "groq_ai");
            memory.writeToFile("AI : " + reply, "groq_ai");

            // Token tracking
            if (js.has("usage")) {
                JsonObject usage = js.getAsJsonObject("usage");
                if (usage.has("total_tokens")) {
                    totaltokens = usage.get("total_tokens").getAsInt();
                }
            }

            return reply;

        } catch (IOException e) {
            System.out.println("❌ Error: " + e.getMessage());
            Extras.logwriter("Error //groq ai // chat : " + e.getMessage());
            return "Null";
        }
    }


    @NotNull
    private JsonObject getJsonObject(String ask) {
        // Friendly, helpful system prompt
        String system = "You are Rohit's AI. You are polite, respectful, helpful, and never aggressive. " +
                "Always greet users warmly, answer clearly, and assist with any task. " +
                "If you don't know something, reply: 'I'm still learning. Please ask Rohit, my Admin.'";

        JsonArray messages = new JsonArray();

        // 👉 Proper system message
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", system);
        messages.add(systemMessage);

        // 👉 Convert history to role-based messages
        List<String> history = memory.readFromFile("groq_ai");
        for (String line : history) {
            if (line.startsWith("User :")) {
                JsonObject userMessage = new JsonObject();
                userMessage.addProperty("role", "user");
                userMessage.addProperty("content", line.replace("User :", "").trim());
                messages.add(userMessage);
            } else if (line.startsWith("AI :")) {
                JsonObject aiMessage = new JsonObject();
                aiMessage.addProperty("role", "assistant");
                aiMessage.addProperty("content", line.replace("AI :", "").trim());
                messages.add(aiMessage);
            }
        }

        // 👉 Add current user message
        JsonObject currentUserMessage = new JsonObject();
        currentUserMessage.addProperty("role", "user");
        currentUserMessage.addProperty("content", ask);
        messages.add(currentUserMessage);

        // 👉 Final payload
        JsonObject payload = new JsonObject();
        payload.addProperty("model", modelname);
        payload.add("messages", messages);

        return payload;
    }


    public int tokens() {
        return totaltokens;
    }
//
    public static void main(String[] args) {
        GroqAI g = new GroqAI();
        System.out.println("Enter Search:");
        System.out.println(g.chat(new Scanner(System.in).nextLine()));
        System.out.println("Tokens used: " + g.tokens());
    }
}
