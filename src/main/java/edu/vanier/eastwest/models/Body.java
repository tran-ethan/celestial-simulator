package edu.vanier.eastwest.models;

import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import lombok.Getter;
import lombok.Setter;

public class Body extends Sphere {
    @Getter
    private String name;
    @Getter
    private double mass;
    @Getter
    private Point3D velocity;
    @Setter @ Getter
    private Point3D acceleration;
    @Getter
    private Color color;
    @Getter @Setter
    private Image texture;

    /**
     * Constructor for a Body object using 4 parameters.
     * @param radius The radius of the Sphere
     * @param mass The mass of the body
     * @param position The position of the body in the space
     * @param color The color of the object
     * @param texture The image on the body
     */
    public Body(String name, double radius, double mass, Point3D position, Point3D velocity, Color color, Image texture) {
        super(radius);
        this.name = name;
        this.mass = mass;
        setPosition(position);
        this.velocity = velocity;
        this.acceleration = new Point3D(0, 0, 0);

        if (texture != null) {
            PhongMaterial material = new PhongMaterial();
            material.setDiffuseMap(texture);
            setMaterial(material);
        }
        if (texture == null) {
            setColor(color);
        }
    }

    /**
     * Updates the velocity of the Body object instance.
     * @param time The time that passes
     */
    public void update(double time) {
        // Update position
        setPosition(getPosition().add(velocity.multiply(time)));

        // Update velocity
        setVelocity(velocity.add(acceleration.multiply(time)));

        // Reset acceleration
        acceleration = new Point3D(0, 0, 0);
    }

    /**
     * Updates the position of the Body object instance.
     * @param position The current x, y, z position of the body.
     */
    public void setPosition(Point3D position) {
        setTranslateX(position.getX());
        setTranslateY(0);
        setTranslateZ(position.getZ());
    }

    public Point3D getPosition() {
        return new Point3D(getTranslateX(), 0, getTranslateZ());
    }

    public void setVelocity(Point3D velocity) {
        this.velocity = new Point3D(velocity.getX(), 0, velocity.getZ());
    }

    public void setColor(Color color) {
        this.color = color;
        setMaterial(new PhongMaterial(color));
    }

    @Override
    public String toString() {
        return String.format("Name: %s\nMass (kg): %.2f\nPosition (m): [%.0f, %.0f, %.0f]\nVelocity (m/s): [%.0f, %.0f, %.0f]",
                getName(),
                getMass(),
                getTranslateX(),
                getTranslateY(),
                getTranslateZ(),
                getVelocity().getX(),
                getVelocity().getY(),
                getVelocity().getZ()
        );
    }
}
