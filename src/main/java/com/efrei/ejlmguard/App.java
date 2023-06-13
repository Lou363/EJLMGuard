package com.efrei.ejlmguard;

import java.io.IOException;

public class App {
    private static DatabaseHandler databaseHandler;
    public static void main(String[] args) throws IOException {
        // I begin by initialising the database
        databaseHandler = new DatabaseHandler();
        // I fill the database with some generic hashes
        // databaseHandler.fillDatabase();
        // I check if hash exists in database
        String exemple = databaseHandler.findDescription("258547e4dc8e7bf245533991345e6eb7");
        System.out.println(exemple);
        databaseHandler.listHashes();




        // END OF THE PROGRAM ROUTINE
        databaseHandler.close();
    }
}
