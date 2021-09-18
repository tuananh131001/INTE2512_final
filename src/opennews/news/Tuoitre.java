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

import opennews.Model.Article;
import opennews.Model.News;
import javafx.scene.image.Image;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;

public class Tuoitre extends News {

    @Override
    public ArrayList<Article> scrapeArticle(String url) throws IOException {
        if (url == null) return new ArrayList<>();
        if (!url.contains("rss")) return scrapeArticleNonRss(url);
        ArrayList<Article> newsList = new ArrayList<>(); //initialize return variable

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
        if (url == null) return new ArrayList<>();
        ArrayList<Article> newsList = new ArrayList<>(); //initialize return variable

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
                try {
                    element = Jsoup.connect(articleUrl).timeout(4000).get();
                } catch (Exception e){
                    System.out.println("skipping an article in Tuoitre..");
                    continue;
                }
                String date = element.getElementsByAttributeValueMatching("name", "pubdate").attr("content");
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
                "relate-container",
                "VCSortableInPreviewMode"
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
        //changes all video attribute to a text with its link
        Elements videos = content.select("[data-src]");
        for (Element video : videos){
            String videoUrl;
            try {
                videoUrl = video.getElementsByAttributeValueMatching("data-src", "vcplayer").first().attr("data-src");
            } catch (NullPointerException E) {
                break;
            }
            video.text("Video link: " + videoUrl);
        }
        //remove all hyperlinks while keeping its content
        Elements hrefs = content.getElementsByAttribute("href");
        for (Element remove : hrefs){
            if (remove.attr("href").contains("tuoitre") && remove.tagName().contains("a")) {
                remove.clearAttributes();
                remove.tagName("a");
            }
        }

        //return clean content
        return content;
    }

    @Override
    public String getFileName(){
        return "src/opennews/urlfiles/tuoitreurl.txt";
    }

}