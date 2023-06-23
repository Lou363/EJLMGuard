package com.efrei.ejlmguard;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

import javax.print.DocFlavor.STRING;
import javax.swing.SwingUtilities;

import com.efrei.ejlmguard.GUI.DatabasePusher;
import com.efrei.ejlmguard.GUI.GUI_Main;
import com.efrei.ejlmguard.GUI.UpdateGUI;

import java.io.*;
import java.net.*;

public class VoucherCatcher {
    private int port = 666;
    private String fwAdress = "192.168.1.254";
    private String voucher = "";

    //TODO: add button to enter the adress of the captive portal
        
    // Create a socket to connect to the server on port 666
    Socket clientSocket = new Socket(fwAdress, port);

    // Send data
    String data = "Test de données";
    OutputStream out = clientSocket.getOutputStream();
    out.write(data.getBytes());

    // Receive server response
    InputStream in = clientSocket.getInputStream();
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    String read = reader.readLine();
    System.out.println(read);

    // Close the socket
    clientSocket.close();


    //getter and setter for port
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    //getter and setter for fwAdress
    public String getFwAdress() {
        return fwAdress;
    }

    public void setFwAdress(String fwAdress) {
        this.fwAdress = fwAdress;
    }

    //getter and setter for voucher
    public String getVoucher() {
        return voucher;
    }

    public void setVoucher(String voucher) {
        this.voucher = voucher;
    }
}
