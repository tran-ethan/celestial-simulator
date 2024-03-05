package edu.vanier.eastwest.models;

import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

public class Body extends Sphere {

    private double mass, radius;
    private Point3D position, velocity, acceleration;
    private ImagePattern sprite;

    public Body(double radius, double mass, Point3D position, Color color) {
        super(radius);
        mass = 1000;
        setPosition(position);
        velocity = new Point3D(0, 0 , 0);
        acceleration = new Point3D(0, 0, 0);
        setMaterial(new PhongMaterial(color));
    }

    public void update(double time, Point3D acceleration){

    }

    public void setPosition(Point3D position) {
        setTranslateX(position.getX());
        setTranslateY(position.getY());
        setTranslateZ(position.getZ());
    }
}
