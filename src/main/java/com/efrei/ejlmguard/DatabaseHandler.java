package com.efrei.ejlmguard;

import org.iq80.leveldb.*;
import org.iq80.leveldb.impl.Iq80DBFactory;

import com.efrei.ejlmguard.GUI.DatabasePusher;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class DatabaseHandler {

    private static final String DB_PATH = "vpsdataset.db";
    private DB database;

    public DatabaseHandler() {
        try {
            database = openDatabase(DB_PATH);
            fillDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fillDatabase() {
        // I write 3 generic MD5 hash and random viruses names
        putHash("f0af8ccf306c99b50f4873c327a27ff0", "NotAVirus");
        putHash("17e3d333f69c55ed26e60cd661e5dd76", "NotAVirus");
        putHash("258547e4dc8e7bf245533991345e6eb7", "NotAVirus");
    }

    public boolean isHashInDatabase(String md5Hash) {
        if (database == null) {
            System.err.println("Database is not initialized");
            return false;
        }
        System.out.println("The received hash is: " + md5Hash + " and the database contains:");
        return getHash(md5Hash) != null;
    }


    public String findDescription(String hash) {
        String description = getHash(hash);
        return description;
    }

    private DB openDatabase(String path) throws IOException {
        Options options = new Options();
        options.createIfMissing(true);
        return Iq80DBFactory.factory.open(new File(path), options);
    }

    private void putHash(String hash, String description) {
        database.put(bytes(hash), bytes(description));
    }

    private String getHash(String hash) {
        // We check bytes(hash) is not null
        byte[] valueBytes;
        synchronized (this) {
            if(bytes(hash) == null) {
                System.err.println("The hash is null");
                return null;
            }
            valueBytes = database.get(bytes(hash));
        }

        
        return valueBytes != null ? asString(valueBytes) : null;
    }

    private byte[] bytes(String str) {
        return str.getBytes();
    }

    private String asString(byte[] bytes) {
        return new String(bytes);
    }

    public void close() throws IOException {
        database.close();
    }

    public void listHashes() throws IOException{
        try (DBIterator iterator = database.iterator()) {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String key = asString(iterator.peekNext().getKey());
                String value = asString(iterator.peekNext().getValue());
                System.out.println(key + " = " + value);
            }
        }
    }


    /* ##########################################
     * #         IMPORT AND EXPORT METHODS      #
    *  ##########################################*/
    public void exportToJSON(String jsonFilePath) throws IOException {

        Gson gson = new Gson();

        try {
            Map<String, String> data = new HashMap<>();
            
            try (DBIterator iterator = database.iterator()) {
                for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                    byte[] keyBytes = iterator.peekNext().getKey();
                    byte[] valueBytes = iterator.peekNext().getValue();
                    
                    String key = new String(keyBytes, StandardCharsets.UTF_8);
                    String value = new String(valueBytes, StandardCharsets.UTF_8);
                    
                    data.put(key, value);
                }
            }
            
            String jsonData = gson.toJson(data);
            
            try (FileWriter writer = new FileWriter(jsonFilePath)) {
                writer.write(jsonData);
            }
            
            System.out.println("Export completed successfully.");
        } finally {
            if (database != null) {
                database.close();
            }
        }
    }

    public void importFromJSON(String jsonFilePath, DatabasePusher dbPusher) throws IOException {
        Gson gson = new Gson();
        JsonReader jsonReader = null;
        int lineCount = 0;

        dbPusher.switchToWriteMode();
        try {
            jsonReader = new JsonReader(new FileReader(jsonFilePath));

            // Clear the database
            try (DBIterator iterator = database.iterator()) {
                for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                    database.delete(iterator.peekNext().getKey());
                }
            }

            // Count the number of lines in the file
            try (BufferedReader reader = new BufferedReader(new FileReader(jsonFilePath))) {
                while (reader.readLine() != null) {
                    lineCount++;
                }
            }

            // Read file in stream mode
            WriteBatch batch = database.createWriteBatch();
            int batchSize = 0;
            int processedLines = 0;
            int progressUpdateInterval = 1000;
            int progress = 0;

            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                String key = jsonReader.nextName();
                String value = jsonReader.nextString();

                byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
                byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);

                batch.put(keyBytes, valueBytes);
                batchSize++;
                processedLines++;

                if (batchSize >= progressUpdateInterval || processedLines == lineCount) {
                    database.write(batch);
                    batch.close();

                    progress = (int) ((float) processedLines / lineCount * 100);
                    if (progress > 100) {
                        progress = 100; // Cap the progress at 100%
                    }

                    dbPusher.updateProgress(progress);

                    if (processedLines != lineCount) {
                        batch = database.createWriteBatch();
                        batchSize = 0;
                    }
                }
            }
            jsonReader.endObject();

            // Write the remaining batch if any
            if (batchSize > 0) {
                database.write(batch);
                batch.close();
            }

            System.out.println("Import completed successfully.");
        } finally {
            if (jsonReader != null) {
                try {
                    jsonReader.close();
                } catch (IOException e) {
                    // Handle the IOException if necessary.
                }
            }
            if (database != null) {
                try {
                    database.close();
                } catch (NullPointerException e) {
                    // Handle the NullPointerException, or log an error message
                    // if necessary.
                }
            }
        }
    }
}
