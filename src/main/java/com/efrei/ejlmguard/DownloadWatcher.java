package com.efrei.ejlmguard;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.Desktop;

import com.efrei.ejlmguard.GUI.DetectorName;
import com.efrei.ejlmguard.GUI.ThreatDetectedGUI;

public class DownloadWatcher { // implements Runnable {

    private boolean continueOperations;
    private boolean realTimeProtection = true;
    private final String DOWNLOAD_DIR;
    private File endFile;
    //private SignatureUtilities signatureUtilities;
    private DatabaseHandler databaseHandler;

    public DownloadWatcher() throws InterruptedException {
        // J'initialise le chemin du dossier de téléchargement
        this.DOWNLOAD_DIR = System.getProperty("user.home") + "/Downloads";
        this.continueOperations = true;

        //signatureUtilities = new SignatureUtilities();
        this.databaseHandler = App.getDatabaseHandler();
    }


    // Fonction qui déplace le fichier dans le dossier de quarantaine
    public void moveToQuarantine(File file){
        // Déplacer le fichier dans un dossier sur le bureau
            
        String desktopPath = System.getProperty("user.home") + "/Desktop";
        String warningDirPath = desktopPath + "/warning";

        // Créer le dossier "warning" sur le bureau s'il n'existe pas déjà
        File warningDir = new File(warningDirPath);
        if (!warningDir.exists()) {
             warningDir.mkdir();
        }

        // Déplacer le fichier dans le dossier "warning" sur le bureau
        // Le nouveau chemin du fichier déplacé est construit en ajoutant un timestamp au nom du fichier
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = now.format(formatter);
        File newFilePath = new File(warningDirPath + "/" + timestamp + "_" + file.getName() + ".qrt");
        if (file.renameTo(newFilePath)) {
            System.out.println("Le fichier a été déplacé dans le dossier restreint : " + newFilePath.getAbsolutePath());
        } else {
            System.out.println("Impossible de déplacer le fichier dans le dossier restreint.");
        }
    }

    public void handleDownloadedFile(Path filePath) {
        if(!realTimeProtection) {
            return;
        }
        System.out.println("Nouveau fichier téléchargé : " + filePath.toString());

        // Convertir le chemin du fichier en objet File
        File file = filePath.toFile();

        // On attend une seconde pour que le fichier soit libre
        try {
            Thread.sleep(1000);
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
            moveToQuarantine(file);

        } else {
            System.out.println("Le fichier est sûr.");
        }

    }

    public void run() {
        try {
            // Crée un objet WatchService
            WatchService watchService = FileSystems.getDefault().newWatchService();

            // Enregistre le répertoire pour la surveillance des événements de création
            Path dir = Paths.get(DOWNLOAD_DIR);
            dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            System.out.println("Surveillance du répertoire " + DOWNLOAD_DIR + " en cours...");

            // Boucle infinie pour attendre les événements de création de fichiers
            while (continueOperations) {
                // Attend les événements
                WatchKey key = watchService.take();

                // Parcourt tous les événements reçus
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    // Vérifie si un nouveau fichier a été créé
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        // Récupère le nom du fichier créé
                        WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                        Path filePath = dir.resolve(pathEvent.context());

                        // Effectue une action sur le fichier téléchargé
                        handleDownloadedFile(filePath);
                    }
                }

                // Réinitialise la clé pour la prochaine itération
                boolean valid = key.reset();
                if (!valid) {
                    // La clé n'est plus valide, arrête la surveillance
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setRealTimeProtection(boolean status) {
        this.realTimeProtection = status;
        if(realTimeProtection) 
            System.out.println("[EJLMGuard] Protection en temps réel activée.");
        else 
            System.out.println("[EJLMGuard] Protection en temps réel désactivée.");
    }

    public void stop() {
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
