package edu.vanier.eastwest.models;


import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;

import static edu.vanier.eastwest.util.Utility.*;

public class Quad {

    public double x;
    public double z;
    public double width;
    public boolean leaf; // Whether this node is a leaf / branch node

    public Body body;
    public Quad[] children; // Reference to children
    public Point3D weightedPositions; // Sum of positions of bodies weighted by mass
    public Point3D centerMass; // Center of mass
    public double totalMass;
    public Group entities; // Spawn rectangles

    public Quad(double x, double z, double width) {
        this.x = x;
        this.z = z;
        this.width = width;
        this.leaf = true;

        this.body = null;
        this.children = new Quad[4];
        this.weightedPositions = new Point3D(0, 0, 0); // Sum of mass times position for all bodies
        this.centerMass = null;
        this.totalMass = 0;
    }

    public Quad(double x, double z, double width, Group entities) {
        this.entities = entities;
        this.x = x;
        this.z = z;
        this.width = width;
        this.leaf = true;
        this.body = null;
        this.children = new Quad[4];

        this.weightedPositions = new Point3D(0, 0, 0); // Mass times position for all bodies
        this.centerMass = null; // Center of mass
        this.totalMass = 0;
    }

    public void subDivide() {
        double newWidth = width / 2;
        children[0] = new Quad(x, z, newWidth, entities); // NW
        children[1] = new Quad(x + newWidth, z, newWidth, entities); // NE
        children[2] = new Quad(x, z + newWidth, newWidth, entities); // SW
        children[3] = new Quad(x + newWidth, z + newWidth, newWidth, entities); // SE

        this.leaf = false;

        Rectangle r0 = createSquare(x,0, z, newWidth);
        Rectangle r1 = createSquare(x + newWidth, 0, z, newWidth);
        Rectangle r2 = createSquare(x, 0,  z + newWidth, newWidth);
        Rectangle r3 = createSquare(x + newWidth, 0, z + newWidth, newWidth);
        entities.getChildren().addAll(r0, r1, r2, r3);
    }

    // Returns index of child at location p, ignore bodies not fully fitting inside square
    int which(Point3D p) {
        double half = width / 2;
        if (p.getZ() < z + half) {
            return p.getX() < x + half ? 0 : 1;
        }
        return p.getX() < x + half ? 2 : 3;
    }

    public void insert(Body body) {
        if (this.leaf) {
            if (this.body == null) {
                // Case 1: Node does not contain a body
                this.body = body;
                this.weightedPositions = this.weightedPositions.add(body.getPosition().multiply(body.getMass()));
                this.totalMass += body.getMass();
            } else {
                // Case: Leaf already contains another body
                Body a = this.body;
                Body b = body;

                this.weightedPositions = this.weightedPositions.add(b.getPosition().multiply(b.getMass()));
                this.totalMass += b.getMass();

                Quad cur = this;
                int qA = cur.which(a.getPosition());
                int qB = cur.which(b.getPosition());
                while (qA == qB) {
                    cur.subDivide();
                    cur = cur.children[qA];
                    qA = cur.which(a.getPosition());
                    qB = cur.which(b.getPosition());

                    // Update total center and mass
                    cur.weightedPositions = cur.weightedPositions.add(a.getPosition().multiply(a.getMass()));
                    cur.weightedPositions = cur.weightedPositions.add(b.getPosition().multiply(b.getMass()));
                    cur.totalMass += a.getMass() + b.getMass();
                }

                cur.subDivide();
                cur.children[qA].body = a;
                cur.children[qB].body = b;

                // Update center of mass and total for lowest-level child
                cur.children[qA].weightedPositions = cur.children[qA].weightedPositions.add(a.getPosition().multiply(a.getMass()));
                cur.children[qB].weightedPositions = cur.children[qA].weightedPositions.add(b.getPosition().multiply(b.getMass()));
                cur.children[qA].totalMass += a.getMass();
                cur.children[qB].totalMass += b.getMass();

                this.body = null;
            }
        } else {
            // Case 2: Node is an
            this.weightedPositions = this.weightedPositions.add(body.getPosition().multiply(body.getMass()));
            this.totalMass += body.getMass();
            this.children[this.which(body.getPosition())].insert(body);
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
