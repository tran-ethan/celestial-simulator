module edu.vanier.eastwest {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.fxyz3d.core;
    requires org.controlsfx.controls;
    requires javafx.graphics;
    requires static lombok;
    requires com.google.gson;
    requires de.jensd.fx.glyphs.fontawesome;


    opens edu.vanier.eastwest to javafx.fxml;
    exports edu.vanier.eastwest;
    exports edu.vanier.eastwest.controllers;
    opens edu.vanier.eastwest.controllers to javafx.fxml;
    exports edu.vanier.eastwest.models;
    opens edu.vanier.eastwest.models to javafx.fxml;
    exports edu.vanier.eastwest.util to com.google.gson;
}