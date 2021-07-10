package sample.news;

public class Vnexpress {
    private String title;
    private String url;
    private String content;

    public Vnexpress(String title, String url) {
        this.title = title;
        this.url = url;
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

    public void setContent(String content) {
        this.content = content;
    }

    // override for listview display the title
    @Override
    public String toString() {
        return title;
    }
}
