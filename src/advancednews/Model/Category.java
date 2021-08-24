package advancednews.Model;

import java.util.ArrayList;
import java.util.Collections;

public class Category {
    protected ArrayList<Article> articleList;
    protected String categoryName;
    protected String categoryUrl;

    public Category(){
        this.articleList = new ArrayList<>();
        categoryName = "";
        categoryUrl = "";
    }

    public Category(String categoryName) {
        this.categoryName = categoryName;
        this.articleList = new ArrayList<>();
        this.categoryUrl = "";
    }

    public void setArticleList(ArrayList<Article> articleList) { this.articleList = articleList; }

    public void setCategoryName(String categoryName){ this.categoryName = categoryName; }

    public String getCategoryName() { return categoryName;}

    public ArrayList<Article> getArticleList() {
        return articleList;
    }
}
