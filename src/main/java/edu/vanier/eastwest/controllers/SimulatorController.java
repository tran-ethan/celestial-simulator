package edu.vanier.eastwest.controllers;

import edu.vanier.eastwest.MainApp;
import edu.vanier.eastwest.models.Body;
import edu.vanier.eastwest.models.MySplitPaneSkin;
import edu.vanier.eastwest.models.Quad;
import edu.vanier.eastwest.models.Vector3D;
import edu.vanier.eastwest.util.SaveFileManager;
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
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.ToggleSwitch;
import org.fxyz3d.shapes.polygon.PolygonMeshView;

import java.io.File;
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
    private Button btnAlgorithm;

    @FXML
    private ToggleButton btnSelection;

    @FXML
    private Pane pane;

    @FXML
    private SplitPane splitPane;

    @FXML
    private Slider sliderSpeed;

    @FXML
    private Slider sliderG;

    @FXML
    private Slider sliderTheta;

    @FXML
    private ToggleSwitch tgl2D;

    @FXML
    private ToggleSwitch tglVector;

    @FXML
    private ToggleSwitch tglAxes;

    @FXML
    private ToggleSwitch tglGrid;

    @FXML
    private ToggleSwitch tglCamRotate;

    @FXML
    private ToggleSwitch tglBarnes;

    @FXML
    private Label lblSelected;

    @FXML
    private Label lblProperties;

    @FXML
    private VBox propertiesPanel;

    @FXML
    private VBox vbTools;

    @FXML
    private MenuItem menuSave;

    @FXML
    private MenuItem menuLoad;

    private Timeline timer;
    private Camera camera;
    private Group entities;
    private SubScene subScene;
    Body selectedBody;

    private double anchorX, anchorY;
    private final int xVariableForVectorSpawning = 5;
    private final int zVariableForVectorSpawning = 5;
    private final int xDistanceForVectorSpawning = 100;
    private final int zDistanceForVectorSpawning = 100;

    private double anchorAngleX, anchorAngleZ, anchorAngleY = 0;

    private final DoubleProperty angleX = new SimpleDoubleProperty(0);
    private final DoubleProperty angleZ = new SimpleDoubleProperty(0);
    private final DoubleProperty angleY = new SimpleDoubleProperty(0);

    // Simulator parameters
    private static boolean usingBarnes = false;
    private static double theta = 0.5;
    private static double G = 1;
    private static final double dt = 0.015; // Time between frames in seconds

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

    /**
     * This object represents the velocity vector of a newly added body
     */
    Cylinder arrow;

    /**
     * Velocity of newly added body
     */
    Point3D velocity = new Point3D(0, 0, 0);
    ToggleButton selectedTool;

    float size = 5000; // Size of plane

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
                new KeyFrame(Duration.seconds(dt), onFinished)
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

        // Remove all previous rectangles
        entities.getChildren().removeIf(node -> node instanceof Rectangle && node != plane);

        if (usingBarnes) {
            updateBodiesBarnes();
            updateVectorsBarnes();
        } else {
            updateBodies();
            updateVectors();
        }

        //updateVectors();

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
        // Gravity formula from https://en.wikipedia.org/wiki/Newton%27s_law_of_universal_gravitation
        Point3D r = p2.subtract(p1);
        double rMag = r.magnitude();

        // Distance between bodies cannot be less than radius of 2 bodies because bodies would be inside each other
        double rMin = r1 + r2;
        return r.multiply(G * (m2 / Math.pow(Math.max(rMag, rMin), 3)));
    }

    private void initBodies() {
        Body sun = new Body("Sun", 30, 100000, new Point3D(0, 0, 0), new Point3D(0, 0, 0), Color.YELLOW, null);
        Body p1 = new Body("Blue", 10, 20000, new Point3D(125, 0, 120), new Point3D(0, 0, 10), Color.BLUE, null);
        Body p2 = new Body("Green", 10, 5000, new Point3D(200, 0, 100), new Point3D(-4, 0, 0), Color.GREEN, null);
        Body p3 = new Body("White", 10, 5000, new Point3D(150, 0, 200), new Point3D(10, 0, 10), Color.WHITE, null);
        Body p4 = new Body("Red", 10, 5000, new Point3D(200, 0, 200), new Point3D(0, 0, -5), Color.RED, null);

        // Body sun = new Body("Sun", 30, 100000, new Point3D(0, 0, -50), Color.rgb(255,255,0,1), null);
        // Body p1 = new Body("Earth", 10, 20000, new Point3D(150, 0, -100), Color.BLUE, null);
        // Body p2 = new Body("A", 10, 5000, new Point3D(0, 0, 100), Color.GREEN, null);
        // Body p3 = new Body("B", 10, 5000, new Point3D(0, 0, 200), Color.WHITE, null);

        entities.getChildren().addAll(sun, p1, p2, p3, p4);
    }

    /***
     * TODO: Make vectors appear only near bodies
     * Creates Vector3D arrows around Body objects.
     */
    private void initVectors() {
        for (int i = -xVariableForVectorSpawning; i <= xVariableForVectorSpawning; i++) {
            for(int j = -zVariableForVectorSpawning; j <= zVariableForVectorSpawning; j++) {
                Vector3D v = new Vector3D(7, 25, new Point3D(i * xDistanceForVectorSpawning, 0, zDistanceForVectorSpawning*j));
                v.getTransforms().add(new Rotate(90, 1, 0, 0));
                v.getTransforms().add(v.getXRotate());
                entities.getChildren().add(v);
            }
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

        // Camera rotate switch
        tglCamRotate.setOnMouseClicked(event -> {
            if (tglCamRotate.isSelected()) {
                rotateTimer.play();
            } else {
                rotateTimer.pause();
            }
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

            if (tglCamRotate.isSelected()) {
                rotateTimer.pause();
            }
        };
        MainApp.scene.setOnMousePressed(mousePressedHandler);

        EventHandler<MouseEvent> mouseReleasedHandler = event -> {
            if (tglCamRotate.isSelected()) {
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
                camera.setTranslateX(camera.getTranslateX() - Math.sin(Math.toRadians(angleY.get() + autoRotateY.angleProperty().get())) * (anchorY - event.getSceneY()));
                camera.setTranslateZ(camera.getTranslateZ() - Math.cos(Math.toRadians(angleY.get() + autoRotateY.angleProperty().get())) * (anchorY - event.getSceneY()));
                camera.setTranslateX(camera.getTranslateX() + Math.cos(Math.toRadians(angleY.get() + autoRotateY.angleProperty().get())) * (anchorX - event.getSceneX()));
                camera.setTranslateZ(camera.getTranslateZ() - Math.sin(Math.toRadians(angleY.get() + autoRotateY.angleProperty().get())) * (anchorX - event.getSceneX()));

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
        btnPan.setOnAction(event -> toggleToolButtons(btnPan));

        // Selection toggle button
        btnSelection.setOnAction(event -> toggleToolButtons(btnSelection));

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

        // Switch between Direct sum and Barnes Hut algorithms
        btnAlgorithm.setOnAction(event -> {
            btnAlgorithm.setText(String.format("Currently using %s Algorithm", usingBarnes ? "Direct Sum" : "Barnes-Hut"));
            sliderTheta.setDisable(usingBarnes);
            tglBarnes.setDisable(usingBarnes);
            usingBarnes = !usingBarnes;
        });

        btnRemove.setOnAction(event -> System.out.println("removing body"));

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

        sliderSpeed.setOnMouseReleased(event -> timer.setRate(sliderSpeed.getValue()));

        sliderG.setOnMouseReleased(event -> G = sliderG.getValue());

        sliderTheta.setOnMouseReleased(event -> theta = sliderTheta.getValue());

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

        //Saving & Loading Body objects
        menuSave.setOnAction(this::save);
        menuLoad.setOnAction(this::load);
    }

    public void spawnBody(String name, double radius, double mass, Color color, Image texture) {
        System.out.printf("Name: %s\n", name);
        System.out.printf("Mass: %.2f\n", mass);
        System.out.printf("Radius: %.2f\n", radius);
        System.out.printf("Color: %s", color);
        System.out.println("Texture: "+ texture);

        // Slight transparency indicates body has not been spawned in yet
        color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.4);
        newBody = new Body(name, radius, mass, new Point3D(0, 0, 0), new Point3D(0, 0, 0), color, texture);
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

    public void confirmPos() {
        // Cylinder start and end point https://netzwerg.ch/blog/2015/03/22/javafx-3d-line/
        Point3D yAxis = new Point3D(0, 1, 0);

        arrow = new Cylinder(1, 2);
        arrow.setMaterial(new PhongMaterial(Color.DARKRED));
        Translate moveToMidpoint = new Translate();
        Rotate rotateAroundCenter = new Rotate();
        arrow.getTransforms().addAll(
                moveToMidpoint,
                rotateAroundCenter
        );
        entities.getChildren().add(arrow);

        newBody.setOnDragDetected(event -> {
            // Capture mouse events only for plane (which acts as drag surface)
            plane.setMouseTransparent(false);
            newBody.setMouseTransparent(true);
            newBody.startFullDrag();
            newBody.setCursor(Cursor.CROSSHAIR);
        });

        plane.setOnMouseDragOver(event -> {
            // Localize mouse position intersect with plane
            Point3D position = event.getPickResult().getIntersectedPoint();
            position = plane.localToParent(position);

            // Distance and midpoint
            Point3D r = position.subtract(newBody.getPosition());
            Point3D mid = position.midpoint(newBody.getPosition());
            double height = r.magnitude();

            // Move cylinder end point
            moveToMidpoint.setX(mid.getX());
            moveToMidpoint.setZ(mid.getZ());

            // Rotate cylinder to end point
            Point3D axisOfRotation = r.crossProduct(yAxis);
            double angle = Math.acos(r.normalize().dotProduct(yAxis));
            rotateAroundCenter.setAxis(axisOfRotation);
            rotateAroundCenter.setAngle(-Math.toDegrees(angle));
            arrow.setHeight(height);

            // Set velocity for confirming body
            velocity = r;
        });
    }

    public void confirmBody() {
        // Reset mouse events
        newBody.setOnDragDetected(null);
        newBody.setOnMouseReleased(null);

        // Full opacity indicates body has been spawned in successfully
        if (newBody.getMaterial() != null) {
            Color color = newBody.getColor();
            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 1);
            newBody.setColor(color);
        }
        plane.setOnMouseDragOver(null);

        // Remove arrow, deselect body, reset velocity
        newBody.setVelocity(velocity.multiply(0.4));
        velocity = new Point3D(0, 0, 0);
        entities.getChildren().remove(arrow);
        arrow = null;
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
     * Calculates the gravity between each Body object using a direct sum algorithm.
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
                    currentBody.update(dt, a);

                    collide(currentBody, comparedBody);
                }
            }
        }
    }

    /***
     * Calculates the gravity between each Body object using the Barnes-Hut algorithm.
     *
     */
    public void updateBodiesBarnes() {
        // https://www.cs.princeton.edu/courses/archive/fall03/cs126/assignments/barnes-hut.html

        // Find min X and Z locations
        double minX = Float.MAX_VALUE;
        double maxX = Float.MIN_VALUE;
        double minZ = Float.MAX_VALUE;
        double maxZ = Float.MIN_VALUE;

        for (Body body: bodies()) {
            minX = Math.min(minX, body.getTranslateX());
            maxX = Math.max(maxX, body.getTranslateX());
            minZ = Math.min(minZ, body.getTranslateZ());
            maxZ = Math.max(maxZ, body.getTranslateZ());
        }

        double length = Math.max(maxX - minX, maxZ - minZ);

        // Create root node with delimiting square
        Quad root = new Quad(minX, minZ, length, entities, tglBarnes.isSelected());

        // Construct Barnes-Hut Tree by inserting bodies into root node
        for (Body body: bodies()) {
            root.insert(body);
        }

        // Compute gravity and collisions for all bodies
        for (Body body: bodies()) {
             attract(body, root);
        }
    }

    public void updateVectorsBarnes() {
        double minMagnitude = 0, maxMagnitude = 0;
        boolean start = false;

        // https://www.cs.princeton.edu/courses/archive/fall03/cs126/assignments/barnes-hut.html

        // Find min X and Z locations
        double minX = Float.MAX_VALUE;
        double maxX = Float.MIN_VALUE;
        double minZ = Float.MAX_VALUE;
        double maxZ = Float.MIN_VALUE;

        for (Body body : bodies()) {
            minX = Math.min(minX, body.getTranslateX());
            maxX = Math.max(maxX, body.getTranslateX());
            minZ = Math.min(minZ, body.getTranslateZ());
            maxZ = Math.max(maxZ, body.getTranslateZ());
        }
        for (Vector3D vector : vectors()) {
            double currentAngle = vector.getAngle();
            double x = 0, z = 0;
            minX = Math.min(minX, vector.getTranslateX());
            maxX = Math.max(maxX, vector.getTranslateX());
            minZ = Math.min(minZ, vector.getTranslateZ());
            maxZ = Math.max(maxZ, vector.getTranslateZ());

            double length = Math.max(maxX - minX, maxZ - minZ);

            // Create root node with delimiting square
            Quad root = new Quad(minX, minZ, length, entities, false);

            // Construct Barnes-Hut Tree by inserting bodies into root node
            for (Body body : bodies()) {
                root.insert(body);
            }

            // Compute gravity
            Point3D temp = attractVector(vector, root);
            //System.out.println(" x: " + temp.getX() + ", z: " + temp.getZ());
            System.out.println(temp.magnitude());
            Point3D sumDirection = new Point3D(temp.getX(), 0, temp.getZ());
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


    Point3D attractVector(Vector3D vector, Quad quad) {
        double x = 0, z = 0;
        // Base case - External nodes
        if (quad.isExternal()) {
            // Ignore if node does not contain a body
            if (quad.body != null) {
                // Gravity
                Point3D temp = getGravity(vector.getPosition(), quad.body.getPosition(), quad.body.getMass(), quad.body.getRadius(), 1);
                x += temp.getX();
                z += temp.getZ();
            }
        } else {
            // Center of mass obtained by diving sum of weighted positions with total mass
            // https://math.libretexts.org/Courses/Mission_College/Math_3B%3A_Calculus_2_(Sklar)/06%3A_Applications_of_Integration/6.06%3A_Moments_and_Centers_of_Mass
            Point3D centerMass = quad.weightedPositions.multiply(1.0 / quad.totalMass);

            // Check if threshold for estimation has been met
            if ((quad.getLength() / vector.getPosition().distance(centerMass)) < theta) {
                // Base case - Estimate internal node as a single body
                Point3D temp = getGravity(vector.getPosition(), centerMass, quad.totalMass, 1,1);
                x += temp.getX();
                z += temp.getZ();
            } else {
                // Recursive case - Threshold has not been met
                for (Quad child : quad.children) {
                    Point3D temp = attractVector(vector, child);
                    x += temp.getX();
                    z += temp.getZ();
                }
            }
        }
        return new Point3D(x, 0, z);
    }

    void attract(Body body, Quad quad) {
        // Base case - External nodes
        if (quad.isExternal()) {
            // Ignore if compared body is the same as current body or node does not contain a body
            if (quad.body != null && body != quad.body) {
                // Gravity
                Point3D a = getGravity(body.getPosition(), quad.body.getPosition(), quad.body.getMass(), quad.body.getRadius(), body.getRadius());
                body.update(dt, a);

                // Collisions
                collide(body, quad.body);
            }
        } else {
            // Center of mass obtained by diving sum of weighted positions with total mass
            // https://math.libretexts.org/Courses/Mission_College/Math_3B%3A_Calculus_2_(Sklar)/06%3A_Applications_of_Integration/6.06%3A_Moments_and_Centers_of_Mass
            Point3D centerMass = quad.weightedPositions.multiply(1.0 / quad.totalMass);

            // Check if threshold for estimation has been met
            if ((quad.getLength() / body.getPosition().distance(centerMass)) < theta) {
                // Base case - Estimate internal node as a single body
                Point3D a = getGravity(body.getPosition(), centerMass, quad.totalMass, body.getRadius(), body.getRadius());
                body.update(dt, a);
            } else {
                // Recursive case - Threshold has not been met
                for (Quad child : quad.children) {
                    attract(body, child);
                }
            }
        }
    }

    /***
     * Rotates the vectors towards the direction of the net gravitational field caused by all the Body objects using a direct sum algorithm.
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

    private void collide(Body a, Body b) {
        // Code adapted from https://stackoverflow.com/questions/345838/ball-to-ball-collision-detection-and-handling
        double distance = a.getPosition().distance(b.getPosition());
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

    /***
     * Saves all the Body objects into a Json file.
     */
    public void save(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        Stage stage = new Stage();
        File saveFile = fileChooser.showSaveDialog(stage);

        if (saveFile != null){
            SaveFileManager.toJson(bodies(), saveFile.getPath());
        }
    }

    /***
     * Loads all the Body objects saved in a Json file.
     */
    public void load(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        Stage stage = new Stage();
        File saveFile = fileChooser.showOpenDialog(stage);

        if (saveFile != null){
            entities.getChildren().removeAll(bodies());
            entities.getChildren().addAll(SaveFileManager.fromJson(saveFile.getPath()));
        }
    }
}
