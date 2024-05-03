package edu.vanier.eastwest.controllers;


import edu.vanier.eastwest.models.Body;
import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point3D;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Duration;
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
    private TextField xField;

    @FXML
    private TextField zField;

    @FXML
    private Button spawnBtn;

    @FXML
    private Button confirmBtn;

    @FXML
    private Button textureBtn;

    @FXML
    private Button posBtn;

    @FXML
    private Pane paneImage;

    private Body previewBody;


    public SimulatorController simulatorController;
    private Image texture = null;

    @FXML
    public void initialize() {
        // Code adapted from https://stackoverflow.com/a/45981297
        // TODO error handling, max/min radius and mass
        Pattern validEditingState = Pattern.compile("-?(([1-9][0-9]*)|0)?(\\.[0-9]*)?");

        UnaryOperator<TextFormatter.Change> filter = c -> {
            String text = c.getControlNewText();
            if (validEditingState.matcher(text).matches()) {
                return c ;
            } else {
                return null ;
            }
        };

        StringConverter<Double> converter = new StringConverter<>() {

            @Override
            public Double fromString(String s) {
                if (s.isEmpty() || "-".equals(s) || ".".equals(s) || "-.".equals(s)) {
                    return 0.0;
                } else {
                    return Double.valueOf(s);
                }
            }

            @Override
            public String toString(Double d) {
                return d.toString();
            }
        };

        TextFormatter<Double> textFormatterRadius = new TextFormatter<>(converter, 10.0, filter);
        radiusField.setTextFormatter(textFormatterRadius);
        TextFormatter<Double> textFormatterMass = new TextFormatter<>(converter, 1000.0, filter);
        massField.setTextFormatter(textFormatterMass);

        colorField.setOnAction(event -> previewBody.setColor(colorField.getValue()));
    }

    public void initController(SimulatorController simulatorController) {
        this.simulatorController = simulatorController;
    }

    public void setXZ(double x, double z) {
        xField.setText(String.format("%.2f", x));
        zField.setText(String.format("%.2f", z));
    }

    public void initBody() {
        simulatorController.previewGroup.getChildren().clear();

        // Only spawn new preview if first time
        if (previewBody == null) {
            previewBody = new Body("", 10, 1, new Point3D(0, 0, 0), new Point3D(0, 0, 0), Color.WHITE, null);
        }

        // Rotate on itself
        RotateTransition spin = new RotateTransition(Duration.seconds(30), previewBody);
        spin.setByAngle(360);
        spin.setAxis(new Point3D(0, 1, 0));
        spin.setCycleCount(RotateTransition.INDEFINITE);
        spin.play();

        simulatorController.previewGroup.getChildren().add(previewBody);
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
            texture = null;
            paneImage.getChildren().clear();

            // Disable buttons
            posBtn.setDisable(false);
            spawnBtn.setDisable(true);
            textureBtn.setDisable(true);
            massField.setDisable(true);
            radiusField.setDisable(true);
            nameField.setDisable(true);
            colorField.setDisable(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void confirmPos(ActionEvent event) {
        posBtn.setDisable(true);
        confirmBtn.setDisable(false);
        xField.setDisable(true);
        zField.setDisable(true);
        simulatorController.confirmPos();
    }

    @FXML
    void confirm(ActionEvent event) {
        simulatorController.confirmBody();

        // Reset buttons
        spawnBtn.setDisable(false);
        posBtn.setDisable(true);
        confirmBtn.setDisable(true);
        textureBtn.setDisable(false);
        massField.setDisable(false);
        radiusField.setDisable(false);
        nameField.setDisable(false);
        colorField.setDisable(false);
        xField.setDisable(false);
        zField.setDisable(false);
    }

    @FXML
    void selectFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        File selectedImage = fileChooser.showOpenDialog(null);
        if (selectedImage == null) {
            return;
        }
        if (selectedImage.getName().endsWith(".png") || selectedImage.getName().endsWith(".jpg")) {
            texture = new Image(selectedImage.toURI().toString());
            previewBody.setTexture(texture);
        }
    }
}

