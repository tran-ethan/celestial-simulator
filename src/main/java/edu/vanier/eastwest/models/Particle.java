package edu.vanier.eastwest.models;

import javafx.geometry.Point3D;
import javafx.scene.paint.Color;

public class Particle extends Body{

    private Color color;
    private Point3D position;

    /**
     * Draws the particle.
     */
    public void draw(){

    }

    /**
     * Constructor for a Particle object.
     * @param color
     * @param position
     */
    public Particle(Color color, Point3D position) {
        super(2, 0, position, color);

    }
}
