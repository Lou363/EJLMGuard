package com.efrei.ejlmguard;

import java.io.IOException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import okhttp3.*;


public class WebAnalysis {
    private static final String VIRUS_TOTAL_API_KEY = "10fe6404a69483c94f3fbe26437c5242875e699692b99fb5f662cca5d3317495";
    private static final String VIRUS_TOTAL_SCAN_URL = "https://www.virustotal.com/api/v3/files";

    private String virusName;
    private Path filePath;
    private String responseBody;
    private String scanId;

    public WebAnalysis(String rawPath) throws IOException {
        this.filePath = Paths.get(rawPath);
        // I check the file exists here
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("File " + filePath + " does not exist");
        }
        // Check the file is less than 32MB
        if (Files.size(filePath) > 32 * 1024 * 1024) {
            throw new IllegalArgumentException("File " + filePath + " is too large");
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
                responseBody = response.body().string();
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

    private String fetchResultFromServer() throws IOException{
        OkHttpClient client = new OkHttpClient();
        
        Request request = new Request.Builder()
        .url("https://www.virustotal.com/api/v3/analyses/"+scanId+"")
        .get()
        .addHeader("accept", "application/json")
        .addHeader("x-apikey", "10fe6404a69483c94f3fbe26437c5242875e699692b99fb5f662cca5d3317495")
        .build();

        Response response = client.newCall(request).execute();

        return response.body().string();
    }

    public boolean getIfMalicious() throws IOException {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(fetchResultFromServer());
            JsonNode dataNode = jsonNode.get("data");
            JsonNode attributesNode = dataNode.get("attributes");
            JsonNode statsNode = attributesNode.get("stats");
            int detectedCount = statsNode.get("malicious").asInt() + statsNode.get("suspicious").asInt();
            int totalCount = detectedCount + statsNode.get("undetected").asInt() + statsNode.get("harmless").asInt();

            if ((double) detectedCount / totalCount > 0.25) {
                JsonNode resultsNode = attributesNode.get("results");
                Iterator<String> fieldNames = resultsNode.fieldNames();
                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    JsonNode resultNode = resultsNode.get(fieldName);
                    String category = resultNode.get("category").asText();
                    if (category.equals("malicious") || category.equals("suspicious")) {
                        virusName = resultNode.get("engine_name").asText() + ": " + resultNode.get("result").asText();
                        break;
                    }
                }
                return true;
            }

            return false;
        }

    public String getVirusName() {
        return virusName;
    }
}
