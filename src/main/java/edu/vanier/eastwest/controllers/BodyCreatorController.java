package edu.vanier.eastwest.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class BodyCreatorController {

    @FXML
    private ColorPicker colorField;

    @FXML
    private TextField massField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField radiusField;

    public SimulatorController simulatorController;

    public void initController(SimulatorController simulatorController) {
        this.simulatorController = simulatorController;
    }

    @FXML
    void confirm(ActionEvent event) {
        String name = nameField.getText();
        double mass = Double.parseDouble(massField.getText());
        double radius = Double.parseDouble(radiusField.getText());
        Color color = colorField.getValue();
        simulatorController.spawnBody(name, mass, radius, color);
    }
}
