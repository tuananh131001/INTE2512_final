package sample.news;

import javafx.scene.image.Image;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sample.Article;
import sample.News;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

public class Zingnews extends News {

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
                try {
                    image = new Image(imageurl);
                } catch (IllegalArgumentException e){
                    System.out.println("Zing News : Image link is error");
                }
                String date = article.getElementsByClass("friendly-time").first().ownText();
                newsList.add(new Article(image, name, articleUrl, date,"Zing News"));
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

        //return clean content
        return content;
    }

    @Override
    public String getFileName(){
        return "src/sample/urlfiles/zingnewsurl.txt";
    }
}
