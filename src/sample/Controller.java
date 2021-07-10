package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import org.jsoup.Jsoup;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sample.news.Vnexpress;

import java.io.IOException;

public class Controller {
    @FXML
    private TextArea titleNewspaper;

    @FXML
    private ListView<Vnexpress> vnexpressListView;
    @FXML
    private TextArea contentTextArea;

    public void initialize() {
        try {
//            Vnexpress new1 = new Vnexpress("Java IO Tutorial","http");
//            Vnexpress new2 = new Vnexpress("1 IO Tutorial","http");
//            Vnexpress new3 = new Vnexpress("2 IO Tutorial","http");
            // Connect to Vnexpress and scrape
            ObservableList<Vnexpress> newList = FXCollections.observableArrayList();

            Document doc = Jsoup.connect("https://vnexpress.net/thoi-su").get();
            Elements body = doc.select("div.col-left-folder-v2");
            Element ele = body.first();
            Elements listArticle = ele.child(0).children();


            for(Element article : listArticle){
                String className = article.child(1).attr("class").toString() ;
                if( className == "" )
                    continue;
                String name = article.child(0).child(0).attr("title");
                String url = article.child(0).child(0).attr("href");
                Vnexpress news = new Vnexpress(name,url);
                newList.add(news);

            }
            // Display each article to listview
            vnexpressListView.getItems().setAll(newList);
            vnexpressListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        } catch (IOException e) {
            System.out.println(e);
        }
    }
    //Function return the content inside the article
    public String returnContent(String url) throws IOException{
        Document doc = Jsoup.connect(url).get();
        Elements body = doc.getElementsByClass("sidebar-1");
        Element content = body.first().child(2);
        return content.text();
    }
    // Function process when user click on the article
    @FXML
    public void handleClickView() throws IOException{
        Vnexpress news = (Vnexpress) vnexpressListView.getSelectionModel().getSelectedItem();
//        System.out.println("The select item is " + news);
        String content = returnContent(news.getUrl());
        contentTextArea.setText(content);

    }
}
