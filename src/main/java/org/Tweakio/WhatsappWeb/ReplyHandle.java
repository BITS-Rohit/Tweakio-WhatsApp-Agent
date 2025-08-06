package org.Tweakio.WhatsappWeb;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.jetbrains.annotations.NotNull;

import static org.Tweakio.WhatsappWeb.Brain.RefreshTime;
import static org.Tweakio.WhatsappWeb.Brain.debugMode;

public class ReplyHandle {
    private final Extras extras = new Extras();
    private final Page page;
    private static final String INPUT_BOX = "div[role='textbox'][aria-label='Type a message']";

    public ReplyHandle(Page page) {
        this.page = page;
    }

    /**
     * Unified reply method:
     * 1) try the inline “double-click” reply
     * 2) if inline fails, fall back to chat-click + type reply
     */
    public void replyToChat(
            ElementHandle chat,
            ElementHandle message,
            String reply,
            String time,
            String sender,
            long cmdTime
    ) {
        // First, build both payloads up front
        String raw = Extras.bootime(cmdTime).replaceAll("\\D+", "");
        String inlinePayload = formatInlineReply(reply, raw);
        String fallbackPayload = formatFallbackReply(reply, time, sender, raw);

        // Step A: try inline flow
        try {
            chat.hover(); chat.click(); page.waitForTimeout(100);

            ElementHandle container = message
                    .evaluateHandle("el => el.closest(\"div[data-id]\")")
                    .asElement();
            if (container == null) throw new RuntimeException("No message container");

            // edge‐offset then forced double‐click
            if (!tryEdgeDoubleClick(container, +10)) {
                tryEdgeDoubleClick(container, -10);
            }
            container.click(new ElementHandle.ClickOptions().setClickCount(2).setForce(true));
            page.waitForTimeout(300);

            // if the inline input never shows, that’s a failure
            Locator inlineBox = page.locator(INPUT_BOX);
            inlineBox.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(2_000));

            // send inline reply
            fillAndEnter(inlinePayload);
            System.out.println("✅ Replied (inline): " + reply);
            extras.sleep(RefreshTime);
            return;  // success, skip fallback

        } catch (Exception e) {
            // inline failed — go to fallback
            if (debugMode) System.err.println("Inline reply failed: " + e.getMessage());
            Extras.logwriter("Inline reply failed, falling back: " + e.getMessage());
        }

        // Step B: fallback flow
        try {
            chat.click(); extras.sleep(200);
            fillAndEnter(fallbackPayload);
            System.out.printf("📤 Replied (fallback): \"%s\"%n", reply);
        } catch (Exception e) {
            if (debugMode) System.err.println("Fallback reply failed: " + e.getMessage());
            Extras.logwriter("Fallback reply failed: " + e.getMessage());
        }
    }

    // ------------------------------------
    // Helpers reused from your code above
    // ------------------------------------

    private boolean tryEdgeDoubleClick(ElementHandle el, double offsetX) {
        el.scrollIntoViewIfNeeded();
        BoundingBox box = el.boundingBox();
        if (box == null) return false;
        double x = offsetX >= 0 ? box.x + offsetX : box.x + box.width + offsetX;
        double y = box.y + box.height / 2;
        page.mouse().move(x, y);
        page.mouse().dblclick(x, y, new Mouse.DblclickOptions().setDelay(50));
        page.waitForTimeout(300);
        return page.locator(INPUT_BOX).isVisible();
    }

    private void fillAndEnter(String text) {
        Locator input = page.locator(INPUT_BOX);
        input.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(2_000));
        input.click();
        input.fill("");
        input.fill(text);
        page.keyboard().press("Enter");
    }

    @NotNull
    private static String formatInlineReply(String reply, String rawTime) {
        int secs;
        try { secs = Integer.parseInt(rawTime); }
        catch (NumberFormatException ex) { secs = 0; }
        secs += 1;
        return "---------------\n"
                + "Command Execute Time: " + secs + " sec\n"
                + reply + "\n"
                + "---------------";
    }

    @NotNull
    private static String formatFallbackReply(
            String reply, String time, String sender, String rawTime
    ) {
        if (reply == null || reply.isEmpty()) {
            reply = "Answer Not Found for this";
        }
        int secs;
        try { secs = Integer.parseInt(rawTime); }
        catch (NumberFormatException ex) { secs = 0; }
        secs += 1;
        return "------------------\n"
                + "Sender/Chat Number : " + sender + "\n"
                + "Command Received at : " + time + "\n"
                + "Command Execution Time : " + secs + "sec\n"
                + "------------------\n"
                + reply;
    }
}
