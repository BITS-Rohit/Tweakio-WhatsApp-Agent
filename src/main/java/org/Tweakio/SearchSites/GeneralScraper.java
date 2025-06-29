package org.Tweakio.SearchSites;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import org.Tweakio.WhatsappWeb.BrowserManager.Browser;

public class GeneralScraper {
    Browser browser ;

    public GeneralScraper(Browser browser) {
        this.browser = browser;
    }

    /*
    This funtion is like giving a url 's html code with js loaded in it
    @params are a Full url to be searched for
    @return the HTML Code of that url
     */

    public String getHtmlWithJsLoaded(String url) {
        Page page = null;
        try {
            page = browser.newPage();
            page.navigate(url, new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.NETWORKIDLE)
                    .setTimeout(7_000)); // 7 sec is enogh
            return page.content();
        } catch (Exception e) {
            return page != null ? page.content() : "Null";
        } finally {
            if (page != null) page.close();
        }
    }

}
