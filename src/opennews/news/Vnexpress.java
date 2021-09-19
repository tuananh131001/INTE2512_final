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
import java.util.Objects;

public class Vnexpress extends News {

    @Override
    public ArrayList<Article> scrapeArticle(String url) throws IOException {
        if (!url.contains("rss")) return scrapeArticleNonRss(url);
        Elements articleElementList = new Elements(); // Create list of element
        ArrayList<Article> articleList = new ArrayList<>(); //Create list of article
        Document doc = Jsoup.connect(url).timeout(5000).get();
        articleElementList.addAll(doc.getElementsByTag("item"));
        // Loop into article Element
        try {
            for (Element articleElement : articleElementList) {
                String urlArticle = articleElement.child(3).ownText(); //Link of the article
                String titleArticle = articleElement.child(0).ownText(); // Title of the article
                String date = Objects.requireNonNull(articleElement.getElementsByTag("Pubdate").first()).ownText();
                Image image = null;
                Document description = Jsoup.parse(articleElement.child(1).ownText());
                String imageurl = description.getElementsByTag("img").attr("src");
                if (imageurl != null && !imageurl.equals("")) image = new Image(imageurl);
                Article article = new Article(image, titleArticle, urlArticle, getTimeSince(date), "VnExpress");
                articleList.add(article);
                if (articleList.size() >= 10) break;
            }
        } catch (Exception e){
            System.out.println(e + " vnexpress");
        }
        return articleList;
    }

    public ArrayList<Article> scrapeArticleNonRss(String url) throws IOException {
        Elements articleElementList = new Elements(); // Create list of element
        ArrayList<Article> articleList = new ArrayList<>(); //Create list of article

        Document doc = Jsoup.connect(url).timeout(5000).get();

        articleElementList.addAll(doc.getElementsByClass("item-news full-thumb article-topstory"));
        articleElementList.addAll(doc.getElementsByClass("item-news item-news-common"));
        articleElementList.addAll(doc.getElementsByClass("item-news full-thumb "));
        try{
            for (Element articleElement : articleElementList){
                if (articleElement.getElementsByClass("thumb-art").size() == 0) continue;
                //image
                Element element = articleElement.getElementsByTag("img").first();
                String imageurl = null;
                Image image = null;
                if (element != null) imageurl = element.attr("src");
                if (imageurl != null && !imageurl.contains("http")) imageurl = element.attr("data-src");
                if (imageurl != null && !imageurl.equals("")) {
                    image = new Image(imageurl);
                }
                //title
                element = articleElement.getElementsByClass("title-news").first();
                assert element != null;
                String titleArticle = element.child(0).ownText();
                //Url
                element = articleElement.getElementsByTag("a").first();
                assert element != null;
                String urlArticle = element.attr("href");
                try {
                    element = Jsoup.connect(urlArticle).timeout(4000).get();
                } catch (Exception e){
                    System.out.println("skipping an article in vnexpress..");
                    continue;
                }
                //Date
                String date = element.getElementsByAttributeValueMatching("name", "pubdate").attr("content");
                Article article = new Article(image, titleArticle, urlArticle, getTimeSince(date), "VnExpress");
                articleList.add(article);
                if (articleList.size() >= 10) break;
            }
        } catch (Exception e){
            System.out.println(e + " vnexpress.java");
        }
        return articleList;
    }

    @Override
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
                "box_brief_info",
                "cohoi_block",
                "tab_content",
                "container banner-folder box_category clearfix",
                "section section_footer",
                "header_new clearfix hidde-mobile",
                "footer-content  width_common",
                "banner-ads",
                "section page-detail top-detail section-bottom-detail",
                "inner-popup",
                "list-news gaBoxLinkDisplay"
        };
        for (String className : classesToRemove) {
            Elements remove = content.getElementsByClass(className);
            remove.remove();
//            remove.attr("visibility", "false");
        }

        //removing all elements with such ids
        String[] idToRemove = {
                "to_top",
                "supper_masthead",
                "main_menu",
                "animation_container"
        };
        for (String idName : idToRemove){
            Element remove = content.getElementById(idName);
            if (remove != null) remove.remove();
        }

        //removing all elements with such tagname
        String[] tagnameToRemove ={
                "header",
                "footer",
                "form",
                "iframe",
                "svg"
        };
        for (String tagname : tagnameToRemove){
            Elements remove = content.getElementsByTag(tagname);
            remove.remove();
        }

        //changes all video attribute to a text with its link
        Elements videos = content.getElementsByAttributeValueMatching("id","video");
        for (Element video : videos){
            Elements vid = video.getElementsByAttributeValueMatching("src", ".m3u8");
            if (vid.size() > 0) {
                String src = Objects.requireNonNull(vid.first()).attr("src");
                video.clearAttributes();
                if (src != null) {
                    video.text("Video link: " + src);
                }
            }
        }

        //remove all hyperlinks while keeping its content
        Elements hrefs = content.getElementsByAttribute("href");
        for (Element remove : hrefs){
            remove.removeAttr("href");
        }

        //attempt to remove all ads
        Elements ads = content.getElementsByAttributeValueMatching("class", "ads|flexbox|header_new|section_common|top-header|section page-detail bottom-detail");
        for (Element remove : ads){
            remove.remove();
        }
        //return clean content
            return content;
    }

    @Override
    public String getFileName(){
        return "src/opennews/urlfiles/vnexpressurl.txt";
    }
}