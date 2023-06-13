package com.efrei.ejlmguard;

import org.iq80.leveldb.*;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;

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
}
