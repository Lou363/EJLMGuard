package com.efrei.ejlmguard;

import org.iq80.leveldb.*;
import org.iq80.leveldb.impl.Iq80DBFactory;

import com.google.gson.Gson;
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

    public boolean isHashInDatabase(String hash){
        return getHash(hash) != null;
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
        byte[] valueBytes = database.get(bytes(hash));
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

    public void importFromJSON(String jsonFilePath) throws IOException {
        Gson gson = new Gson();

        try {
            StringBuilder jsonData = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(jsonFilePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonData.append(line);
                }
            }

            HashMap<String, String> data = gson.fromJson(jsonData.toString(), HashMap.class);

            WriteBatch batch = database.createWriteBatch();
            try {
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    
                    byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
                    byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
                    
                    batch.put(keyBytes, valueBytes);
                }
                
                database.write(batch);
            } finally {
                batch.close();
            }

            System.out.println("Import completed successfully.");
        } finally {
            if (database != null) {
                database.close();
            }
        }
    }
}
