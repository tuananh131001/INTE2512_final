/*
  RMIT University Vietnam
  Course: INTE2512 Object-Oriented Programming
  Semester: 2021B
  Assessment: Final Project
  Created  date: 09/06/2021
  Author:
  Nguyen Tuan Anh s3864077
  Tran Nguyen Ha Khanh s3877707
  Nguyen Vu Minh Duy s3878076
  Phan Thanh Phu s3877814
  Ngo Thanh Nguyen s3856221
  Last modified date: 14/09/2021
  Acknowledgement:
  http://www.java2s.com/Tutorials/Java/JavaFX_How_to/Image/Load_an_Image_from_local_file_system.htm
  https://docs.oracle.com/javafx/2/webview/jfxpub-webview.htm
  https://stackoverflow.com/questions/6530974/getting-a-property-value-and-passing-it-on-to-superclass/6531076#6531076
  https://stackoverflow.com/questions/47743650/javafx-8-property-bindings-for-custom-objects
  https://stackoverflow.com/questions/21083945/how-to-avoid-not-on-fx-application-thread-currentthread-javafx-application-th
  https://stackoverflow.com/questions/541487/implements-runnable-vs-extends-thread-in-java?page=2&tab=votes#tab-top
  https://stackoverflow.com/questions/4691533/java-wait-for-thread-to-finish
  https://stackoverflow.com/questions/13946372/adding-css-file-to-stylesheets-in-javafx
*/
package advancednews.news;

import advancednews.Model.Article;
import advancednews.Model.News;
import javafx.scene.image.Image;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class Zingnews extends News {

    @Override
    public ArrayList<Article> scrapeArticle(String url) throws IOException {
        if (url == null) return new ArrayList<>();
        return scrapeArticleNonRss(url);
    }

    public ArrayList<Article> scrapeArticleNonRss(String url) throws IOException {
        if (url == null) return new ArrayList<>();
        ArrayList<Article> newsList = new ArrayList<>(); //initialize return variable

        Elements listArticle = new Elements(); //initialize article list

        //connect to rss website and add in listArticle all "items"
        Document doc = Jsoup.connect(url).timeout(5000).get();

        listArticle.addAll(doc.getElementsByTag("article"));

        HashSet<String> hs = new HashSet<>();

        //for each article, get its url, description and url
        try {
            for (Element article : listArticle) {
                String name = article.getElementsByClass("article-title").text();
                if (hs.contains(name)) continue;
                hs.add(name);
                String articleUrl = "https://zingnews.vn" + article.getElementsByTag("a").first().attr("href");
                Image image = null;
                String imageurl = null;
                Element element = article.getElementsByTag("img").first();


                if (element != null) imageurl = element.attr("src");
                assert imageurl != null;
                if (imageurl.contains("gif")) imageurl = element.attr("data-src");
                try {
                    image = new Image(imageurl);
                } catch (IllegalArgumentException e){
                    System.out.println("Zing News : Image link is error");

                }
                String date = article.getElementsByClass("date").first().ownText();
                Elements elements = article.getElementsByClass("time");
                String time = "";
                if (elements.size() > 0) time = elements.first().ownText();
                newsList.add(new Article(image, name, articleUrl, getTimeSince(date + " " + time),"Zing News"));
                if (newsList.size() >= 10) break;
            }
        } catch (Exception e){
            System.out.println(e + " zingnews.java");
        }
        return newsList;
    }

    @Override
    public Element scrapeContent(String url) throws IOException {
        //connect to url
        Document content = Jsoup.parse(Jsoup.connect(url).get().toString());

        //removing all elements with such class name
        String[] classesToRemove = {
                "the-article-tags",
                "section recommendation has-sidebar",
                "sidebar",
                "sticky-header sticky-header--show",
                "section article-news-background",
                "z-widget-corona"
        };
        for (String className : classesToRemove) {
            Elements remove = content.getElementsByClass(className);
            remove.remove();
        }
        //removing all elements with such tagname
        String[] tagNameToRemove ={
                "header",
                "strong",
                "footer"
        };
        for (String tagName : tagNameToRemove){
            Elements remove = content.getElementsByTag(tagName);
            for (Element element : remove) {
                if (element == null) continue;
                String attr = element.attr("class");
                if (attr != null && attr.equals("the-article-header")) continue;
                element.remove();
            }
        }
        //removing all elements with such ids
        String[] idToRemove = {
                "pushed_popup",
                "trending",
                "innerarticle",
                "zing-header",
                "article-nextreads",
                "site-header"
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
        return "src/advancednews/urlfiles/zingnewsurl.txt";
    }
}
