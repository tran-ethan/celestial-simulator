package edu.vanier.eastwest.controllers;

import edu.vanier.eastwest.MainApp;
import edu.vanier.eastwest.models.Body;
import edu.vanier.eastwest.models.TreeNode;
import edu.vanier.eastwest.models.Vector3D;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.fxyz3d.shapes.polygon.PolygonMesh;
import org.fxyz3d.shapes.polygon.PolygonMeshView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


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
    private Slider sliderSpeed;

    private AnimationTimer timer;
    private TreeNode node;
    private Camera camera;
    private Group entities;
    private SubScene subScene;


    private double anchorX, anchorY;

    private double anchorAngleX, anchorAngleY = 0;

    private final DoubleProperty angleX = new SimpleDoubleProperty(0);
    private final DoubleProperty angleY = new SimpleDoubleProperty(0);

    private static final float WIDTH = 1280;
    private static final float HEIGHT = 920;

    @FXML
    public void initialize() {
        System.out.println("Starting application...");
        entities = new Group();
        entities.getChildren().addAll(getAxes(1), getGrid(500, 10));

        // Camera
        camera = new PerspectiveCamera(true);
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
                zoom
        );

        // Camera auto spin
        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.seconds(0),
                        new KeyValue(autoRotateY.angleProperty(), 0)
                ),
                new KeyFrame(
                        Duration.seconds(30),
                        new KeyValue(autoRotateY.angleProperty(), 360)
                )
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play(); // Comment this line to disable

        xRotate.angleProperty().bind(angleX);
        yRotate.angleProperty().bind(angleY);

        // Mouse controls
        MainApp.scene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = angleX.get();
            anchorAngleY = angleY.get();
        });

        // Mouse dragging controls
        MainApp.scene.setOnMouseDragged(event -> {
            angleX.set(anchorAngleX - (anchorY - event.getSceneY()));
            angleY.set(anchorAngleY + anchorX - event.getSceneX());
        });

        // Zoom controls
        MainApp.scene.addEventHandler(ScrollEvent.SCROLL, e -> {
            double delta = e.getDeltaY();
            zoom.setZ(zoom.getZ() + delta);
        });
    }

    /**
     * Generates a group containing three cylinders representing X, Y, and Z axes.
     * Each axis is oriented along a different axis direction (X, Y, or Z) and colored accordingly.
     * The intersection of the 3 axes represent the coordinate (0, 0, 0).
     *
     * @param scale The scaling factor applied to the axes
     * @return a Group object containing the X, Y, and Z axes with specified scaling
     */
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

    /**
     * Creates a polygon mesh based on specified width, height, and subdivision parameters.
     *
     * @param width     The width of the mesh
     * @param height    The height of the mesh
     * @param subDivX   The number of subdivisions along the X-axis
     * @param subDivY   The number of subdivisions along the Y-axis
     * @return a PolygonMesh object representing the created mesh
     */
    private PolygonMesh createMesh(float width, float height, int subDivX, int subDivY) {
        final float minX = - width / 2f;
        final float minY = - height / 2f;
        final float maxX = width / 2f;
        final float maxY = height / 2f;

        final int pointSize = 3;
        final int texCoordSize = 2;
        // 4 point indices and 4 texCoord indices per face
        final int faceSize = 8;
        int numDivX = subDivX + 1;
        int numVerts = (subDivY + 1) * numDivX;
        float points[] = new float[numVerts * pointSize];
        float texCoords[] = new float[numVerts * texCoordSize];
        int faceCount = subDivX * subDivY;
        int faces[][] = new int[faceCount][faceSize];

        // Create points and texCoords
        for (int y = 0; y <= subDivY; y++) {
            float dy = (float) y / subDivY;
            double fy = (1 - dy) * minY + dy * maxY;

            for (int x = 0; x <= subDivX; x++) {
                float dx = (float) x / subDivX;
                double fx = (1 - dx) * minX + dx * maxX;

                int index = y * numDivX * pointSize + (x * pointSize);
                points[index] = (float) fx;
                points[index + 1] = (float) fy;
                points[index + 2] = 0.0f;

                index = y * numDivX * texCoordSize + (x * texCoordSize);
                texCoords[index] = dx;
                texCoords[index + 1] = dy;
            }
        }

        // Create faces
        int index = 0;
        for (int y = 0; y < subDivY; y++) {
            for (int x = 0; x < subDivX; x++) {
                int p00 = y * numDivX + x;
                int p01 = p00 + 1;
                int p10 = p00 + numDivX;
                int p11 = p10 + 1;
                int tc00 = y * numDivX + x;
                int tc01 = tc00 + 1;
                int tc10 = tc00 + numDivX;
                int tc11 = tc10 + 1;

                faces[index][0] = p00;
                faces[index][1] = tc00;
                faces[index][2] = p10;
                faces[index][3] = tc10;
                faces[index][4] = p11;
                faces[index][5] = tc11;
                faces[index][6] = p01;
                faces[index++][7] = tc01;
            }
        }

        int[] smooth = new int[faceCount];

        PolygonMesh mesh = new PolygonMesh(points, texCoords, faces);
        mesh.getFaceSmoothingGroups().addAll(smooth);
        return mesh;
    }

    /**
     * Generates a group containing grid lines in the XY, XZ, and YZ planes, with specified size and spacing.
     *
     * @param size  The size of the grid in the X and Y directions
     * @param delta The spacing between grid lines
     * @return a Group object containing grid lines in the XZ plane and its subgrid
     */
    public Group getGrid(float size, float delta) {
        if (delta < 1) {
            delta = 1;
        }
        final PolygonMesh plane = createMesh(size, size, (int) (size / delta), (int) (size / delta));

        final PolygonMesh plane2 = createMesh(size, size, (int) (size / delta / 5), (int) (size / delta / 5));

        PolygonMeshView meshViewXY = new PolygonMeshView(plane);
        meshViewXY.setDrawMode(DrawMode.LINE);
        meshViewXY.setCullFace(CullFace.NONE);

        PolygonMeshView meshViewXZ = new PolygonMeshView(plane);
        meshViewXZ.setDrawMode(DrawMode.LINE);
        meshViewXZ.setCullFace(CullFace.NONE);
        meshViewXZ.getTransforms().add(new Rotate(90, Rotate.X_AXIS));

        PolygonMeshView meshViewYZ = new PolygonMeshView(plane);
        meshViewYZ.setDrawMode(DrawMode.LINE);
        meshViewYZ.setCullFace(CullFace.NONE);
        meshViewYZ.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));

        PolygonMeshView meshViewXY2 = new PolygonMeshView(plane2);
        meshViewXY2.setDrawMode(DrawMode.LINE);
        meshViewXY2.setCullFace(CullFace.NONE);
        meshViewXY2.getTransforms().add(new Translate(size / 1000f, size / 1000f, 0));

        PolygonMeshView meshViewXZ2 = new PolygonMeshView(plane2);
        meshViewXZ2.setDrawMode(DrawMode.LINE);
        meshViewXZ2.setCullFace(CullFace.NONE);
        meshViewXZ2.getTransforms().add(new Translate(size / 1000f, size / 1000f, 0));
        meshViewXZ2.getTransforms().add(new Rotate(90, Rotate.X_AXIS));

        PolygonMeshView meshViewYZ2 = new PolygonMeshView(plane2);
        meshViewYZ2.setDrawMode(DrawMode.LINE);
        meshViewYZ2.setCullFace(CullFace.NONE);
        meshViewYZ2.getTransforms().add(new Translate(size / 1000f, size / 1000f, 0));
        meshViewYZ2.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));

        // return new Group(meshViewXY, meshViewXZ, meshViewYZ, meshViewXY2, meshViewXZ2, meshViewYZ2 );
        // For now, only render the XZ plane
        return new Group(meshViewXZ, meshViewXZ2);
    }

    public List<Body> bodies() {
        return entities.getChildren().stream().filter(n -> n instanceof Body).map(n -> (Body) n).collect(Collectors.toList());
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
