package com.efrei.ejlmguard;

import java.io.*;
import java.net.*;

public class VoucherCatcher {
    private static String voucher = "";

    public static void scriptPinger(String fwAdress, int port) throws IOException {
    // Create a socket to connect to the server on port 666
    Socket clientSocket = new Socket(fwAdress, port);

    // Send data
    String data = "EJLMGUARDauth";
    OutputStream out = clientSocket.getOutputStream();
    out.write(data.getBytes());

    // Receive server response
    InputStream in = clientSocket.getInputStream();
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    voucher = reader.readLine();
    System.out.println(voucher);

    // Close the socket
    clientSocket.close();
    }

    public static void voucherSender(String voucher , String fwAdress) throws IOException {
        // Set the Captive Portal URL
        String captivePortalURL = "https://" + fwAdress + ":8002";
            
        // Create the URL with the voucher code as a query parameter
        String urlString = captivePortalURL + "/?voucher=" + URLEncoder.encode(voucher, "UTF-8");

        // Create a URL object
        URL url = new URL(urlString);

        // Open a connection to the URL
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set the request method to GET
        connection.setRequestMethod("GET");

        // Get the response code
        int responseCode = connection.getResponseCode();

        // Read the response
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder response = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        // Close the reader and connection
        reader.close();
        connection.disconnect();
    }

    public static Boolean InternetCheck() throws IOException {
        InetAddress address = InetAddress.getByName("1.1.1.1");
        boolean reachable = address.isReachable(10000);
        if (reachable) {
            System.out.println("Internet access is available.");
            return true;
        } else {
            System.out.println("Internet access is not available.");
            return false;
        }
        
    }
    
    //getter for voucher
    public static String getVoucher() {
        return voucher;
    }
}
