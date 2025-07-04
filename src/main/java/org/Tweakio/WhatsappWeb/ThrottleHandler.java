package org.Tweakio.WhatsappWeb;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import static org.Tweakio.WhatsappWeb.Brain.Chatitems;
import static org.Tweakio.WhatsappWeb.Brain.Chatlist;

public class ThrottleHandler {
    public static void OldChatsMark(Map<String, String> Throttling, Page page, UnreadHandler unread, Extras extras) {
        try {


            long currentTime = System.currentTimeMillis();
            Iterator<Map.Entry<String, String>> iter = Throttling.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> entry = iter.next();
                String name = entry.getKey();
                String[] parts = entry.getValue().split("#");

                if (parts.length < 2) continue;

                try {
                    long addedTime = Long.parseLong(parts[1]);
                    long elapsed = currentTime - addedTime;

                    // If more than 2 minutes passed
                    if (elapsed > 100_000 + new Random().nextInt(40_000)) {
                        Locator chat = findChatInDOMByName(name, page, extras);
                        if (chat != null) unread.markAsUnread(page, chat);
                        iter.remove(); // clean it from the map regardless
                    }
                } catch (Exception e) {
                    iter.remove();
                }
            }
        } catch (Exception e) {
            System.out.println(" Throttlhandler  // oldchatmark " + e.getMessage());
            Extras.logwriter("oldchatmark failed //oldchatmark // Throttlehandler : " + e.getMessage());
        }
    }

    public static Locator findChatInDOMByName(String name, Page page, Extras extras) {
        try {
            Locator chats = page.locator(Chatlist).locator(Chatitems);
            for (int i = 0; i < chats.count(); i++) {
                if (extras.getChatName(chats.nth(i)).equals(name)) {
                    return chats.nth(i);
                }
            }
        } catch (Exception e) {
            Extras.logwriter("findChatInDOMByName // Throttlehandler : " + e.getMessage());
        }
        return null;
    }
}
