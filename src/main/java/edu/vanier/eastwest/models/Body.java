package edu.vanier.eastwest.models;

import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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
    @Getter
    private Image texture;
    @Getter
    private List<Vector3D> vectors = new ArrayList<>();;

    private PhongMaterial phongMaterial;

    /**
     * Constructor for a Body object using 4 parameters.
     * @param radius The radius of the Sphere
     * @param mass The mass of the body
     * @param position The position of the body in the space
     */
    public Body(String name, double radius, double mass, Point3D position, Point3D velocity) {
        super(radius);
        this.name = name;
        this.mass = mass;
        setPosition(position);
        this.velocity = velocity;
        this.acceleration = new Point3D(0, 0, 0);
    }

    /**
     * Constructor for a Body object using 4 parameters.
     * @param radius The radius of the Sphere
     * @param mass The mass of the body
     * @param position The position of the body in the space
     * @param color The color of the object
     * @param texture The image on the body
     */
    public Body(String name, double radius, double mass, Point3D position, Point3D velocity, Color color, Image texture) {
        this(name, radius, mass, position, velocity);

        if (texture == null) {
            setColor(color);
        } else {
            setTexture(texture);
        }
    }

    /**
     * Updates the velocity of the Body object instance.
     * @param time The time that passes
     */
    public void update(double time) {
        // Update position
        setPosition(getPosition().add(velocity.multiply(time)));

        // Moving the vectors
        for (Vector3D vector : this.vectors){
            vector.setTruePosition(new Point3D(vector.getTruePosition().getX() + this.getVelocity().multiply(time).getX(), 0, vector.getTruePosition().getZ() + this.getVelocity().multiply(time).getZ()));
            double vectorX = Math.round(vector.getTruePosition().getX() / 100) * 100;
            double vectorZ = Math.round(vector.getTruePosition().getZ() / 100) * 100;
            vector.setPosition(new Point3D(vectorX, 0, vectorZ));
        }

        // Update velocity
        setVelocity(velocity.add(acceleration.multiply(time)));

        // Reset acceleration
        acceleration = new Point3D(0, 0, 0);
    }

    /***
     * Returns the position of the Body object within the simulation.
     * @return Point3D position
     */
    public Point3D getPosition() {
        return new Point3D(getTranslateX(), 0, getTranslateZ());
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

    public void setVelocity(Point3D velocity) {
        this.velocity = new Point3D(velocity.getX(), 0, velocity.getZ());
    }

    /***
     * Sets the color of the Body object.
     * @param color The color of the body
     */
    public void setColor(Color color) {
        this.color = color;
        phongMaterial = new PhongMaterial(color);
        setMaterial(phongMaterial);
    }

    public void setTexture(Image texture) {
        this.texture = texture;
        phongMaterial = new PhongMaterial();
        phongMaterial.setDiffuseMap(texture);
        setMaterial(phongMaterial);
    }

    public void setTransparency(double opacity) {
        if (color == null) {
            phongMaterial.setDiffuseColor(Color.rgb(255, 255, 255, opacity));
        } else {
            setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity));
        }
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

    public Body clonePreview() {
        return new Body(this.name, this.getRadius(), this.mass, new Point3D(0, 0, 0), new Point3D(0, 0, 0), this.color, this.texture);
    }
}
