package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.jsoup.Jsoup;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sample.news.Vnexpress;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private TextArea titleNewspaper;

    @FXML
    private ListView<Vnexpress> vnexpressListView;
    @FXML
    private TextArea contentTextArea;

    //Delare webview
    @FXML
    private WebView newsScene;

    private WebEngine engine;


    @Override
    public void initialize(URL url1, ResourceBundle resourceBundle) {
        try {
//            Vnexpress new1 = new Vnexpress("Java IO Tutorial","http");
//            Vnexpress new2 = new Vnexpress("1 IO Tutorial","http");
//            Vnexpress new3 = new Vnexpress("2 IO Tutorial","http");
            ObservableList<Vnexpress> newList = FXCollections.observableArrayList();

//            Document doc = Jsoup.connect("https://vnexpress.net/thoi-su").get();
//            Elements body = doc.select("div.col-left-folder-v2");
//            Element ele = body.first();
//            Elements listArticle = ele.child(0).children();
//            String[] urls = {"https://zingnews.vn/"};
            String[] urls = {"https://vnexpress.net", "https://vnexpress.net/thoi-su", "https://vnexpress.net/the-gioi", "https://vnexpress.net/kinh-doanh"};

            Elements listArticle = new Elements();
            for (String url : urls) {
                Document doc = Jsoup.connect(url).get();
                listArticle.addAll(doc.getElementsByClass("item-news"));
            }
            for(Element article : listArticle){
                if (article.childrenSize() < 2) continue;
                String className = article.child(1).attr("class");
                if(className.equals(""))
                    continue;
                String name = article.child(0).child(0).attr("title");
                String url = article.child(0).child(0).attr("href");
                Image image = null;
                if (article.child(0).child(0).childrenSize() >= 1 && article.child(0).child(0).child(0).childrenSize() >= 2) {
                    String imageurl = article.child(0).child(0).child(0).child(1).attr("src");
                    if (!imageurl.contains("vnexpress")) continue;
                    image = new Image(imageurl);
                }
                Vnexpress news = new Vnexpress(name,url, image);
                newList.add(news);
            }
            vnexpressListView.setCellFactory(param -> new ListCell<Vnexpress>() {
                private ImageView imageView = new ImageView();
                @Override
                public void updateItem(Vnexpress page, boolean empty) {
                    super.updateItem(page, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        if (page.getImage() != null) {
                            imageView.setImage(page.getImage());
                            imageView.setFitHeight(70);
                            imageView.setFitWidth(70);
                        }
                        setGraphic(imageView);
                        setText(page.getTitle());
                        setWrapText(true);
                    }
                }
            });
            vnexpressListView.getItems().setAll(newList);
            vnexpressListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            engine = newsScene.getEngine(); //initialise the engine web view

        } catch (Exception e) {
            System.out.println(e);
        }
    }
    // Function load page
    public void loadPage(String url) throws Exception{
        engine.load(url);
    }
    @FXML
    public void handleClickView() throws Exception{
        Vnexpress news = (Vnexpress) vnexpressListView.getSelectionModel().getSelectedItem();
        if (news == null) return;
//        System.out.println("The select item is " + news);
        loadPage(news.getUrl());
    }
}
