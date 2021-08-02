package sample.news;

import javafx.scene.image.Image;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sample.Article;
import sample.Category;
import sample.News;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Tuoitre implements News {
    @Override
    public ArrayList<Category> scrapeWebsite() throws IOException {
        ArrayList<Category> categoryList = createCategory(); //new category list includes World New Politics etc

        File urlfile = new File("src/sample/tuoitreurl.txt"); //reads a txt file for all urls
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
            ArrayList<Article> articleList = scrapeArticle(url);
            category.setArticleList(articleList);
        }
        return categoryList;

    }

    @Override
    public Category scrapeWebsiteCategory(String categoryName) throws IOException {
        File urlfile = new File("src/sample/tuoitreurl.txt");
        Scanner urlScanner = new Scanner(urlfile);
        HashMap<String, String> urls = new HashMap<String, String>();
        while (urlScanner.hasNextLine()) {
            String[] url = urlScanner.nextLine().split("\\|");
            urls.put(url[1], url[0]);
        }
        System.out.println(urls);
        //crawl from these site
        String url = urls.get(categoryName);
        if (url == null) return null;
        Category category = new Category(categoryName);
        ArrayList<Article> articleList = scrapeArticle(urls.get(categoryName));
        category.setArticleList(articleList);

        return category;
    }

    @Override
    public String findTime(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Elements date = doc.getElementsByClass("date");
        String time = date.first().text();
        return time;
    }

    @Override
    public ArrayList<Article> scrapeArticle(String url) throws IOException {
        if (url == null) return new ArrayList<>();
        ArrayList<Article> newsList = new ArrayList<>(); //initialize return variable

        Elements listArticle = new Elements(); //initialize article list

        //connect to rss website and add in listArticle all "items"
        Document doc = Jsoup.connect(url).get();
        listArticle.addAll(doc.getElementsByTag("item"));

        //for each article, get its url, description and url
        try {
            for (Element article : listArticle) {
                String name = article.child(0).ownText();
                String articleUrl = article.child(1).ownText();
                Image image = null;
                Document description = Jsoup.parse(article.child(3).ownText());
                String imageurl = description.getElementsByTag("img").attr("src");
                if (imageurl != null) {
                    image = new Image(imageurl);
                }
                String date = article.getElementsByTag("Pubdate").first().ownText();
                newsList.add(new Article(image, name, articleUrl, date));
            }
        } catch (Exception e){
            System.out.println(e);
        }
        return newsList;
    }
}