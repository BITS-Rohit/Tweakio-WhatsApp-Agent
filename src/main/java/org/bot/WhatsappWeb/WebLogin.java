package org.bot.WhatsappWeb;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;

public class WebLogin {
    private static final boolean SetHeadless = true;
    private static final String WEB_URL = "https://web.whatsapp.com";
    private static final Path USER_DATA_DIR = Paths.get(System.getProperty("user.dir"), "UserSessions", "User1", "WhatsApp_Session");
    private static final Path QR_SAVE_PATH = Paths.get("src/main/java/org/bot/FilesSaved/qr_code.png");

    private Playwright playwright;
    private BrowserContext context;
    private Page page;
    private HttpServer qrServer;
    private volatile boolean sessionMonitorRunning = false;


    public WebLogin() {
        try {
            init();
        } catch (IOException e) {
            throw new RuntimeException("⚠️ Failed to initialize WebLogin", e);
        }
    }

    public void init() throws IOException {
        Files.createDirectories(USER_DATA_DIR);
        playwright = Playwright.create();

        BrowserType.LaunchPersistentContextOptions options = new BrowserType.LaunchPersistentContextOptions()
                .setHeadless(SetHeadless)
                .setViewportSize(null) // Smaller size saves memory
                .setLocale("en-US")
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "Chrome/121.0.0.0 Safari/537.36")
                .setArgs(List.of(
                        "--disable-dev-shm-usage",
                        "--disable-extensions",
                        "--no-sandbox",
                        "--disable-gpu",
                        "--disable-background-networking",
                        "--disable-default-apps",
                        "--disable-sync",
                        "--disable-translate",
                        "--metrics-recording-only",
                        "--mute-audio",
                        "--no-first-run",
                        "--no-default-browser-check",
                        "--disable-popup-blocking",
                        "--disable-background-timer-throttling",
                        "--disable-features=site-per-process,TranslateUI,BlinkGenPropertyTrees",
                        "--disable-component-update"
                ));


        context = playwright.chromium().launchPersistentContext(USER_DATA_DIR, options);
        context.addInitScript(
                "Object.defineProperty(navigator, 'webdriver', {get: () => undefined});" +
                        "window.chrome = { runtime: {} };" +
                        "Object.defineProperty(navigator, 'languages', {get: () => ['en-US', 'en']});" +
                        "Object.defineProperty(navigator, 'plugins', {get: () => [1, 2, 3, 4, 5]});"
        );

        if (context.pages().isEmpty()) {
            page = context.newPage();
        } else {
            page = context.pages().getFirst();
        }

        page.navigate(WEB_URL);
    }

    public boolean webLogin() throws InterruptedException {

        try {
            Locator qr = page.locator("canvas[aria-label='Scan this QR code to link a device!']");
            qr.waitFor(new Locator.WaitForOptions().setTimeout(8000));
            byte[] qrImage = qr.screenshot();
            Files.createDirectories(QR_SAVE_PATH.getParent());
            Files.write(QR_SAVE_PATH, qrImage);
            System.out.println("✅ QR code saved to: " + QR_SAVE_PATH.toAbsolutePath());
            System.out.println("📲 Ask the user to scan via http://localhost:8080/qr");
            startQrServer();
        } catch (Exception e) {
            Thread.sleep(1000);
            if (waitForLoginSuccess()) {
                startSessionMonitor();
                System.out.println("✅ Logged in ");
                return true;
            } else {
                System.out.println("❌ QR not found and login failed.");

            }
        }
        return false;
    }


    private void startQrServer() throws IOException {
        if (qrServer != null) qrServer.stop(0);
        qrServer = HttpServer.create(new InetSocketAddress(8080), 0);
        qrServer.createContext("/qr", exchange -> {
            byte[] img = Files.readAllBytes(QR_SAVE_PATH);
            exchange.getResponseHeaders().set("Content-Type", "image/png");
            exchange.sendResponseHeaders(200, img.length);
            try (var os = exchange.getResponseBody()) {
                os.write(img);
            }
        });
        qrServer.setExecutor(Executors.newSingleThreadExecutor());
        qrServer.start();
        System.out.println("🔗 QR server running at http://localhost:8080/qr");
    }


    public boolean waitForLoginSuccess() {
        try {
            page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(5000));
            page.waitForSelector("div[role='textbox']", new Page.WaitForSelectorOptions().setTimeout(5000));
            if (qrServer != null) qrServer.stop(0);
            return true;
        } catch (PlaywrightException e) {
            System.out.println("❌ Login timeout or failed: " + e.getMessage());
        }
        return false;
    }

    private void handleReloginIfNeeded() {
        try {
            Locator qr = page.locator("canvas[aria-label*='Scan this QR code']");
            if (qr.isVisible()) {
                System.out.println("🔄 Detected session expiration. Resetting login...");

                // Shutdown and delete session
                shutdown();

                // Delete user-data directory safely
                try (var paths = Files.walk(USER_DATA_DIR)) {
                    paths
                            .sorted(Comparator.reverseOrder())   // delete files before dirs
                            .forEach(path -> {
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException ex) {
                                    System.err.println("❌ Failed to delete: " + path);
                                }
                            });
                }

                // Reinitialize and restart login
                init();
                webLogin();
            }
        } catch (Exception e) {
            System.out.println("⚠️ Relogin check failed: " + e.getMessage());
        }
    }
        public boolean isLoggedIn() {
        try {
            page.waitForLoadState(LoadState.NETWORKIDLE);
            return page.locator("div[role='textbox']").isVisible();
        } catch (Exception e) {
            return false;
        }
    }


    public void startSessionMonitor() {
        sessionMonitorRunning = true;
        new Thread(() -> {
            while (sessionMonitorRunning) {
                try {
                    Thread.sleep(60_000);
                    handleReloginIfNeeded();
                } catch (InterruptedException e) {
                    break;
                }
            }
            System.out.println("🛑 Session monitor stopped.");
        }, "SessionMonitor").start();
    }

    public Page getPage() {
        return page;
    }

    public void shutdown() {
        try {
            sessionMonitorRunning = false;
            if (qrServer != null) qrServer.stop(0);
            if (context != null) context.close();
            if (playwright != null) playwright.close();
        } catch (Exception e) {
            System.out.println("⚠️ Error during shutdown: " + e.getMessage());
        }
    }
}

