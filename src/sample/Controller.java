package sample;

import javafx.animation.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import sample.news.Thanhnien;
import sample.news.Tuoitre;
import sample.news.Vnexpress;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public ScrollPane scrollPaneFilters;
    @FXML
    private StackPane stackPane;
    @FXML
    private BorderPane borderPane;
    @FXML
    public Pagination page;

    BorderPane newsBorder = new BorderPane(); // make a pane for news and exit button

    private WebEngine engine;

    private WebView newsScene;

    //current article list being viewed
    protected ArrayList<Article> newsList;

    protected ProgressBar pb = new ProgressBar();

    //initializing website scrapers
    HashMap<String, News> news;

    //save current category to make sure the we dont load the same category twice, after hashmap update this is not relevant anymore (still reduces loading time, just not noticeable)
    private String currentCategory = "";

    //makes scroll smooth af
    Animation scrollAnimation = new Timeline();
    //makes the above thing work
    double scrollDestination;
    double scrollDirection;

    @Override
    public void initialize(URL url1, ResourceBundle resourceBundle) {
        try {
            newsList = new ArrayList<Article>(); // LAM ON DUNG BO DONG NAY PLEASEEEEEEEEEEEEEEEE

            //init web engine
            newsScene = new WebView();
            engine = newsScene.getEngine();

            //initializing website scrapers
            news = new HashMap<>();
            news.put("VnExpress", new Vnexpress());
            news.put("Thanh Nien", new Vnexpress());
            news.put("Tuoi Tre", new Vnexpress());

            //init elements of the app
            initPagination();
            addNewsSceneListener();
            initNewsBorder();
            initScrollBar();
            initMenuBar();

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // ref: https://stackoverflow.com/questions/25409044/javafx-multiple-buttons-to-same-handler
    final EventHandler<ActionEvent> myHandler = new EventHandler<ActionEvent>(){
        @Override
        public void handle(ActionEvent event) {
            //if a category is chosen, add pagination back in and remove intro text
            ToggleButton button = (ToggleButton) event.getSource();

            Parent parent = button.getParent();
            for (Node ee : parent.getChildrenUnmodifiable()) {
                ee.setDisable(false);
            }
            button.setDisable(true);
            if (currentCategory.equals("")) {
                page.setVisible(true);
                stackPane.getChildren().remove(1);
            }

            //if the same button is pressed twice, do nothing
            String category = button.getText();
            if (currentCategory.equals(category)) return;
            currentCategory = category;

            //scrape all articles of the chosen category
            try {
                newsList = new ArrayList<>();
                for (News news: news.values()){
                    newsList.addAll(news.scrapeWebsiteCategory(category).getArticleList());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            //setting up pagination
            page.setPageCount((newsList.size()+9)/10);
            page.setCurrentPageIndex(0);
            page.setPageFactory(pageIndex -> createPage(pageIndex,newsList));
        }
    };

    public VBox createPage(int pageIndex, ArrayList<Article> articles) {
        VBox articleList = new VBox();
        articleList.setSpacing(5);
        articleList.setPadding(new Insets(5, 30, 30, 30));
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
            viewButton.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles/custombutton.css")).toString());
            vboxArticle.getChildren().addAll(labelArticle,labelSource,labelTime, viewButton);
            try {
                viewButton.setOnAction(event -> {
                    try {
                        Element content = news.get(article.getSource()).scrapeContent(article.getSourceArticle());
                        if (content != null) engine.loadContent(content.toString());
                        newsBorder.setCenter(newsScene); //set center as news scene
                        stackPane.getChildren().add(newsBorder); //add the whole thing on top of the application
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

    //use this to add loading bar
    void addNewsSceneListener() {
        newsScene.getEngine().getLoadWorker().stateProperty().addListener(
                new ChangeListener<Worker.State>() {
                    @Override
                    public void changed(
                            ObservableValue<? extends Worker.State> observable,
                            Worker.State oldValue, Worker.State newValue) {
                        switch (newValue) {
                            case SUCCEEDED:
                            case FAILED:
                            case CANCELLED:
                                newsScene
                                        .getEngine()
                                        .getLoadWorker()
                                        .stateProperty()
                                        .removeListener(this);
                        }

                        if (newValue != Worker.State.SUCCEEDED) {
                            return;
                        }
                        Document doc = Jsoup.parse(engine.executeScript("document.documentElement.outerHTML").toString());
//                        System.out.println("page loaded");
                    }
                });
    }

    void initNewsBorder(){
        newsBorder.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(0), Insets.EMPTY)));
        Button exit = new Button("<< Go back"); //setup exit button
        exit.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles/custombutton.css")).toString());
        exit.setOnAction(actionEvent -> {
            engine.loadContent("");
            stackPane.getChildren().remove(1);
        }); //lambda to remove current news pane

        //exit.setMaxWidth(Double.MAX_VALUE); //set exit button to match the window's width
        newsBorder.setTop(exit); //set button at top of borderpane
        exit.setAlignment(Pos.TOP_CENTER);
    }

    //attempt to make the scrollbar a bit faster and smoother
    void initScrollBar(){
        //apply scroll skin for news scene
        newsScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles/scrollstyle.css")).toExternalForm());
        final double SPEED = 0.004;
        scrollPaneFilters.getContent().setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY() * SPEED;
            //if we're scrolling in a different direction, set the destination as new destination. Otherwise add the previous destination to the current animation
            if (scrollDestination == 0 || scrollDirection*deltaY < 0) {
                scrollDestination = scrollPaneFilters.getVvalue() - deltaY;
            }
            else if (scrollAnimation != null){
                scrollAnimation.pause(); //pause previous animation to prevent weird stuff happening
                scrollDestination -= deltaY;
            }
            //save current direction
            scrollDirection = deltaY;

            //setup animation for scroll
            scrollAnimation = new Timeline(
                    new KeyFrame(Duration.seconds(0.15),
                            new KeyValue(scrollPaneFilters.vvalueProperty(), scrollDestination))
            );

            //reset destination and direction after finish scrolling
            scrollAnimation.setOnFinished(e -> {
                scrollDestination = 0;
                scrollDirection = 0;
            });

            //plays the scroll animation
            scrollAnimation.play();
        });
    }

    void initMenuBar(){
        //create a menu of categories
        ToggleGroup toggleGroup = new ToggleGroup();
        HBox hbox = new HBox();
        String[] ButtonNames = {
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
        for(String buttonName : ButtonNames){
            ToggleButton button = new ToggleButton(buttonName);
            button.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles/custombutton.css")).toString());
            button.setToggleGroup(toggleGroup);
            button.setOnAction(myHandler);
            hbox.getChildren().add(button);
        }
        borderPane.setTop(hbox);
    }

    void initPagination(){
        //make pagination to invisible until a category is clicked
        page.setVisible(false);
        Text intro = new Text("Choose one of the above categories to start watching news!");
        intro.setFont(new Font("Arial", 30));
        borderPane.setCenter(intro);
    }
}

