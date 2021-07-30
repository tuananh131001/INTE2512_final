package sample.news;

import javafx.scene.image.Image;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sample.Article;
import sample.Category;
import sample.News;

import java.io.IOException;
import java.util.ArrayList;

public class Vnexpress implements News {
    @Override
    public ArrayList<Category> srapeWebsite() throws IOException {
        ArrayList<Category> categoryList = createCategory();
        //crawl from these site
        String[] urls = {"https://vnexpress.net",
                "https://vnexpress.net/goc-nhin/covid-19",
                "https://vnexpress.net/thoi-su",
                "https://vnexpress.net/kinh-doanh",
                "https://vnexpress.net/so-hoa",
                "https://vnexpress.net/suc-khoe",
                "https://vnexpress.net/the-thao",
                "https://vnexpress.net/giai-tri",
                "https://vnexpress.net/the-gioi",
                "https://vnexpress.net/hai"
        };
        Elements articleElementList = new Elements();
        int urlPosition = 0;
        //Find and all article element in listArticle
        for (Category category : categoryList) {
            ArrayList<Article> articleList = new ArrayList<>();
            Document doc = Jsoup.connect(urls[urlPosition]).get();
            articleElementList.addAll(doc.getElementsByClass("title-news"));
            // Loop into article Element
            for (Element articleElement : articleElementList) {
                String urlArticle = articleElement.children().first().attr("href"); //Link of the article
                String titleArticle = articleElement.children().first().attr("title"); // Title of the article
                String timeArticle = findTime(urlArticle);
                Image image = null;

                if (articleElement.parent().child(0).child(0).childrenSize() >= 1
                        && articleElement.parent().child(0).child(0).child(0).childrenSize() >= 2) {
                    String imageurl = articleElement.parent().child(0).child(0).child(0).child(1).attr("src");
                    if (!imageurl.contains("vnexpress")) continue;
                    image = new Image(imageurl);
                }
                Article article = new Article(image, titleArticle, urlArticle, timeArticle);
                System.out.println(article);

                articleList.add(article);
            }
            ++urlPosition;
            category.setArticleList(articleList);
        }
        return categoryList;
//        Elements listArticle = new Elements();
//
//        for (String url : urls) {
//            Document doc = Jsoup.connect(url).get();
//            listArticle.addAll(doc.getElementsByClass("item-news"));
//        }
//        //Connect to article and scrape
//        for(Element article : listArticle){
//            ArrayList<Article> articleList = new ArrayList<>();
//            if (article.childrenSize() < 2) continue;
//            String className = article.child(1).attr("class");
//            //Skip ads
//            if(className.equals(""))
//                continue;
//            String name = article.child(0).child(0).attr("title");
//            String url = article.child(0).child(0).attr("href");
//            Image image = null;
//            if (article.child(0).child(0).childrenSize() >= 1 && article.child(0).child(0).child(0).childrenSize() >= 2) {
//                String imageurl = article.child(0).child(0).child(0).child(1).attr("src");
//                if (!imageurl.contains("vnexpress")) continue;
//                image = new Image(imageurl);
//            }
//            // Create object Vnexpress and add to list newsList
//            newsList.add(new Article(image,name,url, "00"));
//        }

    }

    @Override
    public String findTime(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Elements date = doc.getElementsByClass("date");
        String time = date.first().text();
        return time;
    }


}
