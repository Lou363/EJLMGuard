package com.efrei.ejlmguard;

import org.iq80.leveldb.*;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;

public class DatabaseHandler {

    private static final String DB_PATH = "vpsdataset.db";

    public static void fillDatabase() {
        try {
            DB database = openDatabase(DB_PATH);
            putHash(database, "hash1", "Description 1");
            putHash(database, "hash2", "Description 2");
            putHash(database, "hash3", "Description 3");
            database.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String findDescription(String hash) {
        try {
            DB database = openDatabase(DB_PATH);
            String description = getHash(database, hash);
            database.close();
            return description;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static DB openDatabase(String path) throws IOException {
        Options options = new Options();
        options.createIfMissing(true);
        return Iq80DBFactory.factory.open(new File(path), options);
    }

    private static void putHash(DB database, String hash, String description) {
        database.put(bytes(hash), bytes(description));
    }

    private static String getHash(DB database, String hash) {
        byte[] valueBytes = database.get(bytes(hash));
        return valueBytes != null ? asString(valueBytes) : null;
    }

    private static byte[] bytes(String str) {
        return str.getBytes();
    }

    private static String asString(byte[] bytes) {
        return new String(bytes);
    }

    public static void main(String[] args) {
        fillDatabase();
        String description = findDescription("hash2");
        System.out.println("Description for hash2: " + description);
    }
}
