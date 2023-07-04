package com.efrei.ejlmguard.GUI;

import javax.swing.*;

import com.efrei.ejlmguard.App;
import com.efrei.ejlmguard.CaptiveAuth;
import com.efrei.ejlmguard.Version;

import java.awt.*;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CaptiveGUI extends JFrame{

    private JLabel label;
    private JProgressBar progressBar;
    private boolean launchedInBackground;

    public CaptiveGUI(){
        super("Connecting to captive portal..");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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
        label = new JLabel("Testing connection...");
        progressBar = new JProgressBar();
        progressBar.setValue(0);
        progressBar.setIndeterminate(true);
        add(label);
        add(progressBar);
        setVisible(true);
        startSession();
    }

    public void updateLabel(String text){
        label.setText(text);
    }

    public void endSession(){
        setVisible(false);
        dispose();
    }

    private void startSession(){
        Thread captiveThread = new Thread(() -> {
            new CaptiveAuth(this);
            System.out.println("[CAPTIVE] Verifying internet connection...");
            Boolean captivechecked = false;
            while(!captivechecked){
                try{
                    if(CaptiveAuth.InternetCheck() == 0){
                    System.out.println("[CAPTIVE] Internet connection verified, no captive portal detected.");
                    captivechecked = true;
                }
                else{
                    System.out.println("[CAPTIVE] Captive portal detected.");
                    CaptiveAuth.postAuth("192.168.1.254");
                    CaptiveAuth.getAuth("192.168.1.254");
                }
                } catch (Exception e){
                    System.out.println("something went wrong: " + e);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                } finally {
                    this.endSession();
                }
            }
        });
        captiveThread.start();
    }

}
