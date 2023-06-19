package com.efrei.ejlmguard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.*;

public class ConfigurationHandler {
    private String vpsVersion;
    private String programVersion;

    // Default constructor
    public ConfigurationHandler() {
        try {
            loadConfig();
        } catch (IOException e) {
            // If config.json doesn't exist, create it with default values
            createConfig();
        }
    }

    // Getter and Setter for vpsVersion
    public String getVpsVersion() {
        return vpsVersion;
    }

    public void setVpsVersion(String vpsVersion) {
        this.vpsVersion = vpsVersion;
        updateConfig();
    }

    // Getter for programVersion
    public String getProgramVersion() {
        return programVersion;
    }

    // Load config.json file
    private void loadConfig() throws IOException {
        FileReader fileReader = new FileReader("config.json");
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        JsonObject jsonObject = JsonParser.parseReader(bufferedReader).getAsJsonObject();
        this.vpsVersion = jsonObject.get("vps_version").getAsString();
        this.programVersion = jsonObject.get("program_version").getAsString();
        bufferedReader.close();
    }

    // Create config.json file with default values
    private void createConfig() {
        this.programVersion = "0.0.1";
        this.vpsVersion = "1.0.0";

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("vps_version", vpsVersion);
        jsonObject.addProperty("program_version", programVersion);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(jsonObject);

        try {
            FileWriter fileWriter = new FileWriter("config.json");
            fileWriter.write(json);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Update config.json file when vpsVersion is changed
    private void updateConfig() {
        try {
            FileReader fileReader = new FileReader("config.json");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            JsonObject jsonObject = JsonParser.parseReader(bufferedReader).getAsJsonObject();
            bufferedReader.close();

            jsonObject.addProperty("vps_version", vpsVersion);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(jsonObject);

            FileWriter fileWriter = new FileWriter("config.json");
            fileWriter.write(json);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
