package org.Tweakio.WhatsappWeb;

import org.Tweakio.UserSettings.user;
import org.Tweakio.WhatsappWeb.BrowserManager.Browser;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebMain {
    private static final Logger logger = Logger.getLogger(WebMain.class.getName());
    private static final int RESTART_DELAY_MS = 2_000;

    public static void main(String[] args) {
        Extras.logwriter("----------------WebMain initiating. // webmain------------------");
        while (true) {
            WebLogin bot = null;
            Brain monitor = null;
            Thread monitorThread;
            System.out.println("Profile starting : "+ user.PROFILE);
            Extras.logwriter("Profile starting : "+ user.PROFILE);
            Browser browser = Browser.getInstance(user.PROFILE,false);
            try {
                // ───────────────────────────────────────────────────────────────────
                //        Initialize WebLogin and restore any previous state
                // ───────────────────────────────────────────────────────────────────
                Extras.logwriter("Web Login initiated.");
                bot = new WebLogin(browser);
                Extras.logwriter("Web Login completed.");

                Map<String, Set<String>> state = MapSerializer.deserialize();
                Extras.logwriter("Map Deserialized.  // webmain");
                logger.info("Map Data picked up from file ...");
                Extras.logwriter("Map Data picked up from file ... // webmain");
                monitor = new Brain(bot, state,browser);

                // Install a shutdown hook so that if the JVM shuts down,
                // we serialize the processed‐IDs map and close Playwright.
                Brain finalMonitor = monitor;
                WebLogin finalBot = bot;
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    logger.info("Shutting down — serializing state...");
                    Extras.logwriter("Shutting down serializing state... // webmain");
                    MapSerializer.serialize(finalMonitor.getProcessedIds());
                    Extras.logwriter("ProcessIDS Serialize complete. // webmain");
                    //-----
                    // Todo , dp_img map too serialize
                    //-----
                    finalBot.shutdown();
                    Extras.logwriter("Shutdown initiated.");
                }));

                long Time = System.currentTimeMillis();
                boolean loginOkay = bot.webLogin();
                Extras.logwriter("get login check -> loginokay");

                monitor.popupRemove();
                if (!loginOkay) {
                    logger.severe("❌ Login failed—exiting.");
                    Extras.logwriter("if // Login failed - Exiting.");
                    System.exit(1);
                }
                else {
                    Extras.logwriter("else // Login complete. // webmain");
                    logger.info("Login ? : "+ true);
                    monitor.isConnect = true;
                    new SeedCacher(monitor.page).seedCache(monitor.processedIds); // Safely Process IDS fisrt
                    monitor.MessageToOwner(Time);
                }

                // then start the session monitor
                bot.startSessionMonitor();

                // message‐monitoring thread
                Brain finalMonitor1 = monitor;
                monitorThread = new Thread(() -> {
                    Extras.logwriter("get session check -> session monitoring started // webmain");
                    logger.info("🚀 Starting message monitoring...");
                    finalMonitor1.Handler();
                }, "Message-Monitor");

                monitorThread.setUncaughtExceptionHandler((Ignored, e) -> {
                    Extras.logwriter("Uncaught Exception in Message-Monitor // webmain");
                    throw new RuntimeException(e);
                });

                monitorThread.start();
                monitorThread.join();
            }
            catch (InterruptedException ie) {
                logger.info("Main thread interrupted — exiting.");
                Extras.logwriter("Main thread interrupted // webmain");
                Thread.currentThread().interrupt();
                break;
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Fatal error, will restart in 15s:", e.getMessage());
                Extras.logwriter("Fatal error, will restart in 15s: "+e.getMessage() + "// webmain" );
            }
            finally {
                if (monitor != null) {
                    logger.info("Finally block — serializing state…");
                    Extras.logwriter("Finally block serializing state // webmain");
                    MapSerializer.serialize(monitor.getProcessedIds());
                    bot.shutdown();
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(RESTART_DELAY_MS);
                } catch (InterruptedException e) {
                    Extras.logwriter("Main thread interrupted  exiting System. // webmain" + e.getMessage());
                    System.out.println(" Stopping system ...");
                    Thread.currentThread().interrupt();
                    Browser.closeAll();
                    System.exit(1);
                }
            }
        }
    }
}

