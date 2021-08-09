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
    public String findTime(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Elements date = doc.getElementsByClass("date");
        return date.first().text();
    }

    @Override
    public ArrayList<Article> scrapeArticle(String url) throws IOException {
        Elements articleElementList = new Elements(); // Create list of element
        ArrayList<Article> articleList = new ArrayList<>(); //Create list of article
        Document doc = Jsoup.connect(url).get();
        articleElementList.addAll(doc.getElementsByTag("item"));
        // Loop into article Element
        try {
            for (Element articleElement : articleElementList) {
                String urlArticle = articleElement.child(3).ownText(); //Link of the article
                String titleArticle = articleElement.child(0).ownText(); // Title of the article
                String date = articleElement.getElementsByTag("Pubdate").first().ownText();
                Image image = null;
                Document description = Jsoup.parse(articleElement.child(1).ownText());
                String imageurl = description.getElementsByTag("img").attr("src");
                if (imageurl != null && !imageurl.equals("")) image = new Image(imageurl);
                Article article = new Article(image, titleArticle, urlArticle, date, "VnExpress", scrapeContent(urlArticle));
                articleList.add(article);
            }
        } catch (Exception e){
            System.out.println(e);
        }
        return articleList;
    }
    public Element scrapeContent(String url) throws IOException {
        //connect to url
        Document content = Jsoup.parse(Jsoup.connect(url).get().toString());

        //removing all elements with such class name
        String[] classesToRemove = {
                "section header",
                "section top-header",
                "parent",
                "sidebar-2",
                "social_pin",
                "section page-detail middle-detail",
                "section page-detail bottom-detail",
                "footer container",
                "list-news",
                "footer-content  width_common",
                "box_brief_info"
        };
        for (String className : classesToRemove) {
            Elements remove = content.getElementsByClass(className);
            remove.remove();
        }

        //removing all elements with such ids
        String[] idToRemove = {
                "to_top"
        };
        for (String idName : idToRemove){
            Element remove = content.getElementById(idName);
            remove.remove();
        }

        //removing all elements with such tagname
        String[] tagnameToRemove ={
                "header",
                "footer"
        };
        for (String tagname : tagnameToRemove){
            Elements remove = content.getElementsByTag(tagname);
            remove.remove();
        }

        //remove all hyperlinks while keeping its content
        Elements hrefs = content.getElementsByAttribute("href");
        for (Element remove : hrefs){
            remove.clearAttributes();
        }

        //attempt to remove all ads
        Elements ads = content.getElementsByAttributeValueMatching("class", "ads");
        for (Element remove : ads){
            remove.remove();
        }

        //return clean content
        return content;
    }

}
