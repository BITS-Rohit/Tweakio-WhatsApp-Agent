package org.Tweakio.WhatsappWeb;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.FileChooser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.Tweakio.WhatsappWeb.Brain.debugMode;

public class SendMedia {
    private final Page page;
    private final ReplyHandle replyHandle;

    public SendMedia(Page page) {
        this.page = page;
        this.replyHandle = new ReplyHandle(page);
    }

    public void SendFile(
            ElementHandle chat,
            ElementHandle message,
            String reply,
            String time,
            String sender,
            long cmdTime,
            String type,        // "image", "video", "document", "audio"
            String filepath
    ) {
        try {
            focusChat(chat, message);
            fillCaption(reply);

            // Open the + menu
            Locator attachBtn = page.locator("button[title='Attach']");
            if (!attachBtn.isVisible()) {
                System.err.println("❌ Attach button not found");
                return;
            }
            attachBtn.click();
            page.waitForTimeout(400);

            // Click the correct option and fire the chooser
            Locator option = uploadOption(type);
            if (!option.isVisible()) {
                System.err.printf("❌ Upload option not visible for '%s'%n", type);
                return;
            }
            FileChooser chooser = page.waitForFileChooser(option::click);

            // Ensure file exists & upload
            Path path = Paths.get(filepath);
            if (!Files.exists(path)) {
                System.err.printf("❌ File does not exist at %s%n", filepath);
                return;
            }
            chooser.setFiles(path);

            // Same timing as Java version for full‐preview
            page.waitForTimeout(600);
            page.keyboard().press("Enter");
            page.waitForTimeout(300);
            page.keyboard().press("Enter");

            logSuccess(type, reply, sender, time, cmdTime);

        } catch (Exception e) {
            if (debugMode) System.err.println("❌ Error in sendFile: " + e.getMessage());
            Extras.logwriter("Error in SendMedia.sendFile: " + e.getMessage());
            replyHandle.replyToChat(chat, message, reply, time, sender, cmdTime);
        }
    }

    private void focusChat(ElementHandle chat, ElementHandle message) {
        chat.hover();
        chat.click();
        page.waitForTimeout(100);

        ElementHandle container = message
                .evaluateHandle("el => el.closest(\"div[data-id]\")")
                .asElement();
        if (container != null) {
            container.click(new ElementHandle.ClickOptions().setClickCount(3).setForce(true));
            page.waitForTimeout(300);
        } else {
            System.err.println("⚠️ Could not find message container");
        }
    }

    // Fill the message caption box if it's visible
    private void fillCaption(String reply) {
        Locator box = page.locator("div[role='textbox'][aria-label='Type a message']");
        if (box.isVisible()) {
            box.click();
            box.fill(reply);
        }
    }

    // Choose between Photos & videos, Document, or Audio
    private Locator uploadOption(String type) {
        return switch (type.toLowerCase()) {
            case "image", "video" ->
                    page.getByRole(AriaRole.BUTTON,
                            new Page.GetByRoleOptions().setName("Photos & videos"));
            case "audio" ->
                    page.getByRole(AriaRole.BUTTON,
                            new Page.GetByRoleOptions().setName("Audio"));
            default ->
                    page.getByRole(AriaRole.BUTTON,
                            new Page.GetByRoleOptions().setName("Document"));
        };
    }

    private void logSuccess(
            String type,
            String reply,
            String sender,
            String time,
            long cmdTime
    ) {
        String elapsed = Extras.bootime(cmdTime) + " sec";
        System.out.printf(
                "✅ Sent %s with caption ‘%s’ to %s at %s (⏱ %s)%n",
                type, reply, sender, time, elapsed
        );
    }
}
