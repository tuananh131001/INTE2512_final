package advancednews.Model;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class News {

    private HashMap<String, Category> categories = new HashMap<>();

    public ArrayList<Article> scrapeArticle(String url) throws IOException{
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

}
