module JavaFxApplication {
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.web;
    requires org.jsoup;
    requires javafx.media;

    opens advancednews;
    opens advancednews.Model;
}