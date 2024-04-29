package edu.vanier.eastwest.controllers;

import edu.vanier.eastwest.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.IOException;

public class MainController {


    @FXML
    private Button five;

    @FXML
    private Button solar;

    @FXML
    private Button three;

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
