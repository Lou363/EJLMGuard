package com.efrei.ejlmguard.GUI;

import javax.swing.*;

import com.efrei.ejlmguard.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

public class ThreatDetectedGUI extends JFrame {
    
    private JPanel mainPanel;
    private JLabel welcomeLabel;
    private JLabel threatNameLabel;
    private JLabel filePathLabel;
    private JLabel detectionLabel;
    private JButton ignoreButton;
    private JButton deleteButton;
    private JButton quarantineButton;
    private File file;

    public ThreatDetectedGUI(String threatName, String filePath, DetectorName dection){
        super("Threat Detected!");
        // I convert the file path to a File object so I can use it in the action listeners
        file = new File(filePath);
        // I initialize the GUI
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);
        welcomeLabel = new JLabel("Threat Detected");
        welcomeLabel.setForeground(Color.RED);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
        welcomeLabel.setForeground(Color.RED);
        add(welcomeLabel, BorderLayout.NORTH);
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(3, 1));
        threatNameLabel = new JLabel("Threat Name: " + threatName);
        threatNameLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        threatNameLabel.setHorizontalAlignment(JLabel.CENTER);
        filePathLabel = new JLabel("File Path: " + filePath);
        filePathLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        filePathLabel.setHorizontalAlignment(JLabel.CENTER);
        switch(dection){
            case USERSCAN:
                detectionLabel = new JLabel("Detected by: User Scan");
                break;
            case VIRUSTOTAL:
                detectionLabel = new JLabel("Detected by: Virus Total scan");
                break;
            case REALTIME:
                detectionLabel = new JLabel("Detected by: Real Time Protection");
                break;
        }
        detectionLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        detectionLabel.setHorizontalAlignment(JLabel.CENTER);
        mainPanel.add(threatNameLabel);
        mainPanel.add(filePathLabel);
        mainPanel.add(detectionLabel);
        add(mainPanel, BorderLayout.CENTER);
        // At the bottom I add two buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3));
        ignoreButton = new JButton("Ignore");
        deleteButton = new JButton("Delete");
        quarantineButton = new JButton("Quarantine");
        buttonPanel.add(ignoreButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(quarantineButton);
        add(buttonPanel, BorderLayout.SOUTH);
        // I register the action listeners
        ignoreButton.addActionListener(new IgnoreButtonAction());
        deleteButton.addActionListener(new DeleteButtonAction());
        quarantineButton.addActionListener(new QuarantineButtonAction());
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e){
            e.printStackTrace();
        }
        // I render the window and make it visible
        repaint();
        setVisible(true);
        // I wait 500ms before I request focus
        try{
            Thread.sleep(500);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        // I demmand the window to be on top of all other windows
        requestFocus();
        playSound();
    }

    /* ################################
     * #       ACTION LISTENNERS      #
     * ################################
     */

    public class IgnoreButtonAction implements ActionListener{
        public void actionPerformed(ActionEvent e){
            // I request if the user is sure to ignore the threat
            int dialogResult = JOptionPane.showConfirmDialog(null, "Are you sure you want to ignore this threat?", "Warning", JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
                // I close the window
                dispose();
            }

        }
    }

    public class DeleteButtonAction implements ActionListener{
        public void actionPerformed(ActionEvent e){
            // I force the deletion of the file
            file.delete();
            System.out.println("[Threat Handler] Delete successful.");
            dispose();
        }
    }

    public class QuarantineButtonAction implements ActionListener{
        public void actionPerformed(ActionEvent e){
            System.out.println("[Threat Handler] Quarantine requested by user.");
            // I quarantine the file
            ThreatQuarantineHandler.moveToQuarantine(file);
            System.out.println("[Threat Handler] Quarantine successful.");
            dispose();
        }
    }

    /* ######################################
     * #        PLAY WARNING SOUND          #
     * ######################################
     */
    private void playSound() {
        try {
            // Load the sound from the resources folder
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
                    App.class.getResourceAsStream("/soundAlert.wav"));

            // Create a Clip instance to play the sound
            Clip clip = AudioSystem.getClip();

            // Open the audio stream and start playing the sound
            clip.open(audioInputStream);
            clip.start();

            // Release system resources when the sound is complete
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
