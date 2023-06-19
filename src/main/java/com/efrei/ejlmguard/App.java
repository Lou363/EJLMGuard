package com.efrei.ejlmguard;

import java.io.IOException;

public class App {
    private static DatabaseHandler databaseHandler;
    private static ConfigurationHandler configurationHandler;
    public static void main(String[] args) throws IOException {
        // I begin by initialising the configuration
        configurationHandler = new ConfigurationHandler();
        // I begin by initialising the database
        databaseHandler = new DatabaseHandler();
        
        // I update the VPS
        UpdateHandler updateHandler = new UpdateHandler();
        System.out.println("Update available: " + updateHandler.isUpdateAvailable());
        if(updateHandler.isUpdateAvailable())
            updateHandler.updateVPS();

        // I fill the database with some generic hashes
        // databaseHandler.importFromJSON("export.json");

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
}
