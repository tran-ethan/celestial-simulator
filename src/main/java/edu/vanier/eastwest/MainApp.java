package edu.vanier.eastwest;

import edu.vanier.eastwest.controllers.SimulatorController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    public static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("main.fxml"));
        scene = new Scene(loader.load());

        stage.setTitle("Celestial Simulator");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxml));
        scene.setRoot(loader.load());
    }

    public static void main(String[] args) {
        launch();
    }
}