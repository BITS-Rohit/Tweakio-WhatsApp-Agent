package org.Tweakio.WhatsappWeb;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.jetbrains.annotations.NotNull;

import static org.Tweakio.WhatsappWeb.Brain.RefreshTime;
import static org.Tweakio.WhatsappWeb.Brain.debugMode;
//            messageContainer.click(new ElementHandle.ClickOptions().setButton(MouseButton.RIGHT));
// This Right Click Triggers the context menu of
//  {
//  Group Info , Clear Chat , Exit chat , block , mute ,disappering ,  favorites etc.
//  }


public class ReplyHandle {
    private final Extras extras;
    private final Page page;
    private static final String INPUT_BOX_SELECTOR = "div[role='textbox'][aria-label='Type a message']";

    public ReplyHandle(Page page) {
        this.extras = new Extras();
        this.page = page;
    }

    public void replyToChat(
            ElementHandle chat,
            ElementHandle message,
            String reply,
            String time,
            String sender,
            long cmdTime
    ) {
        try {
            chat.hover();
            chat.click();

            ElementHandle msgContainer = message
                    .evaluateHandle("el => el.closest(\"div[data-id]\")")
                    .asElement();
            if (msgContainer == null) {
                System.err.println("⚠️ Could not find message container");
                Extras.logwriter("Could not find message container // replyhandle // replyToChat");
                return;
            }

            msgContainer.hover();
            msgContainer.click();

            // Try edge clicks to trigger input open
            if (!tryEdgeDoubleClick(msgContainer, 10)) {
                tryEdgeDoubleClick(msgContainer, -10);
            }

            // Final fallback: force double click
            msgContainer.hover();
            msgContainer.click(new ElementHandle.ClickOptions()
                    .setClickCount(2)
                    .setForce(true)
            );
            page.waitForTimeout(300);

            Locator inputBox = page.locator(INPUT_BOX_SELECTOR);
            inputBox.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(2000)
            );

            String raw = Extras.bootime(cmdTime).replaceAll("\\D+", "");
            String payload = getFormattedReply(reply, raw);

            inputBox.hover();
            inputBox.click();
            inputBox.fill(""); // clear existing
            inputBox.fill(payload);

            System.out.println("----------------- \n" + payload + "\n---------------------");
            extras.sleep(1000);

            page.keyboard().press("Enter");
            System.out.println("✅ Replied via double-click: " + reply);

            extras.sleep(RefreshTime);

        } catch (Exception e) {
            if (debugMode) System.err.println("Error in replyToChat: " + e.getMessage());
            Extras.logwriter("Error in replyToChat // replyhandler : Fallback : " + e.getMessage());
            replyBack(chat, reply, time, sender, cmdTime);
        }
    }

    /**
     * Attempts to double-click near one horizontal edge of the element.
     * Returns true if input becomes visible.
     */
    private boolean tryEdgeDoubleClick(ElementHandle el, double offsetX) {
        el.scrollIntoViewIfNeeded();
        BoundingBox box = el.boundingBox();
        if (box == null) return false;

        double x = offsetX >= 0 ? box.x + offsetX : box.x + box.width + offsetX;
        double y = box.y + (box.height / 2);

        page.mouse().move(x, y);
        page.mouse().dblclick(x, y, new Mouse.DblclickOptions().setDelay(50));
        page.waitForTimeout(300);

        return page.locator(INPUT_BOX_SELECTOR).isVisible();
    }

    /**
     * Builds the reply message with execution time.
     */
    @NotNull
    private static String getFormattedReply(String reply, String rawTime) {
        int secs;
        try {
            secs = Integer.parseInt(rawTime);
        } catch (NumberFormatException ex) {
            secs = 0;
        }
        secs += 1;
        String elapsed = secs + " sec";

        return "---------------\n"
                + "Command Execute Time: " + elapsed + "\n"
                + reply + "\n"
                + "---------------";
    }

    public void replyBack(ElementHandle chat, String replyMessage, String time, String sender, long cmdTime) {
        try {
            if (replyMessage == null || replyMessage.isEmpty()) {
                replyMessage = "Answer Not Found for this";
            }

            chat.click();
            extras.sleep(200);

            Locator inputBox = page.locator(INPUT_BOX_SELECTOR);
            inputBox.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(2000)
            );

            inputBox.click();
            inputBox.fill(""); // clear first

            String cmdBoot = Extras.bootime(cmdTime).replaceAll("\\D+", "");
            int elapsed = 0;
            try {
                elapsed = Integer.parseInt(cmdBoot) + 1;
            } catch (NumberFormatException ignored) {}

            String sb = "------------------\n" +
                    "Sender/Chat Number : " + sender + "\n" +
                    "Command Recieved at : " + time + "\n" +
                    "Command Execution Time : " + elapsed + "sec\n" +
                    "------------------\n" +
                    replyMessage;

            inputBox.fill(sb);
            page.keyboard().press("Enter");

            System.out.printf("📤 Replied (fallback): \"%s\"%n", replyMessage);
        } catch (Exception e) {
            if (debugMode) System.err.println("❌ replyBack() failed: " + e.getMessage());
            Extras.logwriter("replyBack() failed // replyhandler : " + e.getMessage());
        }
    }
}


//public void reactToMessage(ElementHandle messageContainer, String emojiTitle) {
//    // 1) Hover to surface the reaction button
//    messageContainer.hover(new ElementHandle.HoverOptions().setForce(true));
//    page.waitForTimeout(100);
//
//    // 2) Click the reaction (smiley) icon
//    Locator reactBtn = page.locator("div[data-id='" + messageContainer.getAttribute("data-id") + "'] " +
//                                    "span[data-icon='reaction']");
//    reactBtn.click(new Locator.ClickOptions().setForce(true));
//    page.waitForTimeout(100);
//
//    // 3) Pick your emoji by title (e.g. "😂", "❤️", etc.)
//    //    These six quick-picks are rendered as <button title="😂">…</button>
//    Locator emoji = page.locator("div[role='menu'] button[title='" + emojiTitle + "']");
//    emoji.click(new Locator.ClickOptions().setForce(true));
//}

// Second Reply to chat but less stable
//public void replyToChat2(ElementHandle chat, ElementHandle message, String reply, String time, String sender, long cmdTime) {
//    try {
//        chat.click();
//        ElementHandle messageContainer = message.evaluateHandle("el => el.closest('div[data-id]')").asElement();
//        if (messageContainer == null) {
//            System.err.println("⚠️ Could not find message container");
//            return;
//        }
//
//        //  Convert data-id to use in selector
//        String dataId = messageContainer.getAttribute("data-id");
//        if (dataId == null) {
//            System.err.println("⚠️ Could not read data-id from message container");
//            return;
//        }
//
//        messageContainer.hover(new ElementHandle.HoverOptions().setForce(true));
//
//        BoundingBox box = messageContainer.boundingBox(); // Human like hover
//        if (box == null) {
//            System.err.println("⚠️ Could not get bounding box");
//            return;
//        }
//
//
//        double startX = box.x + box.width * 0.5;
//        double startY = box.y + box.height * 0.5;
//        double hoverX = box.x + box.width * 0.85;
//        double hoverY = box.y + box.height * 0.15;
//
//        page.mouse().move(startX, startY);
//        page.mouse().move(hoverX, hoverY, new Mouse.MoveOptions().setSteps(20));
//
//        //  Locate chevron via Locator and wait
//        Locator chevron = page.locator("div[data-id='" + dataId + "'] span[data-icon='ic-chevron-down-menu']");
//        chevron.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(2_000));
//        page.waitForTimeout(200);
//        chevron.click(new Locator.ClickOptions().setForce(true));
//
//        //  Reply button
//        Locator replyBtn = page.locator("li[role='button']:has-text('Reply')");
//        replyBtn.waitFor(new Locator.WaitForOptions().setTimeout(2_000).setState(WaitForSelectorState.VISIBLE));
//        replyBtn.click();
//
//        //  Input and send
//        Locator inputBox = page.locator("div[aria-label='Type a message'][role='textbox']");
//        inputBox.waitFor(new Locator.WaitForOptions().setTimeout(2_000).setState(WaitForSelectorState.VISIBLE));
//        String cmdBoot = Extras.bootime(cmdTime);
//        String sb = "---------------" +
//                "\n" +
//                "Command Execute Time : " + cmdBoot + " sec" + "\n" + reply +
//                "\n" +
//                "---------------";
//        inputBox.fill(""); // Clear Previous Message
//        inputBox.fill(sb);
//        page.keyboard().press("Enter");
//
//        System.out.println("✅ Replied: " + reply);
//        extras.sleep(RefreshTime);
//
//    } catch (Exception e) {
//        if (debugMode) System.out.println("Error 💀💀 : " + e.getMessage());
//        replyBack(chat, reply, time, sender, cmdTime);
//    }
//}
