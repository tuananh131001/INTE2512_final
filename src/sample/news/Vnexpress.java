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

public class Vnexpress implements News {
    @Override
    public ArrayList<Category> srapeWebsite() throws IOException {
        ArrayList<Category> categoryList = createCategory();
        //crawl from these site
        String[] urls = {"https://vnexpress.net",
                "https://vnexpress.net/goc-nhin/covid-19",
                "https://vnexpress.net/thoi-su",
                "https://vnexpress.net/kinh-doanh",
                "https://vnexpress.net/so-hoa",
                "https://vnexpress.net/suc-khoe",
                "https://vnexpress.net/the-thao",
                "https://vnexpress.net/giai-tri",
                "https://vnexpress.net/the-gioi",
                "https://vnexpress.net/hai"
        };
        int urlPosition = 0;
        //Find and all article element in listArticle
        for (Category category : categoryList) {
            ArrayList<Article> articleList = scrapeArticle(urls[0]);
            category.setArticleList(articleList);
            ++urlPosition;

        }
        return categoryList;

    }

    @Override
    public Category srapeWebsiteCategory(String categoryName) throws IOException {
        File vnexpressUrlFile = new File("src/sample/vnexpressUrl.txt");
        Scanner urlScanner = new Scanner(vnexpressUrlFile);
        HashMap<String, String> vnexpressUrls = new HashMap<String, String>();
        while (urlScanner.hasNextLine()) {
            String[] url = urlScanner.nextLine().split("\\|");
            vnexpressUrls.put(url[1], url[0]);
        }
        System.out.println(vnexpressUrls);
        //crawl from these site
        Category category = new Category(categoryName);
        ArrayList<Article> articleList = scrapeArticle(vnexpressUrls.get(categoryName));
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
        Elements articleElementList = new Elements(); // Create list of element
        ArrayList<Article> articleList = new ArrayList<>(); //Create list of article
        Document doc = Jsoup.connect(url).get();
        articleElementList.addAll(doc.getElementsByTag("item"));
        // Loop into article Element
        for (Element articleElement : articleElementList) {
            String urlArticle = articleElement.child(3).ownText(); //Link of the article
            String titleArticle = articleElement.child(0).ownText(); // Title of the article
//            String timeArticle;
//            if (!urlArticle.contains("video")) {
//                timeArticle = findTime(urlArticle);
//            } else {
//                timeArticle = null;
//            }
            Image image = null;
//            Document description = Jsoup.parse(articleElement.child(3).ownText());
//            String imageurl = description.getElementsByTag("img").attr("src");
            Document description = Jsoup.parse(articleElement.child(1).ownText());
            String imageurl = description.getElementsByTag("img").attr("src");
            try{
                image = new Image(imageurl);
            } catch (IllegalArgumentException e){
                System.out.println("No image " + articleElement);
            }

//            if (imageurl.contains("vnexpress"))
//                image = new Image(imageurl);

//            if (articleElement.parent().child(0).child(0).childrenSize() >= 1
//                    && articleElement.parent().child(0).child(0).child(0).childrenSize() >= 2) {
//                String imageurl = articleElement.parent().child(0).child(0).child(0).child(1).attr("src");
//                if (imageurl.contains("vnexpress"))
//                    image = new Image(imageurl);
//            }
            Article article = new Article(image, titleArticle, urlArticle, null);
            System.out.println(article.getTitleArticle()); // for debug

            articleList.add(article);
        }
        return articleList;
    }


}
