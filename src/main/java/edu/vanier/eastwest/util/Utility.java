package edu.vanier.eastwest.util;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import org.fxyz3d.shapes.polygon.PolygonMesh;
import org.fxyz3d.shapes.polygon.PolygonMeshView;

/**
 * This class contains utility functions that will be used in the simulator.
 * The code for creating the grids and axes can be found
 * <a href="https://stackoverflow.com/questions/51895469/what-is-the-most-practical-way-to-create-coordinate-grid-with-javafx-3d">here</a>
 */
public class Utility {

    /**
     * Generates a group containing three cylinders representing X, Y, and Z axes.
     * Each axis is oriented along a different axis direction (X, Y, or Z) and colored accordingly.
     * The intersection of the 3 axes represent the coordinate (0, 0, 0).
     *
     * @param scale The scaling factor applied to the axes
     * @return a Group object containing the X, Y, and Z axes with specified scaling
     */
    public static Group getAxes(double scale) {
        Cylinder axisX = new Cylinder(1, 9999);
        axisX.getTransforms().addAll(new Rotate(90, Rotate.Z_AXIS));
        PhongMaterial redMat = new PhongMaterial();
        redMat.setDiffuseColor(Color.RED);
        axisX.setMaterial(redMat);

        Cylinder axisY = new Cylinder(1, 9999);
        axisY.setMaterial(new PhongMaterial(Color.GREEN));

        Cylinder axisZ = new Cylinder(1, 9999);
        axisZ.setMaterial(new PhongMaterial(Color.BLUE));
        axisZ.getTransforms().addAll(new Rotate(90, Rotate.X_AXIS));

        Group group = new Group(axisX, axisY, axisZ);
        group.getTransforms().add(new Scale(scale, scale, scale));
        return group;
    }


    /**
     * Creates a polygon mesh based on specified width, height, and subdivision parameters.
     *
     * @param width   The width of the mesh
     * @param height  The height of the mesh
     * @param subDivX The number of subdivisions along the X-axis
     * @param subDivY The number of subdivisions along the Y-axis
     * @return a PolygonMesh object representing the created mesh
     */
    public static PolygonMesh createMesh(float width, float height, int subDivX, int subDivY) {
        final float minX = -width / 2f;
        final float minY = -height / 2f;
        final float maxX = width / 2f;
        final float maxY = height / 2f;

        final int pointSize = 3;
        final int texCoordSize = 2;
        // 4 point indices and 4 texCoord indices per face
        final int faceSize = 8;
        int numDivX = subDivX + 1;
        int numVerts = (subDivY + 1) * numDivX;
        float points[] = new float[numVerts * pointSize];
        float texCoords[] = new float[numVerts * texCoordSize];
        int faceCount = subDivX * subDivY;
        int faces[][] = new int[faceCount][faceSize];

        // Create points and texCoords
        for (int y = 0; y <= subDivY; y++) {
            float dy = (float) y / subDivY;
            double fy = (1 - dy) * minY + dy * maxY;

            for (int x = 0; x <= subDivX; x++) {
                float dx = (float) x / subDivX;
                double fx = (1 - dx) * minX + dx * maxX;

                int index = y * numDivX * pointSize + (x * pointSize);
                points[index] = (float) fx;
                points[index + 1] = (float) fy;
                points[index + 2] = 0.0f;

                index = y * numDivX * texCoordSize + (x * texCoordSize);
                texCoords[index] = dx;
                texCoords[index + 1] = dy;
            }
        }

        // Create faces
        int index = 0;
        for (int y = 0; y < subDivY; y++) {
            for (int x = 0; x < subDivX; x++) {
                int p00 = y * numDivX + x;
                int p01 = p00 + 1;
                int p10 = p00 + numDivX;
                int p11 = p10 + 1;
                int tc00 = y * numDivX + x;
                int tc01 = tc00 + 1;
                int tc10 = tc00 + numDivX;
                int tc11 = tc10 + 1;

                faces[index][0] = p00;
                faces[index][1] = tc00;
                faces[index][2] = p10;
                faces[index][3] = tc10;
                faces[index][4] = p11;
                faces[index][5] = tc11;
                faces[index][6] = p01;
                faces[index++][7] = tc01;
            }
        }

        int[] smooth = new int[faceCount];

        PolygonMesh mesh = new PolygonMesh(points, texCoords, faces);
        mesh.getFaceSmoothingGroups().addAll(smooth);
        return mesh;
    }

    /**
     * Generates a group containing grid lines in the XY, XZ, and YZ planes, with specified size and spacing.
     *
     * @param size  The size of the grid in the X and Y directions
     * @param delta The spacing between grid lines
     * @return a Group object containing grid lines in the XZ plane and its subgrid
     */
    public static PolygonMeshView getGrid(float size, float delta) {
        if (delta < 1) {
            delta = 1;
        }
        final PolygonMesh plane = createMesh(size, size, (int) (size / delta), (int) (size / delta));

        PolygonMeshView meshViewXZ = new PolygonMeshView(plane);
        meshViewXZ.setDrawMode(DrawMode.LINE);
        meshViewXZ.setCullFace(CullFace.NONE);
        meshViewXZ.getTransforms().add(new Rotate(90, Rotate.X_AXIS));

        return meshViewXZ;
    }


    public static Rectangle createSquare(double x, double y, double z, double w) {
        // Remove all previous
//        entities.getChildren().removeIf(n -> n instanceof Cylinder);
//
//        Cylinder x1 = new Cylinder(1, w);
//        Cylinder x2 = new Cylinder(1, w);
//
//        x1.getTransforms().addAll(new Rotate(90, Rotate.Z_AXIS), new Translate(0, -w / 2, 0));
//        x2.getTransforms().addAll(new Rotate(90, Rotate.Z_AXIS), new Translate(0, -w / 2, w));
//
//        x1.setTranslateX(x);
//        x1.setTranslateY(y);
//        x1.setTranslateZ(z);
//
//        x2.setTranslateX(x);
//        x2.setTranslateY(y);
//        x2.setTranslateZ(z);
//
//        Cylinder z1 = new Cylinder(1, w);
//        Cylinder z2 = new Cylinder(1, w);
//
//        z1.getTransforms().addAll(new Rotate(90, Rotate.X_AXIS), new Translate(0, w / 2, 0));
//        z2.getTransforms().addAll(new Rotate(90, Rotate.X_AXIS), new Translate(w, w / 2, 0));
//
//        z1.setTranslateX(x);
//        z1.setTranslateY(y);
//        z1.setTranslateZ(z);
//
//        z2.setTranslateX(x);
//        z2.setTranslateY(y);
//        z2.setTranslateZ(z);
//
//        entities.getChildren().addAll(x1, x2, z1, z2);

        Rectangle p = new Rectangle(w, w, Color.TRANSPARENT);
        p.setStroke(Color.GREEN);
        p.setStrokeWidth(1);
        p.getTransforms().addAll(
                new Rotate(90, Rotate.X_AXIS)
        );
        p.setTranslateX(x);
        p.setTranslateZ(z);
        return p;
    }
}
