package org.Tweakio.WhatsappWeb;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.Tweakio.WhatsappWeb.BrowserManager.Browser;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.Stream;

public class WebLogin {
    private static final String WEB_URL = "https://web.whatsapp.com";

    private final Path USER_DATA_DIR;
    private final Page page;
    Extras e = new Extras();

    public WebLogin(Browser browser) {
        this.page = browser.newPage();
        this.USER_DATA_DIR = Browser.sessionPath(browser.profile);
    }

    /**
     * Navigate to WhatsApp Web and either reuse existing session or perform login and save it.
     */
    public boolean webLogin() {
        // Path to storage state file (Playwright stores JSON state)
        Path storageFile = USER_DATA_DIR.resolve("storageState.json");
        try {
            if (Files.exists(storageFile)) {
                if (!page.url().equals(WEB_URL)) page.navigate(WEB_URL);
                System.out.println("✅ Existing session found. Skipping QR login.");
                Extras.logwriter("Existing session detected, skipping login. // weblogin");
                return true;
            }

            // No existing session: perform login flow
            if (!page.url().equals(WEB_URL)) {
                page.navigate(WEB_URL);
                Extras.logwriter("WebLogin navigation: " + page.url());
            }

            // Wait for QR or direct login
            page.waitForLoadState(LoadState.NETWORKIDLE);
            e.sleep(1500 + new Random().nextInt(500));

            Locator qr = page.locator("canvas[role='img']");
            if (qr.isVisible()) {
                System.out.println("📷 QR code detected. Please scan using your phone.");
                Extras.logwriter("QR code detected, waiting for scan. // weblogin");
            } else {
                System.out.println("ℹ️ No QR code visible — waiting for existing session or other login method.");
                Extras.logwriter("No QR visible, waiting for other login. // weblogin");
            }

            if (waitForLoginSuccess()) {
                System.out.println("✅ Login successful. Saving session state.");
                Extras.logwriter("Login successful, saving storage state. // weblogin");
                // Save storage state for future runs
                page.context().storageState(new BrowserContext.StorageStateOptions().setPath(storageFile));
                return true;
            } else {
                System.out.println("❌ Login failed or timed out.");
                Extras.logwriter("Login failed or timeout. // weblogin");
            }
        } catch (PlaywrightException ex) {
            System.out.println("❌ Playwright exception during login: " + ex.getMessage());
            Extras.logwriter("Playwright exception // weblogin: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("❌ Unexpected error during login: " + ex.getMessage());
            Extras.logwriter("Exception // weblogin: " + ex.getMessage());
        }
        return false;
    }

    public boolean waitForLoginSuccess() {
        try {
            page.waitForLoadState(LoadState.NETWORKIDLE,
                    new Page.WaitForLoadStateOptions().setTimeout(300_000));
            page.waitForSelector("div[role='textbox'] > p.selectable-text.copyable-text",
                    new Page.WaitForSelectorOptions().setTimeout(300_000));
            return true;
        } catch (PlaywrightException e) {
            System.out.println("❌ Login timeout or failed: " + e.getMessage());
            Extras.logwriter("Login timeout or failed // waitForLoginSuccess: " + e.getMessage());
        }
        return false;
    }

    /**
     * Cleans up session if QR reappears (optional monitor).
     */
    private void deleteBrokenSession() {
        try {
            Locator qr = page.locator("canvas[aria-label*='Scan this QR code']");
            if (qr.isVisible()) {
                System.out.println("🔄 Session expired — clearing session files.");
                Extras.logwriter("Session expired, clearing files. // websocket login");
                try (Stream<Path> paths = Files.walk(USER_DATA_DIR)) {
                    paths.sorted(Comparator.reverseOrder())
                            .forEach(path -> {
                                try { Files.deleteIfExists(path); } catch (IOException ignored) {}
                            });
                }
            }
        } catch (Exception ex) {
            System.out.println("⚠️ Error checking broken session: " + ex.getMessage());
            Extras.logwriter("Error checking broken session // deleteBrokenSession: " + ex.getMessage());
        }
    }

    public void startSessionMonitor() {
        new Thread(() -> {
            while (true) {
                e.sleep(60_000);
                deleteBrokenSession();
            }
        }, "SessionMonitor").start();
    }

    public Page getPage() {
        return page;
    }

    public void shutdown() {
        if (!page.isClosed()) page.close();
    }
}

// Dont Delete This Code !!
//---------------------------------------
// screenshot & save
//            byte[] img = qr.screenshot();
//            Files.createDirectories(QR_SAVE_PATH.getParent());
//            Files.write(QR_SAVE_PATH, img);
//
//            startQrServer();
//            System.out.println("✅ QR code saved to: " + QR_SAVE_PATH.toAbsolutePath());
//            System.out.println("📲 Ask user to scan at http://localhost:8080/qr");
//---------------------------------------

//    /**
//     * Starts a simple HTTP server to serve the saved QR image.
//     */
//    private void startQrServer() throws IOException {
//        if (qrServer != null) qrServer.stop(0);
//
//        qrServer = HttpServer.create(new InetSocketAddress(8080), 0);
//        qrServer.createContext("/qr", exchange -> {
//            byte[] img = Files.readAllBytes(QR_SAVE_PATH);
//            exchange.getResponseHeaders().set("Content-Type", "image/png");
//            exchange.sendResponseHeaders(200, img.length);
//            try (var os = exchange.getResponseBody()) {
//                os.write(img);
//            }
//        });
//        qrServer.setExecutor(Executors.newSingleThreadExecutor());
//        qrServer.start();
//        System.out.println("🔗 QR server running at http://localhost:8080/qr");
//    }
