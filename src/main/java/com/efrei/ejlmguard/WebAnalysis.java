package com.efrei.ejlmguard;

import java.io.IOException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import okhttp3.*;


public class WebAnalysis {
    private static final String VIRUS_TOTAL_API_KEY = "10fe6404a69483c94f3fbe26437c5242875e699692b99fb5f662cca5d3317495";
    private static final String VIRUS_TOTAL_SCAN_URL = "https://www.virustotal.com/api/v3/files";

    private String virusName;
    private Path filePath;
    private String responseBody;
    private String scanId;

    public WebAnalysis(String rawPath) {
        this.filePath = Paths.get(rawPath);
        // I check the file exists here
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("File " + filePath + " does not exist");
        }
    }


    public void submitFileForScan() throws IOException {
        OkHttpClient httpClient = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", filePath.getFileName().toString(),
                        RequestBody.create(filePath.toFile(), MediaType.parse("application/octet-stream")))
                .build();

        Request request = new Request.Builder()
                .url(VIRUS_TOTAL_SCAN_URL)
                .addHeader("x-apikey", VIRUS_TOTAL_API_KEY)
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                assert response.body() != null;
                String responseBody = response.body().string();
                System.out.println(responseBody);
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                scanId = rootNode.path("data").path("id").asText();
                // former return
            } else {
                throw new IOException("Error sending file to VirusTotal: " + response.code() + " " + response.message());
            }
        }
    }

    private void getAnalysisResult() throws IOException {
        OkHttpClient httpClient = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(VIRUS_TOTAL_SCAN_URL).newBuilder();
        urlBuilder.addPathSegment(scanId);
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("x-apikey", VIRUS_TOTAL_API_KEY)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            responseBody = response.body().string();
            System.out.println("--------------------");
            System.out.println(responseBody);
            System.out.println("--------------------");
        }
    }

    public void retrieveScanResult() {
        System.out.println("File id: " + scanId);
        try {
            getAnalysisResult();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (responseBody == null) {
            System.out.println("Error getting analysis result.");
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            ArrayNode scans = (ArrayNode) rootNode.path("data").path("attributes").path("last_analysis_results");

            int totalScans = 0;
            int positiveScans = 0;
            virusName = null;

            for (JsonNode scanResult : scans) {
                totalScans++;
                String status = scanResult.path("result").asText();
                if (!status.isEmpty()) {
                    if (virusName == null) {
                        virusName = status;
                    }
                    positiveScans++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
