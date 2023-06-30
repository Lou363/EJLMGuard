package com.efrei.ejlmguard;

import okhttp3.*;
import java.io.IOException;
import java.net.InetAddress;

public class CaptiveAuth {
    public static void postAuth(String fwAdress) throws IOException{
        OkHttpClient client = new OkHttpClient();

        String url = "http://"+fwAdress+":8002/index.php?zone=private_network";
        String bodyContent = "redirurl=http%3A%2F%2Fedge-http.microsoft.com%2Fcaptiveportal%2Fgenerate_204&accept=Login";

        RequestBody body = RequestBody.create(bodyContent, MediaType.parse("application/x-www-form-urlencoded"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Host", fwAdress+":8002")
                .addHeader("Content-Length", "89")
                .addHeader("Cache-Control", "max-age=0")
                .addHeader("Upgrade-Insecure-Requests", "1")
                .addHeader("Origin", "http://"+fwAdress+":8002")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.5672.93 Safari/537.36")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                .addHeader("Referer", "http://"+fwAdress+":8002/index.php?zone=private_network&redirurl=http%3A%2F%2Fedge-http.microsoft.com%2Fcaptiveportal%2Fgenerate_204")
                .addHeader("Accept-Encoding", "gzip, deflate")
                .addHeader("Accept-Language", "en-US,en;q=0.9")
                .addHeader("Connection", "close")
                .build();
        Response response = client.newCall(request).execute();
        System.out.println(response.body().string());
        response.close();
    }

    public static void getAuth(String fwAdress) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://edge-http.microsoft.com/captiveportal/generate_204")
                .addHeader("Host", "edge-http.microsoft.com")
                .addHeader("Cache-Control", "max-age=0")
                .addHeader("Upgrade-Insecure-Requests", "1")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.5672.93 Safari/537.36")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                .addHeader("Referer", "http://"+fwAdress+":8002/")
                .addHeader("Accept-Encoding", "gzip, deflate")
                .addHeader("Accept-Language", "en-US,en;q=0.9")
                .addHeader("Connection", "close")
                .build();

        Response response = client.newCall(request).execute();

        System.out.println(response.code());
        System.out.println(response.body().string());

        response.close();
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
}
