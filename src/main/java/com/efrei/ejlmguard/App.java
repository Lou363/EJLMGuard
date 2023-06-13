package com.efrei.ejlmguard;

import java.io.IOException;

public class App {
    private static DatabaseHandler databaseHandler;
    public static void main(String[] args) throws IOException {
        // I begin by initialising the database
        databaseHandler = new DatabaseHandler();
        // I fill the database with some generic hashes
        // databaseHandler.importFromJSON("export.json");

        // I print the database content
        System.out.println("Database content:");
        databaseHandler.listHashes();


        // I close the database
        databaseHandler.close();
    }
}
