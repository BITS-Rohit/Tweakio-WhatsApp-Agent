package org.Tweakio.WhatsappWeb;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.Tweakio.UserSettings.user;
import org.Tweakio.WhatsappWeb.BrowserManager.Browser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.Tweakio.WhatsappWeb.Brain.debugMode;


public class Extras {
    //---------------------------
    static final Pattern YoutubeVidUrlPattern = Pattern.compile(
            "^(https?://)?(www\\.)?(youtube\\.com|youtu\\.be)/.+$",
            Pattern.CASE_INSENSITIVE
    );
    private static final String LOG_FILE = "sys_logs.txt";
    private static final DateTimeFormatter HMS_FORMATTER = DateTimeFormatter.ofPattern("HH : mm : ss");
    private static final String ChatName = "span[dir='auto'][title]";


    //---------------------------

    public Extras() {
    }


    //----------------------------


    public void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean sendMessageToChat(Page page, String num, String message) {
        return sendMessageTochat(page, num, message);
    }

    private boolean sendMessageTochat(Page page, String num, String message) {
        try {
            // Wait for the "new chat" button to be visible
            page.locator("span[data-icon='new-chat-outline']").click();
            Locator search = page.locator("div[aria-label='Search name or number']");
            Browser.smartFill(page,search,num);

            // 2) Click first search result only
            sleep(500); // Search Load Time
            Locator firstResult = page.locator("div[role='listitem'] span[title][dir='auto']").first();
            firstResult.waitFor(new Locator.WaitForOptions().setTimeout(2_000));
            firstResult.hover();
            firstResult.click();

            // 3) Wait for search overlay to disappear
            search.waitFor(new Locator.WaitForOptions()
                    .setTimeout(5_000)
                    .setState(WaitForSelectorState.DETACHED));

            // 4) Send the message
            Locator inputBox = page.locator("div[aria-label='Type a message'][role='textbox']");
            inputBox.waitFor(new Locator.WaitForOptions().setTimeout(2_000));
            inputBox.hover();
            inputBox.click();
            inputBox.fill("");
            inputBox.fill(message);
            inputBox.press("Enter");
            System.out.println("📤 Replied to " + num + " : ");
            System.out.println("-----------");
            System.out.println(message);
            System.out.println("-----------");
            return true;
        } catch (Exception e) {
            page.reload();
            if (debugMode) System.out.println("❌ Error while sending message: " + e.getMessage());
            Extras.logwriter("Error sending message // Extras // sendmessagetochat: " + e.getMessage());
            return false;
        }
    }

    public boolean isYTvidurl(String link) {
        if (link == null || link.isEmpty()) return false;
        return YoutubeVidUrlPattern.matcher(link).matches();
    }

    public static String Time(long epochMillis) {
        LocalTime time = Instant
                .ofEpochMilli(epochMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalTime();
        return time.format(HMS_FORMATTER);
    }

    protected String getChatName(Locator chat) {
        try {
            Locator names = chat.locator(ChatName);
            int count = names.count();
            if (count > 1) {
                // There’s a second <span> — return its visible text
                String youTag = names.nth(1).innerText().trim();
                if (!youTag.isEmpty()) {
                    return youTag;
                }
            }
            // Otherwise return the first span’s title
            return names.first().getAttribute("title");
        } catch (Exception e) {
            Extras.logwriter("Error getting chat names // Extras //getChatname(Locator): " + e.getMessage());
            return null;
        }
    }


    protected String getChatName(ElementHandle chat) {
        try {
            List<ElementHandle> nameElements = chat.querySelectorAll(ChatName);
            if (nameElements.isEmpty()) {
                return "Unknown Chat";
            }
            // If there’s a second span, it’s the “(You)” tag
            if (nameElements.size() > 1) {
                String youTag = nameElements.get(1).innerText().trim();
                if (!youTag.isEmpty()) {
                    return youTag;
                }
            }
            // Otherwise return the first span’s title attribute
            return nameElements.get(0).getAttribute("title");
        } catch (Exception e) {
            Extras.logwriter("Error in getchatnname(elementHandle) // Extras: " + e.getMessage());
            return "Unknown";
        }
    }


    String normalizeMessageId(String dataId) {
        if (dataId == null) return null;
        try {
            String workingId = dataId.replaceFirst("^(true|false)_", ""); // Remove status prefix
            String[] parts = workingId.split("_");

            if (debugMode) {
                System.out.println("📊 Raw data-id: " + dataId);
                System.out.println("📊 Parts: " + Arrays.toString(parts));
            }

            if (parts.length >= 3) {
                // Group message format: [status]_[groupID]_[messageID]_[sender]
                return parts[2].replace("@c.us", "") + "#" + parts[1] + "#" + parts[0].replace("@g.us", "");
            } else if (parts.length == 2) {
                // Personal message format: [status]_[sender]_[messageID]
                return parts[0].replace("@c.us", "") + "#" + parts[1];
            }
            return dataId + "#unknown";
        } catch (Exception e) {
            System.out.println("⚠️ ID normalization failed for: " + dataId);
            System.out.println("⚠️ Error: " + e.getMessage());
            Extras.logwriter("error normalizing data-id: " + dataId);
            Extras.logwriter("Error // extras // normalziemesageid : " + e.getMessage());
            return null;
        }
    }

    /**
     * Returns the difference in whole seconds between the given start time (in milliseconds)
     * and the current system time.
     *
     * @param startMillis the starting timestamp (e.g. from System.currentTimeMillis())
     * @return the elapsed time in seconds (rounded down)
     */
    public static String bootime(long startMillis) {
        long nowMillis = System.currentTimeMillis();
        long diffMillis = nowMillis - startMillis;
        return String.valueOf(diffMillis / 1000);
    }

    public static String realtime(){
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a"));
    }

    public static void logwriter(String logInput) {
        // Folder path: syslogs/
        String folderPath = "syslogs";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Log file: syslogs/syslogs_PROFILE.txt
        String filePath = folderPath + File.separator + "syslogs_" + user.PROFILE + ".txt";
        String logLine = realtime() + " ==> " + logInput;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(logLine);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("❌ Error writing log: " + e.getMessage());
        }
    }
}
