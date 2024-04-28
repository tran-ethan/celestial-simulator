package edu.vanier.eastwest.util;

import com.google.gson.*;
import edu.vanier.eastwest.models.Body;
import edu.vanier.eastwest.models.Vector3D;
import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.lang.reflect.Type;

public class Serializer implements JsonSerializer<Body>, JsonDeserializer<Body>{
    @Override
    public JsonElement serialize(Body body, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject serializedObject = new JsonObject();
        serializedObject.addProperty("name", body.getName());
        serializedObject.addProperty("mass", body.getMass());
        serializedObject.addProperty("radius", body.getRadius());
        serializedObject.add("color", jsonSerializationContext.serialize(body.getColor()));
        serializedObject.add("velocity", jsonSerializationContext.serialize(body.getVelocity()));
        serializedObject.add("position", jsonSerializationContext.serialize(body.getPosition()));
        serializedObject.add("texture", jsonSerializationContext.serialize(body.getTexture()));
        return serializedObject;
    }

    @Override
    public Body deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext){
        JsonObject serializedObject = jsonElement.getAsJsonObject();
        String name = serializedObject.get("name").getAsString();
        double mass = serializedObject.get("mass").getAsDouble();
        double radius = serializedObject.get("radius").getAsDouble();
        Color color = jsonDeserializationContext.deserialize(serializedObject.get("color"), Color.class);
        Point3D velocity = jsonDeserializationContext.deserialize(serializedObject.get("velocity"), Point3D.class);
        Point3D position = jsonDeserializationContext.deserialize(serializedObject.get("position"), Point3D.class);
        Image texture = jsonDeserializationContext.deserialize(serializedObject.get("texture"), Image.class);
        return new Body(name, radius, mass, position, velocity, color, texture);
    }
}
