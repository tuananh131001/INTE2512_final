package advancednews.news;

import javafx.scene.image.Image;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import advancednews.Model.Article;
import advancednews.Model.News;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Scanner;

public class Tuoitre extends News {

    @Override
    public ArrayList<Article> scrapeArticle(String url) throws IOException {
        if (url == null) return new ArrayList<>();;
        if (!url.contains("rss")) return scrapeArticleNonRss(url);
        ArrayList<Article> newsList = new ArrayList<>();; //initialize return variable

        Elements listArticle = new Elements(); //initialize article list

        //connect to rss website and add in listArticle all "items"
        Document doc = Jsoup.connect(url).timeout(5000).get();
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
                newsList.add(new Article(image, name, articleUrl, getTimeSince(date),"Tuoi Tre"));
                if (newsList.size() >= 10) break;
            }
        } catch (Exception e){
            System.out.println(e + " tuoi tre");
        }
        return newsList;
    }

    public ArrayList<Article> scrapeArticleNonRss(String url) throws IOException {
        if (url == null) return new ArrayList<>();;
        ArrayList<Article> newsList = new ArrayList<>();; //initialize return variable

        Elements listArticle = new Elements(); //initialize article list

        //connect to rss website and add in listArticle all "items"
        Document doc = Jsoup.connect(url).timeout(5000).get();

        listArticle.addAll(doc.getElementsByClass("news-item"));

        //for each article, get its url, description and url
        try {
            for (Element article : listArticle) {
                String name = article.getElementsByClass("title-news").first().child(0).ownText();
                String articleUrl = "https://tuoitre.vn/" + article.getElementsByTag("a").attr("href");
                Image image = null;
                String imageurl = null;
                Element element = article.getElementsByTag("img").first();
                if (element != null) imageurl = element.attr("src");
                if (imageurl != null && !imageurl.equals("")) {
                    image = new Image(imageurl);
                }
                String date = "";
                try {
                    date = Jsoup.connect(articleUrl).timeout(4000).get().getElementsByClass("date-time").first().ownText();
                } catch (Exception e){
                    System.out.println("skipping an article in tuoi tre..");
                    continue;
                }
                newsList.add(new Article(image, name, articleUrl, getTimeSince(date), "Tuoi Tre"));
                if (newsList.size() >= 10) break;
            }
        } catch (Exception e){
            System.out.println(e + " tuoitre.java");
        }
        return newsList;
    }

    @Override
    public Element scrapeContent(String url) throws IOException {
        //connect to url
        Document content = Jsoup.parse(Jsoup.connect(url).get().toString());

        //removing all elements with such class name
        String[] classesToRemove = {
                "header-top",
                "header-bottom",
                "trending",
                "title-content clearfix first",
                "bannerfooter1",
                "box_can_you_care",
                "tagandnetwork",
                "relate-container"
        };
        for (String className : classesToRemove) {
            Elements remove = content.getElementsByClass(className);
            remove.remove();
        }

        //removing all elements with such tagname
        String[] tagnameToRemove ={
                "footer",
                "header"
        };
        for (String tagname : tagnameToRemove){
            Elements remove = content.getElementsByTag(tagname);
            remove.remove();
        }

        //removing all elements with such ids
        String[] idToRemove = {
                "sticky-box"
        };
        for (String idName : idToRemove){
            Element remove = content.getElementById(idName);
            if (remove != null) remove.remove();
        }
        //return clean content
        return content;
    }

    @Override
    public String getFileName(){
        return "src/advancednews/urlfiles/tuoitreurl.txt";
    }

}