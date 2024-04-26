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

    public void subDivide() {
        double newWidth = length / 2;
        children[0] = new Quad(x, z, newWidth, entities); // NW
        children[1] = new Quad(x + newWidth, z, newWidth, entities); // NE
        children[2] = new Quad(x, z + newWidth, newWidth, entities); // SW
        children[3] = new Quad(x + newWidth, z + newWidth, newWidth, entities); // SE

        this.external = false;

        Rectangle r0 = createSquare(x,0, z, newWidth);
        Rectangle r1 = createSquare(x + newWidth, 0, z, newWidth);
        Rectangle r2 = createSquare(x, 0,  z + newWidth, newWidth);
        Rectangle r3 = createSquare(x + newWidth, 0, z + newWidth, newWidth);
        entities.getChildren().addAll(r0, r1, r2, r3);
    }

    // Returns index of child at location p, ignore bodies not fully fitting inside square
    int which(Point3D p) {
        double half = length / 2;
        if (p.getZ() < z + half) {
            return p.getX() < x + half ? 0 : 1;
        }
        return p.getX() < x + half ? 2 : 3;
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
                // Case 2 - Internal node is an internal node
                this.children[this.which(body.getPosition())].insert(body);
            }
        } else {
            // Case 3 - External node already contains another body
            Body a = this.body;

            weightedPositions = weightedPositions.add(body.getPosition().multiply(body.getMass()));
            totalMass += body.getMass();

            Quad current = this;
            int qA = current.which(a.getPosition());
            int qB = current.which(body.getPosition());
            while (qA == qB) {
                current.subDivide();
                current = current.children[qA];
                qA = current.which(a.getPosition());
                qB = current.which(body.getPosition());

                // Update total center and mass
                Point3D posMassA = a.getPosition().multiply(a.getMass());
                Point3D posMassB = body.getPosition().multiply(body.getMass());
                current.weightedPositions = current.weightedPositions.add(posMassA);
                current.weightedPositions = current.weightedPositions.add(posMassB);
                current.totalMass += a.getMass() + body.getMass();
            }

            current.subDivide();
            current.children[qA].body = a;
            current.children[qB].body = body;

            // Update center of mass and total for lowest-level child
            Point3D posMassA = a.getPosition().multiply(a.getMass());
            Point3D posMassB = body.getPosition().multiply(body.getMass());
            current.children[qA].weightedPositions = current.children[qA].weightedPositions.add(posMassA);
            current.children[qB].weightedPositions = current.children[qB].weightedPositions.add(posMassB);
            current.children[qA].totalMass += a.getMass();
            current.children[qB].totalMass += body.getMass();

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
