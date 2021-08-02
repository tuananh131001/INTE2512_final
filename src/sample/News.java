package sample;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public interface News {
    public ArrayList<Category> scrapeWebsite() throws IOException;

    public Category scrapeWebsiteCategory(String nameCategory) throws IOException;

    public String findTime(String url) throws IOException;

    public ArrayList<Article> scrapeArticle(String url) throws IOException;

    public default ArrayList<Category> createCategory() {
        ArrayList<Category> category = new ArrayList<Category>();
        category.add(new Category("New"));
        category.add(new Category("Covid"));
        category.add(new Category("Politics"));
        category.add(new Category("Business"));
        category.add(new Category("Technology"));
        category.add(new Category("Health"));
        category.add(new Category("Sports"));
        category.add(new Category("Entertainment"));
        category.add(new Category("World"));
        category.add(new Category("Others"));
        return category;
    }
}
