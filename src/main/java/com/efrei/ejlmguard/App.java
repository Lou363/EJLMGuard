package com.efrei.ejlmguard;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

import javax.swing.SwingUtilities;

import com.efrei.ejlmguard.GUI.DatabasePusher;
import com.efrei.ejlmguard.GUI.GUI_Main;
import com.efrei.ejlmguard.GUI.UpdateGUI;


public class App {
    private static DatabaseHandler databaseHandler;
    private static ConfigurationHandler configurationHandler;
    private static Thread downloadWatcherThread;
    private static DownloadWatcher downloadWatcher;

    public static void main(String[] args) throws IOException, InterruptedException {
        configurationHandler = new ConfigurationHandler();


        // databaseHandler = new DatabaseHandler();
        
        databaseHandler = new DatabaseHandler();


        /* ######################################
         * #             CAPTIVE UNLOCKING      #
         * ######################################
         */



        /* #######################################
         * #            VPS UPDATE CHECKING      #
         * #######################################
         */
        if (Files.exists(Paths.get("dataset.json"))) {
            final CountDownLatch latch = new CountDownLatch(1); // Create a latch to synchronize threads

            SwingUtilities.invokeLater(() -> {
                DatabasePusher db = new DatabasePusher();

                // Add a window listener to the DatabasePusher window
                db.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        latch.countDown(); // Signal that the window is closed
                    }
                });

                System.out.println("[General] dataset.json exists, starting update...");
            });

            try {
                latch.await(); // Wait until the latch is counted down
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                databaseHandler.close();
                databaseHandler = new DatabaseHandler();
            }
        // If not we check for updates
        } else {
            final CountDownLatch latch = new CountDownLatch(1); // Create a latch to synchronize threads

            SwingUtilities.invokeLater(() -> {
                UpdateGUI updater = new UpdateGUI(true);

                // Add a window listener to the UpdateGUI window
                updater.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        latch.countDown(); // Signal that the window is closed
                    }
                });
            });

            try {
                latch.await(); // Wait until the latch is counted down
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /* #######################################
         * #      PROTECTION INITIALIZATION      #
         * #######################################
         */
        
        downloadWatcherThread = new Thread(() -> {
            try {
                downloadWatcher = new DownloadWatcher();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        downloadWatcherThread.start();
        GUI_Main.main(args);
        
        
        // File file = new File("D:\\Users\\louis\\Downloads\\eicar.com");
        // SignatureUtilities signatureUtilities = new SignatureUtilities(file);
        // System.out.println("Analysis status: "+databaseHandler.isHashInDatabase(signatureUtilities.getMD5()));

        // databaseHandler.listHashes();


        /* ######################################
         * #         END OF THE PROGRAM         #
         * ######################################
         * 
         */
        try {
            downloadWatcherThread.join();
        } catch (InterruptedException e) {
            System.out.println("[General] Thread interrupted by user.");
        } catch (Exception e) {
            System.out.println("[General] An error occured while closing the thread.\nHowever, the pogoram will continue to run.");
        }
        
        System.out.println("[General] Closing database connection...");
        databaseHandler.close();
    }

    public static DatabaseHandler getDatabaseHandler() {
        return databaseHandler;
    }

    public void stopRealTimeProtection() {
        downloadWatcherThread.interrupt();
    }

    public static ConfigurationHandler getConfigurationHandler() {
        return configurationHandler;
    }

    public static void restartProgram() {
        String os = System.getProperty("os.name").toLowerCase();

        try {
            ProcessBuilder processBuilder;
            if (os.contains("win")) {
                // Windows
                processBuilder = new ProcessBuilder("cmd", "/c", "java -jar program.jar");
            } else if (os.contains("mac")) {
                // macOS
                processBuilder = new ProcessBuilder("java", "-jar", "program.jar");
            } else {
                // Linux
                processBuilder = new ProcessBuilder("sh", "-c", "java -jar program.jar");
            }

            processBuilder.inheritIO();
            processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Exit the current program
        System.exit(0);
    }

    public void setProtectionStatus(boolean status) {
        downloadWatcher.setRealTimeProtection(status);
    }
}
