package sample;

import javafx.scene.image.Image;
import sample.news.Vnexpress;

import java.io.IOException;
import java.util.ArrayList;

public abstract class News {
    protected String title;
    protected String articleUrl;
    protected Image imageArticle;

    public News(String title, String articleUrl, Image imageArticle) {
        this.title = title;
        this.articleUrl = articleUrl;
        this.imageArticle = imageArticle;
    }

    public News() {
    }

    protected abstract ArrayList<Vnexpress> crawlVnexpress() throws IOException;
}
