package edu.vanier.eastwest.models;

import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;

//TODO
public class Particle extends Body{

    private Color color;
    @Getter @Setter
    private Point3D position;

    /**
     * TODO Draws the particle.
     */
    public void draw(){

    }

    /**
     * Constructor for a Particle object.
     * @param color
     * @param position
     */
    public Particle(Color color, Point3D position) {
        super("", 2, 0, position, color);
    }
}
