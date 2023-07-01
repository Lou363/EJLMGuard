package com.efrei.ejlmguard;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;

import com.efrei.ejlmguard.GUI.DetectorName;
import com.efrei.ejlmguard.GUI.ThreatDetectedGUI;

public class DownloadWatcher {

    private boolean continueOperations = true;
    private boolean realTimeProtection = true;
    private final String DOWNLOAD_DIR;
    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_DELAY_MS = 1000;
    private File endFile;
    private final String[] bannedExtensions = {".qrt", ".part", ".crdownload"};
    //private SignatureUtilities signatureUtilities;
    private DatabaseHandler databaseHandler;

    public DownloadWatcher() throws InterruptedException {
        // J'initialise le chemin du dossier de téléchargement
        this.DOWNLOAD_DIR = System.getProperty("user.home") + "/Downloads";
        this.continueOperations = true;

        //signatureUtilities = new SignatureUtilities();
        this.databaseHandler = App.getDatabaseHandler();
    }

    /* ##################################################
     * #           ANALYSE EN CAS DE MOUVEMENT          #
     * ##################################################
     */
    public void handleDownloadedFile(Path filePath) {
        if(!realTimeProtection) {
            return;
        }
        // If the extension is in the bannedExtensions array, we don't check the file
        for (String extension : bannedExtensions) {
            if (filePath.toString().endsWith(extension)) {
                return;
            }
        }
        if(!continueOperations) {
            return;
        }
        
        System.out.println("Nouveau fichier téléchargé : " + filePath.toString());

        // Convertir le chemin du fichier en objet File
        File file = filePath.toFile();

        // Wait for the file to be available
        boolean fileAvailable = false;
        int retryCount = 0;
        while (!fileAvailable && retryCount < MAX_RETRY_COUNT) {
            try {
                // Check if the file can be opened
                try (FileChannel channel = FileChannel.open(filePath, StandardOpenOption.WRITE)) {
                    // The file is available
                    fileAvailable = true;
                }
            } catch (NoSuchFileException e) {
                // File not found, wait and retry
                retryCount++;
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // If the file is still not available, handle the error
        if (!fileAvailable) {
            System.err.println("File not available: " + filePath);
            return;
        }
        // Créer une instance de SignatureUtilities pour le nouveau fichier
        SignatureUtilities signatureUtils = new SignatureUtilities(file);

        // Récupérer le MD5 du fichier
        String md5 = signatureUtils.getMD5();
        // Vérifier si le fichier est sûr en utilisant isHashInDatabase
        boolean isindb = databaseHandler.isHashInDatabase(md5);

        if (isindb) {
            System.out.println("Le fichier n'est pas sûr.");
            new ThreatDetectedGUI(databaseHandler.findDescription(md5), filePath.toString(), DetectorName.REALTIME);

            // Appeler fonction qui déplace le fichier dans le dossier de quarantaine
            //moveToQuarantine(file);

        } else {
            System.out.println("Le fichier est sûr.");
        }

    }

    /* ##################################################
     * #           SURVEILLANCE DU DOSSIER              #
     * ##################################################
     */

    public void run() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path dir = Paths.get(DOWNLOAD_DIR);
            dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            System.out.println("Surveillance du répertoire " + DOWNLOAD_DIR + " en cours...");
            while (continueOperations) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                        Path filePath = dir.resolve(pathEvent.context());
                        handleDownloadedFile(filePath);
                    }
                }
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    /* ##################################################
     * #        FIN DE LA SURVEILLANCE DU DOSSIER       #   
     * ##################################################
     */
    // Cette fonction permet de mettre fin à la surveillance du dossier de téléchargement
    public void setRealTimeProtection(boolean status) {
        this.realTimeProtection = status;
        if(realTimeProtection) 
            System.out.println("[EJLMGuard] Protection en temps réel activée.");
        else 
            System.out.println("[EJLMGuard] Protection en temps réel désactivée.");
    }

    public void stop() 
    {
        System.out.println("[EJLMGuard] Arrêt de la protection en temps réel demandé.");
        this.continueOperations = false;
        // I make an event in the download folder to wake up the thread
        // I create a file and delete it to wake up the thread
        try {
            endFile = new File(DOWNLOAD_DIR + "/stop.txt");
            endFile.createNewFile();
            // I write "IGNORE AND DELETE THIS FILE IN FOUND" in the file
            Files.write(endFile.toPath(), "IGNORE AND DELETE THIS FILE IN FOUND \nChecksum: ubisdbusdbui".getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
