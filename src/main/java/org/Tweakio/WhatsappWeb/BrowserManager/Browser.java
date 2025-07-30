package org.Tweakio.WhatsappWeb.BrowserManager;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.BoundingBox;
import org.Tweakio.WhatsappWeb.Extras;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Browser {
    static public boolean HEADLESS = false;
    private static volatile Playwright playwright;
    private static final Map<String, Browser> instances = new ConcurrentHashMap<>();
    private static final Map<String, BrowserContext> browserMap = new ConcurrentHashMap<>();

    private final BrowserContext context;
    public String profile;
    static Random rand = new Random();

    private Browser(String profileName, boolean attachToCDP /*, String proxy */) {
        this.profile = profileName;
        initPlaywright();


        if (attachToCDP) {
            var browser = playwright.chromium().connectOverCDP("http://localhost:9222");
            this.context = browser.contexts().get(0);
            browserMap.put(profileName, context);

        } else {
            Path userDir = sessionPath(profileName);
            BrowserType.LaunchPersistentContextOptions options = Scripts.getChromeOptions();
            // Add proxy if specified
//            if (proxy != null && !proxy.isEmpty()) {
//                String[] proxyParts = proxy.split(":");
//                options.setProxy(new Proxy(proxyParts[0] + ":" + proxyParts[1]));
//            }

            //Example proxy list (format: "host:port")
            //       List<String> proxyList = List.of(
            //                "192.168.1.1:8080",
            //                "192.168.1.2:8080",
            //                "192.168.1.3:8080"
            //        );
            System.out.println("Launching browser with profile: " + userDir);
            this.context = playwright.chromium().launchPersistentContext(userDir, options);
            System.out.println("Browser launched successfully.");

            this.context.addInitScript(Scripts.getStealthScript());
            this.context.addInitScript(Scripts.mouseUI); // Mouse UI script.
            this.context.setExtraHTTPHeaders(Scripts.getHttpHeaders()); // headers
        }
    }

    public static Path sessionPath(String profile) {
        Path p = Paths.get(System.getProperty("user.dir"), "src", "Sessions", profile);
        try {
            Files.createDirectories(p);
        } catch (IOException e) {
            Extras.logwriter("Error creating session directory //Browser : " + e.getMessage());
            throw new RuntimeException("Unable to create session dir", e);
        }
        return p;
    }

    private static synchronized void initPlaywright() {
        if (playwright == null) {
            playwright = Playwright.create();
        }
    }

    /**
     * Thread‐safe factory: reuses one Browser per profileName.
     */
    public static Browser getInstance(String profileName, boolean attachToCDP) {
        String key = profileName.toLowerCase();
        return instances.computeIfAbsent(key, k -> new Browser(k, attachToCDP));
    }

    public BrowserContext getcontext() {
        return context;
    }

    public static BrowserContext getContextByProfile(String profileName) {
        return browserMap.get(profileName.toLowerCase());
    }


    public Page newPage() {
        Page page;
        if (context != null && !context.pages().isEmpty() && context.pages().get(0).url().equals("about:blank")) {
            page = context.pages().get(0);
            return page;
        }
        if (context == null) {
            getInstance(profile, false);
            return newPage();
        }
        page = context.newPage();
        humanize(page);
        return page;
    }

    private void humanize(Page page) {
        page.onLoad(p -> {
            int moves = ThreadLocalRandom.current().nextInt(1, 4);
            for (int i = 0; i < moves; i++) {
                p.mouse().move(
                        ThreadLocalRandom.current().nextInt(0, 400),
                        ThreadLocalRandom.current().nextInt(0, 400),
                        new Mouse.MoveOptions().setSteps(ThreadLocalRandom.current().nextInt(10, 50))
                );
                sleep(ThreadLocalRandom.current().nextInt(100, 600));
            }
        });
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }

    public void closeContext() {
        try {
            context.close();
        } catch (Exception ignored) {
        }
    }

    /**
     * Close *all* contexts & the Playwright engine.
     */
    public static synchronized void closeAll() {
        instances.values().forEach(b -> {
            try {
                b.context.close();
            } catch (Exception ignored) {}
        });
        instances.clear();
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }

    public static void smartFill(Page page, Locator locator, String text) {
        smartHover(page, locator);
        locator.click();
        sleep(700);
        page.keyboard().press("Control+A");
        sleep(700);
        page.keyboard().press("Delete");

        for (char c : text.toCharArray()) {
            String key = String.valueOf(c);
            page.keyboard().press(key);
            sleep(100 + rand.nextInt(200));

            // Simulate typo + correction
            if (rand.nextInt(0, 10) < 2) {
                page.keyboard().press("Backspace");
                sleep(80 + rand.nextInt(100));
                page.keyboard().press(key);
            }
        }

        if (rand.nextInt(0, 5) == 0) {
            page.evaluate("document.activeElement.blur()");
            sleep(300 + rand.nextInt(400));
        }
    }


    public static void smartHover(Page page, Locator locator) {
        sleep(300 + rand.nextInt(500));
        HumanLikeHover(page, locator, 1000);
        sleep(300 + rand.nextInt(300));
    }

    public static void HumanLikeHover(Page page, Locator locator, int durationMs) {
        page.bringToFront();
        locator.scrollIntoViewIfNeeded();
        ElementHandle handle = locator.elementHandle();
        if (handle == null) throw new RuntimeException("Element is not attached to the DOM");

        BoundingBox box = handle.boundingBox();
        if (box == null) throw new RuntimeException("Bounding box is null, element might be invisible");

        double targetX = box.x + box.width / 2;
        double targetY = box.y + box.height / 2;

        double startX = rand.nextInt(100);
        double startY = rand.nextInt(100);
        page.mouse().move(startX, startY);

        int steps = 30;
        int delayPerStep = durationMs / steps;

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double cx1 = startX + rand.nextInt(80,100);
            double cy1 = startY + rand.nextInt(40,50);
            double cx2 = targetX - rand.nextInt(80,100);
            double cy2 = targetY - rand.nextInt(40,50);

            double x = Math.pow(1 - t, 3) * startX + 3 * Math.pow(1 - t, 2) * t * cx1 + 3 * (1 - t) * Math.pow(t, 2) * cx2 + Math.pow(t, 3) * targetX;
            double y = Math.pow(1 - t, 3) * startY + 3 * Math.pow(1 - t, 2) * t * cy1 + 3 * (1 - t) * Math.pow(t, 2) * cy2 + Math.pow(t, 3) * targetY;

            page.mouse().move(x, y);
            sleep(delayPerStep);
        }

        for (int j = 0; j < 2; j++) {
            double dx = rand.nextInt(-3, 4);
            double dy = rand.nextInt(-3, 4);
            page.mouse().move(targetX + dx, targetY + dy);
            sleep(100 + rand.nextInt(100));
        }
    }
}



