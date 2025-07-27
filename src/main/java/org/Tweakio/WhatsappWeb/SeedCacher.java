package org.Tweakio.WhatsappWeb;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.Tweakio.UserSettings.user;

import java.util.*;

import static org.Tweakio.WhatsappWeb.Brain.*;

public class SeedCacher {
    Extras extras = new Extras();
    UnreadHandler unread;
    Page page ;

    List<String> admin_cmd = new ArrayList<>();
    SeedCacher(Page page ) {
        this.page = page;
        unread = new UnreadHandler();
        admin_cmd.add("pause_on");
        admin_cmd.add("pause_off");
        admin_cmd.add("pause_show");
        admin_cmd.add("showq");
        admin_cmd.add(NLP);
        admin_cmd.add(user.QUANTIFIER+" ");
    }

    public void seedCache(Map<String, Set<String>> processedIds) {
        try {
            System.out.println("Seed Cacher Started -----------");
            Extras.logwriter("Seed Cacher Started -----------");

            Locator allChats = page.locator(Chatlist).locator(Chatitems);
            int n = Math.min(MaxChat, allChats.count());
            for (int i = 0; i < n; i++) {
                Locator chat = allChats.nth(i);
                String name = extras.getChatName(chat);

                if(banlist.contains(name)) continue;
                Integer c = unread.getUnreadCountOrNull(chat);
                if (c != null && c == 0) {
                    System.out.println("Skippable... ");
                    Extras.logwriter("Skippable... ");
                    return;
                }
                System.out.println("Count : " + c);
                Extras.logwriter("Count : " + c);

                processedIds.putIfAbsent(name, new HashSet<>());
                chat.hover();
                chat.click();
                Locator botMessages = page.locator(Messages);
                for (int j = 0; j < botMessages.count(); j++) {
                    Locator msg = botMessages.nth(j);
                    String text = msg.textContent().trim().toLowerCase();
                    if (admin_cmd.contains(text.split(" ")[0])) processedIds.get(name).add(extras.normalizeMessageId(msg.getAttribute("data-id")));
                }
                Locator inputBox = page.locator("div[aria-label='Type a message'][role='textbox']");
                if(inputBox.isVisible()){
                    inputBox.hover();
                    inputBox.click();
                    inputBox.fill("");
                }else Extras.logwriter("Input box in Seed Cacher is not visible");
                extras.sleep(1000); // Sleep for 1 sec every time for human like behaviour
                if(unread.markAsUnread(page,chat)) {
                    System.out.println("Chat Marked as Unread");
                    Extras.logwriter("Chat Marked as Unread");
                }
            }
            System.out.println("Seed Cache done ---------------");
            Extras.logwriter("Seed Cache Done ---------------");
        }catch (Exception e){
            System.out.println( "Seed Cache error : "+ e.getMessage());
            Extras.logwriter("Seed Cache error : "+ e.getMessage());
        }
    }
}
