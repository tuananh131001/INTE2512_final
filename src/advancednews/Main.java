package advancednews;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("mainwindow.fxml")));

        double height = Screen.getPrimary().getBounds().getHeight();

        Scene scene = new Scene(root, 1000, height*0.7);
        primaryStage.setScene(scene);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setTitle("nicesu application verry pretty cute hihi");
        primaryStage.show();
        //code to close all windows when main windows exits
        primaryStage.setOnCloseRequest(e -> Platform.exit());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
