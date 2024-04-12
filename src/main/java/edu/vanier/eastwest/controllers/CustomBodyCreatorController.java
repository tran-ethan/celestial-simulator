package edu.vanier.eastwest.controllers;

import edu.vanier.eastwest.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.io.IOException;

public class CustomBodyCreatorController {
    @FXML
    void start(ActionEvent e) throws IOException {
        MainApp.setRoot("customBodyCreator.fxml");
    }
}
