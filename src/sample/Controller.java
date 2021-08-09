package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import javafx.fxml.FXML;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sample.news.Thanhnien;
import sample.news.Tuoitre;
import sample.news.Vnexpress;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;

import javafx.scene.text.TextAlignment;

public class Controller implements Initializable {
    @FXML
    public ScrollPane scrollPaneFilters;
    @FXML
    private TextArea titleNewspaper;
    @FXML
    private MenuBar menuBar;
    @FXML
    private TextArea contentTextArea;
    @FXML
    private VBox vboxApp;
    //Delare webview
    @FXML
    private WebView newsScene;
    @FXML
    private StackPane stackPane;

    private WebEngine engine;

    private ArrayList<Article> newsList;

    @Override
    public void initialize(URL url1, ResourceBundle resourceBundle) {
        try {
            newsList = new ArrayList<Article>(); // LAM ON DUNG BO DONG NAY PLEASEEEEEEEEEEEEEEEE
            ArrayList<Category> categories = new ArrayList<>();

            //making menus for menuBar
            Menu neww = new Menu("New");
            Menu covid = new Menu("Covid");
            Menu politics = new Menu("Politics");
            Menu business = new Menu("Business");
            Menu technology = new Menu("Technology");
            Menu health = new Menu("Health");
            Menu sports = new Menu("Sports");
            Menu entertainment = new Menu("Entertainment");
            Menu world = new Menu("World");
            Menu others = new Menu("Others");

            //adding menus into menubar
            menuBar.getMenus().addAll(neww,covid,politics,business,technology,health,sports,entertainment,world,others);
            menuBar.setStyle("-fx-font-size: 14");

            //init web engine
            newsScene = new WebView();
            engine = newsScene.getEngine();

            //setting up application
            vboxApp.setSpacing(5);
            vboxApp.setPadding(new Insets(30, 70, 30, 70));

//            ArrayList<Category> vnexpressCategoryList = vnexpress.srapeWebsite();
//            newsList = vnexpressCategoryList.get(0).getArticleList();

            //initializing website scrapers
            Vnexpress vnexpress = new Vnexpress();
            Tuoitre tuoitre = new Tuoitre();
            Thanhnien thanhnien = new Thanhnien();

//            ArrayList<Category> categories =
            newsList.addAll(vnexpress.scrapeWebsiteCategory("New", new File("src/sample/vnexpressurl.txt")).getArticleList());
//            newsList.addAll(tuoitre.scrapeWebsiteCategory("New", new File("src/sample/tuoitreurl.txt")).getArticleList());

//            Category thanhnienCategory = thanhnien.scrapeWebsiteCategory("Politics", new File("src/sample/thanhnienurl.txt"));
//            newsList = thanhnienCategory.getArticleList();
//            ArrayList <Category> tuoitreCategories = tuoitre.scrapeWebsite(new File("src/sample/tuoitreurl.txt"));

//            for (Category cat : categories)
//                newsList.addAll(cat.getArticleList());


            //DO NOT CHANGE BEYOND THIS POINT
            for(Article article : newsList) {
                HBox hbox = new HBox();
                hbox.setSpacing(10);
                hbox.setStyle("-fx-background-color: #ebe9e9;");

                if (article.getImageArticle() != null) {
                    ImageView imageView = new ImageView(article.getImageArticle());
                    imageView.setFitHeight(100);
                    imageView.setFitWidth(100);
                    hbox.getChildren().add(imageView);
                }
                else {
                    Label replaceImage = new Label("no image");
                    replaceImage.setStyle("-fx-alignment: CENTER; -fx-background-color: #dddfe1; -fx-pref-width: 100; -fx-pref-height: 100;");
                    hbox.getChildren().add(replaceImage);
                }

                VBox vboxArticle = new VBox();
                vboxArticle.setSpacing(3);

                Label labelArticle = new Label(article.getTitleArticle());
                labelArticle.setFont(new Font("Arial", 18));
                Label labelSource = new Label(article.getSource());
                labelSource.setFont(new Font("Arial", 12));
                Label labelTime = new Label(article.getTimeArticle());
                labelTime.setFont(new Font("Arial", 12));

                Button viewButton = new Button();
                viewButton.setOnAction(event -> {
                    try {
                        double scrollvalue = scrollPaneFilters.getVvalue();
//                            Element element = Jsoup.connect(article.getSourceArticle()).get(); //getting element to show news

//                            engine.loadContent(Jsoup.connect(article.getSourceArticle()).get().toString());
                        Element content = article.getContent();
                        if (content != null) engine.loadContent(article.getContent().toString());

                        BorderPane border = new BorderPane(); // make a pane for news and exit button

                        Button exit = new Button(); //setup exit button
                        exit.setText("exit");
                        exit.setOnAction(actionEvent -> {
                            stackPane.getChildren().remove(1);
                        }); //lambda to remove current news pane

                        exit.setMaxWidth(Double.MAX_VALUE); //set exit button to match the window's widtd

                        border.setTop(exit); //set button at top of borderpane

                        border.setCenter(newsScene); //set center as news scene

                        stackPane.getChildren().add(border); //add the whole thing on top of the application
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                viewButton.setText("View");
                viewButton.setStyle("-fx-font-size: 10; -fx-underline: true;");
                vboxArticle.getChildren().addAll(labelArticle,labelSource,labelTime, viewButton);

                hbox.getChildren().add(vboxArticle);
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
