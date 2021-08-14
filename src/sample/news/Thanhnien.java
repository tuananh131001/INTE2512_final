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
        if (url == null) return new ArrayList<>();
        if (!url.contains("rss")) return scrapeArticleNonRss(url);
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

    public ArrayList<Article> scrapeArticleNonRss(String url) throws IOException {
        if (url == null) return new ArrayList<>();
        Elements articleElementList = new Elements(); // Create list of element
        ArrayList<Article> articleList = new ArrayList<>(); //Create list of article
        Document doc = Jsoup.connect(url).get();
        articleElementList.addAll(doc.getElementsByClass("story"));
        // Loop into article Element
        for (Element articleElement : articleElementList) {
            String name = articleElement.getElementsByClass("story__title").first().ownText();
            String articleUrl = "https://thanhnien.vn/" + articleElement.getElementsByTag("a").attr("href");
            Image image = null;
            String imageurl = null;
            Element element = articleElement.getElementsByTag("img").first();
            if (element != null) imageurl = element.attr("data-src");
            if (imageurl != null) {
                image = new Image(imageurl);
            }
            String date = Jsoup.connect(articleUrl).get().getElementsByTag("time").first().ownText();
            articleList.add(new Article(image, name, articleUrl, date,"Thanh Nien"));
            if (articleList.size() >= 10) break;
        }
        return articleList;
    }

    @Override
    public Element scrapeContent(String url) throws IOException {
        //connect to url
        Document content = Jsoup.parse(Jsoup.connect(url).get().toString());
        String[] classesToRemove = {
                "site-header",
                "site-header__grid affix-top",
                "details__meta",
                "details__morenews",
                "details__tags",
                "modal fade",
                "modal fade modal-signin",
                "zone zone--comment",
                "zone lastest-news lastest-news--gray",
                "zone zone--media dark-media",
                "as-content",
                "site-footer"
        };
        //removing all elements with such ids
        String[] idToRemove = {
                "dablewidget_x7yEvG76"
        };
        for (String idName : idToRemove){
            Element remove = content.getElementById(idName);
            if (remove != null) remove.remove();
        }

        //remove all hyperlinks while keeping its content
        Elements hrefs = content.getElementsByAttribute("href");
        for (Element remove : hrefs){
            remove.clearAttributes();
        }

        for (String className : classesToRemove) {
            Elements remove = content.getElementsByClass(className);
            remove.remove();
        }
        return content;
    }

    @Override
    public String getFileName(){
        return "src/sample/thanhnienurl.txt";
    }

}
