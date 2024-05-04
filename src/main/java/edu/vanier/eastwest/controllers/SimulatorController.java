package edu.vanier.eastwest.controllers;


import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import edu.vanier.eastwest.MainApp;
import edu.vanier.eastwest.models.Body;
import edu.vanier.eastwest.models.Quad;
import edu.vanier.eastwest.models.Vector3D;
import edu.vanier.eastwest.util.MySplitPaneSkin;
import edu.vanier.eastwest.util.SaveFileManager;
import static edu.vanier.eastwest.util.Utility.getAxes;
import static edu.vanier.eastwest.util.Utility.getGrid;

import javafx.animation.*;
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
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;


public class SimulatorController {

    @FXML
    private ToggleButton btnAdd;

    @FXML
    private ToggleButton btnPan;

    @FXML
    private Button btnPlay;

    @FXML
    private Button btnOrigin;

    @FXML
    private FontAwesomeIconView play;

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
    private SplitPane spProperties;

    @FXML
    private VBox propertiesPanel;

    @FXML
    private VBox vbTools;

    @FXML
    private MenuItem menuSave;

    @FXML
    private MenuItem menuLoad;

    @FXML
    private Pane preview;
    private Body previewBody;
    private SubScene subScenePreview;

    private Timeline timer;
    private Camera camera;
    private Group entities;
    public Group previewGroup;
    private SubScene subScene;
    public Body selectedBody;
    Body selectedBodyToRemove;

    //Vector field parameters
    private double anchorX, anchorY;
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


    //This rectangle represents the XZ plane and is used to get cursor positions for dragging objects
    Rectangle plane;


    //This object represents the new body that is created when user clicks on Add Body
    Body newBody;


    //This object represents the velocity vector of a newly added body
    Cylinder arrow;

    //Velocity of newly added body
    Point3D velocity = new Point3D(0, 0, 0);
    ToggleButton selectedTool;
    Quad root;

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
        camera.setFarClip(50000);

        // Sub scene
        subScene = new SubScene(entities, 850, 850, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        pane.getChildren().add(subScene);

        // Initialize
        initBodies();
        initControls();
        initPreview();

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

    /***
     * Initializes the mini viewer for body selection and body creation.
     */
    private void initPreview() {
        PerspectiveCamera previewCam = new PerspectiveCamera(true);
        previewCam.setFarClip(500);
        previewCam.setTranslateZ(-50);

        previewGroup = new Group();

        subScenePreview = new SubScene(previewGroup, 200, 200);
        subScenePreview.setCamera(previewCam);
        subScenePreview.setFill(Color.rgb(0, 0, 0));

        preview.getChildren().add(subScenePreview);
    }

    /***
     * The update method for the main animation.
     * @param event
     */
    private void update(ActionEvent event) {
        // Update size of subScene
        subScene.setHeight(pane.getHeight());
        subScene.setWidth(pane.getWidth());
        subScenePreview.setHeight(preview.getHeight());
        subScenePreview.setWidth(preview.getWidth());

        // Disable tglVectors if there are too many Body objects to prevent a crash
        tglVector.setDisable(bodies().size() > 20);

        // Remove all previous rectangles
        entities.getChildren().removeIf(node -> node instanceof Rectangle && node != plane);

        if (usingBarnes) {
            updateBodiesBarnes();
            updateVectorsBarnes();
        } else {
            updateBodies();
            updateVectors();
        }
        // Move camera around selected planet
        if (selectedBody != null) {
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
        //Mass and radius can't be zero or even less than zero
        if(p1 == null || p2 == null || r1 <= 0 || r2 <= 0 || m2 <= 0){
            return new Point3D(0,0,0);
        }

        // Gravity formula from https://en.wikipedia.org/wiki/Newton%27s_law_of_universal_gravitation
        Point3D r = p2.subtract(p1);
        double rMag = r.magnitude();

        // Distance between bodies cannot be less than radius of 2 bodies because bodies would be inside each other
        double rMin = r1 + r2;
        return r.multiply(G * (m2 / Math.pow(Math.max(rMag, rMin), 3)));
    }

    /***
     * Initializes Body instances depending on the preset and adds them to the entities group.
     */
    private void initBodies() {
        switch (MainApp.preset) {
            case "three" -> {
                double l = 100;
                Point3D p1 = new Point3D(0, 0, l);
                Point3D p2 = new Point3D(l * Math.sqrt(3) / 2, 0, -l * 0.5);
                Point3D p3 = new Point3D(-l * Math.sqrt(3) / 2, 0, -l * 0.5);

                double v = 10;
                Point3D v1 = new Point3D(v, 0, 0);
                Point3D v2 = new Point3D(-v * 0.5, 0, -v * Math.sqrt(3) / 2);
                Point3D v3 = new Point3D(-v * 0.5, 0, v * Math.sqrt(3) / 2);

                double mass = 50000;
                double radius = 20;

                Body b1 = new Body("Mass 1", radius, mass, p1, v1, Color.RED, null);
                Body b2 = new Body("Mass 2", radius, mass, p2, v2, Color.YELLOW, null);
                Body b3 = new Body("Mass 3", radius, mass, p3, v3, Color.BLUE, null);
                entities.getChildren().addAll(b1, b2, b3);

            }
            case "random" -> {
                // Define the range
                int min = -2500;
                int max = 2500;
                int maxVel = 5;
                int minVel = -5;
                int minRad = 20;
                int maxRad = 50;

                // Create a Random object
                Random random = new Random();

                for (int i = 0; i < 100; i++) {
                    int x = random.nextInt((max - min) + 1) + min;
                    int z = random.nextInt((max - min) + 1) + min;
                    int radius = random.nextInt(maxRad - minRad + 1) + minRad;
                    Color color = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
                    Body body = new Body("Body " + i,
                            radius,
                            radius * 1000,
                            new Point3D(x, 0, z),
                            new Point3D(
                                    random.nextInt(maxVel - minVel + 1) + minVel,
                                    0,
                                    random.nextInt(maxVel - minVel + 1) + minVel
                            ),
                            color,
                            null
                    );
                    entities.getChildren().add(body);
                }
            }
            case "solar" -> {
                // Images
                Image sunImg = new Image(MainApp.class.getResource("images/sun.png").toExternalForm());
                Image mercuryImg = new Image(MainApp.class.getResource("images/mercury.jpg").toExternalForm());
                Image venusImg = new Image(MainApp.class.getResource("images/venus.jpg").toExternalForm());
                Image earthImg = new Image(MainApp.class.getResource("images/earth.jpg").toExternalForm());
                Image marsImg = new Image(MainApp.class.getResource("images/mars.jpg").toExternalForm());
                Image jupiterImg = new Image(MainApp.class.getResource("images/jupiter.jpg").toExternalForm());
                Image saturnImg = new Image(MainApp.class.getResource("images/saturn.jpg").toExternalForm());
                Image uranusImg = new Image(MainApp.class.getResource("images/uranus.png").toExternalForm());

                // Bodies
                Body sun = new Body("Sun", 100, 1989000, new Point3D(0, 0, 0), new Point3D(0, 0, 0), null, sunImg);
                Body mercury = new Body("Mercury", 5, 10, new Point3D(240, 0, 0), new Point3D(0, 0, 35), null, mercuryImg);
                Body venus = new Body("Venus", 24, 50, new Point3D(350, 0, 0), new Point3D(0, 0, -25), null, venusImg);
                Body earth = new Body("Earth", 24, 60, new Point3D(0, 0, 420), new Point3D(25, 0, 0), null, earthImg);
                Body mars = new Body("Mars", 7, 3, new Point3D(-480, 0, 0), new Point3D(0, 0, 21), null, marsImg);
                Body jupiter = new Body("Jupiter", 40, 180, new Point3D(0, 0, 680), new Point3D(-20, 0, 0), null, jupiterImg);
                Body saturn = new Body("Saturn", 35, 160, new Point3D(0, 0, -770), new Point3D(20, 0, 0), null, saturnImg);
                Body uranus = new Body("Uranus", 30, 140, new Point3D(-850, 0, 0), new Point3D(0, 0, 15), null, uranusImg);
                entities.getChildren().addAll(sun, mercury, venus, earth, mars, jupiter, saturn, uranus);
            }
            case "load" -> {
                try{
                    load(new ActionEvent());
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    /***
     * Creates Vector3D arrows around each Body instance.
     */
    private void initVectors() {
        for (Body body : bodies()){
            int xVariableForVectorSpawning = 2 + (int) Math.ceil(Math.log10(body.getRadius()));
            int zVariableForVectorSpawning = 2 + (int) Math.ceil(Math.log10(body.getRadius()));
            int xDistanceForVectorSpawning = 100;
            int zDistanceForVectorSpawning = 100;
            for (int i = -xVariableForVectorSpawning; i <= xVariableForVectorSpawning; i++) {
                for (int j = -zVariableForVectorSpawning; j <= zVariableForVectorSpawning; j++) {
                    Vector3D v = new Vector3D(7, 25, new Point3D(i * xDistanceForVectorSpawning + (int) Math.round(body.getTranslateX() / 100) * 100, 0, j * zDistanceForVectorSpawning + (int) Math.round(body.getTranslateZ() / 100) * 100));
                    v.getTransforms().add(new Rotate(90, 1, 0, 0));
                    v.getTransforms().add(v.getXRotate());
                    body.getVectors().add(v);
                    entities.getChildren().add(v);
                }
            }
        }
        if(usingBarnes){
            updateVectorsBarnes();
        }else{
            updateVectors();
        }
    }

    /***
     * Initializes all the EventHandlers for the UI controls.
     */
    private void initControls() {

        // Bind rotation angle to camera with mouse movement
        Rotate x1Rotate = new Rotate(0, Rotate.X_AXIS);
        Rotate x2Rotate = new Rotate(0, Rotate.Z_AXIS);
        Rotate yRotate = new Rotate(0, Rotate.Y_AXIS);
        Rotate initX = new Rotate(-30, Rotate.X_AXIS);
        Rotate autoRotateY = new Rotate(0, Rotate.Y_AXIS);
        Translate zoom = new Translate(0, 0, -2000);
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
            if (selectedTool == btnRemove) {
                bodies().forEach(n -> n.setOnMouseClicked(e -> {
                    selectedBodyToRemove = n;
                    entities.getChildren().remove(selectedBodyToRemove);
                }));
            } else if (selectedTool == btnSelection) {
                // Select planet by clicking it with LMB when not panning
                bodies().forEach(body -> body.setOnMouseClicked(e -> {
                    // Update reference
                    selectedBody = body;

                    // Update preview scene
                    spProperties.setDividerPosition(0, 0.3);
                    previewGroup.getChildren().clear();
                    previewBody = selectedBody.clonePreview();
                    previewBody.setRadius(10);
                    previewGroup.getChildren().add(previewBody);

                    // Rotate selected body in preview
                    RotateTransition spin = new RotateTransition(Duration.seconds(30), previewBody);
                    spin.setByAngle(360);
                    spin.setAxis(new Point3D(0, 1, 0));
                    spin.setCycleCount(RotateTransition.INDEFINITE);
                    spin.play();

                    // Timeline to smoothly jump between camera positions
                    Timeline jump = new Timeline(
                            new KeyFrame(
                                    Duration.seconds(0),
                                    new KeyValue(camera.translateXProperty(), camera.getTranslateX())
                            ),
                            new KeyFrame(
                                    Duration.seconds(0),
                                    new KeyValue(camera.translateYProperty(), camera.getTranslateY())
                            ),
                            new KeyFrame(
                                    Duration.seconds(0),
                                    new KeyValue(camera.translateZProperty(), camera.getTranslateZ())
                            ),
                            new KeyFrame(
                                    Duration.seconds(0.15),
                                    new KeyValue(camera.translateXProperty(), selectedBody.getTranslateX(), Interpolator.EASE_BOTH)
                            ),
                            new KeyFrame(
                                    Duration.seconds(0.15),
                                    new KeyValue(camera.translateYProperty(), selectedBody.getTranslateY(), Interpolator.EASE_BOTH)
                            ),
                            new KeyFrame(
                                    Duration.seconds(0.15),
                                    new KeyValue(camera.translateZProperty(), selectedBody.getTranslateZ(), Interpolator.EASE_BOTH)
                            )
                    );
                    jump.play();

                    // Update properties panel
                    lblSelected.setText(selectedBody.getName());
                    lblProperties.setText(selectedBody.toString());
                }));
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
                    angleX.set(anchorAngleX + (-Math.cos(Math.toRadians(angleY.get() + autoRotateY.angleProperty().get()))) * (anchorY - event.getSceneY())/5);
                    angleZ.set(anchorAngleZ + Math.sin(Math.toRadians(angleY.get() + autoRotateY.angleProperty().get())) * (anchorY - event.getSceneY())/5);
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

        // Reset camera position
        btnOrigin.setOnAction(event -> {
            camera.setTranslateX(0);
            camera.setTranslateY(0);
            camera.setTranslateZ(0);
        });

        // Vector Field visual toggle button
        tglVector.setOnMouseClicked(event -> {
            if (!tglVector.isSelected()) {
                // Remove vector field
                entities.getChildren().removeAll(vectors());
            }
            else {
                // Add vector field
                if (!bodies().isEmpty()){
                    initVectors();
                }
            }
        });

        // Adding bodies
        btnAdd.setOnAction(event -> {
            toggleToolButtons(btnAdd);
            controller.initBody();
        });

        btnRemove.setOnAction(event -> {
            toggleToolButtons(btnRemove);
        });

        // Switch between Direct sum and Barnes Hut algorithms
        btnAlgorithm.setOnAction(event -> {
            btnAlgorithm.setText(String.format("Algorithm: %s", usingBarnes ? "Direct Sum" : "Barnes-Hut"));
            sliderTheta.setDisable(usingBarnes);
            tglBarnes.setDisable(usingBarnes);
            usingBarnes = !usingBarnes;
        });


        // Play, pause, reset buttons
        btnPlay.setOnAction(event -> {
            if (btnPlay.getText().equals("Play")) {
                timer.play();
                btnPlay.setText("Pause");
                play.setGlyphName("PAUSE");
            } else {
                timer.pause();
                btnPlay.setText("Play");
                play.setGlyphName("PLAY");
            }
        });

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

        // Saving & Loading Body objects
        menuSave.setOnAction(this::save);
        menuLoad.setOnAction(this::load);
    }

    /***
     * Spawns in the simulation a preview of the created Body. This preview can be dragged to difference positions.
     * @param name The name of the Body.
     * @param radius The radius of the Body.
     * @param mass The mass of the Body.
     * @param color The color of the Body.
     * @param texture The image texture of the Body.
     */
    public void spawnBody(String name, double radius, double mass, Color color, Image texture) {
        System.out.printf("Name: %s\n", name);
        System.out.printf("Mass: %.2f\n", mass);
        System.out.printf("Radius: %.2f\n", radius);
        System.out.printf("Color: %s\n", color);
        System.out.println("Texture: "+ texture);

        // Slight transparency indicates body has not been spawned in yet
        newBody = new Body(name, radius, mass, new Point3D(0, 0, 0), new Point3D(0, 0, 0), color, texture);
        newBody.setTransparency(0.4);
        controller.setXZ(0,0);
        entities.getChildren().add(newBody);

        newBody.setOnDragDetected(event -> {
            // Capture mouse events only for plane (which acts as drag surface)
            plane.setMouseTransparent(false);
            newBody.setMouseTransparent(true);
            newBody.startFullDrag();
            newBody.setCursor(Cursor.CLOSED_HAND);
        });

        newBody.setOnMouseReleased(event -> {
            // Reset mouse events
            plane.setMouseTransparent(true);
            newBody.setMouseTransparent(false);
            newBody.setCursor(Cursor.OPEN_HAND);
        });
        controller.xField.setOnAction(event -> {
            newBody.setTranslateX(Math.round(Double.parseDouble(controller.xField.getText())));
        });
        controller.zField.setOnAction(event -> {
            newBody.setTranslateZ(Double.parseDouble(controller.zField.getText()));
        });
        plane.setOnMouseDragOver(event -> {
            // Localize mouse position intersect with plane
            Point3D position = event.getPickResult().getIntersectedPoint();
            position = plane.localToParent(position);

            // Move body
            newBody.setTranslateX(position.getX());
            newBody.setTranslateZ(position.getZ());
            controller.setXZ(position.getX(), position.getZ());
        });
        timer.pause();
        btnPlay.setDisable(true);
        btnReset.setDisable(true);
    }

    /***
     * Confirms the position of the created Body. A velocity can be dragged from the created Body.
     */
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
            newBody.setCursor(Cursor.DEFAULT);
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

    /***
     * Confirms the creation of the Body instance.
     */
    public void confirmBody() {
        // Reset mouse events
        newBody.setOnDragDetected(null);
        newBody.setOnMouseReleased(null);
        plane.setOnMouseDragOver(null);

        // Full opacity indicates body has been spawned in successfully
        newBody.setTransparency(1);

        // Remove arrow, deselect body, reset velocity
        newBody.setVelocity(velocity.multiply(0.4));
        velocity = new Point3D(0, 0, 0);
        entities.getChildren().remove(arrow);
        arrow = null;
        newBody = null;
        if(btnPlay.getText().equals("Play")){
            timer.pause();
        }
        else{
            timer.play();
        }
        btnPlay.setDisable(false);
        btnReset.setDisable(false);
        btnAdd.setSelected(false);
        toggleToolButtons(selectedTool);
    }

    /**
     * Manages the ToggleButtons to determine UI behaviour. Unselects all ToggleButtons within the vbox that were not clicked.
     * @param selected ToggleButton that was clicked. This ToggleButton will remain selected.
     */
    private void toggleToolButtons(ToggleButton selected) {
        // Disable selection and add body tools when other tools are selected
        selectedBody = null;
        previewGroup.getChildren().clear();
        pane.setCursor(Cursor.DEFAULT);
        bodies().forEach(n -> n.setOnMouseClicked(null));
        bodyCreator.setVisible(false);
        preview.setVisible(false);
        spProperties.setDividerPosition(0, 0);

        if (selected == selectedTool) {
            // User clicks on same button twice to deselect the tool
            selectedTool = null;
        }else {
            // Tool selection
            selectedTool = selected;
            if (selected == btnPan) {
                pane.setCursor(Cursor.MOVE);
            } else if (selected == btnSelection) {
                propertiesPanel.setManaged(false);
                pane.setCursor(Cursor.CROSSHAIR);
                preview.setVisible(true);
            } else if (selected == btnRemove) {
                pane.setCursor(Cursor.CROSSHAIR);
            } else if (selected == btnAdd) {
                propertiesPanel.setManaged(true);
                pane.setCursor(Cursor.OPEN_HAND);
                bodyCreator.setVisible(true);
                preview.setVisible(true);
                spProperties.setDividerPosition(0, 0.3);
            }
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

    /***
     * Filters the Group entities for Body objects and returns them in a list.
     * @return A List containing all the Body objects in the simulation.
     */
    public List<Body> bodies() {
        return entities.getChildren().stream()
                .filter(n -> n instanceof Body)
                .map(n -> (Body) n)
                .collect(Collectors.toList());
    }

    /***
     * Filters the Group entities for Vector3D objects and returns them in a list.
     * @return A List containing all the Vector3D objects in the simulation.
     */
    public List<Vector3D> vectors() {
        return entities.getChildren().stream()
                .filter(n -> n instanceof Vector3D)
                .map(n -> (Vector3D) n)
                .collect(Collectors.toList());
    }

    /***
     * Updates all Body instances in the simulation using the Direct Sum Algorithm. This method is called each frame if the calculation mode is Barnes-Hut.
     */
    public void updateBodies() {
        for (Body currentBody : bodies()) {
            Point3D p1 = currentBody.getPosition();
            for (Body comparedBody : bodies()) {
                if (currentBody != comparedBody) {
                    Point3D p2 = comparedBody.getPosition();
                    double m2 = comparedBody.getMass();

                    Point3D a = getGravity(p1, p2, m2, currentBody.getRadius(), comparedBody.getRadius());
                    currentBody.setAcceleration(a);

                    collide(currentBody, comparedBody);
                }
                currentBody.update(dt);
            }
        }
    }

    /***
     * Updates all Body instances in the simulation using the Barnes-Hut Algorithm. This method is called each frame if the calculation mode is Barnes-Hut.
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
        root = new Quad(minX, minZ, length, entities, tglBarnes.isSelected());

        // Construct Barnes-Hut Tree by inserting bodies into root node
        for (Body body: bodies()) {
            root.insert(body);
        }

        // Compute gravity and collisions for all bodies
        for (Body body: bodies()) {
             attract(body, root);
        }
    }

    /***
     * TODO JavaDoc
     * @param body
     * @param quad
     */
    private void attract(Body body, Quad quad) {
        // Base case - External nodes
        if (quad.isExternal()) {
            // Ignore if compared body is the same as current body or node does not contain a body
            if (quad.body != null) {
                if (body != quad.body) {
                    // Gravity
                    Point3D a = getGravity(body.getPosition(), quad.body.getPosition(), quad.body.getMass(), quad.body.getRadius(), body.getRadius());
                    body.setAcceleration(a);

                    // Collisions
                    collide(body, quad.body);
                }
                body.update(dt);
            }
        } else {
            // Center of mass obtained by diving sum of weighted positions with total mass
            // https://math.libretexts.org/Courses/Mission_College/Math_3B%3A_Calculus_2_(Sklar)/06%3A_Applications_of_Integration/6.06%3A_Moments_and_Centers_of_Mass
            Point3D centerMass = quad.weightedPositions.multiply(1.0 / quad.totalMass);

            // Check if Barnes-Hut criterion for estimation has been met
            if ((quad.getLength() / body.getPosition().distance(centerMass)) < theta) {
                // Base case - Estimate internal node as a single body
                Point3D a = getGravity(body.getPosition(), centerMass, quad.totalMass, body.getRadius(), body.getRadius());
                body.setAcceleration(a);
                body.update(dt);
            } else {
                // Recursive case - Barnes-Hut criterion has not been met
                for (Quad child : quad.children) {
                    attract(body, child);
                }
            }
        }
    }

    /***
     * Updates all Vector3D instances in the simulation using the Barnes-Hut Algorithm. This method is called each frame if the calculation mode is Barnes-Hut.
     */
    public void updateVectorsBarnes() {
        toggleDuplicateVector();

        double minMagnitude = 0, maxMagnitude = 0;
        boolean start = false;

        for (Vector3D vector : vectors()) {
            // Compute gravity
            Point3D temp = attractVector(vector, root);
            Point3D sumGravityField = new Point3D(temp.getX(), 0, temp.getZ());
            double newAngle = sumGravityField.angle(new Point3D(100, 0, 0));

            rotateVector(vector, newAngle, sumGravityField);

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

    /***
     * TODO JavaDoc
     * @param vector
     * @param quad
     * @return
     */
    private Point3D attractVector(Vector3D vector, Quad quad) {
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


    /***
     * Updates all Vector3D instances in the simulation using a Direct Sum Algorithm. This method is called each frame if the calculation mode is Direct Sum.
     */
    public void updateVectors() {
        toggleDuplicateVector();
        double minMagnitude = 0, maxMagnitude = 0;
        boolean start = false;
        // Update angles
        for (Vector3D vector : vectors()) {
            if(vector.isVisible()) {
                Point3D vectorPosition = vector.getPosition();
                Point3D sumGravityField = new Point3D(0, 0, 0);

                for (Body body : bodies()) {
                    Point3D bodyPosition = body.getPosition();
                    double bodyMass = body.getMass();
                    Point3D gravityField = getGravity(vectorPosition, bodyPosition, bodyMass, body.getRadius(), body.getRadius());
                    //Summing the gravitational field forces
                    sumGravityField = sumGravityField.add(gravityField.getX(), 0, gravityField.getZ());
                }

                double newAngle = sumGravityField.angle(new Point3D(Integer.MAX_VALUE, 0, 0));
                rotateVector(vector, newAngle, sumGravityField);

                // Finding the maximum and minimum magnitudes
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
        }
        // Updating the colors
        for (Vector3D vectorM : vectors()) {
            vectorM.setArrowColor(maxMagnitude, minMagnitude);
        }
    }

    /***
     * Hides Vector3D instances that are at the same position as another Vector3D instance
     */
    private void toggleDuplicateVector(){
        // Removing duplicate vector that is at the same position as another vector
        for (Vector3D vectorIterated : vectors()) {
            for (Vector3D vectorLooped : vectors()) {
                if(!(vectorIterated == vectorLooped) && (vectorIterated.getPosition().equals(vectorLooped.getPosition())) && (vectorLooped.isVisible())) {
                    vectorLooped.setVisible(false);
                    break;
                }
                vectorIterated.setVisible(true);
            }
        }
    }

    /***
     * Rotates a Vector3D instance by setting its angle formed with the positive x-axis.
     * @param vector The vector to rotate.
     * @param newAngle Angle from position x-axis.
     * @param sumGravityField
     */
    private void rotateVector(Vector3D vector, double newAngle, Point3D sumGravityField) {
        double angle;
        if (vector.getPosition().getZ() > 0) {
            angle = newAngle;
            if (sumGravityField.getZ() >= 0) {
                angle = -angle;
            }
        } else {
            angle = -newAngle;
            if (sumGravityField.getZ() < 0) {
                angle = -angle;
            }
        }
        vector.getXRotate().setAngle(angle);
        vector.setMagnitude(sumGravityField.magnitude());
    }

    /***
     * Checks and handles collisions between two Body objects.
     * @param a The first Body instance;
     * @param b The second Body instance;
     */
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
            entities.getChildren().addAll(Objects.requireNonNull(SaveFileManager.fromJson(saveFile.getPath())));
        }
    }
}
