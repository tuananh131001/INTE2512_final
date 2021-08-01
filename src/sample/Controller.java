package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.jsoup.Jsoup;
import javafx.fxml.FXML;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sample.news.Vnexpress;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;

public class Controller implements Initializable {
    @FXML
    private TextArea titleNewspaper;

    @FXML
    private ListView<Article> vnexpressListView;
    @FXML
    private TextArea contentTextArea;

    //Delare webview
    @FXML
    private WebView newsScene;

    private WebEngine engine;
    private List<Article> newsList;

    @Override
    public void initialize(URL url1, ResourceBundle resourceBundle) {
        try {
            Vnexpress vnexpress = new Vnexpress();
//            ArrayList<Category> vnexpressCategoryList = vnexpress.srapeWebsite();
//            newsList = vnexpressCategoryList.get(0).getArticleList();
            Category vnexpressCategory = vnexpress.srapeWebsiteCategory("Thoi Su");
            newsList = vnexpressCategory.getArticleList();
            // Function to update image next to cell of dat article
            vnexpressListView.setCellFactory(param -> new ListCell<Article>() {
            private ImageView imageView = new ImageView();
            @Override
            public void updateItem(Article page, boolean empty) {
                super.updateItem(page, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (page.getImageArticle() != null) {
                        HBox box= new HBox();
                        box.setSpacing(10) ;

                        imageView.setFitHeight(50);
                        imageView.setFitWidth(50);

                        imageView.setImage(page.getImageArticle());

                    }
                    setGraphic(imageView);
                    setText(page.getTitleArticle());
                    setWrapText(true);
                }
            }
        });
        vnexpressListView.getItems().setAll(newsList);
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
        Article news = (Article) vnexpressListView.getSelectionModel().getSelectedItem();
        if (news == null) return;
//        System.out.println("The select item is " + news);
        loadPage(news.getSourceArticle());
    }
}
