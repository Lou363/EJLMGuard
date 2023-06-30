package com.efrei.ejlmguard;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CaptiveAuth {
    public static void postAuth(String fwAdress){
        try {
            String url = "http://"+fwAdress+":8002/index.php?zone=private_network";
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.5672.93 Safari/537.36");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
            String urlParameters = "redirurl=http%3A%2F%2Fedge-http.microsoft.com%2Fcaptiveportal%2Fgenerate_204&accept=Login";
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            System.out.println("Response Code: " + responseCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getAuth() {
        try {
            String url = "http://edge-http.microsoft.com/captiveportal/generate_204";
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.5672.93 Safari/537.36");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
            int responseCode = con.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
