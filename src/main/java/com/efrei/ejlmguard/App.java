package com.efrei.ejlmguard;

public class App {
    public static void main(String[] args) {
        // I launch the DownloadWatcher class in a new thread
        Thread thread = new Thread(() -> {
            new DownloadWatcher();
        });
        thread.start();
        while(true){
            System.out.println("I'll be your guide");
            // I sleep for 1 second
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }    
}
