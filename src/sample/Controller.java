package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.jsoup.Jsoup;
import javafx.fxml.FXML;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sample.news.Thanhnien;
import sample.news.Tuoitre;
import sample.news.Vnexpress;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;

import javafx.scene.text.TextAlignment;

public class Controller implements Initializable {
    @FXML
    private TextArea titleNewspaper;


    @FXML
    private TextArea contentTextArea;
    @FXML
    private VBox vboxApp;
    //Delare webview
//    @FXML
//    private WebView newsScene;
//
//    private WebEngine engine;
    private ArrayList<Article> newsList;

    @Override
    public void initialize(URL url1, ResourceBundle resourceBundle) {
        try {
//            ArrayList<Category> vnexpressCategoryList = vnexpress.srapeWebsite();
//            newsList = vnexpressCategoryList.get(0).getArticleList();

//            Vnexpress vnexpress = new Vnexpress();
//            Category vnexpressCategory = vnexpress.scrapeWebsiteCategory("Politics");
//            newsList = vnexpressCategory.getArticleList();
//            newsList = new ArrayList<Article>();
//            Tuoitre tuoitre = new Tuoitre();
//            ArrayList <Category> tuoitreCategories = tuoitre.scrapeWebsite();
            Thanhnien thanhnien = new Thanhnien();
            Category thanhnienCategory = thanhnien.scrapeWebsiteCategory("Politics", new File("src/sample/vnexpressurl.txt"));
            newsList = thanhnienCategory.getArticleList();
            for(Article article : newsList) {
                HBox hbox = new HBox();
                if (article.getImageArticle() != null) {
                    ImageView imageView = new ImageView(article.getImageArticle());
                    imageView.setFitHeight(100);
                    imageView.setFitWidth(100);

                    hbox.getChildren().add(imageView);

                }
                Label label = new Label(article.getTitleArticle());
                label.setFont(new Font("Arial", 24));

                vboxApp.setPadding(new Insets(30));

                hbox.getChildren().add(label);
                vboxApp.getChildren().add(hbox);
            }
            // Function to update image next to cell of dat article
//            vnexpressListView.setCellFactory(param -> new ListCell<Article>() {
//                private ImageView imageView = new ImageView();
//
//                @Override
//                public void updateItem(Article page, boolean empty) {
//                    super.updateItem(page, empty);
//
//                    if (empty) {
//                        setText(null);
//                        setGraphic(null);
//                    } else {
//                        if (page.getImageArticle() != null) {
//                            HBox box = new HBox();
//                            box.setSpacing(10);
//                            imageView.setFitHeight(50);
//                            imageView.setFitWidth(50);
//
//                            imageView.setImage(page.getImageArticle());
//
//                        }
//
//                        setMinWidth(param.getWidth());
//                        setMaxWidth(param.getWidth());
//                        setPrefWidth(param.getWidth());
//
//                        setPadding(new Insets(0, 0, 5, 0));
//
//                        setWrapText(true);
//                        setTextAlignment(TextAlignment.JUSTIFY);
//
//                        setGraphic(imageView);
//                        setText(page.getTitleArticle());
//                    }
//                }
//            });
//            vnexpressListView.getItems().setAll(newsList);
//            vnexpressListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//            engine = newsScene.getEngine(); //initialise the engine web view

        } catch (Exception e) {
            System.out.println(e);
        }
    }

//    // Function load page
//    public void loadPage(String url) throws Exception {
//        engine.load(url);
//    }

//    @FXML
//    public void handleClickView() throws Exception {
//        Article news = (Article) vnexpressListView.getSelectionModel().getSelectedItem();
//        if (news == null) return;
////        System.out.println("The select item is " + news);
//        loadPage(news.getSourceArticle());
//    }
}
