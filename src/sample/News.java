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

public interface News {

    HashMap<String, Category> categories = new HashMap<>();

    public default ArrayList<Article> scrapeArticle(String url) throws IOException{
        return new ArrayList<>();
    }

    public default ArrayList<Category> createCategory() {
        ArrayList<Category> category = new ArrayList<Category>();
        category.add(new Category("New"));
        category.add(new Category("Covid"));
        category.add(new Category("Politics"));
        category.add(new Category("Business"));
        category.add(new Category("Technology"));
        category.add(new Category("Health"));
        category.add(new Category("Sports"));
        category.add(new Category("Entertainment"));
        category.add(new Category("World"));
        category.add(new Category("Others"));
        return category;
    }

    public default Category scrapeWebsiteCategory(String categoryName,File urlfile) throws IOException {
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
        if (url == null) return category;
        category = new Category(categoryName);
        ArrayList<Article> articleList = scrapeArticle(url);
        category.setArticleList(articleList);
        categories.put(categoryName, category);
        return category;
    }

    public default ArrayList<Category> scrapeWebsite(File urlfile) throws IOException {
        ArrayList<Category> categoryList = createCategory(); //new category list includes World New Politics etc

        Scanner urlScanner = new Scanner(urlfile);
        HashMap<String, String> urls = new HashMap<String, String>();

        //scans the file
        while (urlScanner.hasNextLine()) {
            String[] url = urlScanner.nextLine().split("\\|");
            urls.put(url[1], url[0]);
        }

        //Find and all article element in listArticle
        for (Category category : categoryList) {
            String url = urls.get(category.getCategoryName());
            if (url == null) continue;
            ArrayList<Article> articleList = scrapeArticle(url);
            category.setArticleList(articleList);
        }
        return categoryList;

    }

    public default Element scrapeContent(String url) throws IOException {
        return Jsoup.parse(Jsoup.connect(url).get().toString());
    }

}
