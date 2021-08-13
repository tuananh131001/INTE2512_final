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
import java.util.Map;
import java.util.Scanner;

public class ZingNews extends News {

    @Override
    public ArrayList<Article> scrapeArticle(String url) throws IOException {
        // Create list of element
        Elements articleElementList = new Elements();
        //Create list of article
        ArrayList<Article> articleList = new ArrayList<>();
        Document doc = Jsoup.connect(url).get();
        articleElementList.addAll(doc.getElementsByTag("item"));

        // Loop into article Element
        for (Element articleElement : articleElementList) {
            String titleArticle = articleElement.child(0).ownText(); // Title of the article
            String date = articleElement.getElementsByTag("Pubdate").first().ownText();
            Image image = null;
            Document description = Jsoup.parse(articleElement.child(1).ownText());
            String urlArticle = description.getElementsByTag("a").attr("href");
            String imageurl = description.getElementsByTag("img").attr("src");
            try{
                image = new Image(imageurl);
            } catch (IllegalArgumentException e){
            }

            Article article = new Article(image, titleArticle, urlArticle, date, "ZingNews");

            articleList.add(article);
        }
        return articleList;
    }


}
