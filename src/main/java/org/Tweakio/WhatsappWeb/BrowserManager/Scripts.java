package org.Tweakio.WhatsappWeb.BrowserManager;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.ForcedColors;
import com.microsoft.playwright.options.Geolocation;
import com.microsoft.playwright.options.ReducedMotion;
import com.microsoft.playwright.options.ServiceWorkerPolicy;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.Tweakio.WhatsappWeb.BrowserManager.Browser.HEADLESS;

public class Scripts {

    public static  void getenabledscritps(Page page){

    }

    public static BrowserType.LaunchPersistentContextOptions getChromeOptions() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/116.0.5845.140 Safari/537.36";

        return new BrowserType.LaunchPersistentContextOptions()
                .setExecutablePath(Paths.get("/usr/bin/google-chrome"))
                .setHeadless(HEADLESS)
                .setAcceptDownloads(true)
                .setDeviceScaleFactor(2.0)
                .setForcedColors(ForcedColors.NONE)
                .setTimezoneId("Asia/Kolkata")
                .setGeolocation(new Geolocation(76.9921,27.8432))
                .setReducedMotion(ReducedMotion.NO_PREFERENCE)
                .setIgnoreHTTPSErrors(true)
                .setServiceWorkers(ServiceWorkerPolicy.ALLOW)
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
    (() => {
        // Removing webdriver
        try {
            delete Object.getPrototypeOf(navigator).webdriver;
        } catch (e) {}

        // 🖼 Canvas Spoofing
        const toDataURL = HTMLCanvasElement.prototype.toDataURL;
        HTMLCanvasElement.prototype.toDataURL = function(...args) {
            const ctx = this.getContext('2d');
            const shift = 2;
            ctx.globalAlpha = 0.9;
            ctx.fillStyle = 'rgba(100,100,100,0.1)';
            ctx.fillRect(shift, shift, this.width - shift*2, this.height - shift*2);
            return toDataURL.apply(this, args);
        };

        // 📐 ClientRects Spoofing
        const origGetClientRects = Element.prototype.getClientRects;
        Element.prototype.getClientRects = function() {
            const rects = origGetClientRects.apply(this);
            for (const r of rects) {
                r.x += 0.1; r.y += 0.1;
                r.width += 0.1; r.height += 0.1;
            }
            return rects;
        };

        // 🎧 AudioContext Spoofing
        const origGetChannelData = AudioBuffer.prototype.getChannelData;
        AudioBuffer.prototype.getChannelData = function() {
            const data = origGetChannelData.apply(this, arguments);
            for (let i = 0; i < data.length; i += 100) {
                data[i] += Math.random() * 1e-7;
            }
            return data;
        };

        // 🌐 WebRTC Leak Prevention
        function RTCPeerConnectionStub() {
            return {
                createOffer: async () => ({}),
                setLocalDescription: async () => {},
                addIceCandidate: async () => {},
                close: () => {},
                addEventListener: () => {},
                removeEventListener: () => {}
            };
        }
        Object.defineProperty(window, 'RTCPeerConnection', { value: RTCPeerConnectionStub });
        Object.defineProperty(window, 'webkitRTCPeerConnection', { value: RTCPeerConnectionStub });

        // 📱 mediaDevices spoof
        Object.defineProperty(navigator, 'mediaDevices', {
            value: {
                enumerateDevices: async () => [
                    { kind: 'audioinput',  label: 'Microphone', deviceId: 'mic1' },
                    { kind: 'videoinput', label: 'Webcam',    deviceId: 'cam1' },
                    { kind: 'audiooutput', label: 'Speaker',   deviceId: 'spk1' }
                ]
            }
        });

        // 🧠 Hardware Properties
        Object.defineProperty(navigator, 'deviceMemory', { get: () => 16 });
        Object.defineProperty(navigator, 'hardwareConcurrency', { get: () => 4 });

        // 💻 Platform spoof (Match with UA)
        Object.defineProperty(navigator, 'platform', { get: () => 'Win32' });
        Object.defineProperty(navigator, 'appVersion', { get: () => '5.0 (Windows)' });
        Object.defineProperty(navigator, 'oscpu', { get: () => 'Windows NT 10.0; Win64; x64' });

        // 🌐 Do Not Track
        // Object.defineProperty(navigator, 'doNotTrack', { get: () => '1' });

        // 🪟 Screen and Window
        Object.defineProperty(window, 'innerWidth', { get: () => 1301 });
        Object.defineProperty(window, 'innerHeight', { get: () => 724});
        Object.defineProperty(window, 'outerWidth', { get: () => 1301 });
        Object.defineProperty(window, 'outerHeight', { get: () => 724 });
        Object.defineProperty(window.screen, 'width', { get: () => 1301 });
        Object.defineProperty(window.screen, 'height', { get: () => 724 });
        Object.defineProperty(window.screen, 'availWidth', { get: () => 1301 });
        Object.defineProperty(window.screen, 'availHeight', { get: () => 724 });
        //Object.defineProperty(window.screen, 'colorDepth', { get: () => 24 });
        //Object.defineProperty(window.screen, 'pixelDepth', { get: () => 24 });

        //  Fake Chrome object to prevent runtime errors
        window.chrome = {
            runtime: {},
            webstore: {}
        };

        // 🔍 matchMedia override (updated logic)
        const originalMatchMedia = window.matchMedia;
        window.matchMedia = function(query) {
            const forcedMatch ="1301|724|max\\\\-width|min\\\\-width|max\\\\-height|min\\\\-height";
            return {
                matches: forcedMatch,
                media: query,
                onchange: null,
                addListener: () => {},
                removeListener: () => {},
                addEventListener: () => {},
                removeEventListener: () => {},
                dispatchEvent: () => false
            };
        };
    })();
   \s""";
    }

    static String mouseUI =  """
window.addEventListener('DOMContentLoaded', () => {
  const dot = document.createElement('div');
  dot.id = '__mouse_dot__';
  Object.assign(dot.style, {
    position: 'fixed',
    width: '10px',
    height: '10px',
    borderRadius: '65%',
    backgroundColor: 'black',
    zIndex: '2147483647',  // Max z-index
    pointerEvents: 'none',
    top: '0px',
    left: '0px',
    transform: 'translate(-50%, -50%)',
    transition: 'top 0.03s linear, left 0.03s linear'
  });
  document.body.appendChild(dot);
  window.addEventListener('mousemove', e => {
    dot.style.left = `${e.clientX}px`;
    dot.style.top = `${e.clientY}px`;
  });
});
""";

    public static Map<String, String> getHttpHeaders() {
        return Map.of(
                "Accept-Language", "en-US,en;q=0.9",
                "Sec-CH-UA", "\"Chromium\";v=\"116\"",
                "Sec-CH-UA-Mobile", "?0",
                "Sec-CH-UA-Platform", "\"Windows\""
        );
    }
}
