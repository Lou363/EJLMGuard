package com.efrei.ejlmguard;

import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public class UpdateHandler {
    private static final String LATEST_VERSION_URL = "https://ejlmstorage.blob.core.windows.net/vpsdataset/latest.txt";
    private static final String DATASET_URL = "https://ejlmstorage.blob.core.windows.net/vpsdataset/dataset.json";
    private boolean updateAvailable;

    public UpdateHandler() {
        // Fetch the latest version from the latest.txt file
        String latestVersion = fetchLatestVersion();
        Version currentVersion = new Version(App.getConfigurationHandler().getVpsVersion());

        // Check if an update is available
        if (currentVersion.compareTo(new Version(latestVersion)) < 0) {
            System.out.println("An update is available to VPS version " + latestVersion );
            updateAvailable = true;
        } else {
            System.out.println("No update available.");
        }
    }

    public void updateVPS(){
        // 
    }

    private String fetchLatestVersion() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(LATEST_VERSION_URL))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body().trim();
        } catch (IOException | InterruptedException e) {
            System.out.println("Unable to fetch latest version. Check your internet connection.");
            e.printStackTrace();
        }
        return null;
    }

    private Path downloadDatasetFile() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DATASET_URL))
                .build();

        try {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            Path datasetPath = Files.createTempFile("dataset", ".json");
            Files.copy(response.body(), datasetPath);
            return datasetPath;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }
}