package edu.vanier.eastwest.util;

import com.google.gson.*;
import edu.vanier.eastwest.models.Body;
import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.lang.reflect.Type;

public class TypeAdapter implements JsonSerializer<Body>, JsonDeserializer<Body> {
    @Override
    public JsonElement serialize(Body body, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject serializedObject = new JsonObject();
        serializedObject.addProperty("name", body.getName());
        serializedObject.addProperty("mass", body.getMass());
        serializedObject.addProperty("radius", body.getRadius());
        if(body.getColor() != null) {
            serializedObject.addProperty("color", body.getColor().toString());
        }
        serializedObject.addProperty("velocityX", body.getVelocity().getX());
        serializedObject.addProperty("velocityY", body.getVelocity().getY());
        serializedObject.addProperty("velocityZ", body.getVelocity().getZ());
        serializedObject.addProperty("positionX", body.getPosition().getX());
        serializedObject.addProperty("positionY", body.getPosition().getY());
        serializedObject.addProperty("positionZ", body.getPosition().getZ());
        if(body.getTexture() != null) {
            serializedObject.addProperty("texture", body.getTexture().getUrl());
        }
        return serializedObject;
    }

    @Override
    public Body deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
        JsonObject serializedObject = jsonElement.getAsJsonObject();
        String name = serializedObject.get("name").getAsString();
        double mass = serializedObject.get("mass").getAsDouble();
        double radius = serializedObject.get("radius").getAsDouble();
        Color color = null;
        if(serializedObject.get("color") != null) {
            color = Color.valueOf(serializedObject.get("color").getAsString());
        }
        double velocityX = serializedObject.get("velocityX").getAsDouble();
        double velocityY = serializedObject.get("velocityY").getAsDouble();
        double velocityZ = serializedObject.get("velocityZ").getAsDouble();
        Point3D velocity = new Point3D(velocityX, velocityY, velocityZ);
        double positionX = serializedObject.get("positionX").getAsDouble();
        double positionY = serializedObject.get("positionY").getAsDouble();
        double positionZ = serializedObject.get("positionZ").getAsDouble();
        Point3D position = new Point3D(positionX, positionY, positionZ);
        Image texture = null;
        if(serializedObject.get("texture") != null){
            texture = new Image(serializedObject.get("texture").getAsString());
        }
        return new Body(name, radius, mass, position, velocity, color, texture);
    }
}
