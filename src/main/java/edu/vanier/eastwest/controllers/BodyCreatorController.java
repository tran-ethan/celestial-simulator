package edu.vanier.eastwest.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.control.Button;

public class BodyCreatorController {

    @FXML
    private ColorPicker colorField;

    @FXML
    private TextField massField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField radiusField;

    @FXML
    private Button spawnBtn;

    @FXML
    private Button confirmBtn;


    public SimulatorController simulatorController;

    public void initController(SimulatorController simulatorController) {
        this.simulatorController = simulatorController;
    }

    @FXML
    void spawn(ActionEvent event) {
        try {
            String name = nameField.getText();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name cannot be empty");
            }
            double radius = Double.parseDouble(radiusField.getText());
            double mass = Double.parseDouble(massField.getText());
            Color color = colorField.getValue();
            simulatorController.spawnBody(name, radius, mass, color);
            confirmBtn.setDisable(false);
            spawnBtn.setDisable(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void confirm(ActionEvent event) {
        simulatorController.confirmBody();
    }
}
