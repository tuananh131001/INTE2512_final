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

public class Tuoitre extends News {

    @Override
    public ArrayList<Article> scrapeArticle(String url) throws IOException {
        if (url == null) return new ArrayList<>();
        if (!url.contains("rss")) return scrapeArticleNonRss(url);
        ArrayList<Article> newsList = new ArrayList<>(); //initialize return variable

        Elements listArticle = new Elements(); //initialize article list

        //connect to rss website and add in listArticle all "items"
        Document doc = Jsoup.connect(url).get();
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
                newsList.add(new Article(image, name, articleUrl, date,"Tuoi Tre"));
                if (newsList.size() >= 10) break;
            }
        } catch (Exception e){
            System.out.println(e);
        }
        return newsList;
    }

    public ArrayList<Article> scrapeArticleNonRss(String url) throws IOException {
        if (url == null) return new ArrayList<>();
        ArrayList<Article> newsList = new ArrayList<>(); //initialize return variable

        Elements listArticle = new Elements(); //initialize article list

        //connect to rss website and add in listArticle all "items"
        Document doc = Jsoup.connect(url).get();
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
                if (imageurl != null) {
                    image = new Image(imageurl);
                }
                String date = Jsoup.connect(articleUrl).get().getElementsByClass("date-time").first().ownText();
                newsList.add(new Article(image, name, articleUrl, date,"Tuoi Tre"));
                if (newsList.size() >= 10) break;
            }
        } catch (Exception e){
            System.out.println(e);
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
                "header-top",
                "header-bottom",
                "trending",
                "title-content clearfix first",
                "bannerfooter1",
                "box_can_you_care",
                "tagandnetwork"
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
        //return clean content
        return content;
    }

    @Override
    public String getFileName(){
        return "src/sample/tuoitreurl.txt";
    }
}