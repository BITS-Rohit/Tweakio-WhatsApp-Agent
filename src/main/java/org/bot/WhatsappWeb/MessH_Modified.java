package org.bot.WhatsappWeb;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.bot.AI.Gemini;
import org.bot.AI.GroqAI;
import org.bot.SearchSites.Google.Google;
import org.bot.SearchSites.Youtube.YoutubeAPI;
import org.bot.UserSettings.user;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class MessH_Modified {

    public static class RestartException extends RuntimeException { // Need for the restart
        public RestartException() {
            super("User requested restart");
        }
    }

    private static final String Chatlist = "div[aria-label='Chat list'][role='grid']";
    private static final String Chatitems = "div[role='listitem']";
    private static final String ChatName = "span[dir='auto'][title]";
    private static final int RefreshTime = 800;
    private static int MaxChat = 3;
    private static final String BotMess = "div[data-id] div.copyable-text[data-pre-plain-text] span.selectable-text";
    private static final Pattern YoutubeVidUrlPattern = Pattern.compile(
            "^(https?://)?(www\\.)?(youtube\\.com|youtu\\.be)/.+$",
            Pattern.CASE_INSENSITIVE
    );


    //Content ----------------------------------------------->>>>>>>>>>>>>>>
    private final Page page;
    private final WebLogin web;
    private final String AdminNumber;
    private final String BotNumber;
    private String Quantifier = "/a";
    private final boolean debugMode = true;
    private boolean isGlobalCheck = false;
    private boolean loginAnnounced = false;

    private final Map<String, Set<String>> processedIds;


    // Constructors --------------------------------------------->>>>>>>>>>>>>
    GroqAI groqAI = new GroqAI();
    YoutubeAPI youtubeAPI = new YoutubeAPI();
    Google google = new Google();
    Gemini gemini = new Gemini();

    public MessH_Modified(WebLogin webStart, Map<String, Set<String>> initialState) {
        this.web = webStart;
        this.BotNumber = new user().botNumber;
        this.page = web.getPage();
        this.AdminNumber = new AdminDetails().Admin_Number;
        String adminName = new AdminDetails().adminName;
        this.processedIds = initialState;
    }

    public Map<String, Set<String>> getProcessedIds() {
        return processedIds;
    }

    public boolean isConnected() {
        try {
            if (!loginAnnounced) {
                if (web.webLogin()) {
//                    sendMessageToChat(BotNumber, "✅ Logged in...");
                    loginAnnounced = true;
                }
            }
            Thread.sleep(1000);
            String webUrl = "https://web.whatsapp.com/";
            boolean isLoaded = page.url().equals(webUrl)
                    && page.locator(Chatlist).isVisible();
            if (!isLoaded && debugMode) {
                System.out.println("⚠️ WhatsApp not fully loaded.");
                loginAnnounced = false;
            }
            return isLoaded;
        } catch (RestartException re) {
            throw re;
        } catch (Exception e) {
            if (debugMode) System.out.println("Error💀💀 : " + e.getMessage());
            loginAnnounced = false;
            return false;
        }
    }

    public void Handler() {
        try {
            if (!isConnected()) {
                System.out.println("🔴 WhatsApp Web not ready");
                return;
            }
            Locator chatlist = page.locator(Chatlist);
            if (chatlist.count() == 0) {
                System.out.println("Null ChatList💀💀");
                return;
            }
            int i = 1;
            while (true) { // Will Replace this with at the time of Admin Utility Access Only
                // for this Always True while we will add a Utility Command to stop the program
                // with a special command
                System.out.println("\n 🌎New Chat Check==================>>>>>>>>>>>>>>> " + i);
                sleep(RefreshTime);
                EveryChatCheck(chatlist);
                i++;
            }
        } catch (RestartException e) {
            throw e;
        } catch (Exception e) {
            if ("User requested restart".equals(e.getMessage())) throw e;
            System.out.println("Error💀💀 : " + e.getMessage());
        }
    }

    public void EveryChatCheck(Locator chatlist) {
        try {
            Locator Allchats = chatlist.locator(Chatitems);
            int n = Math.min(MaxChat, Allchats.count());
            if (Allchats.count() == 0) {
                System.out.println("Null ChatList");
                return;
            }
            if (debugMode) {
                System.out.println("Total Chats Fetchable : " + Allchats.count());
                System.out.println("Current Fetching chats : " + n);
            }

            for (int i = 0; i < n; i++) {
                Locator chat = Allchats.nth(i);
                System.out.println("---- Chat no. :  " + (i + 1));
                checkChat(chat); // This will process all bot commands for this chat
            }

        } catch (RestartException re) {
            throw re;
        } catch (Exception e) {
            if (debugMode) System.out.println("Error 💀💀 : " + e.getMessage());
        }
    }

    private void checkChat(Locator chat) {
        try {
            if (chat == null) {
                System.out.println("Chat Element : Null 🔻🔻🔻");
                return;
            }
            chat.click();
            String name = getChatName(chat);
            System.out.println("==> Chat Name : " + name);

            // Ensure we have a set to track this chat’s IDs
            processedIds.putIfAbsent(name, new HashSet<>());
            Set<String> seen = processedIds.get(name);

            Locator botMessages = page.locator(BotMess);
            long count = botMessages.count();
            if (count == 0) {
                System.out.println("No Bot Chats 🔻🔻🔻");
                return;
            }
            System.out.println("==> Messages Loaded ✅ " + count);
            System.out.println("*━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            // Oldest → Newest
            for (int i = 0; i < count; i++) {
                Locator msg = botMessages.nth(i);
                String text = msg.textContent().trim();
                String[] parts = text.split("\\s+");
                if (parts.length < 2 || !parts[0].equalsIgnoreCase(Quantifier)) {
                    continue;  // not a /a <arg> command
                }

                // --- extract & normalize data-id into N_ID ---
                String rawId = msg.getAttribute("data-id");
                if (rawId == null || rawId.isEmpty()) {
                    ElementHandle wrap = msg.evaluateHandle("el => el.closest('[data-id]')").asElement();
                    rawId = wrap != null ? wrap.getAttribute("data-id") : null;
                }
                if (rawId == null || rawId.isEmpty()) {
                    System.out.println("Data-Id not found 🔻🔻🔻");
                    continue;
                }
                String N_ID = normalizeMessageId(rawId);

                // skip if we’ve handled this exact N_ID before && Handle the size of Set
                if (processedIds.get(name).size() > 300) processedIds.put(name, new HashSet<>());
                if (seen.contains(N_ID)) {
                    System.out.println("Returning Back , Already Processed IDS");
                    continue;
                }

                // --- authenticate // Later will add here for the Admin Access Commands Only
                String[] idParts = N_ID.split("#");
                String senderNumber = idParts[0].replaceAll("[^0-9]", "");
                boolean auth = isGlobalCheck;
                if (!auth) {
                    if (idParts.length == 3) {
                        auth = senderNumber.equals(BotNumber)
                                || senderNumber.equals(AdminNumber);
                    } else if (idParts.length == 2) {
                        boolean isOutgoing = (Boolean) msg.evaluate(
                                "el => el.closest('.message-out') !== null"
                        );
                        auth = isOutgoing || senderNumber.equals(AdminNumber);
                    }
                }
                if (!auth) {
                    System.out.println("❌ Unauthorized Command Ignored 🔐");
                    seen.add(N_ID);
                    continue;
                }
                // -----------------------------------------------

                long curTime = System.currentTimeMillis();
                System.out.printf("→ Processing “%s” from %s at %d%n", text, name, curTime);
                queryformat(chat.elementHandle(), msg.elementHandle(), text);
                seen.add(N_ID);
            }

        } catch (RestartException re) {
            throw re;
        } catch (Exception e) {
            System.out.println("Error 💀💀 : " + e.getMessage());
        }
    }


    private void queryformat(ElementHandle chat, ElementHandle target, String preview) {
        // query : /a gc , /a ai <hi how are u>
        String[] parts = preview.split("\\s+");
        String fname = parts[1];
        StringBuilder query = new StringBuilder();
        if (parts.length > 2) for (int i = 2; i < parts.length; i++) query.append(parts[i]).append(" ");
        Fhandler(chat, target, fname, query.toString().trim());
    }


    private void Fhandler(ElementHandle chat, ElementHandle target, String fname, String query) {
        // Empty String AI handalation
        String s = "hey user has give a empty query or wrong format ,now give me a sigma + Very agreesive reply for that directly. Also make that fency Looking a Agent Ai  is telling and  be in normal lowercase also quoete ur answer with * cuz as for whastapp we want ";

        String k = "*" + groqAI.chat("User has given wrong format in input now reply him for that very agreesivley ") + "*"; // Groq AI usage

        fname = fname.toLowerCase().trim();

        if (fname.equals("showgc")) GlobalCheck(chat, target);
        else if (fname.equalsIgnoreCase("s_restart")) SoftRestart(chat, target);
        else if (fname.equalsIgnoreCase("showmaxchat")) replyToChat(chat, target, ShowMaxChat());
        else if (fname.equals("showq")) ShowQuantifier(chat, target);
        else if (fname.equals("help") || fname.equalsIgnoreCase("showmenu")) ShowMenu(chat, target);
        else {
            if (query.isEmpty()) {
                String ll = groqAI.chat(s);
                replyToChat(chat, target, ll);
                return;
            }
            switch (fname) {
                case "setmaxchat":
                    query = query.trim();
                    try {
                        Integer.parseInt(query);
                        setMaxChat(Integer.parseInt(query));
                        replyToChat(chat, target, "😊Max Chat updated Successfully ✨✨✨");
                    } catch (Exception e) {
                        replyToChat(chat, target, "*" + groqAI.chat("User has given wrong input but we wanted number so reply him aggresively ") + "*");
                    }
                    break;
                case "ytd":
                    if (isYTvidurl(query)) {
                        String[] res = new String[1];
                        if (youtubeAPI.ytdownloadfromUrls(query, res)) {
                            replyToChat(chat, target, "Download SuccessFull✨✨✨");
                        } else replyToChat(chat, target, "Error in download...");
                    } else replyToChat(chat, target, k);
                    break;

                case "yts":
                    String ans = youtubeAPI.search(query);
                    replyToChat(chat, target, ans);
                    break;

                case "google":
                    String ans1 = google.google(query);
                    replyToChat(chat, target, ans1);
                    break;

                case "ai":
                    String ans2 = "*" + groqAI.chat(query) + "*";
                    replyToChat(chat, target, ans2);
                    break;

                case "personalai":
                    String ans3 = gemini.ask(query);
                    replyToChat(chat, target, ans3);
                    break;

                case "send":
                    String[] sendParts = query.split("\\s+", 2);
                    String ans4 = groqAI.chat(s);
                    if (sendParts.length < 2) {
                        replyToChat(chat, target, ans4);
                        return;
                    }
                    String phone = sendParts[0];
                    String message = sendParts[1];
                    if (sendMessageToChat(phone, message)) {
                        replyToChat(chat, target, "✅Done Sending.. ");
                    }
                    break;

                case "setgc":
                    GlobalMode(chat, target, query);
                    break;

                case "setq":
                    SetQuantifier(chat, target, query);
                    break;

                default:
                    replyToChat(chat, target, "⚠️ Unknown Agent Function. Accepted format: \n " + "<Quantifer> + <function name> + <query> ");
                    break;
            }
        }
        sleep(2000);
    }

    void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    void ShowMenu(ElementHandle chat, ElementHandle target) {
        String MenuMessage = """
                🌟 *Welcome to Your Smart Assistant!* 🌟
                _Your all-in-one intelligent companion!_

                🧾 *How to Use Commands:*
                ➤ *Format:* `Quantifier ➜ Command Name ➜ Input`
                ➤ _Example:_ `/a ai ➜ Who is Elon Musk?`

                ━━━━━━━━━━━━━━━━━━━━
                💬 *AI & Chat Tools*
                ┌─ 🧠 `ai ➜ your question`
                └─ 👤 `personalai* ➜ your private chat`

                🔍 *Search & Download*
                ┌─ 🔎 `google ➜ your search query`
                ├─ 🎥 `yts ➜ search YouTube`
                └─ 📥 `ytd ➜ YouTube link to download`

                🛠️ *Group & Bot Management*
                ┌─ 📝 `setgc ➜ Enable group-wide commands`
                ├─ 📋 `showgc* ➜ Check group command status`
                ├─ 📌 `setq ➜ Set your quantifier`
                ├─ 📄 `showq ➜ Show current quantifier`
                ├─ ⚙️ `setmaxchat ➜ Set max chat scans`
                ├─ 📊 `showmaxchat ➜ View current scan count`
                ├─ 🔄 `s_restart ➜ Soft Restart Bot `
                └─ 🧨 `hard_restart ➜ (Coming Soon) Full restart ⚠️`

                ✉️ *Messaging Utility*
                └─ 💌 `send ➜ <number> <your message>` (DM any number)

                ━━━━━━━━━━━━━━━━━━━━
                🔔 *Need Help?*
                ➤ Type `Quantifier ➜ help` or `showmenu` anytime!

                💡 *Pro Tips:*
                ✔️ Use short, clear inputs for faster replies.
                ✔️ After a reply, wait 5–10 seconds to avoid spam triggers.

                🛡️ *Safety Notice:*
                _If the bot takes time to respond, it's protecting your account from being flagged!_

                🤖 *Always here to assist you!* 🤖
                """;

        replyToChat(chat, target, MenuMessage);
    }

    public void HardRestart() { // Need to Check first
        try {
            BotReloader.hardRestart();
        } catch (IOException e) {
            System.err.println("Error 💀💀 : "+e.getMessage());
        }
    }

    public void SetQuantifier(ElementHandle chat, ElementHandle target, String quantifier) {
        this.Quantifier = quantifier;
        replyToChat(chat, target, "Quantifier Updated Successfully✨✨✨"); // replacable with ai message
    }

    public void ShowQuantifier(ElementHandle chat, ElementHandle target) {
        String s = "🌐_Current Quantifier :_"+this.Quantifier;
        replyToChat(chat, target, s);
    }

    public void replyToChat(ElementHandle chat, ElementHandle message, String reply) {
        try {
            chat.click();
            ElementHandle messageContainer = message.evaluateHandle("el => el.closest('div[data-id]')").asElement();
            if (messageContainer == null) {
                System.err.println("⚠️ Could not find message container");
                return;
            }

            BoundingBox box = messageContainer.boundingBox(); // Human like hover
            if (box == null) {
                System.err.println("⚠️ Could not get bounding box");
                return;
            }

            double startX = box.x + box.width * 0.5;
            double startY = box.y + box.height * 0.5;
            double hoverX = box.x + box.width * 0.85;
            double hoverY = box.y + box.height * 0.15;

            page.mouse().move(startX, startY);
            page.mouse().move(hoverX, hoverY, new Mouse.MoveOptions().setSteps(10));

            //  Convert data-id to use in selector
            String dataId = messageContainer.getAttribute("data-id");
            if (dataId == null) {
                System.err.println("⚠️ Could not read data-id from message container");
                return;
            }

            //  Locate chevron via Locator and wait
            Locator chevron = page.locator("div[data-id='" + dataId + "'] span[data-icon='ic-chevron-down-menu']");
            chevron.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5_000));
            page.waitForTimeout(200);
            chevron.click(new Locator.ClickOptions().setForce(true));

            //  Reply button
            Locator replyBtn = page.locator("li[role='button']:has-text('Reply')");
            replyBtn.waitFor(new Locator.WaitForOptions().setTimeout(2500).setState(WaitForSelectorState.VISIBLE));
            replyBtn.click();

            //  Input and send
            Locator inputBox = page.locator("div[aria-label='Type a message'][role='textbox']");
            inputBox.waitFor(new Locator.WaitForOptions().setTimeout(2500).setState(WaitForSelectorState.VISIBLE));
            inputBox.fill(reply);
            page.keyboard().press("Enter");

            System.out.println("✅ Replied: " + reply);
            sleep(500);

        } catch (Exception e) {
            if (debugMode) System.out.println("Error 💀💀 : " + e.getMessage());
            replyBack(chat, reply);
        }
    }

    synchronized void GlobalMode(ElementHandle chat, ElementHandle target, String text) {
        boolean check = false;
        String statusMessage = "";

        if (text.equalsIgnoreCase("on")) {
            isGlobalCheck = true;
            check = true;
            statusMessage = "🌍 Global mode activated";
        } else if (text.equalsIgnoreCase("off")) {
            isGlobalCheck = false;
            check = true;
            statusMessage = "🌍 Global mode deactivated";
        } else replyToChat(chat, target, "🗣️Agent : Only on/off accepted ");
        if (check) replyToChat(chat, target, statusMessage);
    }

    public boolean sendMessageToChat(String num, String message) {
        return sendMessageTochat(num, message);
    }

    public boolean sendMessageTochat(String num, String message) {
        try {
            // Wait for the "new chat" button to be visible
            page.locator("span[data-icon='new-chat-outline']").click();
            Locator search = page.locator("div[aria-label='Search name or number']");
            search.click();
            search.fill(num);

            // 2) Click first search result only
            sleep(500);
            Locator firstResult = page.locator("div[role='listitem'] span[title][dir='auto']").first();
            firstResult.waitFor(new Locator.WaitForOptions().setTimeout(2_000));
            firstResult.click();

            // 3) Wait for search overlay to disappear
            search.waitFor(new Locator.WaitForOptions()
                    .setTimeout(2_000)
                    .setState(WaitForSelectorState.DETACHED));

            // 4) Send the message
            Locator inputBox = page.locator("div[aria-label='Type a message'][role='textbox']");
            inputBox.waitFor(new Locator.WaitForOptions().setTimeout(2_000));
            inputBox.fill(message);
            inputBox.press("Enter");
            System.out.printf("📤 Replied to %s: \"%s\"%n", num, message);
            sleep(2000);
            return true;
        } catch (Exception e) {
            page.reload();
            if (debugMode) System.out.println("❌ Error while sending message: " + e.getMessage());
            return false;
        }
    }

    synchronized boolean isYTvidurl(String link) {
        if (link == null || link.isEmpty()) return false;
        return YoutubeVidUrlPattern.matcher(link).matches();
    }

    synchronized void GlobalCheck(ElementHandle chat, ElementHandle target) {
        String s;
        if (isGlobalCheck) s = "on";
        else s = "off";
        replyToChat(chat, target, " 🌐 Global Mode : " + s);
    }

    /////////--------------- Reply Back
    public synchronized void replyBack(ElementHandle chat, String replyMessage) {
        try {
            if (replyMessage == null || replyMessage.isEmpty()) {
                replyMessage = "Answer Not Found for this";
            }
            chat.click();
            sleep(200);

            Locator inputBox = page.locator("div[contenteditable='true'][data-tab='10']");
            inputBox.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(5_000)
            );

            inputBox.click();
            inputBox.fill("");            // clear any existing text
            inputBox.fill(replyMessage);  // type your reply

            page.keyboard().press("Enter");
            System.out.printf("📤 Replied: \"%s\"%n", replyMessage);
        } catch (Exception e) {
            if (debugMode) System.err.println("❌ replyBack() failed: " + e.getMessage());
        }
    }


    private String getChatName(Locator chat) {
        try {
            int count = chat.locator(ChatName).count();
            return chat.locator(ChatName).nth(count - 1).getAttribute("title");
        } catch (Exception e) {
            return null;
        }
    }

    private String getChatName(ElementHandle chat) {
        try {
            List<ElementHandle> nameElements = chat.querySelectorAll(ChatName);
            if (nameElements.isEmpty()) return "Unknown Chat";
            return nameElements.getLast().getAttribute("title");
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private String normalizeMessageId(String dataId) {
        if (dataId == null) return null;
        try {
            System.out.println("📊 Raw data-id: " + dataId);
            String workingId = dataId.replaceFirst("^(true|false)_", ""); // Remove status prefix
            String[] parts = workingId.split("_");
            System.out.println("📊 Parts: " + Arrays.toString(parts));

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
            return null;
        }
    }

    private void setMaxChat(int n) {
        MaxChat = n;
    }

    private String ShowMaxChat() {
        return String.valueOf(MaxChat);
    }

    public void seedCache() {
        Locator chatlist = page.locator(Chatlist);
        Locator allChats = chatlist.locator(Chatitems);
        int n = Math.min(MaxChat, allChats.count());
        for (int i = 0; i < n; i++) {
            Locator chat = allChats.nth(i);
            String name = getChatName(chat);
            processedIds.putIfAbsent(name, new HashSet<>());
            chat.click();
            Locator botMessages = page.locator(BotMess);
            for (int j = 0; j < botMessages.count(); j++) {
                Locator msg = botMessages.nth(j);
                String text = msg.textContent().trim();
                if (text.startsWith(Quantifier + " ")) {
                    String rawId = msg.getAttribute("data-id");
                    String nId = normalizeMessageId(rawId);
                    processedIds.get(name).add(nId);
                }
            }
        }
    }

    public void SoftRestart(ElementHandle chat, ElementHandle target) {
        replyToChat(chat, target, "🔄 Restart Initiated 👺");
        String name = getChatName(chat);
        processedIds.putIfAbsent(name, new HashSet<>());
        String rawId = target.getAttribute("data-id");
        String N_ID = normalizeMessageId(rawId);
        processedIds.get(name).add(N_ID);
        System.out.println(N_ID);
        throw new RestartException();
    }
}

// ---------------> Depreciated Content

//    private boolean isMessageNewer(String lastProcessedTime, String currentMessageTime) {
//        try {
//            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
//            Date lastTime = sdf.parse(lastProcessedTime);
//            Date currentTime = sdf.parse(currentMessageTime);
//            return !currentTime.before(lastTime);
//
//        } catch (Exception e) {
//            System.out.println("⚠️ Time comparison error: " + e.getMessage());
//            return true; // Assume it's new if there's an error
//        }
//    }


//    private void CheckChat(Locator chat) {
//        try {
//            if (chat == null) {
//                System.out.println("Chat Element : Null 🔻🔻🔻");
//                return;
//            }
//            chat.click(); // Open Chat
//            System.out.println(">>>> Chat Opened✅");
//            String name = getChatName(chat);
//            System.out.println("==> Chat Name : " + name);
//            Locator botMessages = page.locator(BotMess);
//            if (botMessages.count() == 0) {
//                System.out.println("No Bot Chats 🔻🔻🔻");
//                return;
//            }
//            else System.out.println("==> Messages Loaded ✅ " + botMessages.count());
//            System.out.println("*━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
//            Locator target;
//            String cn = null, ct = null, cid = null;
//            for (int i = botMessages.count() - 1; i >= 0; i--) {
//                Locator msg = botMessages.nth(i);
//                String[] parts = msg.textContent().trim().split("\\s+");
//                if (parts.length > 0 && parts[0].equalsIgnoreCase(Quantifier)) {
//
////                    String curTime = getMessageTime(msg);
//                    String curTime = String.valueOf(System.currentTimeMillis());
//                    if (debugMode) {
//                        System.out.println("------");
//                        System.out.println("├─ Message  : " + msg.textContent());
//                        System.out.println("├─ Time     : " + curTime);
//                    }
//
//                    if (cn == null && ct == null) {
//                        cn = name;
//                        ct = curTime;
//                    }
//
//                    // ------------- Map Cache Handle
//                    if (cache.containsKey(name)) {
//                        // existing cache logic unchanged...
//                        System.out.println(" ++ Inside Cache Block ++ ");
//                        String value = cache.get(name);
//                        String oldTime = value.split("--")[1];
//
//                        if (Long.parseLong(oldTime)<Long.parseLong(curTime)) {
//                            target = msg;
//                            String id = target.getAttribute("data-id");
//                            if (id == null || id.isEmpty()) {
//                                ElementHandle wrap = target.evaluateHandle(
//                                        "el => el.closest('[data-id]')"
//                                ).asElement();
//                                id = wrap != null ? wrap.getAttribute("data-id") : null;
//                            }
//                            if (id == null || id.isEmpty()) {
//                                System.out.println("Data-Id not found 🔻🔻🔻");
//                                return;
//                            } else if (debugMode) {
//                                System.out.println("++ Data Id Found ++");
//                            }
//
//                            String N_ID = normalizeMessageId(id);
//                            String U_ID = N_ID + "--" + curTime;
//                            if (cid == null) cid = N_ID + "--" + ct;
//                            if (debugMode) System.out.println("UniqueID : " + U_ID);
//
//                            if (cache.get(name).split("--")[0].equalsIgnoreCase(N_ID)) {
//                                System.out.println("++ Returning Back ++");
//                                return;
//                            }
//                            cache.put(cn, cid); // Update cache to previous
//
//                            // Authentication
//                            String[] idParts = N_ID.split("#");
//                            String senderNumber = idParts[0].replaceAll("[^0-9]", "");
//                            boolean auth = isGlobalCheck;
//                            if (!auth) {
//                                if (idParts.length == 3) {
//                                    auth = senderNumber.equals(BotNumber)
//                                            || senderNumber.equals(AdminNumber);
//                                } else if (idParts.length == 2) {
//                                    boolean isOutgoing = (Boolean) target.evaluate(
//                                            "el => el.closest('.message-out') !== null"
//                                    );
//                                    auth = isOutgoing || senderNumber.equals(AdminNumber);
//                                }
//                            }
//                            if (!auth) {
//                                System.out.println("❌ Unauthorized Command Ignored 🔐");
//                                return;
//                            }
//
//                            System.out.println("✅ Authenticated command from: " + senderNumber);
//                            String mess = msg.textContent().trim();
//                            ElementHandle c = chat.elementHandle();
//                            ElementHandle t = target.elementHandle();
//                            queryformat(c, t, mess);
//                        }
//
//                    } else {
//                        if (debugMode) System.out.println(" ++ No cache Block Inside ++");
//
//                        //------------------
//                        // First time we see a command in this chat, seed and process it
//                        String id = msg.getAttribute("data-id");
//                        if (id == null || id.isEmpty()) {
//                            ElementHandle wrap = msg.evaluateHandle(
//                                    "el => el.closest('[data-id]')"
//                            ).asElement();
//                            id = wrap != null ? wrap.getAttribute("data-id") : null;
//                        }
//                        if (id == null || id.isEmpty()) {
//                            System.out.println("Data-Id not found 🔻🔻🔻");
//                            return;
//                        }
//
//                        String N_ID = normalizeMessageId(id);
//                        String initialUID = N_ID + "--" + curTime;
//                        cache.put(name, initialUID);
//
//                        if (debugMode) System.out.println("🔄 Seeded cache for " + name + " -> " + initialUID);
//
//                        // Immediately process this first command
//                        String[] part = N_ID.split("#");
//                        String senderNumber = part[0].replaceAll("[^0-9]", "");
//                        boolean auth = isGlobalCheck;
//                        if (!auth) {
//                            if (part.length == 3) {
//                                auth = senderNumber.equals(BotNumber)
//                                        || senderNumber.equals(AdminNumber);
//                            } else if (part.length == 2) {
//                                boolean isOutgoing = (Boolean) msg.evaluate(
//                                        "el => el.closest('.message-out') !== null"
//                                );
//                                auth = isOutgoing || senderNumber.equals(AdminNumber);
//                            }
//                        }
//                        if (!auth) {
//                            System.out.println("❌ Unauthorized Command Ignored 🔐");
//                            return;
//                        }
//
//                        System.out.println("✅ Authenticated command from: " + senderNumber);
//                        String mess = msg.textContent().trim();
//                        ElementHandle t = msg.elementHandle();
//                        queryformat(chat.elementHandle(), t, mess);
//                        return;
//                        //------------------
//                    }
//                }
//            }
//
//        } catch (Exception e) {
//            System.out.println("Error 💀💀 : " + e.getMessage());
//        }
//    }

//    private String getMessageTime(Locator message) {
//        try {
//            // Get the outer copyable-text container for this message
//            ElementHandle copyable = message.elementHandle()
//                    .evaluateHandle("el => el.closest('div.copyable-text')").asElement();
//
//            if (copyable == null) {
//                System.out.println("⚠️ Could not find copyable-text container");
//                return null;
//            }
//
//            // Within that container, grab the second span under the aria-hidden wrapper
//            JSHandle timeHandle = copyable.evaluateHandle(
//                    "el => { " +
//                            "  const wrapper = el.querySelector('span[aria-hidden]');" +
//                            "  return wrapper && wrapper.querySelectorAll('span')[1] || null;" +
//                            "}"
//            );
//
//            ElementHandle timeEl = timeHandle.asElement();
//            if (timeEl == null) {
//                System.out.println("⚠️ Time element not found inside copyable-text");
//                return null;
//            }
//
//            String timeText = timeEl.textContent().trim();
//            if (debugMode) System.out.println("🕒 Message Time: " + timeText);
//            return timeText;
//
//        } catch (Exception e) {
//            System.out.println("⚠️ Error getting time: " + e.getMessage());
//            return null;
//        }
//    }
