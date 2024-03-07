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

    /**
     * Constructor for a Body object using 4 parameters.
     * @param radius The radius of the Sphere
     * @param mass The mass of the body
     * @param position The position of the body in the space
     * @param color The color of the object
     */
    public Body(double radius, double mass, Point3D position, Color color) {
        super(radius);
        this.mass = mass;
        setPosition(position);
        velocity = new Point3D(0, 0 , 0);
        acceleration = new Point3D(0, 0, 0);
        setMaterial(new PhongMaterial(color));
    }

    /**
     * Updates the velocity of the Body object instance.
     * @param time
     * @param acceleration
     */
    public void update(double time, Point3D acceleration){

    }

    /**
     * Updates the position of the Body object instance.
     * @param position
     */
    public void setPosition(Point3D position) {
        setTranslateX(position.getX());
        setTranslateY(position.getY());
        setTranslateZ(position.getZ());
    }
}
