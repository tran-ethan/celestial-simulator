package edu.vanier.eastwest.models;


import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;

import static edu.vanier.eastwest.util.Utility.*;

public class Quad {

    public double x;
    public double z;
    public double length;
    public boolean external; // Whether this node is a leaf / branch node

    public Body body;
    public Quad[] children; // Reference to children
    public Point3D weightedPositions; // Sum of positions of bodies weighted by mass
    public Point3D centerMass; // Center of mass
    public double totalMass;
    public Group entities; // Spawn rectangles

    public Quad(double x, double z, double length) {
        this.x = x;
        this.z = z;
        this.length = length;
        this.external = true;

        this.body = null;
        this.children = new Quad[4];
        this.weightedPositions = new Point3D(0, 0, 0); // Sum of mass times position for all bodies
        this.centerMass = null;
        this.totalMass = 0;
    }

    public Quad(double x, double z, double length, Group entities) {
        this.entities = entities;
        this.x = x;
        this.z = z;
        this.length = length;
        this.external = true;
        this.body = null;
        this.children = new Quad[4];

        this.weightedPositions = new Point3D(0, 0, 0); // Mass times position for all bodies
        this.centerMass = null; // Center of mass
        this.totalMass = 0;
    }

    public void subdivide() {
        double half = length / 2;
        children[0] = new Quad(x, z, half, entities); // North West
        children[1] = new Quad(x + half, z, half, entities); // North East
        children[2] = new Quad(x, z + half, half, entities); // South West
        children[3] = new Quad(x + half, z + half, half, entities); // South East

        this.external = false;

        Rectangle r0 = createSquare(x,0, z, half);
        Rectangle r1 = createSquare(x + half, 0, z, half);
        Rectangle r2 = createSquare(x, 0,  z + half, half);
        Rectangle r3 = createSquare(x + half, 0, z + half, half);
        entities.getChildren().addAll(r0, r1, r2, r3);
    }

    // Returns index of child at location p, ignore bodies not fully fitting inside square
    int getQuadrant(Point3D pos) {
        double half = length / 2;
        boolean isTop = pos.getZ() < z + half;
        boolean isLeft = pos.getX() < x + half;

        int index;

        if (isTop) {
            index =  isLeft ? 0 : 1;
        } else {
            index = isLeft ? 2 : 3;
        }
        return index;
    }

    public void insert(Body body) {
        if (this.body == null) {
            // Update center of mass and total mass
            Point3D posMass = body.getPosition().multiply(body.getMass());
            weightedPositions = weightedPositions.add(posMass);
            totalMass += body.getMass();

            if (this.external) {
                // Case 1 - External node does not contain a body
                this.body = body;
            } else {
                // Case 2 - Internal node, insert in appropriate quadrant
                children[getQuadrant(body.getPosition())].insert(body);
            }
        } else {
            // Case 3 - External node already contains another body
            Body body2 = this.body;

            weightedPositions = weightedPositions.add(body.getPosition().multiply(body.getMass()));
            totalMass += body.getMass();

            // Reference to quad that will be subdivided
            Quad pointer = this;

            // Quadrants for body A and body B
            int quad1 = pointer.getQuadrant(body2.getPosition());
            int quad2 = pointer.getQuadrant(body.getPosition());

            // Continue subdividing until bodies are no longer in the same quadrant
            while (quad1 == quad2) {
                pointer.subdivide();
                pointer = pointer.children[quad1];
                quad1 = pointer.getQuadrant(body2.getPosition());
                quad2 = pointer.getQuadrant(body.getPosition());

                // Update weighted positions for current node
                Point3D posMassA = body2.getPosition().multiply(body2.getMass());
                Point3D posMassB = body.getPosition().multiply(body.getMass());
                pointer.weightedPositions = pointer.weightedPositions.add(posMassA);
                pointer.weightedPositions = pointer.weightedPositions.add(posMassB);
                pointer.totalMass += body2.getMass() + body.getMass();
            }

            // Add bodies to inner nodes
            pointer.subdivide();
            pointer.children[quad1].body = body2;
            pointer.children[quad2].body = body;

            // Update positions and weights for bottom children
            Point3D posMassA = body2.getPosition().multiply(body2.getMass());
            Point3D posMassB = body.getPosition().multiply(body.getMass());
            pointer.children[quad1].weightedPositions = pointer.children[quad1].weightedPositions.add(posMassA);
            pointer.children[quad2].weightedPositions = pointer.children[quad2].weightedPositions.add(posMassB);
            pointer.children[quad1].totalMass += body2.getMass();
            pointer.children[quad2].totalMass += body.getMass();

            // Internal node do not have bodies
            this.body = null;
        }
    }

    @Override
    public String toString() {
        return "Quad{" +
                "centerMass=" + weightedPositions +
                "\n, center=" + centerMass +
                "\n, totalMass=" + totalMass +
                '}';
    }
}
