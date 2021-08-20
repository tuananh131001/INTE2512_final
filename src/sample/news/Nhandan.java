package sample.news;

import javafx.scene.image.Image;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;
import sample.Article;
import sample.News;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

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
                String name = article.getElementsByTag("a").first().attr("title");
                if (hs.contains(name)) continue;
                hs.add(name);
                String articleUrl = "https://nhandan.vn" + article.getElementsByTag("a").attr("href");
                Image image = null;
                String imageurl = null;
                Element element = article.getElementsByTag("img").first();
                if (element != null) imageurl = element.attr("data-src");
                if (imageurl != null && !imageurl.equals("")) {
                    image = new Image(imageurl);
                }
                Elements dateElements;
                try {
                     dateElements = Jsoup.connect(articleUrl).timeout(500).get().getElementsByAttributeValueMatching("class", "box-date");
                } catch (Exception e){
                    continue;
                }
                Element dateElement = null;
                if (dateElements.size() >= 2) dateElement = dateElements.get(1);
                String date = "";
                if (dateElement != null && dateElement.hasText()) date = dateElement.ownText();
                else {
                    dateElements = article.getElementsByClass("box-meta-small");
                    if (dateElements.size() > 0) dateElement = dateElements.get(0);
                    if (dateElement != null && dateElement.hasText()) date = dateElement.ownText();
                }
                newsList.add(new Article(image, name, articleUrl, date,"Nhan Dan"));
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
                "box-widget box-tags uk-clearfix",
                "box-widget box-likepage uk-clearfix",
                "box-widget box-related",
                "footersite",
                "uk-nav uk-nav-default",
                "box-widget box-widget-tabs "
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

    @Override
    public String getFileName(){
        return "src/sample/urlfiles/Nhandanurl.txt";
    }
}