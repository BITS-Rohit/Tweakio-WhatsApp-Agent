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
                
                // Language spoofing
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


//    static public BrowserType.LaunchPersistentContextOptions getChromeOptions() {
//        return new BrowserType.LaunchPersistentContextOptions()
//                .setExecutablePath(Paths.get("/usr/bin/google-chrome"))
//                .setHeadless(HEADLESS)
//                .setIgnoreDefaultArgs(List.of("--enable-automation"))
//                .setViewportSize(1280, 720)
//                .setLocale("en-US")
//                .setChannel("chrome")
//                .setBypassCSP(true)
//                .setUserAgent(
//                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
//                                "AppleWebKit/537.36 (KHTML, like Gecko) " +
//                                "Chrome/116.0.5845.140 Safari/537.36"
//                )
//                .setArgs(List.of(
//                        "--disable-blink-features=AutomationControlled",
//                        "--disable-dev-shm-usage",
//                        "--no-sandbox",
//                        "--disable-web-security",
//                        "--disable-features=IsolateOrigins,site-per-process,AudioServiceOutOfProcess",
//                        "--disable-webrtc",
//                        "--force-webrtc-ip-handling-policy=disable_non_proxied_udp",
//                        "--window-size=1280,720",
//                        "--start-maximized",
//                        "--font-render-hinting=none",
//                        "--use-fake-ui-for-media-stream",
//                        "--use-fake-device-for-media-stream",
//                        "--lang=en-US",
/// /                        "--mute-audio",                   // silent browsing
//                        "--autoplay-policy=no-user-gesture-required",
//                        "--disable-background-networking",
//                        "--disable-background-timer-throttling",
//                        "--metrics-recording-only",
//                        "--timezone=America/New_York"
//                ));
//    }


//    static public String getStealthScript() {
//        return """
//            // ======================= CORE SPOOFING =======================
//            try { delete navigator.__proto__.webdriver; } catch(e) {}
//            Object.defineProperty(navigator, 'platform', { value: 'Win32', configurable: true });
//            Object.defineProperty(navigator, 'languages', { get: () => ['en-US','en'] });
//            Object.defineProperty(navigator, 'deviceMemory', { value: 8, configurable: true });
//            Object.defineProperty(navigator, 'hardwareConcurrency', { value: 4, configurable: true });
//
//// --------------- Whatsapp Breaking -----------
////            Object.defineProperty(Intl, 'DateTimeFormat', {
////                value: class extends Intl.DateTimeFormat {
////                    constructor(locales, options) {
////                        super(locales, {...options, timeZone: 'America/New_York'});
////                    }
////                }
////            });
////            Date.prototype.getTimezoneOffset = () => 300;
////-----------------------------------------------
//
//            (() => {
//                const pluginNames = [
//                    'PDF Viewer', 'Chrome PDF Viewer', 'Chromium PDF Viewer',
//                    'Microsoft Edge PDF Viewer', 'WebKit built-in PDF'
//                ];
//                navigator.mimeTypes = new MimeTypeArray();
//                navigator.plugins = new PluginArray();
//                pluginNames.forEach((name, i) => {
//                    const plugin = new Plugin();
//                    plugin.name = name;
//                    plugin.filename = 'internal-pdf-viewer';
//                    plugin.description = 'Portable Document Format';
//                    navigator.plugins[i] = plugin;
//                    const mimeType = new MimeType();
//                    mimeType.type = 'application/pdf';
//                    mimeType.suffixes = 'pdf';
//                    mimeType.description = 'Portable Document Format';
//                    mimeType.enabledPlugin = plugin;
//                    navigator.mimeTypes[i] = mimeType;
//                });
//                Object.freeze(navigator.plugins);
//                Object.freeze(navigator.mimeTypes);
//            })();
//
//            (() => {
//                const getParameter = WebGLRenderingContext.prototype.getParameter;
//                WebGLRenderingContext.prototype.getParameter = function(p) {
//                    if (p === 37445) return 'Google Inc. (Google)';
//                    if (p === 37446) return 'ANGLE (Google, Vulkan 1.3.0 (SwiftShader Device (Subzero) (0x0000C0DE)), SwiftShader driver';
//                    return getParameter.call(this, p);
//                };
//                const getParameterWebGL2 = WebGL2RenderingContext.prototype.getParameter;
//                WebGL2RenderingContext.prototype.getParameter = function(p) {
//                    if (p === 37445) return 'Google Inc. (Google)';
//                    if (p === 37446) return 'ANGLE (Google, Vulkan 1.3.0 (SwiftShader Device (Subzero) (0x0000C0DE)), SwiftShader driver';
//                    return getParameterWebGL2.call(this, p);
//                };
//            })();
//
//            HTMLCanvasElement.prototype.__origGetContext = HTMLCanvasElement.prototype.getContext;
//            HTMLCanvasElement.prototype.getContext = function(type, attrs) {
//                const ctx = HTMLCanvasElement.prototype.__origGetContext.call(this, type, attrs);
//                if (type === '2d') {
//                    const origToDataURL = this.toDataURL;
//                    this.toDataURL = function(mime, q) {
//                        ctx.clearRect(0,0,300,150);
//                        ctx.fillStyle = "#000";
//                        ctx.font = "14px Arial";
//                        ctx.fillText("Cwm fjordbank glyphs vext quiz, 😀", 10, 20);
//                        return origToDataURL.call(this, mime, q);
//                    };
//                }
//                return ctx;
//            };
//
//            (() => {
//                const origCreateAnalyser = AudioContext.prototype.createAnalyser;
//                AudioContext.prototype.createAnalyser = function() {
//                    const analyser = origCreateAnalyser.call(this);
//                    analyser.getFloatFrequencyData = function(arr) {
//                        for (let i = 0; i < arr.length; i++) arr[i] = Math.random() * -100;
//                    };
//                    return analyser;
//                };
//            })();
//
//            navigator.mediaDevices.enumerateDevices = () => Promise.resolve([
//                { deviceId:'abc123', kind:'audioinput',  label:'Microphone (Realtek High Definition Audio)', groupId:'g1' },
//                { deviceId:'def456', kind:'audiooutput', label:'Speakers (Realtek High Definition Audio)', groupId:'g1' },
//                { deviceId:'ghi789', kind:'videoinput',  label:'HD Webcam', groupId:'g2' }
//            ]);
//
//            navigator.getBattery = () => Promise.resolve({
//                charging: false,
//                chargingTime: 0,
//                dischargingTime: Infinity,
//                level: 1
//            });
//
//            Object.defineProperties(navigator.connection, {
//                downlink: { value: 10 },
//                effectiveType: { value: '4g' },
//                rtt: { value: 50 },
//                saveData: { value: false }
//            });
//
//            const originalQuery = navigator.permissions.query;
//            navigator.permissions.query = p => {
//                if (p.name === 'notifications' || p.name === 'geolocation') {
//                    return Promise.resolve({state: 'prompt'});
//                }
//                return originalQuery.call(navigator.permissions, p);
//            };
//
//            Object.defineProperty(navigator, 'keyboard', {
//                value: { layoutMap: Promise.resolve(['en-US']) }
//            });
//
//            Object.defineProperty(document, 'fonts', {
//                value: {
//                    ready: Promise.resolve(),
//                    forEach: cb => [
//                        'Arial','Verdana','Helvetica','Times New Roman',
//                        'Courier New','Georgia','Trebuchet MS',
//                        'Comic Sans MS','Impact','Tahoma'
//                    ].forEach(cb)
//                }
//            });
//
//            Object.defineProperty(navigator, 'userAgentData', {
//                value: {
//                    brands: [
//                        { brand: "Chromium", version: "116" },
//                        { brand: "Google Chrome", version: "116" }
//                    ],
//                    mobile: false,
//                    getHighEntropyValues: (hints) => Promise.resolve({
//                        architecture: "x86",
//                        model: "",
//                        platform: "Windows",
//                        platformVersion: "10.0",
//                        uaFullVersion: "116.0.5845.140",
//                        fullVersionList: [
//                            { brand: "Chromium", version: "116.0.5845.140" },
//                            { brand: "Google Chrome", version: "116.0.5845.140" }
//                        ]
//                    })
//                },
//                configurable: true
//            });
//
//            Object.defineProperty(navigator, 'webdriver', {
//                get: () => undefined,
//                configurable: true
//            });
//        """;
//    }

