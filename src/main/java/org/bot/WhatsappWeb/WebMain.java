package org.bot.WhatsappWeb;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebMain {
    private static final Logger logger = Logger.getLogger(WebMain.class.getName());
    private static final int RESTART_DELAY_MS = 5_000;

    public static void main(String[] args) {
        while (true) {
            WebLogin bot = null;
            MessH_Modified monitor = null;
            Thread monitorThread;

            try {

                bot = new WebLogin();
                Map<String, Set<String>> state = MapSerializer.deserialize();
                monitor = new MessH_Modified(bot, state);

                MessH_Modified finalMonitor = monitor;
                WebLogin finalBot = bot;
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    logger.info("Shutting down — serializing state…");
                    MapSerializer.serialize(finalMonitor.getProcessedIds());
                    finalBot.shutdown();
                }));

                bot.webLogin();
                monitor.seedCache();
                bot.startSessionMonitor();

                MessH_Modified finalMonitor1 = monitor;
                monitorThread = new Thread(() -> {
                    logger.info("🚀 Starting message monitoring...");
                    finalMonitor1.Handler();
                }, "Message-Monitor");

                monitorThread.setUncaughtExceptionHandler((t, e) -> {
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
                logger.log(Level.SEVERE, "Fatal error, will restart in 5s:", e);
            }
            finally {
                if (monitor != null && bot != null) {
                    logger.info("Finally block — serializing state…");
                    MapSerializer.serialize(monitor.getProcessedIds());
                    bot.shutdown();
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(RESTART_DELAY_MS);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}
