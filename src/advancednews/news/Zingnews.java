package advancednews.news;

import javafx.scene.image.Image;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import advancednews.Model.Article;
import advancednews.Model.News;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

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
                String date = article.getElementsByClass("date").first().ownText();;
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
                "section article-news-background"
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
                "pushed_popup"
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

    public Duration getTimeSince(String dateTime) throws ParseException {
        Scanner scanner = new Scanner(dateTime);
        String day = scanner.findInLine("(\\d+/\\w+/\\d+)");
        scanner = new Scanner(dateTime);
        String time = scanner.findInLine("(\\d+:\\d+:?\\d+)");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy kk:mm");
        Date date = dateFormat.parse(day + " " + time);
        return Duration.between(date.toInstant(), Instant.now());
    }
}
