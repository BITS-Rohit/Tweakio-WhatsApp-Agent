package org.Tweakio.SearchSites.Amazon.India;

import org.Tweakio.WhatsappWeb.BrowserManager.Browser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SearchAmazonIN {
    private final In_Scraper inScraper;

    public SearchAmazonIN(Browser b ) {
        // Instantiate your In_Scraper (which uses Playwright internally)
        inScraper = new In_Scraper(b);
    }

    /**
     * Public method to get Amazon search‐results content for a given query.
     * Returns a concatenated string of each result snippet (outer HTML),
     * or you can easily switch it to return the full page HTML if desired.
     */
    public String GetContent(String query) {
        return Content(query);
    }

    private String Content(String query) {
        String fullHtml;
        fullHtml = inScraper.searchProducts(query);

        if (fullHtml == null || fullHtml.isEmpty() || fullHtml.equals("null") ) {
            return "No HTML returned from Scraper.  // Amzaon In";
        }

        Document doc = Jsoup.parse(fullHtml);
        StringBuilder builder = new StringBuilder();

        // Main container for products
        Element productsContainer = doc.selectFirst("div.s-main-slot.s-result-list.s-search-results.sg-row");
        if (productsContainer == null) {
            return "No products found";
        }

        // Individual product items
        Elements products = productsContainer.select("div[data-component-type=s-search-result]");
        if (products.isEmpty()) {
            products = productsContainer.select("div.puisg-row");
        }

        System.out.println("Found " + products.size() + " products");

        for (Element product : products) {
            if (product.selectFirst("div[data-component-type=s-sponsored-result]") != null) {
                continue;
            }

            // Extract product information
            String title = extractText(product, "a h2 span");
            String price = extractText(product, "span.a-price span.a-offscreen");
            String originalPrice = extractText(product, "span.a-price.a-text-price span.a-offscreen");
            String discount = extractText(product, "span.a-letter-space + span");
            String rating = extractAttribute(product, "i.a-icon-star-small", "aria-label");
            String reviewCount = extractText(product, "span.a-size-base.s-underline-text");
            String imageUrl = extractAttribute(product, "img.s-image", "src");
            String productUrl = extractAttribute(product, "a.a-link-normal", "href");
            String deliveryInfo = extractText(product, "div.a-row span.a-color-base.a-text-bold");
            String sponsored = product.selectFirst("span:contains(Sponsored)") != null ? "Yes" : "No";
            String limitedDeal = extractText(product, "span.a-badge-text");
            String Rating = extractText(product , "div[data-cy=reviews-block] div.a-row.a-size-small span a i span");
            String ReviewCount = extractText(product , "div[data-cy=reviews-block] div.a-row.a-size-small a span");
            String PastBought = extractText(product, "div[data-cy=reviews-block] div.a-row.a-size-base span");



            // Build product string
            builder.append("Product:\n");
            builder.append("  Title: ").append(title).append("\n");
            builder.append("  Price: ").append(price).append("\n");
            if (originalPrice != null) builder.append("  Original Price: ").append(originalPrice).append("\n");
            if (discount != null) builder.append("  Discount: ").append(discount).append("\n");
            if (rating != null) builder.append("  Rating: ").append(Rating).append("\n");
            if (reviewCount != null) builder.append("  Reviews: ").append(reviewCount).append("\n");
            if (imageUrl != null) builder.append("  Image: ").append(imageUrl).append("\n");
            if (productUrl != null) builder.append("  URL: www.amazon.in").append(productUrl).append("\n");
            if (deliveryInfo != null) builder.append("  Delivery: ").append(deliveryInfo).append("\n");
            builder.append("  Sponsored: ").append(sponsored).append("\n");
            if (limitedDeal != null) builder.append("  Deal: ").append(limitedDeal).append("\n");
            if(ReviewCount != null) builder.append("  Reviews: ").append(ReviewCount).append("\n");
            if(PastBought != null) builder.append("  Past Bought: ").append(PastBought).append("\n");
            builder.append("\n");
        }


        return builder.toString().isEmpty() ? "No products found" : builder.toString();
    }

    // Helper methods for safe extraction-----------------------
    private String extractText(Element context, String selector) {
        Element element = context.selectFirst(selector);
        return element != null ? element.text() : null;
    }

    private String extractAttribute(Element context, String selector, String attribute) {
        Element element = context.selectFirst(selector);
        return element != null ? element.attr(attribute) : null;
    }
    //-----------------------------------------------------------

}

