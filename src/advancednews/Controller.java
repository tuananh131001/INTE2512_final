/*
  RMIT University Vietnam
  Course: INTE2512 Object-Oriented Programming
  Semester: 2021B
  Assessment: Final Project
  Created  date: 09/06/2021
  Author:
  Nguyen Tuan Anh s3864077
  Tran Nguyen Ha Khanh s3877707
  Nguyen Vu Minh Duy s3878076
  Phan Thanh Phu s3877814
  Ngo Thanh Nguyen s3856221
  Last modified date: 14/09/2021
  Acknowledgement:
  http://www.java2s.com/Tutorials/Java/JavaFX_How_to/Image/Load_an_Image_from_local_file_system.htm
  https://docs.oracle.com/javafx/2/webview/jfxpub-webview.htm
  https://stackoverflow.com/questions/6530974/getting-a-property-value-and-passing-it-on-to-superclass/6531076#6531076
  https://stackoverflow.com/questions/47743650/javafx-8-property-bindings-for-custom-objects
  https://stackoverflow.com/questions/21083945/how-to-avoid-not-on-fx-application-thread-currentthread-javafx-application-th
  https://stackoverflow.com/questions/541487/implements-runnable-vs-extends-thread-in-java?page=2&tab=votes#tab-top
  https://stackoverflow.com/questions/4691533/java-wait-for-thread-to-finish
  https://stackoverflow.com/questions/13946372/adding-css-file-to-stylesheets-in-javafx
*/

package advancednews;

import advancednews.Model.Article;
import advancednews.Model.News;
import advancednews.news.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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
import java.net.ConnectException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;

public class Controller implements Initializable {
    @FXML
    public ScrollPane scrollPaneFilters; //to create scroll pane with scroll bars
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
            initPagination(); //set page-navigating features
            initNewsBorder(); //set initial pane to display an article's content
            initScrollBar(); //set scroll bar features
            initMenuBar(); //set the category menu bar
            initLoadingBar(); //set loading bar feature to inform the loading process

            //defaults the program to run articles of the category "New" first
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
            //initially, the pagination is set to be invisible in loading process
            page.setVisible(false);

            // Init category button
            ToggleButton button = (ToggleButton) event.getSource();

            //get categoryName
            String category = button.getText(); //get the name of the chosen category based on the pressed button
            currentCategory = category; //change the current category

            //enable all category buttons to be pressed (except for the current one)
            Parent parent = button.getParent();
            for (Node ee : parent.getChildrenUnmodifiable()) {
                ee.setDisable(false);
            }

            //disable the current category button
            button.setDisable(true);

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
                page.setStyle("-fx-background-color: #fffcf4;");
                page.setCurrentPageIndex(0);
                page.setVisible(true);
                // Check if program is connected to internet
                if (newsList.size() == 0) {
                    //Initialise alert object for warning
                    noInternetAlert();
                    // Handle for OK button

                }
            });

            //start task
            threadNews.start();
        }
    };

    public void noInternetAlert(){
        //Initialise alert object for warning
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("No Internet");
        alert.setHeaderText("No internet connection.Please check your internet again.");
        alert.setContentText("Connect your device to Internet then press OK.Otherwise click X on top right to exit");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && (result.get() == ButtonType.OK)) {
            (((HBox) borderPane.getTop()).getChildren().get(0)).setDisable(false);
            ((ToggleButton) ((HBox) borderPane.getTop()).getChildren().get(0)).fire();
        } else {
            Platform.exit();
        }
    }

    public VBox addProcessBar() {
        // Config progress bar size
        progressBar.setPrefSize(200, 20);
        progressBar.setProgress(0);
        Text percentage = new Text();
        percentage.setFont(new Font("Segoe UI", 20));
        percentage.textProperty().bind(progressBar.progressProperty().multiply(100).asString("%5.0f%%"));
        Text loading = new Text("Loading articles: ");
        loading.setFont(new Font("Segoe UI", 20));

        // create HBox contain progress bar
        HBox hBox = new HBox(loading, progressBar, percentage);
        hBox.setAlignment(Pos.CENTER);
        Text tipText = new Text("If articles do not appear after 15 seconds, please click Reload Category Button");
        tipText.setFont(new Font("Segoe UI", 14));

        //create and return vbox contain progress bar and the tip text
        VBox processBarVbox = new VBox(hBox, tipText);
        processBarVbox.setAlignment(Pos.CENTER);
        processBarVbox.setSpacing(10);
        return processBarVbox;
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
                final Animation[] progressAnimation = {
                        createAnimation(0.5, progressBar.progressProperty(), ++count[0] / newsSize)
                };

                // Start running multiple threads to scrape
                progressAnimation[0].play();
                for (News news : newsHashMap.values()) {
                    // Scape and put in threads
                    ScrapeWebsite scrapeWebsite = new ScrapeWebsite(categoryName, news);
                    Thread thread = new Thread(scrapeWebsite);
                    progressAnimations.put(categoryName, new Pair<>(count[0] / newsSize, progressAnimation[0]));

                    // Synchronize threads and play progress bar
                    scrapeWebsite.setOnSucceeded(e -> {
                        progressAnimation[0] = createAnimation(0.5, progressBar.progressProperty(), ++count[0] / newsSize);
                        progressAnimations.put(categoryName, new Pair<>(count[0] / newsSize, progressAnimation[0]));
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
                for (Thread thread : threads) {
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

    //function to scrape articles from website to a category, then return the list of articles
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
            try {
                list.addAll(news.scrapeWebsiteCategory(category).getArticleList()); //scrape news and add them to article list
            } catch (IOException e) {
                System.out.println("Cannot scrape " + e);
                System.out.println("Please check your connection again");

            }
            return list; //return article list
        }
    }

    //create the overall displaying of articles in each page
    public HBox createPage(int pageIndex, List<Article> articles) {
        // Init container for page
        HBox articleList = new HBox();  //to return
        articleList.setStyle("-fx-alignment: CENTER; -fx-padding: 20 40 20 40; -fx-spacing: 20;");
        // init separated containers
        VBox vboxHighLight = new VBox();  //to store highlight articles
        vboxHighLight.setSpacing(20);
        VBox vboxList = new VBox(); //to store the remained articles
        vboxList.setSpacing(6);

        // Scrape 10 articles based on the page index
        int range = (pageIndex + 1) * 10 - 10; //to get the ordinal number (in the articles list) of the first article of this page
        for (int i = range; i < range + 10 && i < articles.size(); i++) {
            //identify & create highlight article and add it to vboxHighLight container
            if (i % 10 == 0 || i % 10 == 1) {
                VBox vbox = createArticleElementVBox(articles, i);
                vboxHighLight.getChildren().add(vbox);
            }
            //create normal article and add it to vboxList container
            else {
                HBox hbox = createArticleElementHBox(articles, i);
                vboxList.getChildren().add(hbox);
            }
        }

        // Add all articles to articleList and return
        articleList.getChildren().addAll(vboxHighLight, vboxList);
        return articleList;
    }

    // Create normal article element (HBox)
    HBox createArticleElementHBox(List<Article> articles, int position) {
        HBox hbox = new HBox(); //init container
        Pane pane = createArticleElement(articles, position, "hbox"); //this pane contains the header information the article
        //set displaying
        hbox.setStyle("-fx-background-color: #ebe9e9; -fx-spacing: 10;");
        hbox.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles/custombutton.css")).toString());

        //set effect when mouse enter and exit an hbox
        hbox.addEventFilter(MouseEvent.MOUSE_ENTERED, e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), hbox);
            st.setFromX(1);
            st.setFromY(1);
            st.setToX(1.1);
            st.setToY(1.1);
            st.play();
        });
        hbox.addEventFilter(MouseEvent.MOUSE_EXITED, e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), hbox);
            st.setFromX(1.1);
            st.setFromY(1.1);
            st.setToX(1);
            st.setToY(1);
            st.play();
        });

        hbox.getChildren().addAll(pane.getChildren()); //get the information in the pane (the child elements of the pane)
        hbox.setOnMouseMoved(pane.getOnMouseMoved()); //change cursor when mouse moves into an hbox
        hbox.setOnMouseClicked(pane.getOnMouseClicked()); //set action when mouse is clicked
        return hbox;
    }

    // Create highlight article element VBox
    VBox createArticleElementVBox(List<Article> articles, int position) {
        VBox vbox = new VBox(); //init container
        Pane pane = createArticleElement(articles, position, "vbox"); //this pane contains the header information the article
        //set displaying
        vbox.setStyle("-fx-background-color: #ebe9e9; -fx-min-height: 404;-fx-spacing: 5;");
        vbox.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles/custombutton.css")).toString());

        //set effect when mouse enter and exit an hbox
        vbox.addEventFilter(MouseEvent.MOUSE_ENTERED, e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), vbox);
            st.setFromX(1);
            st.setFromY(1);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        vbox.addEventFilter(MouseEvent.MOUSE_EXITED, e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), vbox);
            st.setFromX(1.05);
            st.setFromY(1.05);
            st.setToX(1);
            st.setToY(1);
            st.play();
        });
        vbox.getChildren().addAll(pane.getChildren());
        vbox.setOnMouseMoved(pane.getOnMouseMoved());
        vbox.setOnMouseClicked(pane.getOnMouseClicked());
        return vbox;
    }

    //create general pane for all article (contains the header information of an article)
    Pane createArticleElement(List<Article> articles, int position, String box) {
        Pane pane = new Pane();
        Article article = articles.get(position);

        //set the image displaying for article
        if (article.getImageArticle() != null) {
            ImageView imageView = new ImageView(article.getImageArticle());
            if (box.equals("vbox")) { //for highlight news
                imageView.setFitHeight(312);
                imageView.setFitWidth(550);
            } else { //for normal news
                imageView.setFitWidth(150);
                imageView.setFitHeight(100);
            }
            pane.getChildren().add(imageView); //add image to pane
        }
        //create a replace if the article has no image
        else {
            Label replaceImage = new Label("no image");
            if (box.equals("vbox")) { //for highlight news
                replaceImage.setMinSize(550, 312);
            } else { //for normal news
                replaceImage.setMinSize(150, 100);
            }
            replaceImage.setStyle("-fx-alignment: CENTER; -fx-background-color: #dddfe1;");
            pane.getChildren().add(replaceImage); //add image to pane
        }

        // Article Title
        Label labelArticle = new Label(article.getTitleArticle());
        labelArticle.setWrapText(true);
        labelArticle.setFont(new Font("Arial", 18));

        //Source Name and icon
        ImageView faviconImageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("styles/icons/"
                + article.getSource().toLowerCase() + ".png")), 16, 16, true, true));

        Text labelSource = new Text(article.getSource());
        labelSource.setFont(new Font("Arial Bold", 12));
        HBox sourceNameHBox = new HBox(faviconImageView, labelSource);
        sourceNameHBox.setSpacing(3);

        //Time and date string
        String timeString = Long.toString(article.getTimeArticle().toDays());
        if (timeString.equals("0")) {
            timeString = Long.toString(article.getTimeArticle().toHours());
            if (timeString.equals("0")) {
                timeString = Long.toString(article.getTimeArticle().toMinutes());
                timeString += " Minute" + (timeString.equals("1") ? "" : "s") + " ago";
            } else timeString += " Hour" + (timeString.equals("1") ? "" : "s") + " ago";
        } else timeString += " Day" + (timeString.equals("1") ? "" : "s") + " ago";
        Label labelTime = new Label(timeString);
        labelTime.setFont(new Font("Arial", 12));

        VBox vboxArticle = new VBox(); //this vbox is to store title, source, time
        vboxArticle.setSpacing(5);
        vboxArticle.getChildren().addAll(labelArticle, sourceNameHBox, labelTime);
        pane.getChildren().add(vboxArticle); //add vbox to pane (already add image above)

        //create functions of effect and action for this pane
        try {
            //change cursor icon when mouse move to on the article
            pane.setOnMouseMoved(mouseEvent -> {
                ((Pane) mouseEvent.getSource()).setCursor(Cursor.HAND);
            });

            //set action (go to the article's content page) when mouse is clicked
            pane.setOnMouseClicked(mouseEvent -> {
                try {
                    String source = article.getSource();

                    Element content = newsHashMap.get(source).scrapeContent(article.getSourceArticle());

                    engine.loadContent(content.toString());
                    engine.setUserStyleSheetLocation(Objects.requireNonNull(getClass().getResource("styles/news/" + source.replaceAll("\\s+", "").toLowerCase() + "style.css")).toString());
                    newsBorder.setCenter(newsScene); //set center as news scene
                    stackPane.getChildren().add(newsBorder); //add the whole thing on top of the application
                } catch (IOException e) {
                   /* e.printStackTrace();*/
                    noInternetAlert();
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
            scrollAnimation = createAnimation(0.15, scrollPaneFilters.vvalueProperty(), scrollDestination);

            //reset destination and direction after finish scrolling
            scrollAnimation.setOnFinished(e -> {
                scrollDestination = 0;
                scrollDirection = 0;
            });

            //plays the scroll animation
            scrollAnimation.play();
        });
    }


    //create the menu of category
    void initMenuBar() {
        ToggleGroup toggleGroup = new ToggleGroup(); //group of buttons (each category is a button)
        HBox hbox = new HBox(); //container
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

        //make each category to be a toggle button and add all of them to a togglegroup contained by a hbox
        for (String buttonName : ButtonNames) {
            ToggleButton button = new ToggleButton(buttonName);
            button.setToggleGroup(toggleGroup);
            button.setOnAction(loadHandler);
            button.setId(buttonName);

            hbox.getChildren().add(button);
        }

        //create button to reload program
        Button reloadButton = new Button("Reload Category");
        //add this button to the hbox container
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        hbox.getChildren().addAll(region, reloadButton);
        //set action (reload) for this button
        reloadButton.setOnAction(reloadCategory);

        hbox.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles/custombutton.css")).toString());

        borderPane.setTop(hbox); //add this menu bar to the main border pane
    }

    //action for the reload button
    final EventHandler<ActionEvent> reloadCategory = actionEvent -> {
        HBox hBox = (HBox) ((Button) actionEvent.getSource()).getParent();
        for (Node node : hBox.getChildren()) {
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

    Animation createAnimation(double duration, DoubleProperty property, double value) {
        return new Timeline(
                new KeyFrame(Duration.seconds(duration),
                        new KeyValue(property, value))
        );
    }

    void initLoadingBar() {
        progressBar.autosize();
    }


}
