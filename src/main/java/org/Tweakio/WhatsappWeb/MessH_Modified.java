package org.Tweakio.WhatsappWeb;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.Tweakio.AI.Gemini;
import org.Tweakio.AI.GroqAI;
import org.Tweakio.GithubAutomation.GithubAutoCommitScript;
import org.Tweakio.SearchSites.Google.Google;
import org.Tweakio.SearchSites.Youtube.YoutubeAPI;
import org.Tweakio.UserSettings.user;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

public class MessH_Modified {

    public static class RestartException extends RuntimeException { // Need for the restart
        public RestartException() {
            super("User requested restart");
        }
    }

    //--------------- Selectors Area ----------------------------
    private static final String Chatlist = "div[aria-label='Chat list'][role='grid']";
    private static final String Chatitems = "div[role='listitem']";
    private static final String ChatName = "span[dir='auto'][title]";
    private static final String BotMess = "div[data-id] div.copyable-text[data-pre-plain-text] span.selectable-text";
    private static final Pattern YoutubeVidUrlPattern = Pattern.compile(
            "^(https?://)?(www\\.)?(youtube\\.com|youtu\\.be)/.+$",
            Pattern.CASE_INSENSITIVE
    );
    //-----------------------------------
    private static final int RefreshTime = 400;
    private static int MaxChat = 5;
    private static final DateTimeFormatter HMS_FORMATTER =
            DateTimeFormatter.ofPattern("HH : mm : ss");
    private long cmdTime = System.currentTimeMillis() ;

    //Content ----------------------------------------------->>>>>>>>>>>>>>>
    private final Page page;
    private final WebLogin web;
    private final String AdminNumber;
    private final String BotNumber;
    private String Quantifier = "/a";
    private final boolean debugMode = true;
    private boolean isGlobalCheck = false;
    private boolean loginAnnounced = false;
    private boolean pause = false;

    private final Map<String, Set<String>> processedIds;


    // Constructors --------------------------------------------->>>>>>>>>>>>>
    GroqAI groqAI = new GroqAI();
    YoutubeAPI youtubeAPI = new YoutubeAPI();
    Google google = new Google();
    Gemini gemini = new Gemini();
    GithubAutoCommitScript github = new GithubAutoCommitScript();

    //------------------ Main Code Starts from here
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

    public boolean isConnected(long time) {
        try {
            // wait a moment for the page to begin loading
            String webUrl = "https://web.whatsapp.com/";
            boolean isLoaded = page.url().equals(webUrl)
                    && page.locator(Chatlist).isVisible();
            if (isLoaded && !loginAnnounced) {
                String BootTime = bootime(time);
                String message = "System Boot Time : " + BootTime+ " sec" + "\n";
                sendMessageToChat(BotNumber, message+"✅ Logged in...");
                loginAnnounced = true;
            }
            else if (!isLoaded && debugMode) {
                System.out.println("⚠️ WhatsApp not fully loaded.");
                loginAnnounced = false;
            }
            return !isLoaded;
        }
        catch (RestartException re) {
            throw re;
        }
        catch (Exception e) {
            if (debugMode) System.out.println("Error💀 : " + e.getMessage());
            // In case of any exception, assume “not loaded”
            loginAnnounced = false;
            return true;
        }
    }

    /**
     * If a “Continue” popup appears, wait for it and click the button to dismiss it.
     * Call this early (e.g., after navigation or any action that might trigger the popup).
     */
    public void popupRemove() {
        try {
            // Wait up to 5 seconds for a button whose visible text is “Continue”
            Locator continueBtn = page.locator("button:has-text(\"Continue\")");
            continueBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10_000));
            // 1 min wait time considering that whastapp scan and chat load time is there in worst case
            continueBtn.click();
            System.out.println("✅ Popup “Continue” button clicked.");
        } catch (PlaywrightException e) {
            if (debugMode) System.out.println("⚠-----> No “Continue” popup found : ");
        }
    }



    public void Handler(long time ) {
        try {
            if (isConnected(time)) {
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
            cmdTime = System.currentTimeMillis();
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
                String ReachTime = Time(System.currentTimeMillis());
                String[] parts = text.split("\\s+");
                boolean check = false ;
                if(parts[0].equalsIgnoreCase("showq")
                        || parts[0].equalsIgnoreCase("pause_on")
                        || parts[0].equalsIgnoreCase("pause_off")
                        || parts[0].equalsIgnoreCase("...help"))
                    check=true;

                else if (parts.length < 2 || !parts[0].equalsIgnoreCase(Quantifier) ) {
                    continue;  // not a /a <arg> command
                }
                else check=true;

                if(!check)continue;
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
                if (processedIds.get(name).size() > 300) {
                    processedIds.put(name, new HashSet<>());
                }
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
                seen.add(N_ID);// for the paused commands and  not paused commands
                // -----------------------------------------------

                //===== Pause Auth === Only Admin
                boolean P_Auth = false;
                if (idParts.length == 3) {
                    P_Auth = senderNumber.equals(BotNumber)
                            || senderNumber.equals(AdminNumber);
                } else if (idParts.length == 2) {
                    boolean isOutgoing = (Boolean) msg.evaluate(
                            "el => el.closest('.message-out') !== null"
                    );
                    P_Auth = isOutgoing || senderNumber.equals(AdminNumber);
                }
                //===============================

                // xxxxxxxxxxxxxxxxxxxxxx  Head Commands xxxxxxxxxxxxxxxxxxxxxx
                if (parts[0].equalsIgnoreCase("showq")) {
                    ShowQuantifier(chat.elementHandle(), msg.elementHandle(), ReachTime , senderNumber);
                    continue;
                }
                else if(parts[0].equalsIgnoreCase("pause_on") && P_Auth) {
                    pause=true;
                    replyToChat(chat.elementHandle(), msg.elementHandle() , "Paused !!!" , ReachTime , senderNumber);
                    continue;
                }
                else if(parts[0].equalsIgnoreCase("pause_off") && P_Auth) {
                    pause=false;
                    replyToChat(chat.elementHandle(), msg.elementHandle(),"Pause Off !!!" , ReachTime , senderNumber);
                    continue;
                }
                else if(parts[0].equalsIgnoreCase("pause_show") && P_Auth) {
                    replyToChat(chat.elementHandle(), msg.elementHandle(),"_Current Status Of Pause_ : "+ (pause ? "On " : "Off") , ReachTime , senderNumber);
                    continue;
                } else if (parts[0].equalsIgnoreCase("...help")) {
                    ShowMenu(chat.elementHandle(), msg.elementHandle(), ReachTime , senderNumber);
                }
                //xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
                if(pause)continue; // No Command will be processed if pause is on

                long curTime = System.currentTimeMillis();
                System.out.printf("→ Processing “%s” from %s at %d%n", text, name, curTime);
                queryformat(chat.elementHandle(), msg.elementHandle(), text , ReachTime , senderNumber);
            }

        } catch (RestartException re) {
            throw re;
        } catch (Exception e) {
            System.out.println("Error 💀💀 : " + e.getMessage());
        }
    }


    private void queryformat(ElementHandle chat, ElementHandle target, String preview , String time , String sender) {
        // query : /a gc , /a ai <hi how are u>
        String[] parts = preview.split("\\s+");
        String fname = parts[1];
        StringBuilder query = new StringBuilder();
        if (parts.length > 2) for (int i = 2; i < parts.length; i++) query.append(parts[i]).append(" ");
        Fhandler(chat, target, fname, query.toString().trim() , time , sender);
    }


    private void Fhandler(ElementHandle chat, ElementHandle target, String fname, String query , String time , String sender) {
        // Empty String AI handalation
        String s = "hey user has give a empty query or wrong format ,now give me a sigma + Very agreesive reply for that directly. Also make that fency Looking a Agent Ai  is telling and  be in normal lowercase also quoete ur answer with * cuz as for whastapp we want ";

        String k = "*" + groqAI.chat("User has given wrong format in input now reply him for that very agreesivley ") + "*"; // Groq AI usage

        fname = fname.toLowerCase().trim();

        if (fname.equals("showgc")) GlobalCheck(chat, target,time,sender);
        else if (fname.equalsIgnoreCase("s_restart")) SoftRestart(chat, target,time,sender);
        else if (fname.equalsIgnoreCase("showmaxchat")) replyToChat(chat, target, ShowMaxChat(), time, sender);
        else if (fname.equals("showq")) ShowQuantifier(chat, target,time,sender);
        else if (fname.equals("help") || fname.equalsIgnoreCase("showmenu")) ShowMenu(chat, target,time,sender);
        else if (fname.equals("github")) replyToChat(chat, target, github.commit(), time, sender);
        else {
            if (query.isEmpty()) {
                String ll = groqAI.chat(s);
                replyToChat(chat, target, ll, time, sender);
                return;
            }
            switch (fname) {
                case "setmaxchat":
                    query = query.trim();
                    try {
                        Integer.parseInt(query);
                        setMaxChat(Integer.parseInt(query));
                        replyToChat(chat, target, "*Max Chat updated Successfully* ✨✨✨", time, sender);
                    } catch (Exception e) {
                        replyToChat(chat, target, "*" + groqAI.chat("User has given wrong input but we wanted number so reply him aggresively ") + "*", time, sender);
                    }
                    break;
                case "ytd":
                    if (isYTvidurl(query)) {
                        String[] res = new String[1];
                        if (youtubeAPI.ytdownloadfromUrls(query, res)) {
                            replyToChat(chat, target, "Download SuccessFull✨✨✨",time, sender);
                        } else replyToChat(chat, target, "Error in download...",time, sender);
                    } else replyToChat(chat, target, k,time, sender);
                    break;

                case "yts":
                    String ans = youtubeAPI.search(query);
                    replyToChat(chat, target, ans,time, sender);
                    break;

                case "google":
                    String ans1 = google.google(query);
                    replyToChat(chat, target, ans1,time, sender);
                    break;

                case "ai":
                    String ans2 = "*" + groqAI.chat(query) + "*";
                    replyToChat(chat, target, ans2,time, sender);
                    break;

                case "personalai":
                    String ans3 = gemini.ask(query);
                    replyToChat(chat, target, ans3,time, sender);
                    break;

                case "send":
                    String[] sendParts = query.split("\\s+", 2);
                    String ans4 = groqAI.chat(s);
                    if (sendParts.length < 2) {
                        replyToChat(chat, target, ans4,time, sender);
                        return;
                    }
                    String phone = sendParts[0];
                    String message = sendParts[1];
                    if (sendMessageToChat(phone, message)) {
                        replyToChat(chat, target, "✅Done Sending.. ",time, sender);
                    }
                    break;

                case "setgc":
                    GlobalMode(chat, target, query,time,sender);
                    break;

                case "setq":
                    SetQuantifier(chat, target, query,time,sender);
                    break;

                default:
                    replyToChat(chat, target, "⚠️ Unknown Agent Function. Accepted format: \n " + "<Quantifer> + <function name> + <query> ",time, sender);
                    break;
            }
        }
        sleep(500);
    }

    void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    void ShowMenu(ElementHandle chat, ElementHandle target, String t, String s) {
        String MenuMessage = """
        🌟 *Welcome to Your Smart Assistant! Tweakio!!!* 🌟
        _Your all-in-one intelligent companion!_
        
        ━━━━━━━━━━━━━━━━━━━━
        🧾 *How to Use Commands:*
        ➤ *Format:* `Quantifier ➜ Command Name ➜ Input`
        ➤ _Example:_ `/a ai ➜ Who is Elon Musk?`
        
        ━━━━━━━━━━━━━━━━━━━━
        💬 *AI & Chat Tools*
        ┌─ 🧠 `ai ➜ your question`
        ├─ 👤 `personalai ➜ your private chat`
        └─ ❓ `help ➜ Show this menu again`
        
        🔍 *Search & Download*
        ┌─ 🔎 `google ➜ your search query`
        ├─ 🎥 `yts ➜ search YouTube`
        └─ 📥 `ytd ➜ YouTube link to download`
        
        👨‍🎓 *Student & GitHub Tools*
        └─ 🔧 `github ➜ Perform daily commit to the linked repo`
        
        🛠️ *Group & Bot Management*
        ┌─ 📄 `showq ➜ Display current quantifier without using one`
        ├─ ⏸️ `pause_on ➜ Pause the bot so it ignores commands`
        ├─ 🔍 `pause_show ➜ Show whether the bot is paused or active`
        ├─ ▶️ `pause_off ➜ Resume the bot’s normal operation`
        ├─ 📝 `setgc ➜ Enable group-wide commands`
        ├─ 📋 `showgc ➜ Check group command status`
        ├─ 📌 `setq ➜ Set your custom quantifier`
        ├─ ⚙️ `setmaxchat ➜ Set max number of chats to scan`
        └─ 📊 `showmaxchat ➜ View current scan count`
        
        ✉️ *Messaging Utility*
        └─ 💌 `send ➜ <number> <your message>` (DM any number)
        
        ━━━━━━━━━━━━━━━━━━━━
        🔔 *Need Help?*
        ➤ Type `Quantifier ➜ showmenu` or just `...help` anytime!
        
        💡 *Pro Tips:*
        ✔️ Use short, clear inputs for faster replies.
        ✔️ After a reply, wait 3-5 seconds to avoid spam triggers.
        
        🛡️ *Safety Notice:*
        _If the Agent takes time to respond, it's protecting your account from being flagged!_
        
        🤖 *Always here to assist you!* 🤖
        """;

        replyToChat(chat, target, MenuMessage, t, s);
    }


    public void HardRestart() { // Need to Check first
        try {
            BotReloader.hardRestart();
        } catch (IOException e) {
            System.err.println("Error 💀💀 : " + e.getMessage());
        }
    }

    public void SetQuantifier(ElementHandle chat, ElementHandle target, String quantifier,String T , String s ) {
        this.Quantifier = quantifier;
        replyToChat(chat, target, "Quantifier Updated Successfully✨✨✨",T,s); // replacable with ai message
    }

    public void ShowQuantifier(ElementHandle chat, ElementHandle target, String Time , String sender) {
        String s = "🌐 'Current Quantifier' :  " + this.Quantifier;
        replyToChat(chat, target, s,Time,sender);
    }

    public void replyToChat(ElementHandle chat, ElementHandle message, String reply , String time , String sender) {
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
            String cmdBoot = bootime(cmdTime);
            inputBox.fill("Command Execute Time : "+ cmdBoot + " sec" + "\n" + reply);
            page.keyboard().press("Enter");

            System.out.println("✅ Replied: " + reply);
            sleep(500);

        } catch (Exception e) {
            if (debugMode) System.out.println("Error 💀💀 : " + e.getMessage());
            replyBack(chat, reply, time, sender);
        }
    }

    synchronized void GlobalMode(ElementHandle chat, ElementHandle target, String text, String time, String sender) {
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
        } else replyToChat(chat, target, "🗣️Agent : Only on/off accepted " , time, sender);
        if (check) replyToChat(chat, target, statusMessage,time, sender);
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

    synchronized void GlobalCheck(ElementHandle chat, ElementHandle target , String time , String sender) {
        String s;
        if (isGlobalCheck) s = "on";
        else s = "off";
        replyToChat(chat, target, " 🌐 Global Mode : " + s , time, sender);
    }

    //////////--------------- Reply Back
    public synchronized void replyBack(ElementHandle chat, String replyMessage , String time , String sender) {
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
            inputBox.fill("");      // clear any existing text
            StringBuilder sb = new StringBuilder();
            sb.append("Sender/Chat Number : ").append(sender);
            sb.append("\n");
            sb.append("Command Recieved at : ").append(time);
            sb.append("\n");
            String cmdBoot = bootime(cmdTime);
            sb.append("Command Execution Time : ").append(cmdBoot).append("sec");
            sb.append("\n");
            sb.append(replyMessage);
            inputBox.fill(sb.toString());

            page.keyboard().press("Enter");
            System.out.printf("📤 Replied: \"%s\"%n", replyMessage);
        } catch (Exception e) {
            if (debugMode) System.err.println("❌ replyBack() failed: " + e.getMessage());
        }
    }

    //----------------------------------
    //----------------------------------
    //----------------------------------
    //----------------------------------

    public static String Time(long epochMillis) {
        LocalTime time = Instant
                .ofEpochMilli(epochMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalTime();
        return time.format(HMS_FORMATTER);
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
                String text = msg.textContent().trim().toLowerCase();
                if (text.startsWith(Quantifier + " ")
                        || text.startsWith("showq")
                        || text.startsWith("pause_on")
                        || text.startsWith("pause_off")
                        || text.startsWith("pause_show")) {
                    String rawId = msg.getAttribute("data-id");
                    String nId = normalizeMessageId(rawId);
                    processedIds.get(name).add(nId);
                }
            }
        }
    }

    public void SoftRestart(ElementHandle chat, ElementHandle target , String time , String sender) {
        replyToChat(chat, target, "🔄 Restart Initiated 👺",time,sender);
        String name = getChatName(chat);
        processedIds.putIfAbsent(name, new HashSet<>());
        String rawId = target.getAttribute("data-id");
        String N_ID = normalizeMessageId(rawId);
        processedIds.get(name).add(N_ID);
        System.out.println(N_ID);
        throw new RestartException();
    }

    /**
     * Returns the difference in whole seconds between the given start time (in milliseconds)
     * and the current system time.
     *
     * @param startMillis  the starting timestamp (e.g. from System.currentTimeMillis())
     * @return the elapsed time in seconds (rounded down)
     */
    public static String bootime(long startMillis) {
        long nowMillis = System.currentTimeMillis();
        long diffMillis = nowMillis - startMillis;
        return String.valueOf(diffMillis / 1000);
    }
}