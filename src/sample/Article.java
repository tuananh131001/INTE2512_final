package sample;

import javafx.scene.image.Image;
import org.jsoup.nodes.Element;

public class Article {
    protected Image imageArticle;
    protected String titleArticle;
    protected String sourceArticle;
    protected String timeArticle;
    protected String source;
    protected Element content;

    public Article(Image imageArticle, String titleArticle, String sourceArticle, String timeArticle, String source) {
        this.imageArticle = imageArticle;
        this.titleArticle = titleArticle;
        this.sourceArticle = sourceArticle;
        this.timeArticle = timeArticle;
        this.source = source;
    }
    public Article(Image imageArticle, String titleArticle, String sourceArticle, String timeArticle, String source, Element content) {
        this.imageArticle = imageArticle;
        this.titleArticle = titleArticle;
        this.sourceArticle = sourceArticle;
        this.timeArticle = timeArticle;
        this.source = source;
        this.content = content;
    }

    public Image getImageArticle() {
        return imageArticle;
    }

    public String getTitleArticle() {
        return titleArticle;
    }

    public String getSourceArticle() {
        return sourceArticle;
    }

    public String getTimeArticle(){ return timeArticle; }

    public String getSource() { return source; }

    public Element getContent() { return content; }
}
