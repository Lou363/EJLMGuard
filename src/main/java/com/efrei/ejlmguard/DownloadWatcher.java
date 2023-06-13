package com.efrei.ejlmguard;

public class DownloadWatcher {
    private static final String DOWNLOAD_DIR = "/home/username/Downloads";

    public DownloadWatcher() {
        while (true){
        System.out.println("Heyyy");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
