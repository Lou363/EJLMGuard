package com.efrei.ejlmguard;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.efrei.ejlmguard.GUI.CaptiveGUI;
import com.efrei.ejlmguard.GUI.DatabasePusher;
import com.efrei.ejlmguard.GUI.GUI_swing;
import com.efrei.ejlmguard.GUI.UpdateGUI;


public class App {
    private static DatabaseHandler databaseHandler;
    private static ConfigurationHandler configurationHandler;
    private static Thread downloadWatcherThread;
    private static DownloadWatcher downloadWatcher;

    public static void main(String[] args) throws IOException, InterruptedException {

        /* ###################################
         * #         INITIALIZATION          #
         * ###################################
         */
        if (isLocked()) {
            // Another instance is already running, display a message dialog
            JOptionPane.showMessageDialog(null, "Another instance of " + APP_TITLE + " is already running.", APP_TITLE, JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }
        configurationHandler = new ConfigurationHandler();
        // databaseHandler = new DatabaseHandler();
        
        databaseHandler = new DatabaseHandler();

        /* ######################################
         * #             CAPTIVE UNLOCKING      #
         * ######################################
         */
        final CountDownLatch captiveLatch = new CountDownLatch(1); // Create a latch to synchronize threads

        SwingUtilities.invokeLater(() -> {
            CaptiveGUI captiveGUI = new CaptiveGUI();
            // Add a window listener to the DatabasePusher window
            captiveGUI.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    captiveLatch.countDown(); // Signal that the window is closed
                }
            });
        });

            try {
                captiveLatch.await(); // Wait until the latch is counted down
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

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
        
        downloadWatcher = new DownloadWatcher();
        downloadWatcherThread = new Thread(() -> {
            try {
                downloadWatcher.run();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        downloadWatcherThread.start();
        Thread.sleep(1000);


        if(downloadWatcher == null){
            System.out.println("[FATAL] Download watcher thread failed to start.");
            removeLock();
            System.exit(1);

        }
        final CountDownLatch latch_interafce = new CountDownLatch(1); // Create a latch to synchronize threads
        SwingUtilities.invokeLater(() -> {
                //DatabasePusher db = new DatabasePusher();
                GUI_swing gui_swing = new GUI_swing();

                // Add a window listener to the DatabasePusher window
                gui_swing.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        latch_interafce.countDown(); // Signal that the window is closed
                    }
                });
            });

            try {
                latch_interafce.await(); // Wait until the latch is counted down
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("[General] GUI closed.");

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
            System.out.println("[General] An error occurred while closing the thread.\nHowever, the program will continue to run.");
        }

        // I remove the close.txt file in the OS's download folder
        try {
            Files.deleteIfExists(Paths.get(System.getProperty("user.home") + "/Downloads/stop.txt"));
        } catch (IOException e) {
            System.out.println("[General] An error occurred while deleting the stop.txt file.");
        }
        
        System.out.println("[General] Closing database connection...");
        databaseHandler.close();
        removeLock();
        System.out.println("[GENERAL] Released lock successfully.");
        System.exit(0);
    }

    public static DatabaseHandler getDatabaseHandler() {
        return databaseHandler;
    }

    public static void setDownloadWatcher(DownloadWatcher newDownloadWatcher){
        System.out.println("Registered function.");
        App.downloadWatcher = newDownloadWatcher;
    }

    public static void setDownloadWatcher(Thread newDownloadWatcherThread){
        downloadWatcherThread = newDownloadWatcherThread;
    }

    public static DownloadWatcher getDownloadWatcher(){
        return downloadWatcher;
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
        removeLock();
        System.exit(0);
    }

    public static void setProtectionStatus(boolean status) {
        downloadWatcher.setRealTimeProtection(status);
    }

    private static final String LOCK_FILE = "ejlm.lock";
    private static final String APP_TITLE = "ejlm";
    
    // This allows to check if another instance of the program is already running
    private static boolean isLocked() {
        try {
            File file = new File(LOCK_FILE);
            // Print the path of the lock file
            if (file.createNewFile()) {
                return false;  // Lock file created, no other instance is running
            } else {
                return true;   // Lock file already exists, another instance is running
            }
        } catch (IOException e) {
            e.printStackTrace();
            return true;  // Error occurred, assume another instance is running
        }
    }

    private static void removeLock() {
        File file = new File(LOCK_FILE);
        if (file.exists()) {
            file.delete();
        }
    }
}
