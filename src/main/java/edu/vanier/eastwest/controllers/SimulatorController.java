package edu.vanier.eastwest.controllers;

import edu.vanier.eastwest.models.TreeNode;
import edu.vanier.eastwest.models.Body;
import edu.vanier.eastwest.models.Vector2D;
import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;


public class SimulatorController {

    @FXML
    private AnchorPane pane;

    private AnimationTimer timer;
    private TreeNode node;
    private Camera camera;
    private Group entities;

    private static final float WIDTH = 1280;
    private static final float HEIGHT = 9209;


    @FXML
    public void initialize() {
        System.out.println("Starting application...");
        entities = new Group();

        initBodies();

        // Camera
        camera = new PerspectiveCamera();
        camera.setNearClip(1);
        camera.setFarClip(10000);
        Translate zoom = new Translate(0, 0, -10);
        camera.getTransforms().addAll(
                zoom
        );

        // Sub scene
        SubScene subScene = new SubScene(entities, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        pane.getChildren().add(subScene);

    }

    private void initBodies() {
        Body b1 = new Body(10, 1000, new Point3D(0, 0, 0), Color.RED);
        entities.getChildren().add(b1);
    }

    public void generateGrid(){

    }
    public void updateBodies(){

    }
    public void updateVectors(Vector2D v, Body b){

    }
    public Point2D getGravity(Body b1, Body b2){

        return null;
    }
    public void updateNodes(){

    }
    public void updateAnim(){

    }
    public void collide(Body b1, Body b2){
        
    }
}
