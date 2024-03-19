package edu.vanier.eastwest.controllers;

import edu.vanier.eastwest.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.io.IOException;

public class MainController {

    @FXML
    void start(ActionEvent e) throws IOException {
        MainApp.setRoot("simulator.fxml");
    }

}
