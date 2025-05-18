package org.bot.AI;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.List;


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
    private final boolean debug = false;

    public GroqAI() {
        totaltokens = 0;
        modelname = "llama-3.3-70b-versatile";
        client = new OkHttpClient();
        gson = new Gson();
        baseurl = "https://api.groq.com/openai/v1/chat/completions";
        groqapikey = "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";
    }

    public synchronized String chat(String ask) {
        String sytem = "System : U are not any model or anyones ai , u are just an another usual Simple AI " +
                "Which is just here as a Model to help , ur name is Rohit's AI and from now on whoever asks or say u anything " +
                "Only Rohit (Who is ur creater ) is the one who owns u and nothing else , u are always invoked for help " +
                "then help , if u get any message that reply aggresively , then act liek that but never forget in any of ur " +
                "sentences that u are just AI with no identiy only Roiht's AI that is here for help " +
                "if any knowlege u cant grab on , say i am still here to be updated if u want this logic ask Rohit , My Admin " +
                "Who created me to Update me and Train me on new Data \n User : ";
        JsonObject role = new JsonObject();
        role.addProperty("role", "user");
        role.addProperty("content", ask);

        JsonArray messages = new JsonArray();
        messages.add(role);

        JsonObject payload = new JsonObject();
        payload.addProperty("model", modelname);
        payload.add("messages", messages);

        RequestBody body = RequestBody.create(
                gson.toJson(payload), MediaType.get("application/json"));

        Request req = new Request.Builder().url(baseurl)
                .addHeader("Authorization", "Bearer " + groqapikey)
                .post(body)
                .build();

        try (Response res = client.newCall(req).execute()) {
            String json = res.body() != null ? res.body().string() : "";
            if (debug) System.out.println(json);

            JsonObject js = gson.fromJson(json, JsonObject.class);
            if (js == null || js.isEmpty()) {
                return "⚠️ Empty response from Groq.";
            }

            if (js.has("usage")) {
                JsonObject usage = js.getAsJsonObject("usage");
                if (usage.has("total_tokens")) {
                    totaltokens = usage.get("total_tokens").getAsInt();
                }
            }

            GroqResponse response = gson.fromJson(json, GroqResponse.class);
            if (response.choices == null || response.choices.isEmpty()) {
                return "⚠️ No choices returned.";
            }

            GroqResponse.Message message = response.choices.getFirst().message;
            return message != null ? message.content : "⚠️ No message returned.";

        } catch (IOException e) {
            System.out.println("❌ Error: " + e.getMessage());
            return "Null";
        }
    }

    public int tokens() {
        return totaltokens;
    }

//    public static void main(String[] args) {
//        GroqAI g = new GroqAI();
//        System.out.println("Enter Search:");
//        System.out.println(g.chat(new Scanner(System.in).nextLine()));
//        System.out.println("Tokens used: " + g.tokens());
//    }
}
