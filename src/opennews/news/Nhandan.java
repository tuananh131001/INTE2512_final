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
package opennews.news;

import javafx.scene.image.Image;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import opennews.Model.Article;
import opennews.Model.News;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Nhandan extends News {

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
                //Get title
                String name = Objects.requireNonNull(article.getElementsByTag("a").first()).attr("title");
                if (hs.contains(name)) continue;
                hs.add(name);
                //Get article url
                String articleUrl = "https://nhandan.vn" + article.getElementsByTag("a").attr("href");
                Image image = null;
                String imageurl = null;
                // Get image
                Element element = article.getElementsByTag("img").first();
                if (element != null) imageurl = element.attr("data-src");
                if (imageurl != null && !imageurl.equals("")) {
                    image = new Image(imageurl);
                }
                // Get date of article
                Elements dateElements;
                try {
                     dateElements = Jsoup.connect(articleUrl).timeout(4000).get().getElementsByAttributeValueMatching("class", "box-date");
                } catch (Exception e){
                    System.out.println("skipping an article in Nhan Dan..");
                    continue;
                }
                Element dateElement = null;
                if (dateElements.size() >= 2) dateElement = dateElements.get(1);
                else if (dateElements.size() >= 1) dateElement = dateElements.get(0);
                String date = "";
                if (dateElement != null && dateElement.hasText()) date = dateElement.ownText();
                else {
                    dateElements = article.getElementsByClass("box-meta-small");
                    if (dateElements.size() > 0) dateElement = dateElements.get(0);
                    if (dateElement != null && dateElement.hasText()) date = dateElement.ownText();
                }
                // Add to list the article object
                newsList.add(new Article(image, name, articleUrl, getTimeSince(date),"Nhan Dan"));
                if (newsList.size() >= 10) break;
            }
        } catch (Exception e){
            System.out.println(e + " nhandan.java");
        }
        return newsList;
    }

    @Override
    public Element scrapeContent(String url) throws IOException {
        //connect to url
        Document content = Jsoup.parse(Jsoup.connect(url).get().toString());

        //removing all elements with such ids
        //removing all elements with such class name
        String[] classesToRemove = {
                "headersite",
                "box-widget box-likepage box-likepage-top uk-clearfix",
                "uk-width-1-1 uk-width-1-4@m col-sidebar uk-grid-margin",
                "box-widget box-tags uk-clearfix",
                "box-widget box-likepage uk-clearfix",
                "box-widget box-related",
                "footersite",
                "uk-nav uk-nav-default",
                "box-widget box-widget-tabs ",
                "list-by-topic",
                "box-content",
                "box-header",
                "box-widget boxlist-latest",
                "contref horizontal"
        };
        for (String className : classesToRemove) {
            Elements remove = content.getElementsByClass(className);
            remove.remove();
        }
        //removing all elements with such id
        String[] IdToRemove ={
                "offcanvas-overlay-push"
        };
        for (String idName : IdToRemove){
            Element remove = content.getElementById(idName);
            if (remove != null) remove.remove();
        }

        //change all local page css to its full link
        Elements links = content.getElementsByTag("link");
        for (Element element : links){
            String href = element.attr("href");
            if (!href.contains("nhandan")){
                element.attr("href", "https://nhandan.vn" + href);
            }
        }
        //return clean content
        return content;
    }
    //Get url function for nhan dan
    @Override
    public String getFileName(){
        return "src/opennews/urlfiles/nhandanurl.txt";
    }

    //Function get time return duration
    public java.time.Duration getTimeSince(String dateTime) {
        //Init variable
        Scanner scanner = new Scanner(dateTime);
        String day = scanner.findInLine("(\\d+-\\w+-\\d+)");
        scanner = new Scanner(dateTime);
        String time = scanner.findInLine("(\\d+:\\d+:?\\d+)");
        Date date;
        //Try catch get date follow format then return zero
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy kk:mm");
            date = dateFormat.parse(day + " " + time);
        } catch (Exception e4){
            return Duration.ZERO;
        }
        //return duration
        return Duration.between(date.toInstant(), Instant.now());
    }
}