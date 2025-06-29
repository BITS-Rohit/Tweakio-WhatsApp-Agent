package org.Tweakio.SearchSites.Amazon.India;

import org.Tweakio.WhatsappWeb.BrowserManager.Browser;
import org.Tweakio.SearchSites.GeneralScraper;

public class In_Scraper {

    private final String url = "https://www.amazon.in/s?k=";
    private final GeneralScraper scraper ;

    public In_Scraper(Browser b ) {
        scraper = new GeneralScraper( b );
    }

    public String searchProducts(String query) {
        String encodedQuery = encodeQuery(query);
        return scraper.getHtmlWithJsLoaded(url + encodedQuery);
    }

    private String encodeQuery(String query) {
        return query.trim().replace(" ", "+");
    }
}
