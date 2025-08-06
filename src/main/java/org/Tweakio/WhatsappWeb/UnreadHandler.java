package org.Tweakio.WhatsappWeb;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.MouseButton;
import com.microsoft.playwright.options.WaitForSelectorState;

public class UnreadHandler {

    public Integer getUnreadCountOrNull(Locator chatRow) {
        try {
            if (chatRow.locator("span[aria-label='Unread']").isVisible()) {
                return 0;
            }

            Locator badge = chatRow.locator("span[aria-label*='unread message']").first();
            if (badge.count() == 0) return null;

            String aria = badge.getAttribute("aria-label"); // e.g. "4 unread messages"
            if (aria != null && aria.matches("^\\d+.*")) {
                return Integer.parseInt(aria.replaceAll("\\D.*", ""));
            }

            return Integer.parseInt(badge.textContent().trim());

        } catch (NumberFormatException e) {
            Extras.logwriter("Unread number format exception // unreadhandler // getUnreadCountOrNull: " + e.getMessage());
            return null;
        } catch (Exception e) {
            Extras.logwriter("Unexpected error in getUnreadCountOrNull: " + e.getMessage());
            return null;
        }
    }

    public boolean markAsUnread(Page page, Locator chatRow) {
        try {
            chatRow.click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
            Locator menu = page.locator("div[role='application']");

            Locator markAsRead = menu.locator("li[role='button'] span:has-text('Mark as read')");
            if (markAsRead.isVisible()) {
                System.out.println("⚠️ Chat is already unread (has 'Mark as read'). Skipping...");
                return false;
            }

            Locator[] unreadOptions = new Locator[] {
                    menu.locator("li[role='button'] div:has-text('Mark as unread')"),
                    menu.locator("li[role='button'] span:has-text('Mark as unread')"),
                    menu.locator("span[data-icon='chat-unread-refreshed']")
            };

            for (Locator option : unreadOptions) {
                try {
                    option.waitFor(new Locator.WaitForOptions()
                            .setTimeout(1000)
                            .setState(WaitForSelectorState.VISIBLE));
                    if (option.isVisible()) {
                        option.click();
                        System.out.println("✅ Chat marked as unread.");
                        return true;
                    }
                } catch (Exception ignored) {}
            }

            System.out.println("❌ 'Mark as Unread' option not found.");
            Extras.logwriter("markAsUnread not visible // unreadhandler");

        } catch (Exception e) {
            System.out.println("❌ markAsUnread failed: " + e.getMessage());
            Extras.logwriter("markAsUnread failed // unreadhandler: " + e.getMessage());
        }
        return false;
    }
}
