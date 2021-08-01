package sample;

import java.util.ArrayList;

public class Category {
    protected ArrayList<Article> articleList;
    protected String categoryName;
    protected String categoryUrl;

    public Category(String categoryName) {
        this.categoryName = categoryName;
        this.articleList = null;
        this.categoryUrl = null;
    }

    public void setArticleList(ArrayList<Article> articleList) {
        this.articleList = articleList;
    }

    public ArrayList<Article> getArticleList() {
        return articleList;
    }
}
