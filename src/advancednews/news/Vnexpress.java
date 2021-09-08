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
                String date = articleElement.getElementsByTag("Pubdate").first().ownText();
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
                Element element = articleElement.getElementsByTag("img").first();
                String imageurl = null;
                Image image = null;
                if (element != null) imageurl = element.attr("src");
                if (imageurl != null && !imageurl.contains("http")) imageurl = element.attr("data-src");
                if (imageurl != null && !imageurl.equals("")) {
                    image = new Image(imageurl);
                }
                element = articleElement.getElementsByClass("title-news").first();
                String titleArticle = element.child(0).ownText();
                element = articleElement.getElementsByTag("a").first();
                String urlArticle = element.attr("href");
                try {
                    element = Jsoup.connect(urlArticle).timeout(4000).get();
                } catch (Exception e){
                    System.out.println("skipping an article in vnexpress..");
                    continue;
                }
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
                "footer-content  width_common"
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
                "main_menu"
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
                "iframe"
        };
        for (String tagname : tagnameToRemove){
            Elements remove = content.getElementsByTag(tagname);
            remove.remove();
        }

        //changes all video attribute to a text with its link
        Elements videos = content.getElementsByAttributeValueMatching("src",".m3u8");
        for (Element video : videos){
            Element vid = video.getElementsByAttributeValueMatching("src", "vnecdn").first();
            String src = vid.attr("src");
            if (src != null) {
                video.text("Video link: " + src);
            }
        }

        //remove all hyperlinks while keeping its content
        Elements hrefs = content.getElementsByAttribute("href");
        for (Element remove : hrefs){
            remove.clearAttributes();
            remove.tagName("p");
        }

        //attempt to remove all ads
        Elements ads = content.getElementsByAttributeValueMatching("class", "ads|flexbox|header_new|section_common|top-header");
        for (Element remove : ads){
            remove.remove();
        }
        //return clean content
            return content;
    }

    @Override
    public String getFileName(){
        return "src/advancednews/urlfiles/vnexpressurl.txt";
    }
}