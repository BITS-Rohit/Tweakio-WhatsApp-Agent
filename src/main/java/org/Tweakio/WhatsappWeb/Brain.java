package org.Tweakio.WhatsappWeb;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.Tweakio.AI.Characters.TypesAI;
import org.Tweakio.AI.Chats.AgentAI;
import org.Tweakio.AI.Chats.Gemini;
import org.Tweakio.AI.Chats.GroqAI;
import org.Tweakio.AI.ImageAIs.Dall_E_3;
import org.Tweakio.GithubAutomation.GithubAutoCommitScript;
import org.Tweakio.Manual.Manual;
import org.Tweakio.SearchSites.Amazon.India.SearchAmazonIN;
import org.Tweakio.SearchSites.Google.Google;
import org.Tweakio.SearchSites.Youtube.YoutubeAPI;
import org.Tweakio.UserSettings.ConfigStore;
import org.Tweakio.UserSettings.user;
import org.Tweakio.WhatsappWeb.BrowserManager.Browser;
import org.Tweakio.WhatsappWeb.MediaHandler.SendMedia;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class Brain {

    public static class RestartException extends RuntimeException { // Need for the restart
        public RestartException() {
            super("User requested restart");
        }
    }

    //--------------- Selectors Area ----------------------------
    static final String Chatlist = "div[role='grid']";
    static final String Chatitems = "div[role='listitem']";
    static final String Messages = "div[data-id] div.copyable-text[data-pre-plain-text] span.selectable-text";
    //-----------------------------------
    static final int RefreshTime = 1;
    static int MaxChat = 5;
    private long cmdTime = System.currentTimeMillis();
    public boolean isConnect = false;
    //Content ----------------------------------------------->>>>>>>>>>>>>>>
    public final Page page;
    private final String AdminNumber;
    private final String BotNumber;
    String Quantifier = user.QUANTIFIER;
    static String NLP = "/say";
    public static final boolean debugMode = true;
    private boolean isGlobalCheck = false;
    private boolean loginAnnounced = false;
    private boolean pause = false;


    public final Map<String, Set<String>> processedIds;
    public Map<String, Path> dp_img = new HashMap<>();

    // Constructors --------------------------------------------->>>>>>>>>>>>>
    GroqAI groqAI = new GroqAI();
    AgentAI gpt = new AgentAI();
    YoutubeAPI youtubeAPI = new YoutubeAPI();
    Google google = new Google();
    Gemini gemini = new Gemini();
    GithubAutoCommitScript github = new GithubAutoCommitScript();
    UnreadHandler unread = new UnreadHandler();
    Extras extras = new Extras();
    MenuBar menu = new MenuBar();
    TypesAI typeai = new TypesAI();
    ReplyHandle replyHandle;
    SearchAmazonIN amazonIN;
    SendMedia sendMedia;
    Dall_E_3 dallE3 = new Dall_E_3();
    Browser browser; // Global Browser now

    //------------------ Main Code Starts from here
    public Brain(WebLogin webStart, Map<String, Set<String>> initialState, Browser browser) {

        this.BotNumber = user.BOT_NUMBER;
        amazonIN = new SearchAmazonIN(browser);
        this.browser = browser;
        this.page = webStart.getPage();
        sendMedia = new SendMedia(page);
        replyHandle = new ReplyHandle(page);
        this.AdminNumber = new __().a_num;
        this.processedIds = initialState;
    }

    public Map<String, Set<String>> getProcessedIds() {
        return processedIds;
    }

    public static Set<String> banlist = new HashSet<>();
//    public static Map<String, String> Throttling = new HashMap<>();

    public void MessageToOwner(long time) {
        try {
            // wait a moment for the page to begin loading
            boolean isLoaded = isConnect;
            if (!isLoaded) {
                String webUrl = "https://web.whatsapp.com/";
                isLoaded = page.url().equals(webUrl)
                        && page.locator(Chatlist).isVisible();
            }

            if (isLoaded && !loginAnnounced) {
                String BootTime = Extras.bootime(time);
                String message = "System Boot Time : " + BootTime + " sec" + "\n";
                if (extras.sendMessageToChat(page, AdminNumber, message + "✅ Logged in...")) loginAnnounced = true;
            } else if (!isLoaded && debugMode) {
                System.out.println("⚠️ WhatsApp not fully loaded.");
                loginAnnounced = false;
            }
        } catch (RestartException re) {
            Extras.logwriter("Throw re // messageto owner// Brain");
            throw re;
        } catch (Exception e) {
            extras.sleep(300000);
            if (debugMode) System.out.println("Error💀 : " + e.getMessage());
            Extras.logwriter("Error // Brain // messageto owner " + e.getMessage());
            // In case of any exception, assume “not loaded”
            loginAnnounced = false;
        }
    }

    /**
     * If a “Continue” popup appears, wait for it and click the button to dismiss it.
     * Call this early (e.g., after navigation or any action that might trigger the popup).
     */
    public void popupRemove() {
        try {
            // Wait up to 10 seconds for the "Continue" button to appear
            Locator continueBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("continue"));
            continueBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5_000));
            continueBtn.hover();
            continueBtn.click();
            System.out.println("✅ Popup “Continue” button clicked.");
        } catch (PlaywrightException e) {
            if (debugMode) System.out.println("⚠-----> No “Continue” popup found : ");
            Extras.logwriter(" No continue popup Found. // Brain// popupRemove ");
        }
    }


    public void Handler() {
        try {
            if (isConnect) MessageToOwner(System.currentTimeMillis());
            int i = 1;
            long j = System.currentTimeMillis();

            while (page.locator(Chatlist) != null || page.locator(Chatlist).count() != 0) {
                Locator chatlist = page.locator(Chatlist);
                if (Math.abs(System.currentTimeMillis() - j) / 1000 > 10 + new Random().nextInt(5)) {
                    System.out.println("Throttle check started ");
//                    ThrottleHandler.OldChatsMark(Throttling, page, unread, extras);
                    j = System.currentTimeMillis();
                    System.out.println("Throttle check finished ");
                }
                System.out.println("\n 🌎New Chat Check==================>>>>>>>>>>>>>>> " + i);
                EveryChatCheck(chatlist);
                i++;
                // todo prcoess downlodable queues here  // after every cycle
            }
            System.out.println("Chat List is not working. ");
            Browser.closeAll(); // Clean up.
            System.exit(0); // Safe exit
        } catch (RestartException e) {
            Extras.logwriter("Error : Throw re // Brain// Handler");
            throw e;
        } catch (Exception e) {
            if ("User requested restart".equals(e.getMessage())) throw e;
            System.out.println("Error💀💀 : " + e.getMessage());
            Extras.logwriter("Error // Brain// Handler" + e.getMessage());
        }
    }

    public void EveryChatCheck(Locator chatlist) {
        try {
            Locator Allchats = chatlist.locator(Chatitems);
            Allchats.hover();
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

                // At this interval we will add a queue for Time taking req

                extras.sleep(1_00);
            }

        } catch (RestartException re) {
            Extras.logwriter("Throw re // Brain// EveryChatCheck");
            throw re;
        } catch (Exception e) {
            if (debugMode) System.out.println("Error 💀💀 : " + e.getMessage());
            Extras.logwriter("Error -> // Brain// EveryChatCheck : " + e.getMessage());
        }
    }

    private void checkChat(Locator chat) {
        try {
            if (chat == null) {
                System.out.println("Chat Element : Null 🔻🔻🔻");
                return;
            }
            String name = extras.getChatName(chat);
            System.out.println("==> Chat Name : " + name);

            // Ban------------------
            if (banlist.contains(name)) {
                System.out.println("[Banned Chat] ");
                return;
            }

            // Pre Maturly Check
            Integer c = unread.getUnreadCountOrNull(chat);
            if (c != null && c == 0) {
                System.out.println("Skippable... ");
                return;
            }
            System.out.println("Count : " + c);

            cmdTime = System.currentTimeMillis();
            chat.hover();
            chat.click();


            // Ensure we have a set to track this chat’s IDs
            processedIds.putIfAbsent(name, new HashSet<>());
            Set<String> seen = processedIds.get(name);

            Locator botMessages = page.locator(Messages);
            long count = botMessages.count();
            if (count == 0) {
                System.out.println("No Bot Chats 🔻🔻🔻");
                return;
            }
            System.out.println("==> Messages Loaded :  " + count);
            System.out.println("*━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            // Oldest → Newest
            for (int i = 0; i < count; i++) {
                Locator msg = botMessages.nth(i);
                String text = msg.textContent().trim();

                String ReachTime = Extras.Time(System.currentTimeMillis());
                String[] parts = text.split("\\s+");

                Set<String> allowedCommands = Set.of("showq", "pause_on", "pause_off", "...help");

                boolean check = allowedCommands.contains(parts[0].toLowerCase()) ||
                        (parts.length >= 2 && parts[0].equalsIgnoreCase(Quantifier) || parts[0].equalsIgnoreCase(NLP));

                if (!check) continue;

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
                String N_ID = extras.normalizeMessageId(rawId);

                // skip if we’ve handled this exact N_ID before && Handle the size of Set
                if (processedIds.get(name).size() > 300) {
                    processedIds.put(name, new HashSet<>());
                }
                if (seen.contains(N_ID)) {
                    System.out.println("Returning Back , Already Processed IDS");
                    continue;
                }


                // --- authenticate // Later will add here for the Admin Access Commands Only
                System.out.println("Nid : " + N_ID);
                String[] idParts = N_ID.split("#");
                String senderNumber = idParts[0].replaceAll("[^0-9]", "");

                boolean auth = isGlobalCheck;
                if (name.length() > 5) {
                    String a = name.substring(name.length() - 6);
                    System.out.println(a);
                    if (a.equalsIgnoreCase("(you)")) auth = true;
                }
                boolean auth1 = senderNumber.equals(BotNumber)
                        || senderNumber.equals(AdminNumber);
                if (!auth) {
                    if (idParts.length == 3) {
                        auth = auth1;
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
                    P_Auth = auth1;
                } else if (idParts.length == 2) {
                    boolean isOutgoing = (Boolean) msg.evaluate(
                            "el => el.closest('.message-out') !== null"
                    );
                    P_Auth = isOutgoing || senderNumber.equals(AdminNumber);
                }
                //===============================

                // xxxxxxxxxxxxxxxxxxxxxx  Head Commands xxxxxxxxxxxxxxxxxxxxxx
                String cmd = parts[0].toLowerCase();

                switch (cmd) {
                    case "showq" -> ShowQuantifier(chat.elementHandle(), msg.elementHandle(), ReachTime, senderNumber);
                    case "pause_on" -> {
                        if (P_Auth) {
                            pause = true;
                            replyHandle.replyToChat(chat.elementHandle(), msg.elementHandle(),
                                    "Paused !!!", ReachTime, senderNumber, cmdTime);
                        }
                    }
                    case "pause_off" -> {
                        if (P_Auth) {
                            pause = false;
                            replyHandle.replyToChat(chat.elementHandle(), msg.elementHandle(),
                                    "Pause Off !!!", ReachTime, senderNumber, cmdTime);
                        }
                    }
                    case "pause_show" -> {
                        if (P_Auth) {
                            String status = pause ? "On" : "Off";
                            replyHandle.replyToChat(chat.elementHandle(), msg.elementHandle(),
                                    "_Current Status Of Pause_: " + status,
                                    ReachTime, senderNumber, cmdTime);
                        }
                    }
                    case "...help" -> ShowMenu(chat.elementHandle(), msg.elementHandle(), ReachTime, senderNumber);
                    default -> {
                        // fall‑through to next logic or ignore unknown commands
                    }
                }


                //xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
                if (pause) {
                    continue; // No Command will be processed if pause is on
                }

                long curTime = System.currentTimeMillis();
                System.out.printf("→ Processing “%s” from %s at %d%n", text, name, curTime);
                if (parts[0].equalsIgnoreCase(NLP)) {
                    // Convert From NLP to CLI
                    text = gemini.ask(text, false, true);
                    System.out.println("AI Formatted text : " + text);
                }
                queryformat(chat.elementHandle(), msg.elementHandle(), text, ReachTime, senderNumber);
            }

            // Before moving to next check Mark as Unread if processed anything
//            extras.sleep(1500);
//            if (!Throttling.containsKey(name)) {
//                Throttling.put(name, 15 + new Random().nextInt(4) + "#" + System.currentTimeMillis());
//                unread.markAsUnread(page, chat);
//            } else { // Means it is already in tbe throtle chats
//                String entry = Throttling.get(name);
//                if (hasThrottleExpired(entry)) {
//                    Throttling.remove(name);
//                    unread.markAsUnread(page, chat);
//                } else {
//                    // Todo log it
//                }
//            }


        } catch (RestartException re) {
            Extras.logwriter("Throw re // checkchat // brain");
            throw re;
        } catch (Exception e) {
            System.out.println("Error 💀💀 : " + e.getMessage());
            Extras.logwriter("Error // checkchat // brain " + e.getMessage());
        }
    }


    private void queryformat(ElementHandle chat, ElementHandle target, String preview, String time, String sender) {
        // query : // showgc , // Google <hi how are u>
        String[] parts = preview.split("\\s+");
        String fname = parts[1];
        StringBuilder query = new StringBuilder();
        if (parts.length > 2) for (int i = 2; i < parts.length; i++) query.append(parts[i]).append(" ");
        Fhandler(chat, target, fname, query.toString().trim(), time, sender);
    }


    private void Fhandler(ElementHandle chat, ElementHandle target, String fname, String query, String time, String sender) {
        String s = "hey user has give a empty query or wrong format ,now give me a sigma + Very aggressive reply for that directly. Also make that fancy Looking a Agent Ai is telling and be in normal lowercase also quote ur answer with * cuz as for whatsapp we want ";
        String k = "*" + groqAI.chat("User has given wrong format in input now reply him for that very aggressively") + "*"; // Groq AI usage
        fname = fname.toLowerCase().trim();

        if (fname.equals("showgc")) ShowGlobalMode(chat, target, time, sender);
        else if (fname.equals("ban")) ban(extras.getChatName(chat));
        else if (fname.equals("unban")) unban(extras.getChatName(chat));
        else if (fname.equals("s_restart")) SoftRestart(chat, target, time, sender);
        else if (fname.equals("imglist")) handleImageList(chat, replyHandle, target, time, sender, cmdTime);
        else if (fname.equals("showmaxchat"))
            replyHandle.replyToChat(chat, target, ShowMaxChat(), time, sender, cmdTime);
        else if (fname.equals("showq")) ShowQuantifier(chat, target, time, sender);
        else if (fname.equals("help") || fname.equals("showmenu")) ShowMenu(chat, target, time, sender);
        else if (fname.equals("github")) replyHandle.replyToChat(chat, target, github.commit(), time, sender, cmdTime);
        else if (fname.equals("h_restart")) HardRestart(chat, target, "Initiating ...", time, sender, cmdTime);
        else if (fname.equals("getdp")) {
            if (!getdp(chat, target, time, sender, cmdTime))
                replyHandle.replyToChat(chat, target, "Error in processing", time, sender, cmdTime);

        } else if (fname.equals("img_url"))
            replyHandle.replyToChat(chat, target, dallE3.get_Pic_Url_With_Prompt(query), time, sender, cmdTime);
        else if (fname.equals("imgd"))
            sendMedia.SendFile(chat, target, "Downloaded!!", time, sender, cmdTime, "image", dallE3.downloadImageFromInput(query).toString(), page);
        else if (!query.isEmpty()) {
            switch (fname) {
                case "setmaxchat":
                    try {
                        int max = Integer.parseInt(query.trim());
                        setMaxChat(max);
                        replyHandle.replyToChat(chat, target, "*Max Chat updated Successfully* ✨✨✨", time, sender, cmdTime);
                    } catch (Exception e) {
                        replyHandle.replyToChat(chat, target, k, time, sender, cmdTime);
                    }
                    break;

                case "ytd":
                    if (extras.isYTvidurl(query)) {
                        String[] res = new String[1];
                        String[] name = new String[1];
                        if (youtubeAPI.ytdownloadfromUrls(query, res, name)) {
                            sendMedia.SendFile(chat, target, "SuccessFully Downloaded -> Here is ur file", time, sender, cmdTime, "document", res[0], page);
                        } else {
                            replyHandle.replyToChat(chat, target, "Error in download...", time, sender, cmdTime);
                        }
                    } else {
                        replyHandle.replyToChat(chat, target, k, time, sender, cmdTime);
                    }
                    break;

                case "yts":
                    String ans = youtubeAPI.search(query);
                    replyHandle.replyToChat(chat, target, ans, time, sender, cmdTime);
                    break;

                case "google":
                    String ans1 = google.google(query);
                    replyHandle.replyToChat(chat, target, ans1, time, sender, cmdTime);
                    break;

                case "ai":
                    String ans2 = "*" + groqAI.chat(query) + "*";
                    replyHandle.replyToChat(chat, target, ans2, time, sender, cmdTime);
                    break;

                case "personalai":
                    String ans3 = gemini.ask(query, true, true);
                    replyHandle.replyToChat(chat, target, ans3, time, sender, cmdTime);
                    break;

                case "gpt":
                    String ans4 = gpt.sendToAgent(query);
                    replyHandle.replyToChat(chat, target, ans4, time, sender, cmdTime);
                    break;

                case "setgc":
                    setGlobalMode(chat, target, query, time, sender);
                    break;

                case "setq":
                    SetQuantifier(chat, target, query, time, sender);
                    break;

                case "manual":
                    String desp = new Manual().ManualHandler(query);
                    replyHandle.replyToChat(chat, target, desp, time, sender, cmdTime);
                    break;

                case "setimg":
                    handleImageSetting(query, replyHandle, chat, target, time, sender, cmdTime);
                    break;

                case "addimg":
                    ConfigStore.addIntroImage(query);
                    replyHandle.replyToChat(chat, target, "Added Image Successfully ", time, sender, cmdTime);
                    break;

                case "amazon_s":
                    replyHandle.replyToChat(chat, target, amazonIN.GetContent(query), time, sender, cmdTime);
                    break;

                case "love", "romantic", "romance":
                    String al = typeai.askType("love", query + "Sender : " + sender);
                    replyHandle.replyToChat(chat, target, al, time, sender, cmdTime);
                    break;

                case "emo", "emotional", "therapist":
                    String el = typeai.askType("therapist", query);
                    replyHandle.replyToChat(chat, target, el, time, sender, cmdTime);
                    break;

                case "shraddha", "shradha", "sh":
                    String qq = "[Sender- %s : %s] : %s".formatted(Extras.realtime(), sender, query);
                    String sl = typeai.askType("shraddha", qq);
                    replyHandle.replyToChat(chat, target, sl, time, sender, cmdTime);
                    break;

                default:
                    replyHandle.replyToChat(chat, target, "⚠️ Unknown Agent Function. Accepted format: \n<Quantifier> + <function name> + <query>", time, sender, cmdTime);
                    break;
            }
        } else {
            // query is empty
            System.out.println("Empty Section -----");
            String ll = groqAI.chat(s);
            replyHandle.replyToChat(chat, target, ll, time, sender, cmdTime);
        }

        extras.sleep(RefreshTime);
    }


    //----
    public void ShowMenu(ElementHandle chat, ElementHandle target, String t, String s) {
        String MenuMessage = menu.Menu();

        sendMedia.SendFile(chat, target, MenuMessage, t, s, cmdTime, "image", dallE3.downloadImageFromInput(user.INTRO_IMG_URL + "##intro").toString(), page);

    }

    // ----
    public void HardRestart(ElementHandle chat, ElementHandle message, String reply, String time, String sender, long cmdTime) {
        replyHandle.replyToChat(chat, message, reply, time, sender, cmdTime);
        extras.sleep(1000); // For Messages to send
        Reloader.hardRestart();
    }

    public void SetQuantifier(ElementHandle chat, ElementHandle target, String quantifier, String T, String s) {
        Quantifier = quantifier;
        replyHandle.replyToChat(chat, target, "Quantifier Updated Successfully✨✨✨", T, s, cmdTime); // replacable with AI message
    }

    //----
    public void ShowQuantifier(ElementHandle chat, ElementHandle target, String Time, String sender) {
        String s = "🌐 `Current Quantifier` :  " + Quantifier;
        replyHandle.replyToChat(chat, target, s, Time, sender, cmdTime);
    }

    void setGlobalMode(ElementHandle chat, ElementHandle target, String text, String time, String sender) {
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
        } else replyHandle.replyToChat(chat, target, "🗣️Agent : Only on/off accepted ", time, sender, cmdTime);
        if (check) replyHandle.replyToChat(chat, target, statusMessage, time, sender, cmdTime);
    }

    public void ShowGlobalMode(ElementHandle chat, ElementHandle target, String time, String sender) {
        String s;
        if (isGlobalCheck) s = "on";
        else s = "off";
        replyHandle.replyToChat(chat, target, " 🌐 Global Mode : " + s, time, sender, cmdTime);
    }

    private void setMaxChat(int n) {
        MaxChat = n;
    }

    public String ShowMaxChat() {
        return String.valueOf(MaxChat);
    }

    public void SoftRestart(ElementHandle chat, ElementHandle target, String time, String sender) {
        replyHandle.replyToChat(chat, target, "🔄 Restart Initiated 👺", time, sender, cmdTime);
        extras.sleep(500);
        String name = extras.getChatName(chat);
        processedIds.putIfAbsent(name, new HashSet<>());
        String rawId = target.getAttribute("data-id");
        String N_ID = extras.normalizeMessageId(rawId);
        processedIds.get(name).add(N_ID);
        System.out.println(N_ID);
        throw new RestartException();
    }

    public boolean getdp(ElementHandle chat, ElementHandle target, String time, String sender, long cmdTime) {
        try {
            String name = extras.getChatName(chat);

            // 1) If already cached, just resend
            if (dp_img.containsKey(name)) {
                sendMedia.SendFile(chat, target, "DP of " + name,
                        time, sender, cmdTime, "image",
                        dp_img.get(name).toString(), page);
                return true;
            }

            // 2) Open that chat thread
            chat.hover();
            chat.click();

            // 3) Click the "menu" (three‑dot) button in the header
            Locator menuBtn = page.locator("button[title='Menu'] span[data-icon='more-refreshed']");
            menuBtn.waitFor(new Locator.WaitForOptions().setTimeout(2_000));
            menuBtn.hover();
            menuBtn.click();

            // 4) Try both "Contact info" and "Group info"
            Locator infoItem;
            try {
                infoItem = page.getByText("Contact info");
                infoItem.waitFor(new Locator.WaitForOptions().setTimeout(2_000));
            } catch (PlaywrightException e1) {
                try {
                    infoItem = page.getByText("Group info");
                    infoItem.waitFor(new Locator.WaitForOptions().setTimeout(2_000));
                } catch (PlaywrightException e2) {
                    System.out.println("❌ Couldn't find 'Contact info' or 'Group info'.");
                    Extras.logwriter("Couldn't find 'Contact info' or 'Group info'. // getdp // brain");
                    return false;
                }
            }

            // Click info item
            infoItem.hover();
            infoItem.click();

            // 5) Wait for and grab the large DP in the side pane
            Locator bigDpContainer = page.locator("div[role='button'] img[src*='cdn.whatsapp.net']");
            Locator bigDp = bigDpContainer.first();

            bigDp.waitFor(new Locator.WaitForOptions().setTimeout(2_000));
            if (!bigDp.isVisible()) {
                System.out.println("Full‑res DP not visible");
                return false;
            }

            // 6) Download the image
            String link = bigDp.getAttribute("src");
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(link).get().build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    System.out.println("Failed to download DP");
                    return false;
                }

                // 7) Save locally
                InputStream in = response.body().byteStream();
                Path folder = Paths.get("src/main/java/org/Tweakio/FilesSaved");
                Files.createDirectories(folder);
                Path file = folder.resolve("dp-" + name + ".jpg");
                Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);

                // 8) Cache & send
                dp_img.put(name, file);
                sendMedia.SendFile(chat, target, "DP of " + name,
                        time, sender, cmdTime, "image", file.toString(), page);

                System.out.println("✅ Saved full‑res DP → " + file);
                return true;
            }

        } catch (Exception e) {
            System.out.println("⚠ Error in getdp: " + e.getMessage());
            Extras.logwriter("Error in getdp // brain: " + e.getMessage());
            return false;
        }
    }

    void ban(String name) {
        banlist.add(name);
    }

    void unban(String name) {
        banlist.remove(name);
    }

    /**
     * Returns true if the throttle duration has passed since startTime.
     *
     * @param throttleEntry A String in the form "durationInSec#startTimestampMillis"
     * @return true if now ≥ startTimestamp + durationInSec*1000
     */
    public static boolean hasThrottleExpired(String throttleEntry) {
        String[] parts = throttleEntry.split("#");
        int durationSeconds = Integer.parseInt(parts[0]);
        long startMillis = Long.parseLong(parts[1]);
        long elapsedMillis = System.currentTimeMillis() - startMillis;
        return elapsedMillis >= durationSeconds * 1000L;
    }

    public void handleImageSetting(String query, ReplyHandle replyHandle, ElementHandle chat, ElementHandle target, String time, String sender, long cmdTime) {
        boolean isNumber = false;

        try {
            isNumber = Integer.parseInt(query) > 0;
        } catch (Exception ignored) {
        }

        if (isNumber) {
            int idx = Integer.parseInt(query);
            if (idx < ConfigStore.getIntroImageList().size()) {
                ConfigStore.setIntroImageByIndex(idx);
                replyHandle.replyToChat(chat, target, "`✅ Image set successfully.`", time, sender, cmdTime);
            } else {
                replyHandle.replyToChat(chat, target, "`❌ Invalid index.`", time, sender, cmdTime);
            }
        } else {
            ConfigStore.setIntroImageUrl(query);
            replyHandle.replyToChat(chat, target, "`✅ Image set successfully.`", time, sender, cmdTime);
        }
    }

    public void handleImageList(ElementHandle chat, ReplyHandle replyHandle, ElementHandle target, String time, String sender, long cmdTime) {
        List<String> imageList = ConfigStore.getIntroImageList();
        if (imageList.isEmpty()) {
            replyHandle.replyToChat(chat, target, "⚠️ No intro images available.", time, sender, cmdTime);
            return;
        }

        StringBuilder formatted = new StringBuilder("🖼️ *Intro Image List:*\n\n");
        for (int i = 0; i < imageList.size(); i++) {
            formatted.append(i).append(". ").append(imageList.get(i)).append("\n");
        }

        replyHandle.replyToChat(chat, target, formatted.toString(), time, sender, cmdTime);
    }
}


//                case "send":
//                    String[] sendParts = query.split("\\s+", 2);
//                    if (sendParts.length < 2) {
//                        replyHandle.replyToChat(chat, target, groqAI.chat(s), time, sender, cmdTime);
//                        return;
//                    }
//                    String phone = sendParts[0];
//                    String message = sendParts[1];
//                    if (extras.sendMessageToChat(page, phone, message)) {
//                        replyHandle.replyToChat(chat, target, "✅Done Sending... ", time, sender, cmdTime);
//                    }
//                    break;