package advancednews.Model;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class News {

    private HashMap<String, Category> categories = new HashMap<>();

    public ArrayList<Article> scrapeArticle(String url) throws IOException {
        return new ArrayList<>();
    }

    public Category scrapeWebsiteCategory(String categoryName) throws IOException {
        File urlfile = new File(getFileName());
        Category category = categories.get(categoryName);
        if (category != null) return category;
        Scanner urlScanner = new Scanner(urlfile);
        //Hashmap to store url and category name
        HashMap<String, String> urlsHashMap = new HashMap<String, String>();
        while (urlScanner.hasNextLine()) {
            String[] url = urlScanner.nextLine().split("\\|");
            urlsHashMap.put(url[1], url[0]);
        }
        //crawl from these site
        String urlCategory = urlsHashMap.get(categoryName);
        category = new Category(categoryName);
        if (urlCategory == null) return category;
        ArrayList<Article> articleList = scrapeArticle(urlCategory);
        category.setArticleList(articleList);
        categories.put(categoryName, category);
        return category;
    }


    public Element scrapeContent(String url) throws IOException {
        return Jsoup.parse(Jsoup.connect(url).get().toString());
    }

    public String getFileName(){
        return "";
    }

    public Duration getTimeSince(String dateTime) throws ParseException {
        Scanner scanner = new Scanner(dateTime);
        String day = scanner.findInLine("(\\d+/\\w+/\\d+)");
        if (day == null) day = scanner.findInLine("(\\d+ \\w+ \\d+)");
        if (day == null) day = scanner.findInLine("(\\d+/\\d+/\\d+)");
        String time = scanner.findInLine("(\\d+:\\d+:?\\d+)");
        SimpleDateFormat dateFormat;
        Date date;
        try {
            dateFormat = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss");
            date =  dateFormat.parse(day + " " + time);
        } catch (Exception e) {
            try {
            dateFormat = new SimpleDateFormat("dd MMM yyyy kk:mm:ss");
            date = dateFormat.parse(day + " " + time);
            } catch (Exception e2){
                dateFormat = new SimpleDateFormat("dd/MM/yyyy kk:mm");
                date = dateFormat.parse(day + " " + time);
            }
        }
        return Duration.between(date.toInstant(), Instant.now());
    }
}
