package advancednews.Model;

import javafx.scene.image.Image;

import java.time.Duration;

public class Article {
    protected Image image;
    protected String titleArticle;
    protected String sourceArticle;
    protected Duration timeArticle;
    protected String source;

    public Article(Image image, String titleArticle, String sourceArticle, Duration timeArticle, String source) {
        this.image = image;
        this.titleArticle = titleArticle;
        this.sourceArticle = sourceArticle;
        this.timeArticle = timeArticle;
        this.source = source;
    }

    public String getTitleArticle() {
        return titleArticle;
    }

    public String getSourceArticle() {
        return sourceArticle;
    }

    public Duration getTimeArticle(){ return timeArticle; }

    public String getSource() { return source; }

    public Image getImageArticle() { return image; }
}
