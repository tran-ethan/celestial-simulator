package edu.vanier.eastwest.models;


import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import lombok.Getter;

import static edu.vanier.eastwest.util.Utility.*;

/**
 * A Quad is a node in the Quadtree structure used in the Barnes-Hut algorithm.
 * The Barnes-Hut algorithm works by approximating the forces of bodies that are sufficiently far away
 * as a single body by using its center of mass.
 * <p>
 * It represents a square region in space in the two-dimensional XZ plane. The root node represents the whole space,
 * and each of its four children represent a quadrant within that space.
 * <p>
 * Modeled after <a href="https://www.cs.princeton.edu/courses/archive/fall03/cs126/assignments/barnes-hut.html">this document</a>.
 */
public class Quad {

    @Getter
    private final double x;
    @Getter
    private final double z;
    @Getter
    private final double length;
    @Getter
    private boolean external; // Whether this node is an external or internal node
    private boolean spawn; // Whether to spawn rectangles to visualize barnes hut

    public Body body;
    public Quad[] children; // Reference to children
    public double totalMass;
    public Point3D weightedPositions; // Sum of positions of bodies times mass
    public Group entities; // Spawn rectangles

    public Quad(double x, double z, double length, Group entities, boolean spawn) {
        this.entities = entities;
        this.x = x;
        this.z = z;
        this.length = length;
        this.external = true;

        this.body = null;
        this.children = new Quad[4];
        this.totalMass = 0;
        this.weightedPositions = new Point3D(0, 0, 0); // Mass times position for all bodies
        this.spawn = spawn;
    }

    /**
     * Subdivides the current quadrant into four smaller quadrants.
     * Since quadrant now has children, it is no longer an external node.
     * Visualizes the Barnes-Hut algorithm by creating rectangles for each node.
     */
    public void subdivide() {
        double half = length / 2;
        // North West
        children[0] = new Quad(x, z, half, entities, spawn);
        // North East
        children[1] = new Quad(x + half, z, half, entities, spawn);
        // South West
        children[2] = new Quad(x, z + half, half, entities, spawn);
        // South East
        children[3] = new Quad(x + half, z + half, half, entities, spawn);

        this.external = false;

        // Visualize rectangles (but slows down the simulation significantly)
        if (spawn) {
            Rectangle quad0 = createSquare(x, z, half);
            Rectangle quad1 = createSquare(x + half, z, half);
            Rectangle quad2 = createSquare(x, z + half, half);
            Rectangle quad3 = createSquare(x + half, z + half, half);
            entities.getChildren().addAll(quad0, quad1, quad2, quad3);
        }
    }

    /**
     * Determines the quadrant of the quadrant that contains the given position.
     *
     * @param pos The position to determine the quadrant for
     * @return An integer representing the quadrant index:
     *         0 for North West, 1 for North East, 2 for South West, 3 for South East
     */
    public int getQuadrant(Point3D pos) {
        if(pos == null){
            return -1;
        }
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

    /**
     * Inserts a body into the Quadtree structure.
     * <p>
     * If the quadrant has no bodies and is external, the body is added directly.
     * If the quadrant is internal, recursively insert the into the right quadrant.
     * If the quadrant is external and already contains a body, the quadrant is subdivided
     * until each body occupies a different quadrant.
     *
     * @param body The body to be inserted into the Quadtree
     */
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
                quad1 = pointer.getQuadrant(body.getPosition());
                quad2 = pointer.getQuadrant(body2.getPosition());

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

            // After being subdivided the current quadrant is an internal node (no bodies)
            this.body = null;
        }
    }
}
