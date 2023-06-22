package com.efrei.ejlmguard;

import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import okhttp3.OkHttpClient;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WebAnalysis {
    private static final String VIRUS_TOTAL_API_KEY = "10fe6404a69483c94f3fbe26437c5242875e699692b99fb5f662cca5d3317495";
    private static final String VIRUS_TOTAL_SCAN_URL = "https://www.virustotal.com/api/v3/files";
    
    private String filePath;
    private String fileId;
    private String fileName;

    public WebAnalysis(String filePath) {
        // Check that the file exists
        if (!Files.exists(Path.of(filePath))) {
            System.out.println("File does not exist: " + filePath);
            throw new IllegalArgumentException("File does not exist: " + filePath);
        }
        // Check the file is less than 32MB
        try{
            if (Files.size(Path.of(filePath)) > 32 * 1024 * 1024) {
                System.out.println("File is too large: " + filePath);
                throw new IllegalArgumentException("File is too large: " + filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.filePath = filePath;
        // I extract the file name and its extension from the file path
        this.fileName = filePath.substring(filePath.lastIndexOf("\\") + 1);
    }


    public void submitFileForScan() throws IOException, InterruptedException {
    byte[] fileData = Files.readAllBytes(Path.of(filePath));
        String base64FileData = java.util.Base64.getEncoder().encodeToString(fileData);

        OkHttpClient client = new OkHttpClient();

        // i obtain the mediaTypeString type
        String mediaTypeString = URLConnection.guessContentTypeFromName(filePath);
        MediaType mediaType = MediaType.parse(mediaTypeString);

        String requestBodyString = "-----011000010111000001101001\r\n" +
                "Content-Disposition: form-data; name=\"file\"\r\n\r\n" +
                "data:" + mediaTypeString + ";name=" + filePath + ";base64," + base64FileData + "=\r\n" +
                "-----011000010111000001101001--\r\n\r\n";

        RequestBody body = RequestBody.create(requestBodyString.getBytes(), mediaType);

        Request request = new Request.Builder()
                .url("https://www.virustotal.com/api/v3/files")
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("x-apikey", "10fe6404a69483c94f3fbe26437c5242875e699692b99fb5f662cca5d3317495")
                .addHeader("content-type", "multipart/form-data; boundary=---011000010111000001101001")
                .build();
        System.out.println("File submission started:\n" + request.toString() + "/n");
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println(response.body().string());
            } else {
                System.out.println("File submission failed. Error: " + response.code() + " - " + response.message());
            }
        }
        System.out.println("File submission completed");
    }
    
    public void retrieveScanResult() throws IOException, InterruptedException {
        String scanResultUrl = VIRUS_TOTAL_SCAN_URL + "/" + fileId;
        
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(scanResultUrl))
                .header("x-apikey", VIRUS_TOTAL_API_KEY)
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Scan result: " + response.body());
    }
}
