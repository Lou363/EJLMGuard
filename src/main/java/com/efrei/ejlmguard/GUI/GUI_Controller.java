package com.efrei.ejlmguard.GUI;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

import com.efrei.ejlmguard.SignatureUtilities;
import com.efrei.ejlmguard.DatabaseHandler;
import com.efrei.ejlmguard.App;



import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

public class GUI_Controller {

    DatabaseHandler db = App.getDatabaseHandler();

  @FXML
  private Button Analyser;

  @FXML
  private Button parcourir;

  @FXML
  private TextField placeholder;

  @FXML
  void findFile(ActionEvent event) {

    JFileChooser choose = new JFileChooser(
    FileSystemView
    .getFileSystemView()
    .getHomeDirectory()
    );

        // Ouvrez le fichier
    int res = choose.showOpenDialog(null);
    // Enregistrez le fichier
    // int res = choose.showSaveDialog(null);
    if (res == JFileChooser.APPROVE_OPTION) {
      File file = choose.getSelectedFile();
      placeholder.setText(file.getAbsolutePath());
      
    }
  }

  public File getFile(String Path){
    try {
     
      String adapted = Path.replaceAll("\\\\",  "\\\\\\\\");
      String[] name = adapted.split("\\\\");
      File f = new File(adapted);
      FileInputStream fis = new FileInputStream(f);

      
      //JOptionPane.showMessageDialog(null, "Analyse de " + name[name.length - 1] +  " en cours");
       JFrame frame = new GUI_ecranChargement(name[name.length - 1]);
        frame.setTitle("");
        frame.setLocationRelativeTo(null);
        frame.setSize(350, 150);
        frame.setVisible(true);

        return f;
      /*File f = new File(adapted);
      FileWriter fw = new FileWriter(f, true); //filewriiter allows to write to the directory
      BufferedWriter bw = new BufferedWriter(fw); //allow to buffer the information from the fw
        
      bw.write("z\n");
      bw.write("------------------------------------------------\n");
      bw.close();*/
    } catch (FileNotFoundException e) {
    // TODO Auto-generated catch block
      JOptionPane.showMessageDialog(null, "File not found");
      return null;
    }
    
  }

  @FXML
  void analyse(ActionEvent event) {
    if(placeholder.getText() == "" ){
      JOptionPane.showMessageDialog(null, "Text field empty");
    }else{
      File f = getFile(placeholder.getText());
      SignatureUtilities si = new SignatureUtilities(f);
      System.out.println(si.getMD5() + "\n" + si.getSha1() + "\n" + si.getSha256());

      if(db.isHashInDatabase(si.getMD5())){
        System.out.println(db.isHashInDatabase(si.getMD5()));
      }else{
        System.out.println("nope");
        System.out.println(db.findDescription(si.getMD5()));
        System.out.println(db.findDescription(si.getSha1()));
        System.out.println(db.findDescription(si.getSha256()));
      }

    }
  }

  

}

