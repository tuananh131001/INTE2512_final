package sample.news;

import javafx.scene.image.Image;

public class Vnexpress {
    private String title;
    private String url;
    private String content;
    private Image image;

    public Vnexpress(String title, String url, Image image) {
        this.title = title;
        this.url = url;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public Image getImage(){
        return image;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return title;
    }
}
