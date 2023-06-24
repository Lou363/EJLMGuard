package com.efrei.ejlmguard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.lang.model.SourceVersion;

public class SignatureUtilities {

    private String md5;
    private String sha1;
    private String sha256;
    private File file;
    
    public SignatureUtilities(File file){
        this.file = file;
        // We check the file exists
        if(!file.exists()){
            throw new IllegalArgumentException("File does not exist");
        }
        try {
            md5 = calculateHash("MD5");
            sha1 = calculateHash("SHA-1");
            sha256 = calculateHash("SHA-256");
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }

    }

    public SignatureUtilities() {
    }

    private String calculateHash(String algorithm) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        try (FileInputStream fis = new FileInputStream(file);
             DigestInputStream dis = new DigestInputStream(fis, md)) {
            while (dis.read() != -1) {
                // Read the file stream to update the digest
            }
        }
        byte[] hashBytes = md.digest();
        StringBuilder hashString = new StringBuilder();
        for (byte hashByte : hashBytes) {
            String hex = Integer.toHexString(0xff & hashByte);
            if (hex.length() == 1) {
                hashString.append('0');
            }
            hashString.append(hex);
        }
        return hashString.toString();
    }

    // Getters
    public String getMD5() {
        return md5;
    }
    public String getSha1() {
        return sha1;
    }
    public String getSha256() {
        return sha256;
    }
}
