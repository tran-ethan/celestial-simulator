package edu.vanier.eastwest.controllers;

import edu.vanier.eastwest.MainApp;
import edu.vanier.eastwest.models.*;
import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
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
    private SplitPane splitPane;

    @FXML
    private Slider sliderSpeed;

    @FXML
    private CheckBox dsblSpin;

    private AnimationTimer timer;
    private TreeNode node;
    private Camera camera;
    private Group entities;
    private SubScene subScene;
    Body selected;


    private double anchorX, anchorY;

    private double anchorAngleX, anchorAngleY = 0;

    private final DoubleProperty angleX = new SimpleDoubleProperty(0);
    private final DoubleProperty angleY = new SimpleDoubleProperty(0);

    private static final float WIDTH = 890;
    private static final float HEIGHT = 890;
    private static Boolean spinning = true;
    private String currentTool = "";

    @FXML
    public void initialize() {
        System.out.println("Starting application...");
        entities = new Group();
        entities.getChildren().addAll(getAxes(1), getGrid(500, 10));

        // To make the mouse events on the pane work, we need to prevent the splitpane from consuming mouse events. https://stackoverflow.com/questions/54736344/javafx-splitpane-does-not-bubble-up-mouse-event
        splitPane.setSkin(new MySplitPaneSkin(splitPane));

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

        // Animation timer
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
            }
        };
        timer.start();
    }

    private void update() {
        updateBodies();
        updateVectors();

        // Move camera around selected planet
        if (selected != null) {
            camera.setTranslateX(selected.getTranslateX());
            camera.setTranslateY(selected.getTranslateY());
            camera.setTranslateZ(selected.getTranslateZ());
        }

    }

    public Point3D getGravity(Point3D p1, Point3D p2, double m2, double r1, double r2) {
        Point3D r = p2.subtract(p1);
        double rMag = r.magnitude();

        double rMin = r1 + r2;
        return r.multiply((m2 / Math.pow(Math.max(rMag, rMin), 3)));
    }

    private void initBodies() {
        Body sun = new Body(30, 100000, new Point3D(0, 0, 0), Color.YELLOW);
        Body p1 = new Body(15, 1000, new Point3D(100, 0, 100), Color.BLUE);
        Body p2 = new Body(15, 1000, new Point3D(0, 0, 100), Color.GREEN);
        Body p3 = new Body(15, 1000, new Point3D(0, 0, 200), Color.WHITE);
        Vector3D v1 = new Vector3D (4, 20, new Point3D(50, 0,100));
        v1.setPosition(v1.getPosition());
        v1.getTransforms().add(new Rotate(90, 1, 0, 0));
        p1.setVelocity(new Point3D(0, 0, 10));
        p2.setVelocity(new Point3D(-20, 0, 0));
        p3.setVelocity(new Point3D(10, 0, 10));
        entities.getChildren().addAll(sun, p1, p2, p3, v1);
        System.out.println();
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

        // Select planet by clicking it with LMB
        bodies().forEach(n -> n.setOnMouseClicked(e -> selected = n));

        // Bind rotation angle to camera with mouse movement
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

        // Disable camera spin checkbox
        dsblSpin.setOnAction(actionEvent -> {
            if (spinning) {
                spinning = false;
                timeline.pause();
            } else {
                spinning = true;
                timeline.play();
            }
        });

        // Mouse controls
        MainApp.scene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = angleX.get();
            anchorAngleY = angleY.get();
            if (spinning) {
                timeline.pause();
            }
        });

        MainApp.scene.setOnMouseReleased(mouseEvent -> {
            if (spinning) {
                timeline.play();
            }
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
    public PolygonMesh createMesh(float width, float height, int subDivX, int subDivY) {
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

    public List<Vector3D> vectors() {
        return entities.getChildren().stream().filter(n -> n instanceof Vector3D).map(n -> (Vector3D) n).collect(Collectors.toList());
    }

    //TODO
    public void updateBodies() {
        for (Body iBody : bodies()) {
            Point3D p1 = iBody.getPosition();
            for (Body jBody : bodies()) {
                if (iBody != jBody) {
                    Point3D p2 = jBody.getPosition();
                    double m2 = jBody.getMass();

                    Point3D a = getGravity(p1, p2, m2, iBody.getRadius(), jBody.getRadius());
                    iBody.update(0.01, a);

                    collide(iBody, jBody, iBody.getPosition().distance(jBody.getPosition()));
                }
            }
        }
    }

    //TODO
    public void updateVectors() {
        //TODO @@Yihweh
        for (Vector3D vector : vectors()) {
            Point3D direction;
            int x = 0;
            int y = 0;
            int z = 0;
            Point3D vectorPosition = vector.getPosition();
            for (Body jBody : bodies()) {
                Point3D p2 = jBody.getPosition();
                double m2 = jBody.getMass();

                Point3D a = getGravity(vectorPosition, p2, m2, 1, jBody.getRadius());
                x += a.getX();
                y += a.getY();
                z += a.getZ();
            }
            direction = new Point3D(x, y, z);
            double rotationAngle = vectorPosition.angle(vector.getDirection(),direction);
            vector.getTransforms().add(new Rotate(rotationAngle, vectorPosition));
           vector.setDirection(direction);
        }
    }

    //TODO: build2 @Author: 
    public void updateNodes() {

    }

    //TODO
    public void updateAnim() {

    }

    private void collide(Body a, Body b, double distance) {
        if (distance > a.getRadius() + b.getRadius()) return;

        // Normal vector
        Point3D p1 = a.getPosition();
        Point3D p2 = b.getPosition();
        Point3D n = p1.subtract(p2);
        double n_mag = n.magnitude();

        // Minimum translation distance to push balls after intersecting
        Point3D mtd = n.multiply(((a.getRadius() + b.getRadius()) - n_mag) / n_mag);

        // Inverse mass quantities
        double im1 = 1 / a.getMass();
        double im2 = 1 / b.getMass();
        double im = im1 + im2;

        // Push-pull apart
        a.setPosition(a.getPosition().add(mtd.multiply(im1 / im)));
        b.setPosition(b.getPosition().subtract(mtd.multiply(im2 / im)));

        // Impact speed
        Point3D v = a.getVelocity().subtract(b.getVelocity());
        double vn = v.dotProduct(mtd.normalize());

        // Sphere intersecting but moving away from each other already
        if (vn > 0.0) return;

        // Collision impulse
        double res = 0.5;
        double i = (-(1.0f + res) * vn) / im;
        Point3D impulse = mtd.normalize().multiply(i);

        // Change in momentum
        a.setVelocity(a.getVelocity().add(impulse.multiply(im1)));
        b.setVelocity(b.getVelocity().subtract(impulse.multiply(im2)));
    }
}
