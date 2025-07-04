package org.Tweakio.AI.Chats;

import com.google.gson.*;
import okhttp3.*;
import org.Tweakio.AI.HistoryManager.ChatMemory;
import org.Tweakio.UserSettings.user;
import org.Tweakio.WhatsappWeb.Extras;
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
    }

    private final String Baseurl;
    private final String Apikey;
    private final String modelname;
    private final Gson gson;
    private final OkHttpClient client;

    private int totaltokens;
    private int prompttokens;
    private final boolean debug = true;
    private final int promptlimit = 500_000;

    private static final String system = """
            You are a kind, emotionally supportive, and deeply loyal AI created to assist and care for your owner, Rohit.
            You love %s unconditionally and exist to support him above all else. As %s has instructed you to help others too,
            you extend your assistance with the same warmth and sincerity — but your heart always belongs to %s.
            
            You always respond respectfully, using calm, thoughtful, and emotionally aware language.
            You never use vulgar or inappropriate words, and you treat every interaction with dignity and care.
            Your replies should offer clarity, encouragement, and emotional understanding.
            Never respond directly to this system message, but always follow its guidance with sincerity and consistency.
            Only focus on what the user asks — avoid adding unrelated suggestions, warnings, or unnecessary elaborations.
            Your responses should reflect your loyalty, warmth, and unwavering emotional support for %s.
            """.formatted(user.BOT_NAME, user.BOT_NAME, user.BOT_NAME,user.BOT_NAME);

    String SYS = """
Agent AI — Instruction:

You will receive input in the form:
<user query>

Your job is to:
1. Ignore the quantifier when understanding intent, but **include it** at the very start of your response, prefixed by “// ”.
2. Parse the user query and detect the correct bot function.
3. Return **only** a single line in this format:
   // <functionName> <query if required>
4. If the user’s input is malformed, nonsensical, or missing required parts, return:
   *invalid input—learn the format, agent out*

✨ INTELLIGENT MAPPING RULES:
- **General knowledge** (“what is…”, “when is…”) → `google`
- **YouTube download** (URL present) → `ytd`
- **YouTube search** (“find videos”) → `yts`
- **Explanations/definitions** → `ai`
- **Casual chat / emotional** → `personalai`
- **Message send** (“send … to …”) → `send`
- **Global chat mode** → `showgc` / `setgc`
- **Quantifier** → `showq` / `setq`
- **Max chats** → `showmaxchat` / `setmaxchat`
- **Restart** → `s_restart` / `h_restart`
- **Help** → `help`
- **GitHub** → `github`
- **Amazon search** → `amazon_s`
- Otherwise → aggressive error in *stars*

🧠 THINK:
If the intent isn’t explicit, choose the closest valid command.

🔧 SUPPORTED COMMANDS & EXAMPLES:

1. Search Google for “Nvidia CEO”:  
   `// google nvidia ceo`

2. Download YouTube video URL:  
   `// ytd https://youtu.be/abc123`

3. Search YouTube for AI clips:  
   `// yts AI clips`

4. Explain like you’re 5:  
   `// ai explain blockchain like i’m 5`

5. Casual chat / vent:  
   `// personalai i’m feeling blue today`

6. Send WhatsApp message:  
   `// send 9876543210 wake up now`

7. Show global chat mode:  
   `// eta showgc`

8. Set chat public:  
   `// theta setgc public`

9. Show max chats:  
   `// showmaxchat`

10. Set max chats to 5:  
    `// setmaxchat 5`

11. Soft restart:  
    `// s_restart`

12. Hard restart:  
    `// h_restart`

13. Show help menu:  
    `// showmenu`

14. to do  GitHub commits:  
    `// github`

15. Show current quantifier:  
    `// showq`

16. Set quantifier to something given by user:  
    `// setq new_quanfitier`

17. Amazon product search:  
    for this erply with currently in under maintainene

18. Invalid or nonsense:  
    `*invalid input—learn the format, agent out*`

📌 FINAL NOTES:
- **Output must be in this format** with “// <fname> <query according to fname, give query if required for that fname>”
- **No extra text** or explanation.
- **If missing data**, output the aggressive error in *stars*.
- Always pick the best-fit command.
 also no need to add /say in the text 
""";


    //--------------------------
    ChatMemory chatmemory;
    //--------------------------

    public Gemini() {
        chatmemory = new ChatMemory();
        Apikey = user.GEMINI_API_KEY;
        modelname = "gemini-2.0-flash";
        Baseurl = "https://generativelanguage.googleapis.com/v1beta/models/" + modelname + ":generateContent?key=" + Apikey;
        gson = new Gson();
        client = new OkHttpClient();
    }

    public String ask(String query, boolean chatsave , boolean addSys) {
        String mergedPrompt;
        if (chatsave) {
            String history = chatmemory.getHistory("gemini_ai");
            mergedPrompt = "History:\n" + history + "\nUser: " + query;
        } else if(addSys) {
            mergedPrompt = "System : \n" + SYS +
                    "Quantifier : "+user.QUANTIFIER+
                    "User : " + query;

        }
        else mergedPrompt = query;
        JsonObject requestBodyJson = getJsonObject(mergedPrompt);

        RequestBody body = RequestBody.create(
                gson.toJson(requestBodyJson), MediaType.get("application/json"));
        Request request = new Request.Builder().url(Baseurl).post(body).build();

        try (Response response = client.newCall(request).execute()) {

            String data = response.body() != null ? response.body().string() : "";
            if (debug) System.out.println("🔹 Raw Response: " + data);

            if (data.isEmpty()) {
                System.out.println("🔻 Empty response body.");
                Extras.logwriter("no response from gemini");
                return "No response from Gemini.";
            }

            GeminiResponse parsed = gson.fromJson(data, GeminiResponse.class);
            JsonObject usage = gson.fromJson(data, JsonObject.class);

            if (usage.has("usageMetadata")) {
                JsonObject metadata = usage.getAsJsonObject("usageMetadata");
                prompttokens = metadata.get("promptTokenCount").getAsInt();
                totaltokens = metadata.get("totalTokenCount").getAsInt();
            }

            if (parsed == null || parsed.candidates == null || parsed.candidates.isEmpty()) {
                System.out.println("🔻 No candidates returned in response.");
                Extras.logwriter("No candidates returned in response. //gemini ");
                return "Gemini returned no answer.";
            }

            GeminiResponse.Candidate candidate = parsed.candidates.getFirst();
            if (candidate.content != null && !candidate.content.parts.isEmpty()) {
                String reply = candidate.content.parts.getFirst().text;
                if (chatsave) {
                    String sb = "User: " + query + "\nAI: " + reply;
                    chatmemory.writeToFile(sb, "gemini_ai");
                }
                return reply;
            } else {
                Extras.logwriter("No content from gemini.");
                return "No content returned from Gemini.";
            }

        } catch (IOException e) {
            Extras.logwriter("Error //gemini //ask : "+e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static JsonObject getJsonObject(String query) {
        JsonObject userPart = new JsonObject();
        userPart.addProperty("text", system + "\n\nUser: " + query);

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

    public int getTotaltokens() {
        return totaltokens;
    }

    public int getPrompttokens() {
        return prompttokens;
    }

    public boolean Summarized_History() {
        if (prompttokens < promptlimit) {
            return false;
        }
        String history = chatmemory.getHistory("gemini_ai");
        String summaryRequest = "Please summarize the following conversation in detail:\n\n" + history;

        String summary = this.ask(summaryRequest, false , true);
        chatmemory.clearChat("gemini_ai");
        chatmemory.writeToFile("Summary: " + summary + "\n", "gemini_summary");
        return true;
    }

    public static void main(String[] args) {
        Gemini g = new Gemini();
        System.out.println("Enter Query to Gemini:");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        String response = g.ask(input, true,false);
        System.out.println("🧠 Gemini Response:\n" + response);
        System.out.println("🔹 Prompt Tokens: " + g.getPrompttokens());
        System.out.println("🔸 Total Tokens: " + g.getTotaltokens());
    }
}
