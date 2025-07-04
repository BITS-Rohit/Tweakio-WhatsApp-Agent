package org.Tweakio.WhatsappWeb;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
//import com.sun.net.httpserver.HttpServer;
import org.Tweakio.WhatsappWeb.BrowserManager.Browser;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.Stream;

public class WebLogin {
    private static final String WEB_URL = "https://web.whatsapp.com";
//    private static final Path QR_SAVE_PATH = Paths.get("src/main/java/org/bot/FilesSaved/qr_code.png");

    private final Path USER_DATA_DIR;

    private final Page page;
    Extras e = new Extras();

//    private HttpServer qrServer;
    private volatile boolean sessionMonitorRunning = false;

    /**
     * Constructs or reuses the WhatsApp login page using GetPage singleton.
     */
    public WebLogin(Browser browser) {
        page = browser.newPage();
        USER_DATA_DIR = Browser.sessionPath(browser.profile);
    }

    /**
     * Navigate to WhatsApp Web and either save QR or detect a logged-in session.
     */
    public boolean webLogin() {
        if (!page.url().equals(WEB_URL)) {
            page.navigate(WEB_URL);
            Extras.logwriter("Web Login naviagtion // weblogin  : "+page.url());
        }

        try {
            page.waitForLoadState(LoadState.NETWORKIDLE);
            e.sleep(1500 + new Random().nextInt(500));

            Locator qr = page.locator("canvas[role='img']");
            if (qr.isVisible()) {
                System.out.println("📷 QR code detected.");
                System.out.println("ℹ️ You may scan the QR OR enter login code on your phone.");
                Extras.logwriter("Qr code detected. && waiting for sign in // weblogin  ");
            } else {
                System.out.println("ℹ️ No QR code visible — waiting for login via other method.");
                Extras.logwriter("No QR code visible <UNK> waiting for login via other method. // weblogin  ");
            }

            // Always wait for successful login regardless of method
            if (waitForLoginSuccess()) {
                System.out.println("✅ Login successful.");
                Extras.logwriter("Login successful. // weblogin  ");
                return true;
            } else {
                System.out.println("❌ Login failed or timeout.");
                Extras.logwriter("Login failed. // weblogin  ");
                return false;
            }

        } catch (PlaywrightException e) {
            System.out.println("❌ Login failed (Playwright exception): " + e.getMessage());
            Extras.logwriter("Login failed (Playwright exception) // weblogin : " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Login failed (Exception): " + e.getMessage());
            Extras.logwriter("Login failed (Exception) // weblogin : " + e.getMessage());
        }

        return false;
    }


    /**
     * Waits for the WhatsApp chat textbox to appear, indicating login success.
     */

    public boolean waitForLoginSuccess() {
        try {
            page.waitForLoadState(LoadState.NETWORKIDLE,
                    new Page.WaitForLoadStateOptions().setTimeout(300_000));
            page.waitForSelector("div[role='textbox'] > p.selectable-text.copyable-text",
                    new Page.WaitForSelectorOptions().setTimeout(300_000));

//            if (qrServer != null) qrServer.stop(0);
            return true;
        } catch (PlaywrightException e) {
            System.out.println("❌ Login timeout or failed: " + e.getMessage());
            Extras.logwriter("Login timeout or failed // weblogin //waitforloginsuccess : " + e.getMessage());
        }
        return false;
    }

    /**
     * Periodically checks for an expired QR and clears session files if needed.
     */
    private void deleteBrokenSession() {
        try {
            Locator qr = page.locator("canvas[aria-label*='Scan this QR code']");
            if (qr.isVisible()) {
                System.out.println("🔄 Session expired—resetting...");

                try (Stream<Path> paths = Files.walk(USER_DATA_DIR)) {
                    paths.sorted(Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException ignored) {
                                }
                            });
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ Error checking broken session: " + e.getMessage());
            Extras.logwriter("Error checking broken session //weblogin // deleltebrokensession : " + e.getMessage());
        }
    }

    /**
     * Launches a background thread that resets session if QR reappears.
     */
    public void startSessionMonitor() {
        sessionMonitorRunning = true;
        new Thread(() -> {
            while (sessionMonitorRunning) {
                e.sleep(60_000);
                deleteBrokenSession();
            }
            System.out.println("🛑 Session monitor stopped.");
            Extras.logwriter("Session monitor stopped. // weblogin  // startsessionmonitor");
        }, "SessionMonitor").start();
    }

    public Page getPage() {
        return page;
    }

    public void shutdown() {
        sessionMonitorRunning = false;
//        if (qrServer != null) qrServer.stop(0);
        if (!page.isClosed()) page.close();
    }
}

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
