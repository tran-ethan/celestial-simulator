package edu.vanier.eastwest.controllers;

import edu.vanier.eastwest.MainApp;
import edu.vanier.eastwest.models.Body;
import edu.vanier.eastwest.models.MySplitPaneSkin;
import edu.vanier.eastwest.models.Quad;
import edu.vanier.eastwest.models.Vector3D;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.controlsfx.control.ToggleSwitch;
import org.fxyz3d.shapes.polygon.PolygonMeshView;

import java.util.List;
import java.util.stream.Collectors;

import static edu.vanier.eastwest.util.Utility.*;


public class SimulatorController {

    @FXML
    private ToggleButton btnAdd;

    @FXML
    private ToggleButton btnPan;

    @FXML
    private Button btnPause;

    @FXML
    private Button btnPlay;

    @FXML
    private ToggleButton btnRemove;

    @FXML
    private Button btnReset;

    @FXML
    private ToggleButton btnSelection;

    @FXML
    private Pane pane;

    @FXML
    private SplitPane splitPane;

    @FXML
    private Slider sldrSpeed;

    @FXML
    private CheckBox enableSpin;

    @FXML
    private ToggleSwitch tgl2D;

    @FXML
    private ToggleSwitch tglVector;

    @FXML
    private ToggleSwitch tglAxes;

    @FXML
    private ToggleSwitch tglGrid;

    @FXML
    private Label lblSelected;

    @FXML
    private Label lblProperties;

    @FXML
    private VBox propertiesPanel;

    @FXML
    private VBox vbTools;

    private Timeline timer;
    private Quad root;
    private Camera camera;
    private Group entities;
    private SubScene subScene;
    Body selectedBody;

    private double anchorX, anchorY;

    private double anchorAngleX, anchorAngleZ, anchorAngleY = 0;

    private final DoubleProperty angleX = new SimpleDoubleProperty(0);
    private final DoubleProperty angleZ = new SimpleDoubleProperty(0);
    private final DoubleProperty angleY = new SimpleDoubleProperty(0);


    private static Boolean spinning = false;
    private static double theta = 0.7;

    BodyCreatorController controller;
    AnchorPane bodyCreator;

    /**
     * This rectangle represents the XZ plane and is used to get cursor positions for dragging objects
     */
    Rectangle plane;

    /**
     * This object represents the new body that is created when user clicks on Add Body
     */
    Body newBody;
    ToggleButton selectedTool;

    float size = 10000; // Size of plane

    @FXML
    public void initialize() {
        entities = new Group();

        // Create XZ plane for dragging
        plane = new Rectangle(size, size, Color.TRANSPARENT);
        plane.getTransforms().addAll(
                new Rotate(90, Rotate.X_AXIS),
                new Translate(-size / 2, -size / 2, 0)
        );
        plane.setMouseTransparent(true);
        plane.setDepthTest(DepthTest.DISABLE);

        // Add axes, grid, and plane
        entities.getChildren().addAll(getAxes(1), getGrid(size, 100), plane);

        // To make the mouse events on the pane work, we need to prevent the splitpane from consuming mouse events. https://stackoverflow.com/questions/54736344/javafx-splitpane-does-not-bubble-up-mouse-event
        splitPane.setSkin(new MySplitPaneSkin(splitPane));

        // Camera
        camera = new PerspectiveCamera(true);
        camera.setNearClip(1);
        camera.setFarClip(20000);

        // Sub scene
        subScene = new SubScene(entities, 850, 850, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        pane.getChildren().add(subScene);

        // Initialize entities
        initBodies();
        initControls();

        // Animation timer
        EventHandler<ActionEvent> onFinished = this::update;
        timer = new Timeline(
                new KeyFrame(Duration.millis(10), onFinished)
        );
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();

        // Add body properties panel
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("bodyCreator.fxml"));
            bodyCreator = loader.load();
            bodyCreator.setVisible(false);
            controller = loader.getController();
            controller.initController(this);
            propertiesPanel.getChildren().add(bodyCreator);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void update(ActionEvent event) {
        // Update size of subScene
        subScene.setHeight(pane.getHeight());
        subScene.setWidth(pane.getWidth());

        // updateBodies();
        updateBodiesBarnes();
        // updateVectors();

        if (selectedBody != null) {
            // Move camera around selected planet
            camera.setTranslateX(selectedBody.getTranslateX());
            camera.setTranslateY(selectedBody.getTranslateY());
            camera.setTranslateZ(selectedBody.getTranslateZ());

            // Update properties panel
            lblSelected.setText(selectedBody.getName());
            lblProperties.setText(selectedBody.toString());
        } else {
            lblSelected.setText("<No Body Selected>");
            lblProperties.setText("");
        }

    }

    /**
     * Returns a Point3D vector representing gravity between 2 bodies
     *
     * @param p1 Point3D position of the influenced body.
     * @param p2 Point3D position of the influencing body.
     * @param m2 Mass of the influencing body.
     * @param r1 Radius of the influenced body.
     * @param r2 Radius of the influencing body.
     * @return Point3D vector representing the vector gravitational force on p1 by p2.
     */
    public Point3D getGravity(Point3D p1, Point3D p2, double m2, double r1, double r2) {
        Point3D r = p2.subtract(p1);
        double rMag = r.magnitude();

        double rMin = r1 + r2;
        return r.multiply((m2 / Math.pow(Math.max(rMag, rMin), 3)));
    }

    private void initBodies() {
        Body sun = new Body("Sun", 30, 100000, new Point3D(0, 0, -50), Color.YELLOW);
        Body p1 = new Body("Blue", 10, 20000, new Point3D(150, 0, -100), Color.BLUE);
        Body p2 = new Body("Green", 10, 5000, new Point3D(200, 0, 100), Color.GREEN);
        Body p3 = new Body("White", 10, 5000, new Point3D(150, 0, 200), Color.WHITE);
        Body p4 = new Body("Red", 10, 5000, new Point3D(160, 0, 175), Color.RED);
        p1.setVelocity(new Point3D(0, 0, 10));
        p2.setVelocity(new Point3D(-20, 0, 0));
        p3.setVelocity(new Point3D(10, 0, 10));
        p4.setVelocity(new Point3D(0, 0, 100));
        entities.getChildren().addAll(sun, p1, p2, p3, p4);
        // entities.getChildren().addAll(sun, p1);
    }

    /***
     * TODO: Make vectors appear only near bodies
     * Creates Vector3D arrows around Body objects.
     */
    private void initVectors() {
        for (int i = -5; i <= 5; i++) {
            Vector3D v = new Vector3D(4, 20, new Point3D(i * 100, 0, 0));
            v.getTransforms().add(new Rotate(90, 1, 0, 0));
            v.getTransforms().add(v.getXRotate());
            entities.getChildren().add(v);
        }
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

        // Bind rotation angle to camera with mouse movement
        Rotate x1Rotate = new Rotate(0, Rotate.X_AXIS);
        Rotate x2Rotate = new Rotate(0, Rotate.Z_AXIS);
        Rotate yRotate = new Rotate(0, Rotate.Y_AXIS);
        Rotate initX = new Rotate(-30, Rotate.X_AXIS);
        Rotate autoRotateY = new Rotate(0, Rotate.Y_AXIS);
        Translate zoom = new Translate(0, 0, -500);
        camera.getTransforms().addAll(
                x1Rotate,
                x2Rotate,
                yRotate,
                autoRotateY,
                initX,
                zoom
        );

        // Camera auto spin
        Timeline rotateTimer = new Timeline(
                new KeyFrame(
                        Duration.seconds(0),
                        new KeyValue(autoRotateY.angleProperty(), 0)
                ),
                new KeyFrame(
                        Duration.seconds(30),
                        new KeyValue(autoRotateY.angleProperty(), 360)
                )
        );
        rotateTimer.setCycleCount(Timeline.INDEFINITE);

        x1Rotate.angleProperty().bind(angleX);
        x2Rotate.angleProperty().bind(angleZ);
        yRotate.angleProperty().bind(angleY);

        // Disable camera spin checkbox
        enableSpin.setOnAction(actionEvent -> {
            if (!spinning) {
                rotateTimer.play();
            } else {
                rotateTimer.pause();
            }
            spinning = !spinning;
        });

        // Mouse controls
        EventHandler<MouseEvent> mousePressedHandler = event -> {
            // Select planet by clicking it with LMB when not panning
            if (selectedTool == btnSelection) {
                bodies().forEach(n -> n.setOnMouseClicked(e -> selectedBody = n));
            }

            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = angleX.get();
            anchorAngleZ = angleZ.get();
            anchorAngleY = angleY.get();

            if (spinning) {
                rotateTimer.pause();
            }
        };
        MainApp.scene.setOnMousePressed(mousePressedHandler);

        EventHandler<MouseEvent> mouseReleasedHandler = event -> {
            if (spinning) {
                rotateTimer.play();
            }
        };
        MainApp.scene.setOnMouseReleased(mouseReleasedHandler);

        // Mouse dragging controls
        EventHandler<MouseEvent> mouseDraggedHandler = event -> {
            // Camera rotation with RMB
            if (event.isSecondaryButtonDown()) {
                if (!tgl2D.isSelected()) {
                    angleX.set(anchorAngleX + (-Math.cos(Math.toRadians(angleY.get()))) * (anchorY - event.getSceneY())/5);
                    angleZ.set(anchorAngleZ + Math.sin(Math.toRadians(angleY.get())) * (anchorY - event.getSceneY())/5);
                    angleY.set(anchorAngleY + (anchorX - event.getSceneX())/5);
                } else {
                    angleY.set(anchorAngleY + (anchorX - event.getSceneX())/5);
                }
            }

            // Panning tool for camera
            else if (selectedTool == btnPan) {
                camera.setTranslateX(camera.getTranslateX() - Math.sin(Math.toRadians(angleY.get())) * (anchorY - event.getSceneY()));
                camera.setTranslateZ(camera.getTranslateZ() - Math.cos(Math.toRadians(angleY.get())) * (anchorY - event.getSceneY()));
                camera.setTranslateX(camera.getTranslateX() + Math.cos(Math.toRadians(angleY.get())) * (anchorX - event.getSceneX()));
                camera.setTranslateZ(camera.getTranslateZ() - Math.sin(Math.toRadians(angleY.get())) * (anchorX - event.getSceneX()));

                anchorX = event.getSceneX();
                anchorY = event.getSceneY();
            }
        };
        MainApp.scene.setOnMouseDragged(mouseDraggedHandler);

        // Zoom controls using mouse wheel scroll
        MainApp.scene.addEventHandler(ScrollEvent.SCROLL, e -> {
            double delta = e.getDeltaY();
            zoom.setZ(zoom.getZ() + delta);
        });

        // Pan toggle button
        btnPan.setOnAction(event -> {
            toggleToolButtons(btnPan);
        });

        // Selection toggle button
        btnSelection.setOnAction(event -> {
            toggleToolButtons(btnSelection);
        });

        // Vector Field visual toggle button
        tglVector.setOnMouseClicked(event -> {
            if (!tglVector.isSelected()){
                // Remove vector field
                entities.getChildren().removeAll(vectors());
            }
            else {
                // Add vector field
                initVectors();
            }
        });

        // Adding bodies
        btnAdd.setOnAction(event -> {
            toggleToolButtons(btnAdd);
            timer.pause();
            bodyCreator.setVisible(true);
        });

        btnRemove.setOnAction(event -> {
            System.out.println("removing body");
        });

        // Play, pause, reset buttons
        btnPlay.setOnAction(event -> timer.play());

        btnPause.setOnAction(event -> timer.stop());

        btnReset.setOnAction(event -> {
            entities.getChildren().removeIf(object -> object instanceof Body);
            timer.play();
        });

        tglAxes.setOnMouseClicked(event -> {
            if (tglAxes.isSelected()) {
                entities.getChildren().add(getAxes(1));
            } else {
                // Only group in entities is the group containing cylinders
                entities.getChildren().removeIf(object -> object instanceof Group);
            }
        });

        tglGrid.setOnMouseClicked(event -> {
            if (tglGrid.isSelected()) {
                entities.getChildren().add(getGrid(size, 100));
            } else {
                entities.getChildren().removeIf(object -> object instanceof PolygonMeshView);
            }
        });

        sldrSpeed.setOnMouseReleased(event -> timer.setRate(sldrSpeed.getValue()));

        tgl2D.setOnMouseClicked(event -> {
            if (tgl2D.isSelected()) {
                angleX.set(0);
                angleZ.set(0);
                angleY.set(0);
                initX.setAngle(-90);
            } else {
                initX.setAngle(-30);
            }
        });
    }

    public void spawnBody(String name, double radius, double mass, Color color) {
        System.out.printf("Name: %s\n", name);
        System.out.printf("Mass: %.2f\n", mass);
        System.out.printf("Radius: %.2f\n", radius);
        System.out.printf("Color: %s", color);

        // Slight transparency indicates body has not been spawned in yet
        color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.4);
        newBody = new Body(name, radius, mass, new Point3D(0, 0, 0), color);
        entities.getChildren().add(newBody);

        newBody.setOnDragDetected(event -> {
            // Capture mouse events only for plane (which acts as drag surface)
            plane.setMouseTransparent(false);
            newBody.setMouseTransparent(true);
            newBody.startFullDrag();
            newBody.setCursor(Cursor.MOVE);
        });

        newBody.setOnMouseReleased(event -> {
            // Reset mouse events
            plane.setMouseTransparent(true);
            newBody.setMouseTransparent(false);
            newBody.setCursor(Cursor.DEFAULT);
        });

        plane.setOnMouseDragOver(event -> {
            // Localize mouse position intersect with plane
            Point3D position = event.getPickResult().getIntersectedPoint();
            position = plane.localToParent(position);

            // Move body
            newBody.setTranslateX(position.getX());
            newBody.setTranslateY(position.getY());
            newBody.setTranslateZ(position.getZ());
        });
    }

    public void confirmBody() {
        // Reset mouse events
        newBody.setOnDragDetected(null);
        newBody.setOnMouseReleased(null);

        // Full opacity indicates body has been spawned in successfully
        Color color = newBody.getColor();
        color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 1);
        newBody.setColor(color);
        plane.setOnMouseDragOver(null);
        newBody = null;

        toggleToolButtons(null);
    }

    /**
     * Unselects all ToggleButtons within the vbox that were not clicked.
     * @param selected ToggleButton that was clicked. This ToggleButton will remain selected.
     */
    private void toggleToolButtons(ToggleButton selected) {
        // Disable selection and add body tools when other tools are selected
        selectedBody = null;
        bodies().forEach(n -> n.setOnMouseClicked(null));
        bodyCreator.setVisible(false);
        timer.play();

        if (selected == selectedTool) {
            // User clicks on same button twice to deselect the tool
            selectedTool = null;
        } else {
            // Tool selection
            selectedTool = selected;
        }

        // Deselect all other tools
        for (Node node : vbTools.getChildren()){
            if (node instanceof ToggleButton) {
                if (!node.equals(selected)) {
                    ((ToggleButton) node).setSelected(false);
                }
            }
        }
    }

    public List<Body> bodies() {
        return entities.getChildren().stream()
                .filter(n -> n instanceof Body)
                .map(n -> (Body) n)
                .collect(Collectors.toList());
    }

    public List<Vector3D> vectors() {
        return entities.getChildren().stream().filter(n -> n instanceof Vector3D).map(n -> (Vector3D) n).collect(Collectors.toList());
    }

    /***
     * TODO Documentation, Use Barnes Hut
     *
     */
    public void updateBodies() {
        for (Body currentBody : bodies()) {
            Point3D p1 = currentBody.getPosition();
            for (Body comparedBody : bodies()) {
                if (currentBody != comparedBody) {
                    Point3D p2 = comparedBody.getPosition();
                    double m2 = comparedBody.getMass();

                    Point3D a = getGravity(p1, p2, m2, currentBody.getRadius(), comparedBody.getRadius());
                    currentBody.update(0.01, a);

                    collide(currentBody, comparedBody, currentBody.getPosition().distance(comparedBody.getPosition()));
                }
            }
        }
    }

    public void updateBodiesBarnes() {
        // Find bounding square x,y locations
        double minX = Float.MAX_VALUE;
        double maxX = Float.MIN_VALUE;
        double minY = Float.MAX_VALUE;
        double maxY = Float.MIN_VALUE;
        double minZ = Float.MAX_VALUE;
        double maxZ = Float.MIN_VALUE;

        for (Body body: bodies()) {
            minX = Math.min(minX, body.getTranslateX());
            maxX = Math.max(maxX, body.getTranslateX());
            minY = Math.min(minY, body.getTranslateY());
            maxY = Math.max(maxY, body.getTranslateY());
            minZ = Math.min(minZ, body.getTranslateZ());
            maxZ = Math.max(maxZ, body.getTranslateZ());
        }

//        System.out.printf("Max: %.2f\n", maxX);
//        System.out.printf("Min: %.2f\n", minX);

        double width = Math.max(maxX - minX, maxZ - minZ);

        entities.getChildren().removeIf(node -> node instanceof Rectangle && node != plane);

        // Construct Tree
        root = new Quad(minX, minZ, width, entities);
        for (Body body: bodies()) {
            root.insert(body);
        }

        // Insert planets into tree one by one
        for (Body body: bodies()) {
            gravitate(body, root);
        }
    }

    void gravitate(Body p, Quad tn) {
        System.out.println("Examining body: " + p.getName());
        if (tn.leaf) {
            if (tn.body == null || p == tn.body) return;
            // If leaf node and contains a body, calculate force directly with that body
            System.out.printf("Compared body: %s\n", tn.body);
            Point3D a = getGravity(p.getPosition(), tn.body.getPosition(), tn.body.getMass(), tn.body.getRadius(), p.getRadius());
            p.update(0.01, a);

            return;
        }

        if (tn.center == null) {
            tn.center = tn.centerMass.multiply(1.0 / tn.count);
        }

        if ((tn.width / p.getPosition().distance(tn.center)) < theta) {

            // ???
            p.update(0.01, getGravity(p.getPosition(), tn.center, tn.totalMass, tn.body.getRadius(), p.getRadius()));
            return;
        }

        for (Quad child : tn.children) gravitate(p, child);
    }

    /***
     * TODO Documentation, Use Barnes Hut
     *
     */
    public void updateVectors() {
        double minMagnitude = 0, maxMagnitude = 0;
        boolean start = false;
        //Updating angle
        for (Vector3D vector : vectors()) {
            Point3D vectorPosition = vector.getPosition();
            double currentAngle = vector.getAngle();
            double x = 0;
            double z = 0;
            for (Body body : bodies()) {
                Point3D p2 = body.getPosition();
                double m2 = body.getMass();
                Point3D a = getGravity(vectorPosition, p2, m2, 1, body.getRadius());
                //Summing the gravitational field forces
                x += a.getX();
                z += a.getZ();
            }

            Point3D sumDirection = new Point3D(x, 0, z);
            double newAngle = sumDirection.angle(new Point3D(100, 0, 0));

            double angle;
            if (vector.getPosition().getZ() > 0) {
                angle = newAngle - currentAngle;
                if (sumDirection.getZ() > 0) {
                    angle = -angle;
                }
            } else {
                angle = currentAngle - newAngle;
                if (sumDirection.getZ() < 0) {
                    angle = -angle;
                }
            }
            Rotate rotate = new Rotate(angle, Rotate.X_AXIS);
            vector.getXRotate().angleProperty().set(vector.getXRotate().getAngle() + angle);
            vector.setAngle(newAngle);
            vector.setMagnitude(sumDirection.magnitude());

            // Updating colors
            if (!start) {
                maxMagnitude = vector.getMagnitude();
                minMagnitude = vector.getMagnitude();
                start = true;
            } else {
                if (vector.getMagnitude() > maxMagnitude) {
                    maxMagnitude = vector.getMagnitude();
                }
                if (vector.getMagnitude() < minMagnitude) {
                    minMagnitude = vector.getMagnitude();
                }
            }
        }

        for (Vector3D vectorM : vectors()) {
            vectorM.setArrowColor(maxMagnitude, minMagnitude);
        }
    }

    //TODO: build2 @Author: 
    public void updateNodes() {

    }

    //TODO: build 2
    public void updateAnim() {

    }

    private void collide(Body a, Body b, double distance) {
        // Code adapted from https://stackoverflow.com/questions/345838/ball-to-ball-collision-detection-and-handling
        if (distance > a.getRadius() + b.getRadius()) {
            return;
        }

        // Normal vector
        Point3D p1 = a.getPosition();
        Point3D p2 = b.getPosition();
        Point3D n = p1.subtract(p2);
        double nMag = n.magnitude();

        // Minimum translation distance to push balls after intersecting
        Point3D mtd = n.multiply(((a.getRadius() + b.getRadius()) - nMag) / nMag);

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
        double i = (1 + res) * -vn / im;
        Point3D impulse = mtd.normalize().multiply(i);

        // Change in momentum
        a.setVelocity(a.getVelocity().add(impulse.multiply(im1)));
        b.setVelocity(b.getVelocity().subtract(impulse.multiply(im2)));
    }
}
