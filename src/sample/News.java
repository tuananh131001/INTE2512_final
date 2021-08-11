package sample;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public abstract class News {

    private HashMap<String, Category> categories = new HashMap<>();

    public ArrayList<Article> scrapeArticle(String url) throws IOException{
        return new ArrayList<>();
    }

    public Category scrapeWebsiteCategory(String categoryName,File urlfile) throws IOException {
        Category category = categories.get(categoryName);
        if (category != null) return category;
        Scanner urlScanner = new Scanner(urlfile);
        HashMap<String, String> urls = new HashMap<String, String>();
        while (urlScanner.hasNextLine()) {
            String[] url = urlScanner.nextLine().split("\\|");
            urls.put(url[1], url[0]);
        }
        //crawl from these site
        String url = urls.get(categoryName);
        category = new Category(categoryName);
        if (url == null) return category;
        ArrayList<Article> articleList = scrapeArticle(url);
        category.setArticleList(articleList);
        categories.put(categoryName, category);
        return category;
    }


    public Element scrapeContent(String url) throws IOException {
        return Jsoup.parse(Jsoup.connect(url).get().toString());
    }

}
