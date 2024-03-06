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
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Translate;

import java.util.ArrayList;


public class SimulatorController {
    
    private ArrayList<Body> bodies;
    private ArrayList<Vector3D> vectors;
    //The highest and lowest magnitude found in the
    private double highestMagnitude, lowestMagnitude;

    @FXML
    private AnchorPane pane;

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

        // Camera
        camera = new PerspectiveCamera();
        camera.setNearClip(1);
        camera.setFarClip(10000);
        Translate zoom = new Translate(0, 0, -10);
        camera.getTransforms().addAll(
                zoom
        );

        // Sub scene
        subScene = new SubScene(entities, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        pane.getChildren().add(subScene);

        // Initialize
        initBodies();
        pane.requestFocus();
        initControls();
    }

    private void initBodies() {
        Body b1 = new Body(30, 1000, new Point3D(0, 0, 0), Color.RED);
        entities.getChildren().add(b1);
    }

    private void initControls() {
        MainApp.scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case W -> System.out.println("forward");
                case A -> camera.setTranslateX(camera.getTranslateX() - 10);
                case S -> camera.setTranslateZ(camera.getTranslateZ() - 10);
                case D -> camera.setTranslateX(camera.getTranslateX() + 10);
                case SPACE -> System.out.println("HELLO");
            }
        });
    }

    public void generateGrid(){

    }

    public void updateBodies(){

    }
    public void updateVectors(Vector3D v, Body b){

    }

    public Point2D getGravity(Body b1, Body b2){
        return null;
    }

    //TODO: build2 @Author: 
    public void updateNodes(){

    }

    public void updateAnim(){

    }

    public void collide(Body b1, Body b2){
        
    }
}
