package org.Tweakio.WhatsappWeb.MediaHandler;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.FileChooser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.Tweakio.WhatsappWeb.Extras;
import org.Tweakio.WhatsappWeb.ReplyHandle;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.Tweakio.WhatsappWeb.MessH_Modified.debugMode;

public class SendMedia {
    ReplyHandle replyHandle;

    public SendMedia(Page page){
        replyHandle= new ReplyHandle(page);
    }


    // Yt Songs Sender
    public void SendFile(
            ElementHandle chat,
            ElementHandle message,
            String reply,
            String time,
            String sender,
            long cmdTime,
            String type,        // "image", "video", "document"
            String filepath,
            Page page
    ) {
        try {
            // 1. Hover + click chat
            chat.hover();
            chat.click();
            page.waitForTimeout(100);

            // 2. Click on the last message to focus
            ElementHandle msgContainer = message
                    .evaluateHandle("el => el.closest(\"div[data-id]\")")
                    .asElement();
            if (msgContainer != null) {
                msgContainer.click(new ElementHandle.ClickOptions().setClickCount(3).setForce(true));
                page.waitForTimeout(300);
            } else {
                System.err.println("⚠️ Could not find message container");
            }

            Locator captionBox = page.locator("div[role='textbox'][aria-label='Type a message']");
            if (captionBox.isVisible()) {
                captionBox.hover();
                captionBox.click();
                captionBox.fill(reply);
            } else {
                System.out.println("ℹ️ TEXTBOX not found or not applicable //Send media.");
            }

            Locator attachBtn = page.locator("button[title='Attach']");
            if (!attachBtn.isVisible()) {
                System.err.println("❌ Could not find Attach button");
                return;
            }
            attachBtn.hover();
            attachBtn.click();
            page.waitForTimeout(400);


            Locator targetOption = switch (type.toLowerCase()) {
                case "image", "video" -> page.locator("li:has(span:has-text('Photos & videos'))");
                default -> page.locator("li:has(span:has-text('Document'))");
            };

            if (!targetOption.isVisible()) {
                System.err.println("❌ Media type button not visible for: " + type);
                return;
            }
            FileChooser chooser = page.waitForFileChooser(targetOption::click);
            Path filePath = Paths.get(filepath);
            if (!Files.exists(filePath)) {
                System.err.println("❌ Cannot upload: file does not exist at " + filepath);
                return;
            }
            chooser.setFiles(filePath);
            page.waitForTimeout(600);

            page.keyboard().press("Enter");
            page.waitForTimeout(300);

            String elapsed = Extras.bootime(cmdTime) + " sec";
            System.out.println("✅ Sent " + type +
                    " with caption: " + reply +
                    "\nTo: " + sender +
                    " at: " + time +
                    "\n⏱ Command executed in: " + elapsed);

        } catch (Exception e) {
            if (debugMode) System.err.println("❌ Error in SendFile: " + e.getMessage());
            replyHandle.replyToChat(chat, message, reply, time, sender, cmdTime);
        }
    }

}
