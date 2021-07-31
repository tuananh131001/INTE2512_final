package sample.news;

import javafx.scene.image.Image;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sample.News;

import java.io.IOException;
import java.util.ArrayList;

public class Tuoitre extends News {
    public Tuoitre(String title, String articleUrl,Image imageArticle) {
        super(title,articleUrl,imageArticle);
    }
    public Tuoitre() {
        super();
    }
    @Override
    protected ArrayList<News> crawlNews() throws IOException {
        ArrayList<News> newsList = new ArrayList<>();

        //crawl from these site
        String[] urls = {"https://tuoitre.vn/rss/thoi-su.rss"};

        Elements listArticle = new Elements();
        for (String url : urls) {
            Document doc = Jsoup.connect(url).get();
            listArticle.addAll(doc.getElementsByTag("item"));
        }
        for(Element article : listArticle){
            String name = article.child(0).ownText();
            String url = article.child(1).ownText();
            Image image = null;
            Document description = Jsoup.parse(article.child(3).ownText());
            String imageurl = description.getElementsByTag("img").attr("src");
            if (imageurl != null) {
                image = new Image(imageurl);
            }
            // Create object Vnexpress and add to list newsList
            newsList.add(new Tuoitre(name,url, image));
        }
        return newsList;
    }


    public String getTitle() {
        return title;
    }


    public String getUrl() {
        return this.articleUrl;
    }

    public Image getImage(){
        return imageArticle;
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public String getNewsName(){ return "TuoiTre";}
}