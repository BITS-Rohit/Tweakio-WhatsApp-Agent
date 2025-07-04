package org.Tweakio.WhatsappWeb.BrowserManager;

import com.microsoft.playwright.BrowserType;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.Tweakio.WhatsappWeb.BrowserManager.Browser.HEADLESS;

public class Scripts {

    public static BrowserType.LaunchPersistentContextOptions getChromeOptions() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/116.0.5845.140 Safari/537.36";

        return new BrowserType.LaunchPersistentContextOptions()
                .setExecutablePath(Paths.get("/usr/bin/google-chrome"))
                .setHeadless(HEADLESS)
                .setViewportSize(1280, 720)
                .setLocale("en-US")
                .setUserAgent(userAgent)
                .setArgs(List.of(
                        "--disable-blink-features=AutomationControlled",
                        "--start-maximized",
                        "--no-sandbox"
                ));
    }

    public static String getStealthScript() {
        return """
                // Remove webdriver flag
                Object.defineProperty(navigator, 'webdriver', { get: () => undefined });
                
                // Add subtle canvas fingerprint noise
                (function() {
                  const orig = HTMLCanvasElement.prototype.toDataURL;
                  HTMLCanvasElement.prototype.toDataURL = function() {
                    const ctx = this.getContext('2d');
                    if (ctx) {
                      const img = ctx.getImageData(0,0,this.width,this.height);
                      for (let i = 0; i < img.data.length; i += 20) {
                        img.data[i]   ^= Math.random()*10;
                        img.data[i+1] ^= Math.random()*10;
                        img.data[i+2] ^= Math.random()*10;
                      }
                      ctx.putImageData(img,0,0);
                    }
                    return orig.apply(this, arguments);
                  };
                })();
                
                // Language spoofingqqq
                Object.defineProperty(navigator, 'languages', {
                  get: () => ['en-US', 'en']
                });
                """;
    }

    public static Map<String, String> getHttpHeaders() {
        return Map.of(
                "Accept-Language", "en-US,en;q=0.9",
                "Sec-CH-UA", "\"Chromium\";v=\"116\"",
                "Sec-CH-UA-Mobile", "?0",
                "Sec-CH-UA-Platform", "\"Windows\""
        );
    }
}
