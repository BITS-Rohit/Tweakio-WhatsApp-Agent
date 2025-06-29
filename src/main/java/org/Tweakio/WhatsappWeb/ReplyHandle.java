package org.Tweakio.WhatsappWeb;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.jetbrains.annotations.NotNull;

import static org.Tweakio.WhatsappWeb.MessH_Modified.RefreshTime;
import static org.Tweakio.WhatsappWeb.MessH_Modified.debugMode;
//            messageContainer.click(new ElementHandle.ClickOptions().setButton(MouseButton.RIGHT));
// This Right Click Triggers the context menu of
//  {
//  Group Info , Clear Chat , Exit chat , block , mute ,disappering ,  favorites etc.
//  }

public class ReplyHandle {
    Extras extras;
    Page page;

    public ReplyHandle(Page page) {
        extras = new Extras();
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
                return;
            }

            msgContainer.hover();
            msgContainer.click();

            tryEdgeDoxubleClick(msgContainer, /* leftOffset= */ 10);
            tryEdgeDoxubleClick(msgContainer, /* rightOffset= */ -10);

            msgContainer.hover();
            msgContainer.click(new ElementHandle.ClickOptions()
                    .setClickCount(2)
                    .setForce(true)
            );
            page.waitForTimeout(300);

            Locator inputBox = page.locator("div[role='textbox'][aria-label='Type a message']");
            inputBox.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(2_000)
            );

            String raw = Extras.bootime(cmdTime).replaceAll("\\D+", "");
            String payload = getString(reply, raw);



            inputBox.hover();
            inputBox.click();
            inputBox.fill(payload);
            System.out.println("----------------- \n"+payload);
            System.out.println("---------------------");
            extras.sleep(1000);
            page.keyboard().press("Enter");


            System.out.println("✅ Replied via double-click: " + reply);
            extras.sleep(RefreshTime);

        } catch (Exception e) {
            if (debugMode) System.err.println("Error in replyToChat: " + e.getMessage());
            replyBack(chat, reply, time, sender, cmdTime);
        }
    }


    /**
     * Attempts to double-click near one horizontal edge of the element.
     * If offsetX is positive, clicks at x = box.x + offsetX.
     * If offsetX is negative, clicks at x = box.x + box.width + offsetX.
     * Returns true if the reply-input becomes visible within 1s.
     */
    private void tryEdgeDoxubleClick(ElementHandle el, double offsetX) {
        el.scrollIntoViewIfNeeded();
        // compute box
        BoundingBox box = el.boundingBox();
        if (box == null) return;

        double x;
        if (offsetX >= 0) {
            x = box.x + offsetX;
        } else {
            x = box.x + box.width + offsetX;
        }
        double y = box.y + (box.height / 2);

        // perform the double-click
        page.mouse().move(x, y);
        page.mouse().dblclick(x, y, new Mouse.DblclickOptions().setDelay(50));
    }


    @NotNull
    private static String getString(String reply, String raw) {
        int secs ;
        try {
            secs = Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            secs=0;
        }
        secs += 1;
        String elapsed = secs + " sec";
        return "---------------\n"
                + "Command Execute Time: " + elapsed + "\n"
                + reply + "\n"
                + "---------------";
    }


    void replyBack(ElementHandle chat, String replyMessage, String time, String sender, long cmdTime) {
        try {
            if (replyMessage == null || replyMessage.isEmpty()) {
                replyMessage = "Answer Not Found for this";
            }
            chat.click();
            extras.sleep(200);

            Locator inputBox = page.locator("div[aria-label='Type a message'][role='textbox']");
            inputBox.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(2_000)
            );

            inputBox.click();
            inputBox.fill("");      // clear any existing text
            StringBuilder sb = new StringBuilder();
            sb.append("------------------");
            sb.append("\n");
            sb.append("Sender/Chat Number : ").append(sender);
            sb.append("\n");
            sb.append("Command Recieved at : ").append(time);
            sb.append("\n");
            String cmdBoot = Extras.bootime(cmdTime);
            cmdBoot = String.valueOf(Integer.parseInt(cmdBoot)+1);
            sb.append("Command Execution Time : ").append(cmdBoot).append("sec");
            sb.append("\n");
            sb.append("------------------");
            sb.append("\n");
            sb.append(replyMessage);
            inputBox.fill(sb.toString());

            page.keyboard().press("Enter");
            System.out.printf("📤 Replied: \"%s\"%n", replyMessage);
        } catch (Exception e) {
            if (debugMode) System.err.println("❌ replyBack() failed: " + e.getMessage());
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
