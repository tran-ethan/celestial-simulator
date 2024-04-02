package edu.vanier.eastwest.models;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import lombok.*;

import java.util.List;

public class Vector3D extends Group {
    //Imported Code: https://stackoverflow.com/a/43736085
    //2D Array of the turbo colormap in rgb taken from https://gist.github.com/mikhailov-work/6a308c20e494d9e0ccc29036b28faa7a
    static int[][] turbo_srgb_bytes = {
            {48, 18, 59}, {50, 21, 67}, {51, 24, 74}, {52, 27, 81}, {53, 30, 88}, {54, 33, 95}, {55, 36, 102}, {56, 39, 109},
            {57, 42, 115}, {58, 45, 121}, {59, 47, 128}, {60, 50, 134}, {61, 53, 139}, {62, 56, 145}, {63, 59, 151}, {63, 62, 156},
            {64, 64, 162}, {65, 67, 167}, {65, 70, 172}, {66, 73, 177}, {66, 75, 181}, {67, 78, 186}, {68, 81, 191}, {68, 84, 195},
            {68, 86, 199}, {69, 89, 203}, {69, 92, 207}, {69, 94, 211}, {70, 97, 214}, {70, 100, 218}, {70, 102, 221}, {70, 105, 224},
            {70, 107, 227}, {71, 110, 230}, {71, 113, 233}, {71, 115, 235}, {71, 118, 238}, {71, 120, 240}, {71, 123, 242}, {70, 125, 244},
            {70, 128, 246}, {70, 130, 248}, {70, 133, 250}, {70, 135, 251}, {69, 138, 252}, {69, 140, 253}, {68, 143, 254}, {67, 145, 254},
            {66, 148, 255}, {65, 150, 255}, {64, 153, 255}, {62, 155, 254}, {61, 158, 254}, {59, 160, 253}, {58, 163, 252}, {56, 165, 251},
            {55, 168, 250}, {53, 171, 248}, {51, 173, 247}, {49, 175, 245}, {47, 178, 244}, {46, 180, 242}, {44, 183, 240}, {42, 185, 238},
            {40, 188, 235}, {39, 190, 233}, {37, 192, 231}, {35, 195, 228}, {34, 197, 226}, {32, 199, 223}, {31, 201, 221}, {30, 203, 218},
            {28, 205, 216}, {27, 208, 213}, {26, 210, 210}, {26, 212, 208}, {25, 213, 205}, {24, 215, 202}, {24, 217, 200}, {24, 219, 197},
            {24, 221, 194}, {24, 222, 192}, {24, 224, 189}, {25, 226, 187}, {25, 227, 185}, {26, 228, 182}, {28, 230, 180}, {29, 231, 178},
            {31, 233, 175}, {32, 234, 172}, {34, 235, 170}, {37, 236, 167}, {39, 238, 164}, {42, 239, 161}, {44, 240, 158}, {47, 241, 155},
            {50, 242, 152}, {53, 243, 148}, {56, 244, 145}, {60, 245, 142}, {63, 246, 138}, {67, 247, 135}, {70, 248, 132}, {74, 248, 128},
            {78, 249, 125}, {82, 250, 122}, {85, 250, 118}, {89, 251, 115}, {93, 252, 111}, {97, 252, 108}, {101, 253, 105}, {105, 253, 102},
            {109, 254, 98}, {113, 254, 95}, {117, 254, 92}, {121, 254, 89}, {125, 255, 86}, {128, 255, 83}, {132, 255, 81}, {136, 255, 78},
            {139, 255, 75}, {143, 255, 73}, {146, 255, 71}, {150, 254, 68}, {153, 254, 66}, {156, 254, 64}, {159, 253, 63}, {161, 253, 61},
            {164, 252, 60}, {167, 252, 58}, {169, 251, 57}, {172, 251, 56}, {175, 250, 55}, {177, 249, 54}, {180, 248, 54}, {183, 247, 53},
            {185, 246, 53}, {188, 245, 52}, {190, 244, 52}, {193, 243, 52}, {195, 241, 52}, {198, 240, 52}, {200, 239, 52}, {203, 237, 52},
            {205, 236, 52}, {208, 234, 52}, {210, 233, 53}, {212, 231, 53}, {215, 229, 53}, {217, 228, 54}, {219, 226, 54}, {221, 224, 55},
            {223, 223, 55}, {225, 221, 55}, {227, 219, 56}, {229, 217, 56}, {231, 215, 57}, {233, 213, 57}, {235, 211, 57}, {236, 209, 58},
            {238, 207, 58}, {239, 205, 58}, {241, 203, 58}, {242, 201, 58}, {244, 199, 58}, {245, 197, 58}, {246, 195, 58}, {247, 193, 58},
            {248, 190, 57}, {249, 188, 57}, {250, 186, 57}, {251, 184, 56}, {251, 182, 55}, {252, 179, 54}, {252, 177, 54}, {253, 174, 53},
            {253, 172, 52}, {254, 169, 51}, {254, 167, 50}, {254, 164, 49}, {254, 161, 48}, {254, 158, 47}, {254, 155, 45}, {254, 153, 44},
            {254, 150, 43}, {254, 147, 42}, {254, 144, 41}, {253, 141, 39}, {253, 138, 38}, {252, 135, 37}, {252, 132, 35}, {251, 129, 34},
            {251, 126, 33}, {250, 123, 31}, {249, 120, 30}, {249, 117, 29}, {248, 114, 28}, {247, 111, 26}, {246, 108, 25}, {245, 105, 24},
            {244, 102, 23}, {243, 99, 21}, {242, 96, 20}, {241, 93, 19}, {240, 91, 18}, {239, 88, 17}, {237, 85, 16}, {236, 83, 15},
            {235, 80, 14}, {234, 78, 13}, {232, 75, 12}, {231, 73, 12}, {229, 71, 11}, {228, 69, 10}, {226, 67, 10}, {225, 65, 9},
            {223, 63, 8}, {221, 61, 8}, {220, 59, 7}, {218, 57, 7}, {216, 55, 6}, {214, 53, 6}, {212, 51, 5}, {210, 49, 5},
            {208, 47, 5}, {206, 45, 4}, {204, 43, 4}, {202, 42, 4}, {200, 40, 3}, {197, 38, 3}, {195, 37, 3}, {193, 35, 2},
            {190, 33, 2}, {188, 32, 2}, {185, 30, 2}, {183, 29, 2}, {180, 27, 1}, {178, 26, 1}, {175, 24, 1}, {172, 23, 1},
            {169, 22, 1}, {167, 20, 1}, {164, 19, 1}, {161, 18, 1}, {158, 16, 1}, {155, 15, 1}, {152, 14, 1}, {149, 13, 1},
            {146, 11, 1}, {142, 10, 1}, {139, 9, 2}, {136, 8, 2}, {133, 7, 2}, {129, 6, 2}, {126, 5, 2}, {122, 4, 3}
    };
    @Getter @Setter
    private double angle;
    @Getter @Setter
    private double magnitude;
    int height;
    int radius;
    int rounds = 360;
    Group arrow;

    @Getter @Setter
    Rotate xRotate = new Rotate(0, Rotate.X_AXIS);

    public Vector3D(int r, int h, Point3D p) {
        setPosition(p);
        radius = r;
        magnitude = 0;
        height = h / 5 * 2;

        arrow = creatingArrow();
    }
    public Point3D getPosition() {
        return new Point3D(getTranslateX(), getTranslateY(), getTranslateZ());
    }

    public void setPosition(Point3D position) {
        setTranslateX(position.getX());
        setTranslateY(position.getY());
        setTranslateZ(position.getZ());
    }

    /**
     * TODO @Yihweh
     * Update the magnitude and direction of vector
     */
    public void update() {

    }

    /**
     * TODO @Yihweh Draw the updated version of the vector with it's new direction and color.
     */
    public void draw() {

    }

    /**
     * TODO @Yihweh TestCase
     * Compare the magnitude of the vector to the highest and lowest found in
     * the list of vectors and assign him a hexadecimal color code based on that
     *
     * @param maxMagnitude The highest magnitude found in the list of vectors
     * @param minMagnitude The lowest magnitude found in the list of vectors
     * @return The hexadecimal color code that needs to be applied to this vector
     */
    public String getColor(double maxMagnitude, double minMagnitude) {
        if (magnitude < 0 || maxMagnitude < 0 || minMagnitude < 0) {
            return "negativeMagnitude";
        } else if (maxMagnitude == 0 || minMagnitude == 0) {
            return "zeroMinMax";
        } else if (magnitude == 0) {
            return "zeroMagnitude";
        } else if (maxMagnitude < minMagnitude) {
            return "brokenBounds";
        } else if (magnitude > maxMagnitude || magnitude < minMagnitude) {
            return "magnitudeOutOfBounds";
        }
        float percentage = (float) ((magnitude - minMagnitude) / (maxMagnitude - minMagnitude));
        int r = turbo_srgb_bytes[Math.round(255.0f * (percentage))][0];
        int g = turbo_srgb_bytes[Math.round(255.0f * (percentage))][1];
        int b = turbo_srgb_bytes[Math.round(255.0f * (percentage))][2];
        return String.format("#%02X%02X%02X", r, g, b);
    }

    //TODO solve the color problem (comes from magnitude or hex)
    public void setArrowColor(double maxMagnitude, double minMagnitude){
        if(getColor(maxMagnitude, minMagnitude).contains("#")){
        for(Node n: getChildren()){
            if(n instanceof Shape3D){
                ((Shape3D) n).setMaterial(new PhongMaterial(Color.web(getColor(maxMagnitude, minMagnitude))));
            }
            else if(n instanceof Group){
                for(Node z: ((Group) n).getChildren()){
                    if(z instanceof MeshView){
                        ((MeshView) z).setMaterial(new PhongMaterial(Color.web(getColor(maxMagnitude, minMagnitude))));
                    }
                }
            }
        }
        }
    }

    //TODO
    private TriangleMesh creatingTriangleMesh() {
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

        return mesh;
    }

    //TODO
    private Group creatingArrow(){
        Group cone = new Group();
        PhongMaterial material = new PhongMaterial(Color.BLUE);


        TriangleMesh mesh = creatingTriangleMesh();

        Cylinder c1 = new Cylinder(radius, 0.01);
        c1.setMaterial(material);

        Cylinder c2 = new Cylinder(radius / 2, height / 2 * 3);
        c2.setTranslateY(height / 4 * 3);
        c2.setMaterial(material);

        MeshView meshView = new MeshView();
        meshView.setMesh(mesh);
        meshView.setMaterial(material);
        meshView.setTranslateZ(height / 2);
        cone.getChildren().addAll(meshView, c1, c2);
        cone.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        getChildren().addAll(cone, c1, c2);
        return cone;
    }

}
