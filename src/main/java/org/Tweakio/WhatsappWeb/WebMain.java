package org.Tweakio.WhatsappWeb;

import org.Tweakio.UserSettings.user;
import org.Tweakio.WhatsappWeb.BrowserManager.Browser;

import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebMain {
    private static final Logger logger = Logger.getLogger(WebMain.class.getName());
    private static final int RESTART_DELAY_MS = 2_000;

    public static void main(String[] args) {
        while (true) {
            WebLogin bot = null;
            MessH_Modified monitor = null;
            Thread monitorThread;
            Scanner in = new Scanner(System.in);
            System.out.println("Give profile : ");
            String profileName = in.nextLine();
            in.close();
            Browser browser = Browser.getInstance(profileName,false);
            try {
                // ───────────────────────────────────────────────────────────────────
                //        Initialize WebLogin and restore any previous state
                // ───────────────────────────────────────────────────────────────────
                bot = new WebLogin(browser);
                Map<String, Set<String>> state = MapSerializer.deserialize();
                logger.info("Map Data picked up from file ...");
                monitor = new MessH_Modified(bot, state,browser);

                // Install a shutdown hook so that if the JVM shuts down,
                // we serialize the processed‐IDs map and close Playwright.
                MessH_Modified finalMonitor = monitor;
                WebLogin finalBot = bot;
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    logger.info("Shutting down — serializing state…");
                    MapSerializer.serialize(finalMonitor.getProcessedIds());
                    //-----
                    // Todo , dp_img map too serialize
                    //-----
                    finalBot.shutdown();
                }));

                long Time = System.currentTimeMillis();
                boolean loginOkay = bot.webLogin();

                monitor.popupRemove();
                if (!loginOkay) {
                    logger.severe("❌ Login failed—exiting.");
                    System.exit(1);
                }
                else {
                    logger.info("Login ? : "+ true);
                    monitor.isConnect = true;
                    new SeedCacher(monitor.page).seedCache(monitor.processedIds); // Safely Process IDS fisrt
                    monitor.MessageToOwner(Time);
                }

                // then start the session monitor
                bot.startSessionMonitor();

                // message‐monitoring thread
                MessH_Modified finalMonitor1 = monitor;
                monitorThread = new Thread(() -> {
                    logger.info("🚀 Starting message monitoring...");
                    finalMonitor1.Handler();
                }, "Message-Monitor");

                monitorThread.setUncaughtExceptionHandler((_, e) -> {
                    throw new RuntimeException(e);
                });

                monitorThread.start();
                monitorThread.join();
            }
            catch (InterruptedException ie) {
                logger.info("Main thread interrupted — exiting.");
                Thread.currentThread().interrupt();
                break;
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Fatal error, will restart in 15s:", e);
            }
            finally {
                if (monitor != null) {
                    logger.info("Finally block — serializing state…");
                    MapSerializer.serialize(monitor.getProcessedIds());
                    bot.shutdown();
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(RESTART_DELAY_MS);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    Browser.closeAll();
                    System.exit(1);
                }
            }
        }
    }
}
