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
    @Setter @Getter
    private Point3D velocity;
    @Getter @Setter
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

        if(texture != null){
            System.out.println("exist");
            PhongMaterial material = new PhongMaterial();
            material.setDiffuseMap(texture);
            setMaterial(material);
        }
        if(texture == null){
            System.out.println("no exist");
            setColor(color);
        }
        System.out.println(1);
        //else if(texture == null){
           // setColor(color);
      //  }
    }

    /**
     * Updates the velocity of the Body object instance.
     * @param time The time that passes
     * @param acceleration The acceleration of the body
     */
    public void update(double time, Point3D acceleration){
        // Update position
        setPosition(getPosition().add(velocity.multiply(time)));

        // Update velocity
        velocity = velocity.add(acceleration.multiply(time));
    }

    /**
     * Updates the position of the Body object instance.
     * @param position The current x, y, z position of the body.
     */
    public void setPosition(Point3D position) {
        setTranslateX(position.getX());
        setTranslateY(position.getY());
        setTranslateZ(position.getZ());
    }

    public Point3D getPosition() {
        return new Point3D(getTranslateX(), getTranslateY(), getTranslateZ());
    }

    public void setColor(Color color) {
        this.color = color;
        setMaterial(new PhongMaterial(color));
    }

    public Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        return String.format("Name: %s\nMass: %.2f\nPosition: [%.0f, %.0f, %.0f]\nVelocity: [%.0f, %.0f, %.0f]",
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
