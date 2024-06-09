package edu.vanier.eastwest.controllers;

import edu.vanier.eastwest.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class MainController {

    @FXML
    private ImageView background;

    @FXML
    private AnchorPane pane;

    @FXML
    public void initialize() {
        System.out.println("starting main");
        background.fitWidthProperty().bind(pane.widthProperty());
        background.fitHeightProperty().bind(pane.heightProperty());
    }

    @FXML
    void start() {
        try {
            MainApp.setRoot("simulator.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void load(ActionEvent event) {
        // Set preset name
        MainApp.preset = ((Button) event.getSource()).getId();
        start();
    }
}
