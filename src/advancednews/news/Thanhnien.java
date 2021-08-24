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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Scanner;

public class Thanhnien extends News {

    @Override
    public ArrayList<Article> scrapeArticle(String url) throws IOException {
        if (url == null) return new ArrayList<>();
        if (!url.contains("rss")) return scrapeArticleNonRss(url);
        Elements articleElementList = new Elements(); // Create list of element
        ArrayList<Article> articleList = new ArrayList<>(); //Create list of article

        Document doc = Jsoup.connect(url).timeout(5000).get();

        articleElementList.addAll(doc.getElementsByTag("item"));
        // Loop into article Element
        try {
            for (Element articleElement : articleElementList) {
                String titleArticle = articleElement.child(0).ownText(); // Title of the article
                String date = articleElement.getElementsByTag("Pubdate").first().ownText();
                Image image = null;
                Document description = Jsoup.parse(articleElement.child(1).ownText());
                String urlArticle = description.getElementsByTag("a").attr("href"); //Link of the article
                String imageurl = description.getElementsByTag("img").attr("src");
                if (imageurl != null && !imageurl.equals("")) {
                    image = new Image(imageurl);
                }
                Article article = new Article(image, titleArticle, urlArticle, getTimeSince(date), "Thanh Nien");
                articleList.add(article);
                if (articleList.size() >= 10) break;
            }
        } catch (Exception e){
            System.out.println(e + " thanhnien.java");
        }
        return articleList;
    }

    public ArrayList<Article> scrapeArticleNonRss(String url) throws IOException {
        if (url == null) return new ArrayList<>();;
        Elements articleElementList = new Elements(); // Create list of element
        ArrayList<Article> articleList = new ArrayList<>();; //Create list of article

        Document doc = Jsoup.connect(url).timeout(5000).get();

        articleElementList.addAll(doc.getElementsByClass("story"));
        // Loop into article Element
        try {
            for (Element articleElement : articleElementList) {
                String name = articleElement.getElementsByClass("story__title").first().ownText();
                String articleUrl = "https://thanhnien.vn/" + articleElement.getElementsByTag("a").attr("href");
                Image image = null;
                String imageurl = null;
                Element element = articleElement.getElementsByTag("img").first();
                if (element != null) imageurl = element.attr("data-src");
                if (imageurl != null && !imageurl.equals("")) {
                    image = new Image(imageurl);
                }
                String date = "";
                try {
                    Document document = Jsoup.connect(articleUrl).timeout(4000).get();
                    date = document.getElementsByTag("time").first().ownText();
                } catch (Exception e) {
                    System.out.println("skipping an article in Thanh Nien..");
                    continue;
                }
                articleList.add(new Article(image, name, articleUrl, getTimeSince(date), "Thanh Nien"));
                if (articleList.size() >= 10) break;
            }
        } catch (Exception e){
            System.out.println(e + " thanhnien.java");
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
                "details__morenews",
                "details__tags",
                "modal fade",
                "details__meta",
                "modal fade modal-signin",
                "zone zone--comment",
                "zone lastest-news lastest-news--gray",
                "zone zone--media dark-media",
                "as-content",
                "site-footer",
                "floating-bar affix-top",
                "floating-bar affix",
                "floating-bar",
                "media-list",
                "details__bottombanner",
                "native-ad.",
                "inread-ads",
                "site-header__grid affix has-ss-box",
                "zone__content",
                "site-footer",
                "sidebar sidebar--col300",
                "body--h"
        };
        for (String className : classesToRemove) {
            Elements remove = content.getElementsByClass(className);
            remove.remove();
        }

        //removing all elements with such ids
        String[] idToRemove = {
                "dablewidget_x7yEvG76",
                "dablewidget_1oVpGklP",
                "dablewidget_AlmNaEX1",
                "animation_container",
                "dablewidget_1oVpGklP",
                "dablewidget",
                "abde"
        };
        for (String idName : idToRemove){
            Element remove = content.getElementById(idName);
            if (remove != null) remove.remove();
        }

        String[] tagToRemove = {
//                "nav",
                "iframe"
        };
        for (String tagName : tagToRemove){
            Elements remove = content.getElementsByTag(tagName);
            remove.remove();
        }


        //change all local page css to its full link
        Elements links = content.getElementsByTag("link");
        for (Element element : links){
            String href = element.attr("href");
            if (href.contains("css") && !href.contains("https")){
                element.attr("href", "https:" + href);
            }
        }

        return content;
    }

    @Override
    public String getFileName(){
        return "src/advancednews/urlfiles/thanhnienurl.txt";
    }

    public Duration getTimeSince(String dateTime) throws ParseException {
        Scanner scanner = new Scanner(dateTime);
        String day = scanner.findInLine("(\\d+ \\w+ \\d+)");
        if (day == null) day = scanner.findInLine("(\\d+/\\d+/\\d+)");
        scanner = new Scanner(dateTime);
        String time = scanner.findInLine("(\\d+:\\d+:?\\d+)");
        SimpleDateFormat dateFormat;
        Date date;
        try {
            dateFormat = new SimpleDateFormat("dd MM yyyy kk:mm:ss");
            date =  dateFormat.parse(day + " " + time);
        } catch (Exception e){
            try {
                dateFormat = new SimpleDateFormat("dd MMM yyyy kk:mm:ss");
                date = dateFormat.parse(day + " " + time);
            } catch (Exception e2){
                dateFormat = new SimpleDateFormat("dd/MM/yyyy kk:mm");
                date = dateFormat.parse(day + " " + time);
            }
        }
        return Duration.between(date.toInstant(), Instant.now());
    }
}
