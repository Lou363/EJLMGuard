package com.efrei.ejlmguard;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App {
    private static DatabaseHandler databaseHandler;
    private static ConfigurationHandler configurationHandler;
    public static void main(String[] args) throws IOException {
        configurationHandler = new ConfigurationHandler();

        databaseHandler = new DatabaseHandler();

        if (Files.exists(Paths.get("dataset.json"))) {
            System.out.println("Applied update successfully.");
            databaseHandler.importFromJSON("dataset.json");
            // I delete the file
            Files.delete(Paths.get("dataset.json"));
        } else {
            UpdateGUI updater = new UpdateGUI(true);
        }
        
        // I print the database content
        System.out.println("Database content:");
        //databaseHandler.listHashes();


        // I close the database
        databaseHandler.close();
    }

    public static DatabaseHandler getDatabaseHandler() {
        return databaseHandler;
    }

    public static ConfigurationHandler getConfigurationHandler() {
        return configurationHandler;
    }

    public static void setDatabaseHandler(DatabaseHandler databaseHandler) {
        App.databaseHandler = databaseHandler;
    }

    public static void restartProgram() {
        String os = System.getProperty("os.name").toLowerCase();

        try {
            ProcessBuilder processBuilder;
            if (os.contains("win")) {
                // Windows
                processBuilder = new ProcessBuilder("cmd", "/c", "java -jar program.jar");
            } else if (os.contains("mac")) {
                // macOS
                processBuilder = new ProcessBuilder("java", "-jar", "program.jar");
            } else {
                // Linux
                processBuilder = new ProcessBuilder("sh", "-c", "java -jar program.jar");
            }

            processBuilder.inheritIO();
            processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Exit the current program
        System.exit(0);
    }
}
