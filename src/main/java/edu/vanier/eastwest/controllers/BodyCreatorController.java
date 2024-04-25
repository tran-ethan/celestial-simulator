package edu.vanier.eastwest.controllers;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

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

    @FXML
    private Button textureBtn;


    public SimulatorController simulatorController;
    private Image texture = null;

    @FXML
    public void initialize(){

        //Code taken from https://stackoverflow.com/a/45981297
        Pattern validEditingState = Pattern.compile("-?(([1-9][0-9]*)|0)?(\\.[0-9]*)?");

        UnaryOperator<TextFormatter.Change> filter = c -> {
            String text = c.getControlNewText();
            if (validEditingState.matcher(text).matches()) {
                return c ;
            } else {
                return null ;
            }
        };

        StringConverter<Double> converter = new StringConverter<Double>() {

            @Override
            public Double fromString(String s) {
                if (s.isEmpty() || "-".equals(s) || ".".equals(s) || "-.".equals(s)) {
                    return 0.0 ;
                } else {
                    return Double.valueOf(s);
                }
            }


            @Override
            public String toString(Double d) {
                return d.toString();
            }
        };

        TextFormatter<Double> textFormatterRadius = new TextFormatter<>(converter, 0.0, filter);
        radiusField.setTextFormatter(textFormatterRadius);
        TextFormatter<Double> textFormatterMass = new TextFormatter<>(converter, 0.0, filter);
        massField.setTextFormatter(textFormatterMass);

    }
    public void initController(SimulatorController simulatorController) {
        this.simulatorController = simulatorController;
    }


    @FXML
    void spawn(ActionEvent event) {
        try {
            String name = nameField.getText();
            if (name.isEmpty() || massField.getText().isEmpty() || radiusField.getText().isEmpty()) {
                throw new IllegalArgumentException("Text fields cannot be empty");
            }
            double radius = Double.parseDouble(radiusField.getText());
            double mass = Double.parseDouble(massField.getText());
            Color color = colorField.getValue();
            simulatorController.spawnBody(name, radius, mass, color, texture);
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

    @FXML
    void selectFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        File selectedImage = fileChooser.showOpenDialog(null);
        if(selectedImage == null){
            return;
        }
        if(selectedImage.getName().endsWith(".png") || selectedImage.getName().endsWith(".jpg")){
            System.out.println("picture selected");
            texture = new Image(selectedImage.toURI().toString());
        }
    }
}

