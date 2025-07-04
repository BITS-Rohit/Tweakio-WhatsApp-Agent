package org.Tweakio.AI.HistoryManager;

import org.Tweakio.UserSettings.user;
import org.Tweakio.WhatsappWeb.Extras;

import java.io.*;
import java.util.*;

public class ChatMemory {

    String profile = user.PROFILE;

    private final String AgentAI_FILE = "src/main/java/org/Tweakio/AI/ChatLogs/%s/_Agent_AI_chat_history.txt".formatted(profile);
    private final String GroqAI_FILE = "src/main/java/org/Tweakio/AI/ChatLogs/%s/_Groq_AI_chat_history.txt".formatted(profile);
    private final String Gemini_FILE = "src/main/java/org/Tweakio/AI/ChatLogs/%s/_Gemini_AI_chat_history.txt".formatted(profile);
    private final String Therapist_FILE = "src/main/java/org/Tweakio/AI/ChatLogs/%s/TypeAI/Therapist.txt".formatted(profile);
    private final String Love_FILE = "src/main/java/org/Tweakio/AI/ChatLogs/%s/TypeAI/Love.txt".formatted(profile);
    private final String Shraddha_FILE = "src/main/java/org/Tweakio/AI/ChatLogs/%s/TypeAI/Shraddha.txt".formatted(profile);

    private final Map<String, String> paths = new HashMap<>();

    public ChatMemory() {
        addCustomAIs();
    }

    private void addCustomAIs() {
        paths.put("therapist", Therapist_FILE);
        paths.put("love", Love_FILE);
        paths.put("agent_ai", AgentAI_FILE);
        paths.put("groq_ai", GroqAI_FILE);
        paths.put("gemini_ai", Gemini_FILE);
        paths.put("shraddha", Shraddha_FILE);
    }

    public void writeToFile(String line, String aiName) {
        String filePath = get_AI_File(aiName);
        if (filePath == null) {
            System.out.println("❌ Invalid AI name: " + aiName);
            return;
        }
        ensureFilePathExists(filePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("❌ Error writing to file: " + e.getMessage());
            Extras.logwriter("Error writing to file //chatmemory : " + e.getMessage());
        }
    }

    private String get_AI_File(String aiName) {
        return paths.get(aiName.toLowerCase());
    }

    public void ensureFilePathExists(String filePath) {
        try {
            File file = new File(filePath);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs(); // create parent directories
            }
            if (!file.exists()) {
                file.createNewFile(); // create empty file
            }
        } catch (IOException e) {
            System.out.println("❌ Could not create file or folder: " + e.getMessage());
            Extras.logwriter("Could not create file or folder //Chatmemory :  " + e.getMessage());
        }
    }

    public List<String> readFromFile(String aiName) {
        List<String> history = new ArrayList<>();
        String filePath = get_AI_File(aiName);
        if (filePath == null) {
            System.out.println("❌ Invalid AI name: " + aiName);
            Extras.logwriter("Invalid AI name //chtmemory : " + aiName);
            return history;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                history.add(line);
            }
        } catch (IOException e) {
            System.out.println("❌ Error reading from file: " + e.getMessage());
            Extras.logwriter("Error reading from file //chatmemory : " + e.getMessage());
        }
        return history;
    }

    public String getHistory(String aiName) {
        List<String> history = readFromFile(aiName);
        if (history.isEmpty()) {
            return "📝 No history found for: " + aiName;
        }

        StringBuilder sb = new StringBuilder();
        for (String line : history) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    public String getRecentHistory(String aiName, int limit) {
        List<String> history = readFromFile(aiName);
        int size = history.size();
        List<String> recent = history.subList(Math.max(0, size - limit), size);
        return String.join("\n", recent);
    }

    public String getFormattedHistory(String aiName, int n) {
        String filePath = get_AI_File(aiName);
        if (filePath == null) {
            System.out.println("❌ Invalid AI name: " + aiName);
            Extras.logwriter("Invalid AI name //chtmemory  // getformattedhistory: " + aiName);
            return "";
        }

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.out.println("❌ Error reading from file: " + e.getMessage());
            Extras.logwriter("Error reading from file //chatmemory  //getformatedhistory : " + e.getMessage());
            return "";
        }

        int totalChats = lines.size() / 2;
        int startChatIndex = (n == 0) ? 0 : Math.max(totalChats - n, 0);
        int startLineIndex = startChatIndex * 2;
        StringBuilder formatted = new StringBuilder();

        for (int i = startLineIndex; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("User :")) {
                formatted.append("[User] ").append(line.substring(7).trim());
            } else if (line.startsWith("AI :")) {
                formatted.append("[AI] ").append(line.substring(5).trim());
            }
        }

        return formatted.toString();
    }

    public boolean clearChat(String aiName) {
        String filePath = get_AI_File(aiName);
        if (filePath == null) {
            Extras.logwriter("clearChat: invalid AI name  //clearcht //chatmemory '" + aiName + "'");
            return false;
        }

        try (PrintWriter pw = new PrintWriter(filePath)) {
            Extras.logwriter("Cleared chat history for: " + aiName);
            return true;
        } catch (IOException e) {
            Extras.logwriter("Error clearing chat //clearchat //chatmemory  '" + aiName + "': " + e.getMessage());
            return false;
        }
    }
}
