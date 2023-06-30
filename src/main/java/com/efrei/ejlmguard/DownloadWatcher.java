package com.efrei.ejlmguard;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import static java.nio.file.StandardWatchEventKinds.*;


import com.efrei.ejlmguard.GUI.DetectorName;
import com.efrei.ejlmguard.GUI.ThreatDetectedGUI;

public class DownloadWatcher { // implements Runnable {

    private boolean continueOperations = true;
    private boolean realTimeProtection = true;
    private final String DOWNLOAD_DIR;
    private File endFile;
    private final String[] bannedExtensions = {".qrt", ".part", ".crdownload"};
    //private SignatureUtilities signatureUtilities;
    private DatabaseHandler databaseHandler;

    public DownloadWatcher() throws InterruptedException {
        // J'initialise le chemin du dossier de téléchargement
        this.DOWNLOAD_DIR = System.getProperty("user.home");// + "/Downloads";
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
        System.out.println("Nouveau fichier téléchargé : " + filePath.toString());

        // Convertir le chemin du fichier en objet File
        File file = filePath.toFile();

        // On attend une seconde pour que le fichier soit libre
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Si le fichier est occupé, on attend 1 seconde
        while (file.renameTo(file) == false) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
            System.out.println("[REALTIME] Début de l'indexage, cela peut prendre quelques minutes...");
            recursiveRegister(dir, watchService);
            System.out.println("[REALTIME] Fin de l'indexage. Tout est prêt !\n");
            System.out.println("Surveillance du répertoire " + DOWNLOAD_DIR + " en cours...");
            
            while (continueOperations) {
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException e) {
                    break; // Sortir de la boucle si le thread est interrompu
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == ENTRY_CREATE || kind == ENTRY_MODIFY || kind == ENTRY_DELETE) {
                        WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                        Path filePath = dir.resolve(pathEvent.context());
                        handleDownloadedFile(filePath);
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    break; // Sortir de la boucle si la clé n'est plus valide
                }
            }
            
            System.out.println("Arrêt de la surveillance du répertoire " + DOWNLOAD_DIR);
            watchService.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Nous enrengistrons les sous répertoire de manière récursive afin d'ignorer AppData si sous Windows
    private void recursiveRegister(Path dir, WatchService watchService) throws IOException {
        if (!isWindows() || !isAppDataDirectory(dir)) {
            try {
                if (!isMacHiddenFile(dir)) {
                    dir.register(watchService, ENTRY_CREATE);
                }
            } catch (AccessDeniedException e) {
                // Ignorer le répertoire si l'accès est refusé
                return;
            }
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path subPath : stream) {
                if (Files.isDirectory(subPath)) {
                    try {
                        recursiveRegister(subPath, watchService);
                    } catch (AccessDeniedException e) {
                        // Ignorer le sous-répertoire si l'accès est refusé
                    }
                }
            }
        }
    }


    // SPECIFITE PAR SYSTEME D'EXPLOITATION

    // Détecte si le système d'exploitation est Windows
    private boolean isWindows() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("windows");
    }

    // Si nous sommes sous Windows, nous ignorons le dossier AppData
    private boolean isAppDataDirectory(Path dir) {
        String path = dir.toString();
        return path.contains("AppData");
    }

    private boolean isMacHiddenFile(Path file) throws IOException {
    if (isMac()) {
        return Files.isHidden(file);
    }
    return false;
}

// Détecte si le système d'exploitation est macOS
private boolean isMac() {
    String osName = System.getProperty("os.name").toLowerCase();
    return osName.contains("mac");
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
            Files.write(endFile.toPath(), "IGNORE AND DELETE THIS FILE IN FOUND".getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
