package sample.news;

import javafx.scene.image.Image;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sample.Article;
import sample.News;

import java.io.IOException;
import java.util.ArrayList;

public class Thanhnien extends News {

    @Override
    public ArrayList<Article> scrapeArticle(String url) throws IOException {
        Elements articleElementList = new Elements(); // Create list of element
        ArrayList<Article> articleList = new ArrayList<>(); //Create list of article
        Document doc = Jsoup.connect(url).get();
        articleElementList.addAll(doc.getElementsByTag("item"));
        // Loop into article Element
        for (Element articleElement : articleElementList) {
            String titleArticle = articleElement.child(0).ownText(); // Title of the article
            String date = articleElement.getElementsByTag("Pubdate").first().ownText();
            Image image = null;
            Document description = Jsoup.parse(articleElement.child(1).ownText());
            String urlArticle = description.getElementsByTag("a").attr("href"); //Link of the article
            String imageurl = description.getElementsByTag("img").attr("src");
            try{
                image = new Image(imageurl);
            } catch (IllegalArgumentException e){
            }
            Article article = new Article(image, titleArticle, urlArticle, date, "Thanh Nien");
            articleList.add(article);
            if (articleList.size() >= 10) break;
        }
        return articleList;
    }
    @Override
    public Element scrapeContent(String url) throws IOException {
        //connect to url
        Document content = Jsoup.parse(Jsoup.connect(url).get().toString());
        return content;
    }

}
