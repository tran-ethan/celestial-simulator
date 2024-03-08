package edu.vanier.eastwest.controllers;

import edu.vanier.eastwest.MainApp;
import edu.vanier.eastwest.models.TreeNode;
import edu.vanier.eastwest.models.Body;
import edu.vanier.eastwest.models.Vector3D;
import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.util.ArrayList;


public class SimulatorController {
    
    private ArrayList<Body> bodies;
    private ArrayList<Vector3D> vectors;
    //The highest and lowest magnitude found in the
    private double highestMagnitude, lowestMagnitude;

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnPan;

    @FXML
    private Button btnPause;

    @FXML
    private Button btnPlay;

    @FXML
    private Button btnRemove;

    @FXML
    private Button btnReset;

    @FXML
    private Button btnSelection;

    @FXML
    private Button btnVector;

    @FXML
    private Pane pane;

    @FXML
    private Slider sldrSpeed;

    private AnimationTimer timer;
    private TreeNode node;
    private Camera camera;
    private Group entities;
    private SubScene subScene;

    private static final float WIDTH = 1280;
    private static final float HEIGHT = 920;

    @FXML
    public void initialize() {
        System.out.println("Starting application...");
        entities = new Group();
        entities.getChildren().add(getAxes(1));

        // Camera
        camera = new PerspectiveCamera();
        camera.setNearClip(1);
        camera.setFarClip(10000);

        // Sub scene
        subScene = new SubScene(entities, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        pane.getChildren().add(subScene);

        // Initialize
        initBodies();
        initControls();
    }

    private void initBodies() {
        Body b1 = new Body(30, 1000, new Point3D(0, 0, 0), Color.RED);
        entities.getChildren().add(b1);
    }

    private void initControls() {
        MainApp.scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case W -> camera.setTranslateZ(camera.getTranslateZ() + 10);
                case A -> camera.setTranslateX(camera.getTranslateX() - 10);
                case S -> camera.setTranslateZ(camera.getTranslateZ() - 10);
                case D -> camera.setTranslateX(camera.getTranslateX() + 10);
            }
        });

        Rotate xRotate = new Rotate(0, Rotate.X_AXIS);
        Rotate yRotate = new Rotate(0, Rotate.Y_AXIS);
        Rotate autoRotateY = new Rotate(0, Rotate.Y_AXIS);
        Translate zoom = new Translate(0, 0, -500);
        camera.getTransforms().addAll(
                xRotate,
                yRotate,
                autoRotateY,
                new Rotate(-30, Rotate.X_AXIS),
                zoom                                  // Move camera -50px in z direction
        );
    }

    public Group getAxes(double scale) {
        Cylinder axisX = new Cylinder(1, 1000);
        axisX.getTransforms().addAll(new Rotate(90, Rotate.Z_AXIS));
        axisX.getTransforms().addAll(new Translate(0, -500, 0));
        PhongMaterial redMat = new PhongMaterial();
        redMat.setDiffuseColor(Color.RED);
        axisX.setMaterial(redMat);

        Cylinder axisY = new Cylinder(1, 1000);
        axisY.setMaterial(new PhongMaterial(Color.GREEN));
        axisY.getTransforms().addAll(new Translate(0, 500, 0));

        Cylinder axisZ = new Cylinder(1, 1000);
        axisZ.setMaterial(new PhongMaterial(Color.BLUE));
        axisZ.getTransforms().addAll(new Rotate(90, Rotate.X_AXIS));
        axisZ.getTransforms().addAll(new Translate(0, 500, 0));

        Group group = new Group(axisX, axisY, axisZ);
        group.getTransforms().add(new Scale(scale, scale, scale));
        return group;
    }

    public void generateGrid() {

    }

    public void updateBodies() {

    }
    public void updateVectors(Vector3D v, Body b) {

    }

    public Point2D getGravity(Body b1, Body b2){
        return null;
    }

    //TODO: build2 @Author: 
    public void updateNodes() {

    }

    public void updateAnim() {

    }

    public void collide(Body b1, Body b2) {
        
    }
}
