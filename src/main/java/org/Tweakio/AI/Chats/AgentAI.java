package org.Tweakio.AI.Chats;

import okhttp3.*;
import org.Tweakio.AI.HistoryManager.ChatMemory;
import org.Tweakio.UserSettings.user;
import org.Tweakio.WhatsappWeb.Extras;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Scanner;

public class AgentAI {
    private final OkHttpClient client;
    private final ChatMemory chatMemory;
    private final String apiURL;

    public AgentAI() {
        this.apiURL = user.AGENT_AI_KEY;

        this.client = new OkHttpClient();

        this.chatMemory = new ChatMemory();
    }

    public String sendToAgent(String userInput) {
        chatMemory.ensureFilePathExists("agent_ai");

        String historyTxt = chatMemory.getHistory("agent_ai");  // full raw history
        String prompt     =
                "The following is a conversation between a user and an AI assistant. " +
                        historyTxt +
                        "User: " + userInput + "\nAI:";

        JSONObject bodyJson = new JSONObject()
                .put("user_input", prompt);
        RequestBody body = RequestBody.create(
                bodyJson.toString(),
                MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(apiURL)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Extras.logwriter("Http error in agent ai : "+response.code() + " ---- Response Message : " + response.message());
                return "❌ HTTP " + response.code() + ": " + response.message();
            }

            String respBody = response.body() != null ? response.body().string() : "";
            JSONObject obj = new JSONObject(respBody);
            String botReply = obj.optString("response", null);
            if (botReply == null) {
                Extras.logwriter("error in response in agent ai : "+respBody);
                return "⚠️ Unexpected response: " + respBody;
            }

            chatMemory.writeToFile(
                    "User: " + userInput + "\nAI: " + botReply,
                    "agent_ai"
            );

            return botReply;
        } catch (IOException e) {
            return "❌ Error: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        AgentAI ai = new AgentAI();
        System.out.print("Ask GPT: ");
        String question = new Scanner(System.in).nextLine();
        String answer   = ai.sendToAgent(question);
        System.out.println("🤖 AgentAI: " + answer);
    }
}
