package advancednews;

import advancednews.Model.Article;
import advancednews.Model.News;
import advancednews.news.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
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
    protected List<Article> newsList;

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
            newsList = Collections.synchronizedList(new ArrayList<>()); // LAM ON DUNG BO DONG NAY PLEASE

            //init web engine
            newsScene = new WebView();
            engine = newsScene.getEngine();

            //setting up news scrapers
            newsHashMap = new LinkedHashMap<>();
            newsHashMap.put("VnExpress", new Vnexpress());
            newsHashMap.put("Tuoi Tre", new Tuoitre());
            newsHashMap.put("Thanh Nien", new Thanhnien());
            newsHashMap.put("Nhan Dan", new Nhandan());
            newsHashMap.put("Zing News", new Zingnews());

            //initializing threads
            threadsHash = new HashMap<>();
            
            // Init animation
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
    final EventHandler<ActionEvent> loadHandler = new EventHandler<>() {
        @Override
        public void handle(ActionEvent event) {
            // Init button
            page.setVisible(false);
            ToggleButton button = (ToggleButton) event.getSource();

            //get categoryName
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
                stackPane.getChildren().add(addProcessBar());
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
            Task<List<Article>> loadNewsListTask = new LoadNewsListTask(category);
            Thread threadNews = new Thread(loadNewsListTask);
            threadsHash.put(category, threadNews); //put the thread in for checkup

            //makes sure thread is stopped when program shuts down
            threadNews.setDaemon(true);

            //load pagination after task finished
            loadNewsListTask.setOnSucceeded(e -> {
                // Load article and sort by time
                if (!currentCategory.equals(category)) return;
                newsList = ((LoadNewsListTask) e.getSource()).getValue();
                newsList.sort(Comparator.comparing(Article::getTimeArticle));

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

    public HBox addProcessBar() {
        // Config progress bar size
        progressBar.setPrefSize(200, 20);
        progressBar.setProgress(0);
        Text percentage = new Text();
        percentage.setFont(new Font("Segoe UI", 20));
        percentage.textProperty().bind(progressBar.progressProperty().multiply(100).asString("%5.0f%%"));
        Text loading = new Text("Loading articles: ");
        loading.setFont(new Font("Segoe UI", 20));

        // Return HBox contain progrerss bar
        HBox hBox = new HBox(loading, progressBar, percentage);
        hBox.setAlignment(Pos.CENTER);
        return hBox;
    }

    public class LoadNewsListTask extends Task<List<Article>> {
        // init categoryName
        String categoryName;

        LoadNewsListTask(String category) {
            this.categoryName = category;
        }

        @Override
        protected List<Article> call() {
            List<Article> list = Collections.synchronizedList(new ArrayList<>());
            try {
                // Init variable for threads
                int newsSize = newsHashMap.size();
                final double[] count = {0};
                ArrayList<Thread> threads = new ArrayList<>();
                progressBar.progressProperty().set(0);
                final Animation[] progressAnimation = {increaseProgressBar(count, newsSize)};

                // Start running multiple threads to scrape
                progressAnimation[0].play();
                for (News news : newsHashMap.values()) {
                    // Scape and put in threads
                    ScrapeWebsite scrapeWebsite = new ScrapeWebsite(categoryName, news);
                    Thread thread = new Thread(scrapeWebsite);
                    progressAnimations.put(categoryName, new Pair<>( count[0] / newsSize, progressAnimation[0]));

                    // Synchronize threads and play progress bar
                    scrapeWebsite.setOnSucceeded(e -> {
                        progressAnimation[0] = increaseProgressBar(count, newsSize);
                        progressAnimations.put(categoryName, new Pair<>( count[0] / newsSize, progressAnimation[0]));
                        synchronized (list) {
                            list.addAll(((ScrapeWebsite) e.getSource()).getValue());
                        }
                        if (currentCategory.equals(categoryName)) progressAnimation[0].play();
                    });

                    // Start and add to thread
                    thread.start();
                    threads.add(thread);
                }
                // Synchronized threads
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
    // Increase progress bar each time scrape article
    public Timeline increaseProgressBar(double[] count,int newsSize){
        return new Timeline(
                new KeyFrame(Duration.seconds(0.5),
                        new KeyValue(progressBar.progressProperty(), ++count[0] / newsSize)
                )
        );
    }

    public static class ScrapeWebsite extends Task<ArrayList<Article>> {
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


    public HBox createPage(int pageIndex, List<Article> articles) {
        // Init container for page
        HBox articleList = new HBox();
        articleList.setStyle("-fx-alignment: CENTER; -fx-padding: 20 40 20 40; -fx-spacing: 10;");
        VBox vboxHighLight = new VBox();
        vboxHighLight.setSpacing(10);
        VBox vboxList = new VBox();
        vboxList.setSpacing(6);


        // Scrape 10 articles only
        int range = (pageIndex + 1) * 10 - 10;
        for (int i = range; i < range + 10 && i < articles.size(); i++) {
            if (i%10 == 0 || i%10 == 1) {
                VBox vbox = createArticleElementVBox(articles, i);
                vboxHighLight.getChildren().add(vbox);
            }
            else {
                HBox hbox = createArticleElementHBox(articles, i);
                vboxList.getChildren().add(hbox);
            }
        }

        // Add all articles to articleList and return
        articleList.getChildren().addAll(vboxHighLight,vboxList);
        return articleList;
    }

    // Create Article element HBox
    HBox createArticleElementHBox(List<Article> articles, int position){
        HBox hbox = new HBox();
        hbox.setStyle("-fx-background-color: #ebe9e9; -fx-spacing: 10;");
        Pane pane = createArticleElement(articles, position, "hbox");
        hbox.getChildren().addAll(pane.getChildren());
        hbox.setOnMouseMoved(pane.getOnMouseMoved());
        hbox.setOnMouseClicked(pane.getOnMouseClicked());
        return hbox;
    }

    // Create Article Element VBox
    VBox createArticleElementVBox(List<Article> articles, int position){
        VBox vbox = new VBox();
        Pane pane = createArticleElement(articles, position, "vbox");
        vbox.setStyle("-fx-background-color: #ebe9e9; -fx-max-width: 600; -fx-spacing: 3;-fx-padding: 0 0 20 0;");
        vbox.getChildren().addAll(pane.getChildren());
        vbox.setOnMouseMoved(pane.getOnMouseMoved());
        vbox.setOnMouseClicked(pane.getOnMouseClicked());
        return vbox;
    }

    Pane createArticleElement(List<Article> articles, int position, String box){
        Pane pane = new Pane();
        Article article = articles.get(position);

        if (article.getImageArticle() != null) {
            ImageView imageView = new ImageView(article.getImageArticle());
            if (box.equals("vbox")) {
                imageView.setFitHeight(300);
                imageView.setFitWidth(600);
            }
            else {
                imageView.setFitHeight(100);
                imageView.setFitWidth(100);
            }
            pane.getChildren().add(imageView);
        } else {
            Label replaceImage = new Label("no image");
            if (box.equals("vbox")){
                replaceImage.setMinSize(600,300);
            }
            else {
                replaceImage.setMinSize(100,100);
            }
            replaceImage.setStyle("-fx-alignment: CENTER; -fx-background-color: #dddfe1;");
            pane.getChildren().add(replaceImage);
        }

        Label labelArticle = new Label(article.getTitleArticle());
        labelArticle.setWrapText(true);
        labelArticle.setFont(new Font("Arial", 18));
        Label labelSource = new Label(article.getSource());
        labelSource.setFont(new Font("Arial", 12));
        String timeString = Long.toString(article.getTimeArticle().toDays());
        if (timeString.equals("0")) {
            timeString = Long.toString(article.getTimeArticle().toHours());
            if (timeString.equals("0")) {
                timeString = Long.toString(article.getTimeArticle().toMinutes());
                timeString += " Minute" + (timeString.equals("1")?"":"s") + " ago";
            }
            else timeString += " Hour" + (timeString.equals("1")?"":"s") + " ago";
        }
        else timeString += " Day" + (timeString.equals("1")?"":"s") + " ago";
        Label labelTime = new Label(timeString);
        labelTime.setFont(new Font("Arial", 12));


        VBox vboxArticle = new VBox();
        vboxArticle.setSpacing(3);
        vboxArticle.getChildren().addAll(labelArticle, labelSource, labelTime);
        pane.getChildren().add(vboxArticle);
        try {
            pane.setOnMouseMoved(mouseEvent -> { //change cursor icon when mouse move to on the article
                ((Pane) mouseEvent.getSource()).setCursor(Cursor.HAND);
            });
            pane.setOnMouseClicked(mouseEvent -> {
                try {
                    String source = article.getSource();
                    Element content = newsHashMap.get(source).scrapeContent(article.getSourceArticle());
                    engine.loadContent(content.toString());
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

        return pane;
    }

    void initNewsBorder() {
        //make nice background for news border
        newsBorder.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(0), Insets.EMPTY)));
        Button exit = new Button("<< Go back"); //setup exit button
        //apply css
        exit.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles/custombutton.css")).toString());
        exit.setOnAction(actionEvent -> stackPane.getChildren().remove(1)); //lambda to remove current news pane
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
            button.setToggleGroup(toggleGroup);
            button.setOnAction(loadHandler);
            button.setId(buttonName);

            hbox.getChildren().add(button);
        }

        Button reloadButton = new Button("Reload Category");
        reloadButton.setStyle("-fx-pref-height: 29;");
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        hbox.getChildren().addAll(region, reloadButton);

        reloadButton.setOnAction(reloadCategory);

        hbox.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles/custombutton.css")).toString());

        borderPane.setTop(hbox);
    }

    final EventHandler<ActionEvent> reloadCategory = actionEvent -> {
        HBox hBox = (HBox) ((Button) actionEvent.getSource()).getParent();
        for (Node node : hBox.getChildren()){
            if (currentCategory.equals(node.getId())) {
                threadsHash.put(node.getId(), null);
                for (News news : newsHashMap.values()) {
                    news.resetCategory(node.getId());
                }
                node.setDisable(false);
                ((ToggleButton) node).fire();
            }
        }
    };

    void initPagination() {
        //make pagination to invisible until a categoryName is clicked
        page.setVisible(false);
    }

    void initLoadingBar() {
        progressBar.autosize();
    }

//    @FXML
//    void onMouseMove(MouseEvent mouse) {
//        mouse.setC
//    }
}
