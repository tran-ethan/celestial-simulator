package edu.vanier.eastwest.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import edu.vanier.eastwest.models.Body;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class SaveFileManager {
    private static Gson gson = new GsonBuilder().registerTypeAdapter(Body.class, new TypeAdapter()).create();

    /**
     * Writes all Body instances into a json file
     * @param list The List of Body instances
     * @param filePath The file path to save the file in
     */
    public static void toJson(List<Body> list, String filePath) {
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            gson.toJson(list, fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads all Body instances previously saved in a json file and adds them all to a List
     * @param filePath The file path of the json file
     * @return List of Body instances that was previously saved
     */
    public static List<Body> fromJson(String filePath) {
        try (FileReader reader = new FileReader(filePath)){
            Type type = new TypeToken<List<Body>>(){}.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
