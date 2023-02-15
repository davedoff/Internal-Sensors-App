package com.davidhalldoff.internalsensors.io;

import android.content.Context;
import android.util.Log;

import com.davidhalldoff.internalsensors.model.Sensors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class FileIO {

    /**
     * Saves a list of Sensors objects to a Json file
     *
     * @param context The context
     * @param sensorList The sensor list that will be saved
     * @param filename The name of the file that will be saved
     */
    public static void saveValuesToJson(Context context, ArrayList<Sensors> sensorList, String filename) {
        try {
            // Create a new JSONArray to store the accelerometer values in
            JSONArray jsonArray = new JSONArray();

            // Loop through the accelerometers values and add them to the JSONArray
            for (Sensors sensor : sensorList) {
                // Create a new JSONObject to represent the accelerometer value
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("time", sensor.getTimestamp());
                jsonObject.put("angle", sensor.getAngle());

                // Add the JSONObject to the JSONArray
                jsonArray.put(jsonObject);
            }

            // Convert JSONObject to String format
            String userString = jsonArray.toString();

            // Define the File Path and its Name
            File file = new File(context.getFilesDir(), String.format("%s_%s.json", filename, System.currentTimeMillis()));
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(userString);
            bufferedWriter.close();

            Log.e("saveValuesToJson", "Saving...");
            Log.e("File path", context.getFilesDir().toString());

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }
}

