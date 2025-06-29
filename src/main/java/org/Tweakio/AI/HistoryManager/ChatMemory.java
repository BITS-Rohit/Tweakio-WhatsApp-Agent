package org.Tweakio.AI.HistoryManager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ChatMemory {

    private final String AgentAI_FILE = "src/main/java/org/Tweakio/AI/ChatLogs/_Agent_AI_chat_history.txt";
    private final String GroqAI_FILE = "src/main/java/org/Tweakio/AI/ChatLogs/_Groq_AI_chat_history.txt";
    private final String Gemini_FILE = "src/main/java/org/Tweakio/AI/ChatLogs/_Gemini_AI_chat_history.txt";

    public ChatMemory() {
    }

    // Append a new line to the correct AI's file
    public void writeToFile(String line, String aiName) {
        String filePath = get_AI_File(aiName);
        if (filePath == null) {
            System.out.println("❌ Invalid AI name: " + aiName);
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("❌ Error writing to file: " + e.getMessage());
        }
    }

    // Get file path based on AI name
    private String get_AI_File(String aiName) {
        if (aiName.equalsIgnoreCase("agent_ai")) return AgentAI_FILE;
        if (aiName.equalsIgnoreCase("groq_ai")) return GroqAI_FILE;
        if (aiName.equalsIgnoreCase("gemini_ai")) return Gemini_FILE;
        return null;
    }

    // Read the entire chat history of a specific AI
    public List<String> readFromFile(String aiName) {
        List<String> history = new ArrayList<>();
        String filePath = get_AI_File(aiName);
        if (filePath == null) {
            System.out.println("❌ Invalid AI name: " + aiName);
            return history;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                history.add(line);
            }
        } catch (IOException e) {
            System.out.println("❌ Error reading from file: " + e.getMessage());
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
            return "";
        }

        int totalChats = lines.size() / 2;
        int startChatIndex = (n == 0) ? 0 : Math.max(totalChats - n, 0);
        int startLineIndex = startChatIndex * 2; // Each chat = 2 lines
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

    public boolean clearChat(String ai_name) {
        boolean check = false;
        if (ai_name.equalsIgnoreCase("agent_ai")) {
            try {
                new PrintWriter(AgentAI_FILE).close();
                check = true;
            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage());
            }
        } else if (ai_name.equalsIgnoreCase("groq_ai")) {
            try {
                new PrintWriter(GroqAI_FILE).close();
                check = true;

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else if (ai_name.equalsIgnoreCase("gemini_ai")) {
            try {
                new PrintWriter(Gemini_FILE).close();
            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage());
            }
        }
        return check;
    }
}
