/*
  RMIT University Vietnam
  Course: INTE2512 Object-Oriented Programming
  Semester: 2021B
  Assessment: Final Project
  Created  date: 09/06/2021
  Author:
  Nguyen Tuan Anh s3864077
  Tran Nguyen Ha Khanh s3877707
  Nguyen Vu Minh Duy s3878076
  Phan Thanh Phu s3877814
  Ngo Thanh Nguyen s3856221
  Last modified date: 14/09/2021
  Acknowledgement:
  http://www.java2s.com/Tutorials/Java/JavaFX_How_to/Image/Load_an_Image_from_local_file_system.htm
  https://docs.oracle.com/javafx/2/webview/jfxpub-webview.htm
  https://stackoverflow.com/questions/6530974/getting-a-property-value-and-passing-it-on-to-superclass/6531076#6531076
  https://stackoverflow.com/questions/47743650/javafx-8-property-bindings-for-custom-objects
  https://stackoverflow.com/questions/21083945/how-to-avoid-not-on-fx-application-thread-currentthread-javafx-application-th
  https://stackoverflow.com/questions/541487/implements-runnable-vs-extends-thread-in-java?page=2&tab=votes#tab-top
  https://stackoverflow.com/questions/4691533/java-wait-for-thread-to-finish
  https://stackoverflow.com/questions/48048943/javafx-8-scroll-bar-css
*/
package opennews.Model;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

public class News {
    // Variable of class News
    private final HashMap<String, Category> categories = new HashMap<>();

    // Method scape article from a link and return list of article
    public ArrayList<Article> scrapeArticle(String url) throws IOException {
        return new ArrayList<>();
    }

    public Category scrapeWebsiteCategory(String categoryName) throws IOException {
        Category categoryElement = categories.get(categoryName);
        // Check category if nothing in category
        if (categoryElement != null) return categoryElement;

        // initialize the Scanner
        Scanner urlScanner = new Scanner(new File(getFileName()));

        //Hashmap to store url and category name
        HashMap<String, String> urlsHashMap = new HashMap<>();
        while (urlScanner.hasNextLine()) {
            String[] url = urlScanner.nextLine().split("\\|");
            urlsHashMap.put(url[1], url[0]);
        }

        // Get website information
        String urlCategory = urlsHashMap.get(categoryName);
        categoryElement = new Category(categoryName);
        if (urlCategory == null) return categoryElement;

        //crawl from these site
        ArrayList<Article> articleList = scrapeArticle(urlCategory);
        categoryElement.setArticleList(articleList);
        categories.put(categoryName, categoryElement);
        return categoryElement;
    }

    // FUnction scrape content from a url and return that website element
    public Element scrapeContent(String url) throws IOException {
        return Jsoup.parse(Jsoup.connect(url).get().toString());
    }

    // Function get file name
    public String getFileName() {
        return "";
    }

    // Function get time from an article and return duration
    public Duration getTimeSince(String dateTime) {
        // Init variable
        SimpleDateFormat dateFormat;
        Date date;

        // Init date and time scanner
        Scanner scanner = new Scanner(dateTime);

        // Find time
        String day = scanner.findInLine("(\\d+/\\w+/\\d+)");
        if (day == null) day = scanner.findInLine("(\\d+ \\w+ \\d+)");
        if (day == null) day = scanner.findInLine("(\\d+/\\d+/\\d+)");
        scanner = new Scanner(dateTime);
        String time = scanner.findInLine("(\\d+:\\d+:?\\d+)");

        // Parse time from website
        try {
            dateFormat = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss");
            date = dateFormat.parse(day + " " + time);
        } catch (Exception e) {
            try {
                dateFormat = new SimpleDateFormat("dd MMM yyyy kk:mm:ss");
                date = dateFormat.parse(day + " " + time);
            } catch (Exception e2) {
                try {
                    dateFormat = new SimpleDateFormat("dd/MM/yyyy kk:mm");
                    date = dateFormat.parse(day + " " + time);
                } catch (Exception e3) {
                    try {
                        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+07:00'");
                        date = dateFormat.parse(dateTime);
                    } catch (Exception e4){
                        return Duration.ZERO;
                    }
                }
            }
        }

        // Return time and date
        return Duration.between(date.toInstant(), Instant.now());
    }

    //Function reset category
    public void resetCategory(String category) {
        categories.put(category, null);
    }
}
