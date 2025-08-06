package org.Tweakio.WhatsappWeb;

import org.Tweakio.UserSettings.user;
import org.Tweakio.WhatsappWeb.BrowserManager.Browser;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebMain {
    private static final Logger logger = Logger.getLogger(WebMain.class.getName());
    private static final int RESTART_DELAY_MS = 2_000;
    private static final int AUTOSAVE_INTERVAL_SEC = 5;

    public static void main(String[] args) {
        Extras.logwriter("----------------WebMain initiating. // webmain------------------");

        ScheduledExecutorService autosaveScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Autosave-Scheduler");
            t.setDaemon(true);
            return t;
        });

        while (true) {
            WebLogin bot = null;
            Brain monitor = null;
            Thread monitorThread;

            System.out.println("Profile starting : " + user.PROFILE);
            Extras.logwriter("Profile starting : " + user.PROFILE);
            Browser browser = Browser.getInstance(user.PROFILE, false);

            try {
                // ─── Login & state restore ───────────────────────────────
                Extras.logwriter("Web Login initiated.");
                bot = new WebLogin(browser);
                Extras.logwriter("Web Login completed.");

                Map<String, Set<String>> state = MapSerializer.deserialize();
                Extras.logwriter("Map deserialized.  // webmain");
                logger.info("Map data picked up from file ...");

                monitor = new Brain(bot, state, browser);

                // ─── Schedule autosave every AUTOSAVE_INTERVAL_SEC ───────
                Map<String, Set<String>> processedIds = monitor.getProcessedIds();
                autosaveScheduler.scheduleAtFixedRate(() -> {
                    MapSerializer.serialize(processedIds);
                    logger.fine("Autosaved processedIds.");
                }, AUTOSAVE_INTERVAL_SEC, AUTOSAVE_INTERVAL_SEC, TimeUnit.SECONDS);

                // ─── JVM shutdown hook ──────────────────────────────────
                WebLogin finalBot = bot;
                Brain finalMonitor = monitor;
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    logger.info("Shutdown hook: serializing state...");
                    Extras.logwriter("Shutdown hook: serializing state... // webmain");
                    MapSerializer.serialize(finalMonitor.getProcessedIds());
                    finalBot.shutdown();
                }));

                // ─── Perform login & initial seeding ──────────────────
                long startTime = System.currentTimeMillis();
                boolean loginOkay = bot.webLogin();
                monitor.popupRemove();

                if (!loginOkay) {
                    logger.severe("❌ Login failed—exiting.");
                    Extras.logwriter("Login failed—exiting. // webmain");
                    System.exit(1);
                }

                monitor.isConnect = true;
                new SeedCacher(monitor.page).seedCache(monitor.processedIds);
                monitor.MessageToOwner(startTime);

                // ─── Start message‐monitor thread ─────────────────────
                Brain finalMonitor1 = monitor;
                monitorThread = new Thread(() -> {
                    Extras.logwriter("Session monitoring started // webmain");
                    logger.info("🚀 Starting message monitoring...");
                    finalMonitor1.Handler();
                }, "Message-Monitor");

                monitorThread.setUncaughtExceptionHandler((t, e) -> {
                    Extras.logwriter("Uncaught Exception in Message-Monitor // webmain");
                    throw new RuntimeException(e);
                });

                monitorThread.start();
                monitorThread.join();

            } catch (InterruptedException ie) {
                logger.info("Main thread interrupted — exiting.");
                Extras.logwriter("Main thread interrupted // webmain");
                Thread.currentThread().interrupt();
                break;

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Fatal error, will restart shortly:", e);
                Extras.logwriter("Fatal error, will restart shortly: " + e.getMessage() + " // webmain");
            } finally {
                // Cancel the autosave while we clean up
                autosaveScheduler.shutdownNow();

                if (monitor != null) {
                    logger.info("Finally block — serializing state…");
                    Extras.logwriter("Finally block serializing state // webmain");
                    MapSerializer.serialize(monitor.getProcessedIds());
                }
                if (bot != null) {
                    bot.shutdown();
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(RESTART_DELAY_MS);
                } catch (InterruptedException e) {
                    Extras.logwriter("Interrupted during restart delay — exiting. // webmain");
                    Thread.currentThread().interrupt();
                    Browser.closeAll();
                    System.exit(1);
                }
            }
        }
    }
}
