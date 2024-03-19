module edu.vanier.eastwest {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.fxyz3d.core;
    requires org.controlsfx.controls;


    opens edu.vanier.eastwest to javafx.fxml;
    exports edu.vanier.eastwest;
    exports edu.vanier.eastwest.controllers;
    opens edu.vanier.eastwest.controllers to javafx.fxml;
    exports edu.vanier.eastwest.models;
    opens edu.vanier.eastwest.models to javafx.fxml;
}