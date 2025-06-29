package org.Tweakio.WhatsappWeb.BrowserManager;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Mouse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ThreadLocalRandom;

public class Browser {
    static public boolean HEADLESS = false;
    private static volatile Playwright playwright;
    private static final Map<String, Browser> instances = new ConcurrentHashMap<>();
    private static final Set<Page> pageManager = new CopyOnWriteArraySet<>(); // Future refences Ready page Manager
    private static final Map<String , BrowserContext> browserMap = new ConcurrentHashMap<>();

    private final BrowserContext context;
    public String profile;

    private Browser(String profileName, boolean attachToCDP /*, String proxy */) {
        profile  =  profileName;
        initPlaywright();


        if (attachToCDP) {
            var browser = playwright.chromium().connectOverCDP("http://localhost:9222");
            this.context = browser.contexts().getFirst();
            browserMap.put(profileName, context);

        } else {
            Path userDir = sessionPath(profileName);
            var opts = Scripts.getChromeOptions();

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
            this.context = playwright.chromium().launchPersistentContext(userDir, opts);
            System.out.println("Browser launched successfully.");

            this.context.addInitScript(Scripts.getStealthScript());
            this.context.setExtraHTTPHeaders(Scripts.getHttpHeaders()); // headers
        }
    }

    public static Path sessionPath(String profile) {
        Path p = Paths.get(System.getProperty("user.dir"), "src", "Sessions", profile);
        try {
            Files.createDirectories(p);
        } catch (IOException e) {
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

    public BrowserContext getcontext(){
        return context;
    }

    public static BrowserContext getContextByProfile(String profileName) {
        return browserMap.get(profileName.toLowerCase());
    }


    public Page newPage() {
        Page page ;
        if(context!=null && !context.pages().isEmpty()  && context.pages().getFirst().url().equals("about:blank")) {
            page = context.pages().getFirst();
            pageManager.add(page);
            return page;
        }
        if(context==null) {
            getInstance(profile,false);
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
            } catch (Exception ignored) {
            }
        });
        instances.clear();
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }

//    public static void main(String[] args) throws InterruptedException {
//        Browser b = Browser.getInstance("rohit1", false);
//        Page page = b.newPage();
//        page.bringToFront();
//
//        // First visit to warm up
//        page.navigate("https://google.com");
//        GeneralScraper s = new GeneralScraper(b);
//
//        System.out.println(s.getHtmlWithJsLoaded("https://in.pinterest.com/pin/1071082723882103076/"));
//        page.waitForLoadState(LoadState.NETWORKIDLE);
//        sleep(2000 + new Random().nextInt(3000));
//
//        // Real navigation
//        page.navigate("https://bot.sannysoft.com");
//        sleep(4000 + new Random().nextInt(3000));
//        page.navigate("https://amiunique.org/fingerprint");
//        sleep(4000 + new Random().nextInt(3000));
//    }
}



