package com.efrei.ejlmguard;

import java.time.format.DateTimeFormatter;

import java.time.LocalDateTime;

import java.io.File;

public class ThreatQuarantineHandler {
    public static void moveToQuarantine(File file){
        // Déplacer le fichier dans un dossier sur le bureau
            
        String desktopPath = System.getProperty("user.home") + "/Desktop";
        String warningDirPath = desktopPath + "/EJLMGuard_quarantine";

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
}
