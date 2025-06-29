package org.Tweakio.WhatsappWeb;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.MouseButton;
import com.microsoft.playwright.options.WaitForSelectorState;

public class UnreadHandler {


    public Integer getUnreadCountOrNull(Locator chatRow) {
        Locator unread = chatRow.locator("span[aria-label='Unread']");
        if(unread.isVisible()){
            return 0;
        }
        Locator badge = chatRow.locator("span[aria-label*='unread message']").first();
        if (badge.count() == 0) return null;

        String aria = badge.getAttribute("aria-label"); // e.g. "4 unread messages"
        if (aria != null && aria.matches("^\\d+.*")) {
            try {
                return Integer.parseInt(aria.replaceAll("\\D.*", ""));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        try {
            return Integer.parseInt(badge.textContent().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }




    public boolean markAsUnread(Page page, Locator chatRow) {
        try {
            chatRow.click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));

            Locator menu = page.locator("div[role='application']");
            // Check if "Mark as read" is present
            Locator markAsRead = menu.locator("li[role='button'] > span:has-text(\"Mark as read\")");
            if (markAsRead.isVisible()) {
                System.out.println("⚠️ Chat is already unread (has 'Mark as read' option). Skipping...");
                return false; // No need to mark as unread again
            }

            Locator markAsRead2 = menu.locator("li[role='button'] > div:has-text(\"Mark as read\")");
            if(markAsRead2.isVisible()){
                markAsRead2.click();
                return true;
            }

            Locator markAsUnread2 = menu.locator("li[role='button'] > div:has-text(\"Mark as unread\")");
            if(markAsUnread2.isVisible()){
                markAsUnread2.click();
                return true;
            }

            // Proceed to find and click "Mark as Unread"
            Locator markAsUnread = menu.locator("span[data-icon='chat-unread-refreshed']");
            Locator markunread2 = menu.locator("li[role='button'] > span:has-text(\"Mark as unread\")");
            markAsUnread.waitFor(new Locator.WaitForOptions()
                    .setTimeout(2000)
                    .setState(WaitForSelectorState.VISIBLE));

            if (markAsUnread.isVisible()) {
                markAsUnread.click();
                System.out.println("✅ Chat marked as unread.");
                return true;
            } else {
                if(markunread2.isVisible()) {
                    markunread2.click();
                    System.out.println(" Chat marked as unread with selector 2 .");
                    return true;
                }
                System.out.println("❌ 'Mark as Unread' option not visible.");
            }
        } catch (Exception e) {
            System.out.println("❌ markAsUnread failed: " + e.getMessage());
        }
        return false;
    }

}
