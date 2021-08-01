package sample.news;

import javafx.scene.image.Image;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sample.Article;
import sample.Category;
import sample.News;

import java.io.IOException;
import java.util.ArrayList;

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
            ArrayList<Article> articleList = scrapeArticle(urls,urlPosition);
            category.setArticleList(articleList);
            ++urlPosition;

        }
        return categoryList;

    }

    @Override
    public Category srapeWebsiteCategory(String categoryName) throws IOException {
        //crawl from these site
        Category category = new Category(categoryName);
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
        int urlPosition = 2;
        ArrayList<Article> articleList = scrapeArticle(urls,urlPosition);
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
    public ArrayList<Article> scrapeArticle(String[] urls,int urlPosition) throws IOException {
        Elements articleElementList = new Elements();
        ArrayList<Article> articleList = new ArrayList<>();
        Document doc = Jsoup.connect(urls[urlPosition]).get();
        articleElementList.addAll(doc.getElementsByClass("title-news"));
        // Loop into article Element
        for (Element articleElement : articleElementList) {
            String urlArticle = articleElement.children().first().attr("href"); //Link of the article
            String titleArticle = articleElement.children().first().attr("title"); // Title of the article
            String timeArticle;
            if (!urlArticle.contains("video")) {
                timeArticle = findTime(urlArticle);
            } else {
                timeArticle = null;
            }
            Image image = null;

            if (articleElement.parent().child(0).child(0).childrenSize() >= 1
                    && articleElement.parent().child(0).child(0).child(0).childrenSize() >= 2) {
                String imageurl = articleElement.parent().child(0).child(0).child(0).child(1).attr("src");
                if (imageurl.contains("vnexpress"))
                    image = new Image(imageurl);
            }
            Article article = new Article(image, titleArticle, urlArticle, timeArticle);
            System.out.println(article.getTitleArticle());

            articleList.add(article);
        }
        return articleList;
    }


}
