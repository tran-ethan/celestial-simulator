package edu.vanier.eastwest.models;

import javafx.geometry.Point3D;

public class Vector3D {

    private Point3D direction, start, end;
    private double magnitude;

    /**
     * Update the magnitude, direction and end of the vector
     */
    public void update(){

    }

    /**
     * Draw the updated version of the vector with it's new direction, end and color.
     */
    public void draw(){

    }

    /**
     * Compare the magnitude of the vector to the highest and lowest found in
     * the list of vectors and assign him a hexadecimal color code based on that
     * @param maxMagnitude The highest magnitude found in the list of vectors
     * @param minMagnitude The lowest magnitude found in the list of vectors
     * @return The hexadecimal color code that needs to be applied to this vector
     */
    public String getColor(double maxMagnitude, double minMagnitude){
        double percentage = (magnitude-minMagnitude)/(maxMagnitude-minMagnitude);
        if(maxMagnitude == 0||minMagnitude == 0){
            return "zeroMinMax";
        }
        else if(magnitude == 0){
            return "zeroMagnitude";
        }
        else if(percentage == 0){
            return "#0000ff";
        }
        return null;
    }
}
