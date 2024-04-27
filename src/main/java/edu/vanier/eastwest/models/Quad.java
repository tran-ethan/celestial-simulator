package edu.vanier.eastwest.models;


import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import lombok.Getter;

import static edu.vanier.eastwest.util.Utility.*;

public class Quad {

    private final double x;
    private final double z;
    @Getter
    private final double length;
    @Getter
    private boolean external; // Whether this node is an external or internal node

    public Body body;
    public Quad[] children; // Reference to children
    public double totalMass;
    public Point3D weightedPositions; // Sum of positions of bodies times mass
    public Group entities; // Spawn rectangles

    public Quad(double x, double z, double length, Group entities) {
        this.entities = entities;
        this.x = x;
        this.z = z;
        this.length = length;
        this.external = true;

        this.body = null;
        this.children = new Quad[4];
        this.totalMass = 0;
        this.weightedPositions = new Point3D(0, 0, 0); // Mass times position for all bodies
    }

    public void subdivide() {
        double half = length / 2;
        // North West
        children[0] = new Quad(x, z, half, entities);
        // North East
        children[1] = new Quad(x + half, z, half, entities);
        // South West
        children[2] = new Quad(x, z + half, half, entities);
        // South East
        children[3] = new Quad(x + half, z + half, half, entities);

        this.external = false;

        Rectangle quad0 = createSquare(x,0, z, half);
        Rectangle quad1 = createSquare(x + half, 0, z, half);
        Rectangle quad2 = createSquare(x, 0,  z + half, half);
        Rectangle quad3 = createSquare(x + half, 0, z + half, half);
        entities.getChildren().addAll(quad0, quad1, quad2, quad3);
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
            // Update weighted positions and total mass for current quadrant
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

            // Body of the current quadrant conflicts with new added body
            Body body2 = this.body;

            Point3D posMass = body.getPosition().multiply(body.getMass());
            weightedPositions = weightedPositions.add(posMass);
            totalMass += body.getMass();

            // Reference to quad that will be subdivided
            Quad pointer = this;

            // Quadrants for body 2 (body already in this quadrant) and body 1 (body that will be added)
            int quad1 = pointer.getQuadrant(body.getPosition());
            int quad2 = pointer.getQuadrant(body2.getPosition());

            // Continue subdividing until bodies are no longer in the same quadrant
            while (quad2 == quad1) {
                pointer.subdivide();
                pointer = pointer.children[quad2];
                quad2 = pointer.getQuadrant(body2.getPosition());
                quad1 = pointer.getQuadrant(body.getPosition());

                // Update weighted positions for current node
                Point3D posMass1 = body.getPosition().multiply(body.getMass());
                Point3D posMass2 = body2.getPosition().multiply(body2.getMass());
                pointer.weightedPositions = pointer.weightedPositions.add(posMass1);
                pointer.weightedPositions = pointer.weightedPositions.add(posMass2);
                pointer.totalMass += body.getMass() + body2.getMass();
            }

            // Add bodies to inner nodes
            pointer.subdivide();
            pointer.children[quad1].body = body;
            pointer.children[quad2].body = body2;

            // Update positions and weights for bottom children
            Point3D posMass1 = body.getPosition().multiply(body.getMass());
            Point3D posMass2 = body2.getPosition().multiply(body2.getMass());
            pointer.children[quad1].weightedPositions = pointer.children[quad1].weightedPositions.add(posMass1);
            pointer.children[quad2].weightedPositions = pointer.children[quad2].weightedPositions.add(posMass2);
            pointer.children[quad1].totalMass += body.getMass();
            pointer.children[quad2].totalMass += body2.getMass();

            // After being subdivided the current quadrant is an internal node
            this.body = null;
        }
    }

    @Override
    public String toString() {
        return "Quad{" +
                "centerMass=" + weightedPositions +
                "\n, totalMass=" + totalMass +
                '}';
    }
}
