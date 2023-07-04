package com.efrei.ejlmguard.GUI;

import com.efrei.ejlmguard.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DatabasePusher extends JFrame {

    private JLabel label;
    private JProgressBar progressBar;

    public DatabasePusher() {
        super("Pushing updates");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        // Set the look to the OS look
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        setSize(300, 100);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new FlowLayout());
        label = new JLabel("Pushing update to database... This may take a while.");
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        add(label);
        add(progressBar);
        setVisible(true);

        // I launch the update in a new thread so that the GUI can be updated
        Thread thread = new Thread(() -> {
            try {
                App.getDatabaseHandler().importFromJSON("dataset.json", this);
                // I delete the file
                Files.delete(Paths.get("dataset.json"));
                processFinished();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        thread.start();
    }

    private void processFinished() {
        System.out.println("[DB PUSHER] Successfully pushed updates to database.");
        dispose();
    }

    public void switchToWriteMode(){
        SwingUtilities.invokeLater(() -> progressBar.setIndeterminate(false));
        SwingUtilities.invokeLater(() -> progressBar.setValue(0));
        SwingUtilities.invokeLater(() -> label.setText("Writing to local database..."));
    }
    public void updateProgress(int progress) {
        SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
    }
}
