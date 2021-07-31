package sample;

import javafx.scene.image.Image;
import sample.news.Vnexpress;

import java.io.IOException;
import java.util.ArrayList;

public abstract class News {
    protected String title;
    protected String articleUrl;
    protected Image imageArticle;
    protected String newsName;

    public News(String title, String articleUrl, Image imageArticle) {
        this.title = title;
        this.articleUrl = articleUrl;
        this.imageArticle = imageArticle;
    }

    public News() {
    }

    public Image getImage(){
        return imageArticle;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return this.articleUrl;
    }

    public abstract String getNewsName();

    protected abstract ArrayList<News> crawlNews() throws IOException;
}
