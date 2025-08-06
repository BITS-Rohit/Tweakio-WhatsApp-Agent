package org.Tweakio.WhatsappWeb;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.*;

import static org.Tweakio.UserSettings.user.QUANTIFIER;
import static org.Tweakio.WhatsappWeb.Brain.*;


public class SeedCacher {
    private final Extras extras = new Extras();
    private final UnreadHandler unread = new UnreadHandler();
    private final Page page;
    private final Set<String> adminCmds = Set.of(
            "pause_on", "pause_off", "pause_show", "showq", NLP, QUANTIFIER + " "
    );

    public SeedCacher(Page page) {
        this.page = page;
    }

    public void seedCache(Map<String, Set<String>> processedIds) {
        try {
            log("Seed Cacher Started -----------");

            Locator allChats = page.locator(Chatlist).locator(Chatitems);
            int limit = Math.min(allChats.count(), MaxChat);
            for (int i = 0; i < limit; i++) {
                Locator chat = allChats.nth(i);
                processChat(chat, processedIds);
            }

            log("Seed Cache Done ---------------");
        } catch (Exception e) {
            log("Seed Cache error : " + e.getMessage());
        }
    }

    private void processChat(Locator chat, Map<String, Set<String>> processedIds) {
        String name = extras.getChatName(chat);
        if (banlist.contains(name)) return;

        Integer unreadCount = unread.getUnreadCountOrNull(chat);
        if (unreadCount != null && unreadCount == 0) {
            log("Skippable (no unread) for: " + name);
            return;
        }

        log("Processing chat '" + name + "' with " + unreadCount + " unread");
        processedIds.putIfAbsent(name, new HashSet<>());

        focusChat(chat);
        collectAdminCommands(chat, processedIds.get(name));
        clearInputBox();
        markUnreadIfNeeded(chat);
    }

    private void focusChat(Locator chat) {
        chat.hover();
        chat.click();
        page.waitForTimeout(100);
    }

    private void collectAdminCommands(Locator chat, Set<String> seenIds) {
        Locator msgs = page.locator(Messages);
        for (int j = 0; j < msgs.count(); j++) {
            Locator msg = msgs.nth(j);
            String text = msg.textContent().trim().toLowerCase();
            String firstToken = text.split(" ")[0];
            if (adminCmds.contains(firstToken)) {
                String id = extras.normalizeMessageId(msg.getAttribute("data-id"));
                seenIds.add(id);
            }
        }
    }

    private void clearInputBox() {
        Locator input = page.locator("div[aria-label='Type a message'][role='textbox']");
        if (input.isVisible()) {
            input.click();
            input.fill("");
        } else {
            Extras.logwriter("Input box in Seed Cacher is not visible");
        }
        extras.sleep(1000);
    }

    private void markUnreadIfNeeded(Locator chat) {
        if (unread.markAsUnread(page, chat)) {
            log("Chat marked as unread");
        }
    }

    private void log(String msg) {
        System.out.println(msg);
        Extras.logwriter(msg);
    }
}
