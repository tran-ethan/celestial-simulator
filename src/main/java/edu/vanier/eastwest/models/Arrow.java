package edu.vanier.eastwest.models;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;

//Imported Code: https://stackoverflow.com/a/43736085

public class Arrow extends Group {
    int height;
    int radius;
    int rounds = 360;

    Point3D position;

    public Arrow(int r, int h, Point3D p) {
        position = p;
        radius = r;
        height = h/5*2;
        Group cone = new Group();
        PhongMaterial material = new PhongMaterial(Color.BLUE);

        float[] points = new float[rounds * 12];
        float[] textCoords = {
                0.5f, 0,
                0, 1,
                1, 1
        };
        int[] faces = new int[rounds * 12];

        for (int i = 0; i < rounds; i++) {
            int index = i * 12;
            //0
            points[index + 2] = height / 2;
            //1
            points[index + 3] = (float) Math.cos(Math.toRadians(i)) * radius;
            points[index + 4] = (float) Math.sin(Math.toRadians(i)) * radius;
            points[index + 5] = -height / 2;
            //2
            points[index + 6] = (float) Math.cos(Math.toRadians(i + 1)) * radius;
            points[index + 7] = (float) Math.sin(Math.toRadians(i + 1)) * radius;
            points[index + 8] = -height / 2;
            //3
            points[index + 11] = height / 2;
        }

        for (int i = 0; i < rounds; i++) {
            int index = i * 12;
            faces[index] = i * 4;
            faces[index + 1] = 0;
            faces[index + 2] = i * 4 + 1;
            faces[index + 3] = 1;
            faces[index + 4] = i * 4 + 2;
            faces[index + 5] = 2;

            faces[index + 6] = i * 4;
            faces[index + 7] = 0;
            faces[index + 8] = i * 4 + 2;
            faces[index + 9] = 1;
            faces[index + 10] = i * 4 + 3;
            faces[index + 11] = 2;
        }

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(points);
        mesh.getTexCoords().addAll(textCoords);
        mesh.getFaces().addAll(faces);

        Cylinder c1 = new Cylinder(radius, 0.01);
        c1.setMaterial(material);

        Cylinder c2 = new Cylinder (radius/2, height /2*3);
        c2.setTranslateY(height /4*3);
        c2.setMaterial(material);


        MeshView meshView = new MeshView();
        meshView.setMesh(mesh);
        meshView.setMaterial(material);
        //meshView.setDrawMode(DrawMode.LINE);
        meshView.setTranslateZ(height/2);
        cone.getChildren().addAll(meshView);
        Rotate r1 = new Rotate(90, Rotate.X_AXIS);
        cone.getTransforms().add(r1);
        getChildren().addAll(cone, c1, c2);


    }

    public void setPosition(Point3D position) {
        setTranslateX(position.getX());
        setTranslateY(position.getY());
        setTranslateZ(position.getZ());
    }

    public Point3D getPosition() {
        return position;
    }
}
