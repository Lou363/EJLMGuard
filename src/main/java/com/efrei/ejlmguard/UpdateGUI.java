package com.efrei.ejlmguard;

import javax.swing.*;
import java.awt.*;

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

public class UpdateGUI extends JFrame{

    private JLabel label;
    private JProgressBar progressBar;
    private boolean launchedInBackground;

    private static final String LATEST_VERSION_URL = "https://ejlmstorage.blob.core.windows.net/vpsdataset/latest.txt";
    private static final String DATASET_URL = "https://ejlmstorage.blob.core.windows.net/vpsdataset/dataset.json";
    private boolean updateAvailable;

    public UpdateGUI(boolean launchedInBackground){
        super("Update");
        this.launchedInBackground = launchedInBackground;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Set the look to the OS look
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e){
            e.printStackTrace();
        }
        setSize(300, 100);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new FlowLayout());
        label = new JLabel("Checking for updates...");
        progressBar = new JProgressBar();
        progressBar.setValue(0);
        progressBar.setIndeterminate(true);
        add(label);
        add(progressBar);
        setVisible(true);
        // I launch the update in a new thread so that the GUI can be updated
        new Thread(() -> {
            checkUpdate();
            if(updateAvailable){
                updateprogressBarType(false);
                updateLabel("Downloading update...");
                updateVPS();
                // Once this is done, I update the dataset
                App.getConfigurationHandler().setVpsVersion(fetchLatestVersion());
                // I now push the dataset.json file to the database
                updateLabel("Writing update to dataset...");
                updateprogressBarType(launchedInBackground);
                App.restartProgram();

            }
            dispose();
        }).start();
    }

    public void updateProgress(int progress){
        progressBar.setValue(progress);
    }

    private void sendMessage(String message, String title, int messageType){
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }
    private void updateprogressBarType(boolean indeterminate){
        progressBar.setIndeterminate(indeterminate);
    }

    public void updateLabel(String text){
        label.setText(text);
    }

    private void checkUpdate() {
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

    private void updateVPS(){
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DATASET_URL))
                .build();

        try {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            long contentLength = response.headers().firstValueAsLong("Content-Length").orElse(-1L);
            long bytesRead = 0L;
            int bufferSize = 4096;
            byte[] buffer = new byte[bufferSize];
            int bytesReadThisTime;
            double progress;

            try (InputStream inputStream = response.body();
                 FileOutputStream outputStream = new FileOutputStream("dataset.json")) {

                while ((bytesReadThisTime = inputStream.read(buffer, 0, bufferSize)) != -1) {
                    outputStream.write(buffer, 0, bytesReadThisTime);
                    bytesRead += bytesReadThisTime;

                    // Update progress
                    progress = (double) bytesRead / contentLength;
                    int progressPercentage = (int) (progress * 100);
                    SwingUtilities.invokeLater(() -> updateProgress(progressPercentage));
                }
            }

            // Download complete
            SwingUtilities.invokeLater(() -> updateProgress(100));
            System.out.println("Dataset downloaded successfully.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
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
            if(!launchedInBackground)
                sendMessage("Unable to fetch latest version. Check your internet connection.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return null;
    }


    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

}
