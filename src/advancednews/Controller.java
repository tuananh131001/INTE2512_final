package advancednews;

import javafx.animation.*;
import javafx.concurrent.Task;
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
import javafx.util.Pair;
import org.jsoup.nodes.Element;
import advancednews.Model.Article;
import advancednews.Model.News;
import advancednews.news.*;

import java.io.IOException;
import java.net.URL;
import java.util.*;

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

    protected ProgressBar progressBar = new ProgressBar();

    //initializing website scrapers
    LinkedHashMap<String, News> newsHashMap;

    HashMap<String, Thread> threadsHash;

    //makes scroll bar smooth
    HashMap<String, Pair<Double, Animation>> progressAnimations;
    Text progressText;

    //makes scroll smooth af
    Animation scrollAnimation = new Timeline();
    //makes the above thing work
    double scrollDestination;
    double scrollDirection;

    String currentCategory;

    @Override
    public void initialize(URL url1, ResourceBundle resourceBundle) {
        try {
            newsList = new ArrayList<Article>(); // LAM ON DUNG BO DONG NAY PLEASEEEEEEEEEEEEEEEE

            //init web engine
            newsScene = new WebView();
            engine = newsScene.getEngine();

            //setting up news scrapers
            newsHashMap = new LinkedHashMap<String, News>();
            newsHashMap.put("VnExpress", new Vnexpress());
            newsHashMap.put("Tuoi Tre", new Tuoitre());
            newsHashMap.put("Thanh Nien", new Thanhnien());
            newsHashMap.put("Nhan Dan", new Nhandan());
            newsHashMap.put("Zing News", new Zingnews());

            //initializing threads
            threadsHash = new HashMap<>();

            progressAnimations = new HashMap<>();
            progressText = new Text();

            //init elements of the app
            initPagination();
            initNewsBorder();
            initScrollBar();
            initMenuBar();
            initLoadingBar();

            //defaults the program to run News articles first
            currentCategory = "New";
            ((ToggleButton) ((HBox) borderPane.getTop()).getChildren().get(0)).fire();

        } catch (Exception e) {
            System.out.println(e + " initialize");
        }
    }

    // ref: https://stackoverflow.com/questions/25409044/javafx-multiple-buttons-to-same-handler
    final EventHandler<ActionEvent> myHandler = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            page.setVisible(false);
            ToggleButton button = (ToggleButton) event.getSource();
            //get category
            String category = button.getText();
            currentCategory = category;

            //enable all buttons
            Parent parent = button.getParent();
            for (Node ee : parent.getChildrenUnmodifiable()) {
                ee.setDisable(false);
            }
            //disable current button
            button.setDisable(true);

            //remove intro text
            if (stackPane.getChildren().size() >= 2) {
                stackPane.getChildren().remove(1);
            }

            //add progress bar in
            if (stackPane.getChildren().size() == 1) {
                progressBar.setPrefSize(200, 20);
                progressBar.setProgress(0);
                Text percentage = new Text();
                percentage.setFont(new Font("Segoe UI", 20));
                percentage.textProperty().bind(progressBar.progressProperty().multiply(100).asString("%5.0f%%"));
                Text loading = new Text("Loading articles: ");
                loading.setFont(new Font("Segoe UI", 20));
                HBox hBox = new HBox(loading, progressBar, percentage);
                hBox.setAlignment(Pos.CENTER);
                stackPane.getChildren().add(hBox);
            }

            //check if same thread is running
            Thread check = threadsHash.get(category);
            if (check != null && check.isAlive()) {
                Pair<Double, Animation> p = progressAnimations.get(category);
                progressBar.setProgress(p.getKey());
                p.getValue().play();
                return; //if its running dont bother running it
            }

            //create a loading news list task
            Task<ArrayList<Article>> loadNewsListTask = new LoadNewsListTask(category);
            Thread threadNews = new Thread(loadNewsListTask);
            threadsHash.put(category, threadNews); //put the thread in for checkup

            //makes sure thread is stopped when program shuts down
            threadNews.setDaemon(true);
            //load pagination after task finished
            loadNewsListTask.setOnSucceeded(e -> {
                if (!currentCategory.equals(category)) return;
                newsList = ((LoadNewsListTask) e.getSource()).getValue();
                //removing progress bar
                if (stackPane.getChildren().size() >= 2) {
                    stackPane.getChildren().remove(1);
                }
                //setting up pagination
                page.setPageCount((newsList.size() + 9) / 10);
                page.setPageFactory(pageIndex -> createPage(pageIndex, newsList));
                page.setCurrentPageIndex(0);
                page.setVisible(true);
            });
            //start task
            threadNews.start();
        }
    };


    public class LoadNewsListTask extends Task<ArrayList<Article>> {
        String category;

        LoadNewsListTask(String category) {
            this.category = category;
        }

        @Override
        protected ArrayList<Article> call() {
            ArrayList<Article> list = new ArrayList<>();
            try {
                int newsSize = newsHashMap.size();
                final double[] count = {0};
                ArrayList<Thread> threads = new ArrayList<>();
                progressBar.progressProperty().set(0);
                final Animation[] progressAnimation = {new Timeline(
                        new KeyFrame(Duration.seconds(0.5),
                                new KeyValue(progressBar.progressProperty(), ++count[0] / newsSize)
                        )
                )};
                progressAnimation[0].play();
                for (News news : newsHashMap.values()) {
                    ScrapeWebsite scrapeWebsite = new ScrapeWebsite(category, news);
                    Thread thread = new Thread(scrapeWebsite);
                    progressAnimations.put(category, new Pair<>( count[0] / newsSize, progressAnimation[0]));
                    scrapeWebsite.setOnSucceeded(e -> {
                        progressAnimation[0] = new Timeline(
                                new KeyFrame(Duration.seconds(0.5),
                                        new KeyValue(progressBar.progressProperty(), ++count[0]/newsSize)
                                )
                        );
                        progressAnimations.put(category, new Pair<>( count[0] / newsSize, progressAnimation[0]));
                        synchronized (list) {
                            list.addAll(((ScrapeWebsite) e.getSource()).getValue());
                        }
                        if (currentCategory.equals(category)) progressAnimation[0].play();
                    });
                    thread.start();
                    threads.add(thread);
                }
                for (Thread thread : threads){
                    synchronized (this) {
                        thread.join();
                    }
                }
            } catch (InterruptedException e) {
                System.out.println(e + " LoadNewsListTask");
            }
            return list;
        }
    }

    public class ScrapeWebsite extends Task<ArrayList<Article>> {
        String category;
        News news;

        ScrapeWebsite(String category, News news) {
            this.category = category;
            this.news = news;
        }

        @Override
        protected ArrayList<Article> call() {
            ArrayList<Article> list = new ArrayList<>();
            try{
                list.addAll(news.scrapeWebsiteCategory(category).getArticleList());
            } catch (IOException e) {
                System.out.println(e + " ScrapeWebsite");
            }
            return list;
        }
    }

    public VBox createPage(int pageIndex, ArrayList<Article> articles) {
        VBox articleList = new VBox();
        articleList.setSpacing(5);
        articleList.setPadding(new Insets(5, 30, 30, 30));
        int range = (pageIndex + 1) * 10 - 10;
        for (int i = range; i < range + 10 && i < articles.size(); i++) {
            HBox hbox = createArticleElement(articles,i);
            articleList.getChildren().add(hbox);
        }
        return articleList;
    }

    HBox createArticleElement(ArrayList<Article> articles, int position){
        HBox hbox = new HBox();
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #ebe9e9;");
        Article article = articles.get(position);

        if (article.getImageArticle() != null) {
            ImageView imageView = new ImageView(article.getImageArticle());
            imageView.setFitHeight(100);
            imageView.setFitWidth(100);
            hbox.getChildren().add(imageView);
        } else {
            Label replaceImage = new Label("no image");
            replaceImage.setStyle("-fx-alignment: CENTER; -fx-background-color: #dddfe1; -fx-pref-width: 100; -fx-pref-height: 100;");
            hbox.getChildren().add(replaceImage);
        }

        Label labelArticle = new Label(article.getTitleArticle());
        labelArticle.setFont(new Font("Arial", 18));
        Label labelSource = new Label(article.getSource());
        labelSource.setFont(new Font("Arial", 12));
        Label labelTime = new Label(article.getTimeArticle());
        labelTime.setFont(new Font("Arial", 12));

        VBox vboxArticle = new VBox();
        vboxArticle.setSpacing(3);
        Button viewButton = new Button("View");
        //disable view button focus cause after article got added the damn thing is still in focus and will try to add more panes if you hit space or enter
        viewButton.setFocusTraversable(false);
        viewButton.setStyle("-fx-font-size: 10; -fx-underline: true;");
        viewButton.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles/custombutton.css")).toString());
        vboxArticle.getChildren().addAll(labelArticle, labelSource, labelTime, viewButton);

        try {
            viewButton.setOnAction(event -> {
                try {
                    String source = article.getSource();
                    Element content = newsHashMap.get(source).scrapeContent(article.getSourceArticle());
                    engine.loadContent(content.toString());
                    newsScene.setPrefSize(5000, 5000);
                    engine.setUserStyleSheetLocation(Objects.requireNonNull(getClass().getResource("styles/news/" + source.replaceAll("\\s+", "").toLowerCase() + "style.css")).toString());
                    newsBorder.setCenter(newsScene); //set center as news scene
                    stackPane.getChildren().add(newsBorder); //add the whole thing on top of the application
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.out.println(e + " createPage");
        }
        hbox.getChildren().add(vboxArticle);
        return hbox;
    }

    void initNewsBorder() {
        //make nice background for news border
        newsBorder.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(0), Insets.EMPTY)));
        Button exit = new Button("<< Go back"); //setup exit button
        //apply css
        exit.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles/custombutton.css")).toString());
        exit.setOnAction(actionEvent -> {
            stackPane.getChildren().remove(1);
        }); //lambda to remove current news pane
        newsBorder.setTop(exit); //set button at top of borderpane
    }

    //attempt to make the scrollbar a bit faster and smoother
    void initScrollBar() {
        //apply scroll skin for news scene
        newsScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles/scrollstyle.css")).toExternalForm());
        final double SPEED = 0.004;
        scrollPaneFilters.getContent().setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY() * SPEED;
            //if we're scrolling in a different direction, set the destination as new destination. Otherwise add the previous destination to the current animation
            if (scrollDestination == 0 || scrollDirection * deltaY < 0) {
                scrollDestination = scrollPaneFilters.getVvalue() - deltaY;
            } else if (scrollAnimation != null) {
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

    void initMenuBar() {
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

        for (String buttonName : ButtonNames) {
            ToggleButton button = new ToggleButton(buttonName);
            button.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles/custombutton.css")).toString());
            button.setToggleGroup(toggleGroup);
            button.setOnAction(myHandler);

            hbox.getChildren().add(button);
        }

        borderPane.setTop(hbox);
    }

    void initPagination() {
        //make pagination to invisible until a category is clicked
        page.setVisible(false);
    }

    void initLoadingBar() {
        progressBar.autosize();
    }
}
