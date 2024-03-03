package edu.vanier.eastwest.models;

import javafx.geometry.Point2D;

public class Vector2D {

    private Point2D direction, start, end;
    private double magnitude;

    public void update(){

    }
    public void draw(){

    }

    /**
     * Compare the magnitude of the vector to the highest and lowest found in
     * the list of vectors and assigne him a hexadecimal color code based on that
     * @param maxMagnitude The highest magnitude found in the list of vectors
     * @param minMagnitude The lowest magnitude found in the list of vectors
     * @return The hexadecimal color code that needs to be applied to this vector
     */
    public String getColor(double maxMagnitude, double minMagnitude){
        double percentage = (magnitude-minMagnitude)/(maxMagnitude-minMagnitude)
        if(highest == null||lowest == null){
            return "nullMinMax"
        }
        else if(magnitude == null){
            return "nullMagnitude"
        }
        else if(percentage == 0){
            return
        }
        return null;
    }
}
