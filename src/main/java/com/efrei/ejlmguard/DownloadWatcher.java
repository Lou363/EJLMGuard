package com.efrei.ejlmguard;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class DownloadWatcher implements Runnable {

    private static final String DOWNLOAD_DIR = "/Users/emilia/Downloads";
    //private SignatureUtilities signatureUtilities;
    private DatabaseHandler databaseHandler;

    public DownloadWatcher() throws InterruptedException {

        //signatureUtilities = new SignatureUtilities();
        databaseHandler = App.getDatabaseHandler();

        try {
            // Crée un objet WatchService
            WatchService watchService = FileSystems.getDefault().newWatchService();

            // Enregistre le répertoire pour la surveillance des événements de création
            Path dir = Paths.get(DOWNLOAD_DIR);
            dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            System.out.println("Surveillance du répertoire " + DOWNLOAD_DIR + " en cours...");

            // Boucle infinie pour attendre les événements de création de fichiers
            while (true) {
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

    public void handleDownloadedFile(Path filePath) {
        System.out.println("Nouveau fichier téléchargé : " + filePath.toString());

        // Convertir le chemin du fichier en objet File
        File file = filePath.toFile();

        // Créer une instance de SignatureUtilities pour le nouveau fichier
        SignatureUtilities signatureUtils = new SignatureUtilities(file);

        // Récupérer le MD5 du fichier
        String md5 = signatureUtils.getMD5();
        System.out.println("MD5 : " + md5);
        // Vérifier si le fichier est sûr en utilisant isHashInDatabase
        boolean isSafe = databaseHandler.isHashInDatabase(md5);

        if (isSafe) {
            System.out.println("Le fichier n'est pas sûr.");
        } else {
            System.out.println("Le fichier est sûr.");
        }

    }

    @Override
    public void run() {
        try {
            new DownloadWatcher();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
