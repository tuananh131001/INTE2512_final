package sample.news;

import javafx.scene.image.Image;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sample.News;

import java.io.IOException;
import java.util.ArrayList;

public class Vnexpress extends News {
    public Vnexpress(String title, String articleUrl,Image imageArticle) {
        super(title,articleUrl,imageArticle);
    }
    public Vnexpress() {
        super();
    }
    @Override
    protected ArrayList<Vnexpress> crawlVnexpress() throws IOException {
        ArrayList<Vnexpress> newsList = new ArrayList<>();

        //crawl from these site
        String[] urls = {"https://vnexpress.net",
                "https://vnexpress.net/thoi-su",
                "https://vnexpress.net/the-gioi",
                "https://vnexpress.net/kinh-doanh"};

        Elements listArticle = new Elements();
        for (String url : urls) {
            Document doc = Jsoup.connect(url).get();
            listArticle.addAll(doc.getElementsByClass("item-news"));
        }
        for(Element article : listArticle){
            if (article.childrenSize() < 2) continue;
            String className = article.child(1).attr("class");
            //Skip ads
            if(className.equals(""))
                continue;
            String name = article.child(0).child(0).attr("title");
            String url = article.child(0).child(0).attr("href");
            Image image = null;
            if (article.child(0).child(0).childrenSize() >= 1 && article.child(0).child(0).child(0).childrenSize() >= 2) {
                String imageurl = article.child(0).child(0).child(0).child(1).attr("src");
                if (!imageurl.contains("vnexpress")) continue;
                image = new Image(imageurl);
            }
            // Create object Vnexpress and add to list newsList
            newsList.add(new Vnexpress(name,url, image));
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
}
