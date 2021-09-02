package advancednews.Model;

import java.util.ArrayList;

public class Category {
    protected ArrayList<Article> articleList;
    protected String categoryName;
    protected String categoryUrl;


    public Category(String categoryName) {
        this.categoryName = categoryName;
        this.articleList = new ArrayList<>();
        this.categoryUrl = "";
    }

    public void setArticleList(ArrayList<Article> articleList) {
        this.articleList = articleList;
    }


    public ArrayList<Article> getArticleList() {
        return articleList;
    }
}
