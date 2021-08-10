package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.fxml.FXML;
import org.jsoup.nodes.Element;
import sample.news.Thanhnien;
import sample.news.Tuoitre;
import sample.news.Vnexpress;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {
    @FXML
    public ScrollPane scrollPaneFilters;
    @FXML
    private TextArea titleNewspaper;
    @FXML
    private TextArea contentTextArea;
    @FXML
    private VBox vboxApp;

    //Delare webview
    @FXML
    private WebView newsScene;
    @FXML
    private StackPane stackPane;
    @FXML
    private BorderPane borderPane;
    @FXML
    public Pagination page;

    private WebEngine engine;

    protected ArrayList<Article> newsList;

    //initializing website scrapers
    Vnexpress vnexpress;

    Tuoitre tuoitre;

    Thanhnien thanhnien;

    private String currentCategory = "";

    @Override
    public void initialize(URL url1, ResourceBundle resourceBundle) {
        try {
            newsList = new ArrayList<Article>(); // LAM ON DUNG BO DONG NAY PLEASEEEEEEEEEEEEEEEE


            //init web engine
            newsScene = new WebView();
            engine = newsScene.getEngine();

            //initializing website scrapers
            vnexpress = new Vnexpress();

            tuoitre = new Tuoitre();

            thanhnien = new Thanhnien();

            //make pagination to invisible until a category is clicked
            page.setVisible(false);
            Text intro = new Text("Choose one of the above categories to start watching news!");
            intro.setFont(new Font("Arial", 30));
            stackPane.getChildren().add(intro);

//            ArrayList<Category> categories =
//            ArrayList<Category> categories =
            HBox hbox = new HBox();
            String[] classesToRemove = {
                    "New",
                    "Covid",
                    "Politics",
                    "Business",
                    "Technology",
                    "Health",
                    "Sports",
                    "Entertainment",
                    "World",
                    "Others",
            };
            hbox.setSpacing(10);
            for(String button : classesToRemove){
                Button button1 = new Button(button);
                button1.setStyle("-fx-text-fill: rgb(46,17,191)");
                hbox.getChildren().add(button1);
                button1.setOnAction(myHandler);
            }
            borderPane.setTop(hbox);

            // ref: https://stackoverflow.com/questions/25409044/javafx-multiple-buttons-to-same-handler

        } catch (Exception e) {
            System.out.println(e);
        }
    }
    // ref: https://stackoverflow.com/questions/25409044/javafx-multiple-buttons-to-same-handler
    final EventHandler<ActionEvent> myHandler = new EventHandler<ActionEvent>(){
        @Override
        public void handle(ActionEvent event) {
            if (currentCategory.equals("")) {
                page.setVisible(true);
                stackPane.getChildren().remove(1);
            }
            String category = ((Button) event.getSource()).getText();
            if (currentCategory.equals(category)) return;
            currentCategory = category;
            try {
                newsList = vnexpress.scrapeWebsiteCategory(category, new File("src/sample/vnexpressurl.txt")).getArticleList();
                newsList.addAll(tuoitre.scrapeWebsiteCategory(category, new File("src/sample/tuoitreurl.txt")).getArticleList());
                newsList.addAll(thanhnien.scrapeWebsiteCategory(category, new File("src/sample/thanhnienurl.txt")).getArticleList());
            } catch (IOException e) {
                e.printStackTrace();
            }
            page.setPageCount((newsList.size()+9)/10);
            page.setCurrentPageIndex(0);
            page.setPageFactory(pageIndex -> createPage(pageIndex,newsList));
        }
    };

    public VBox createPage(int pageIndex, ArrayList<Article> articles) {
        VBox articleList = new VBox();
        articleList.setSpacing(5);
        articleList.setPadding(new Insets(30, 70, 30, 70));
        int range = (pageIndex+1)*10-10;
        for (int i = range; i < range+10 && i < articles.size();i++) {
            Article article = articles.get(i);
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

            Button viewButton = new Button("View");
            viewButton.setStyle("-fx-font-size: 10; -fx-underline: true;");
            vboxArticle.getChildren().addAll(labelArticle,labelSource,labelTime, viewButton);
            try {
                viewButton.setOnAction(event -> {
                    try {
                        Element content = null;
                        switch (article.getSource()) {
                            case "Thanh Nien": {
                                content = thanhnien.scrapeContent(article.getSourceArticle());
                                break;
                            }
                            case "Tuoi Tre":{
                                content = tuoitre.scrapeContent(article.getSourceArticle());
                                break;
                            }
                            case "VnExpress": {
                                content = vnexpress.scrapeContent(article.getSourceArticle());
                                break;
                            }
                        }
                        if (content != null) engine.loadContent(content.toString());

                        BorderPane border = new BorderPane(); // make a pane for news and exit button
                        border.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(0), Insets.EMPTY)));

                        Button exit = new Button("<< Go back"); //setup exit button
                        exit.setOnAction(actionEvent -> {
                            stackPane.getChildren().remove(1);
                        }); //lambda to remove current news pane

                        //exit.setMaxWidth(Double.MAX_VALUE); //set exit button to match the window's width
                        border.setTop(exit); //set button at top of borderpane
                        exit.setAlignment(Pos.TOP_CENTER);
                        border.setCenter(newsScene); //set center as news scene

                        stackPane.getChildren().add(border); //add the whole thing on top of the application
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e){
                System.out.println(e);
            }

            hbox.getChildren().add(vboxArticle);
            articleList.getChildren().add(hbox);
        }
        return articleList;
    }
}
