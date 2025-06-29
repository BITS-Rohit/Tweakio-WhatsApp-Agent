package org.Tweakio.AI.Chats;

import com.google.gson.Gson;
import okhttp3.*;
import org.Tweakio.AI.HistoryManager.ChatMemory;
import org.Tweakio.UserSettings.user;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Scanner;

public class AgentAI {
    user u;
    OkHttpClient client;
    String apiURL;
    Gson gson;
    ChatMemory chatMemory;
    int totaltokens;
    String profile ;

    public AgentAI(String  profile) {
        this.profile = profile;
        apiURL = user.AgentAIKey; // This is the full POST URL
        client = new OkHttpClient();
        gson = new Gson();
        chatMemory = new ChatMemory();
    }

    public String sendToAgent(String userInput) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String history = chatMemory.getFormattedHistory("agent_ai",0);
        history = history.replace("\"", "\\\"");
        userInput = userInput.replace("\"", "\\\"");
        history= history+ userInput;
//        System.out.println("prompt : ---- \n" + history);
//        System.out.println("--------");
        String jsonBody = String.format("{\"user_input\": \"%s\"}", history);

        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(apiURL)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) return "❌ Failed: " + response;

            String json = response.body() != null ? response.body().string() : "";
            JSONObject obj = new JSONObject(json);
            System.out.println(json);

            if (obj.has("response")) {
                String botReply = obj.getString("response");

                String sb = "User : " + userInput +
                        "\n" +
                        "AI : " + botReply;
                History(sb);

                return botReply;
            } else {
                return "⚠️ Unexpected response: " + json;
            }

        } catch (IOException e) {
            return "❌ Error: " + e.getMessage();
        }
    }

    public void History(String chat){
        chatMemory.writeToFile(chat, "agent_ai");
    }

//    public static void main(String[] args) {
//        AgentAI ai = new AgentAI();
//        System.out.println("Ask GPT : ");
//        String reply = ai.sendToAgent(new Scanner(System.in).nextLine());
//        System.out.println("🤖 AgentAI: " + reply);
//    }
}
