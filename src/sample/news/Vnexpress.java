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

public class Vnexpress extends News {

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
                Article article = new Article(image, titleArticle, urlArticle, date, "VnExpress");
                articleList.add(article);
                if (articleList.size() >= 10) break;
            }
        } catch (Exception e){
            System.out.println(e);
        }
        return articleList;
    }

    @Override
    public Element scrapeContent(String url) throws IOException {
        //connect to url
        Document content = Jsoup.parse(Jsoup.connect(url).get().toString());
        content.append("<link rel=" + '\"' + "@stylesheet" + '\"' + "href="+'\"'+"@styles/scrollstyle.css"+'\"' + "media="+'\"'+"screen"+'\"'+">");

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
                "tab_content"
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
            if (remove != null) remove.remove();
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
        Elements ads = content.getElementsByAttributeValueMatching("class", "ads|flexbox");
        for (Element remove : ads){
            remove.remove();
        }
        //return clean content
        return content;
    }

    @Override
    public String getFileName(){
        return "src/sample/vnexpressurl.txt";
    }
}
