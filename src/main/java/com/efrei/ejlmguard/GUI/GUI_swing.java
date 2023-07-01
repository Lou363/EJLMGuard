package com.efrei.ejlmguard.GUI;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

import com.efrei.ejlmguard.App;
import com.efrei.ejlmguard.DatabaseHandler;
import com.efrei.ejlmguard.DownloadWatcher;
import com.efrei.ejlmguard.SignatureUtilities;
import com.efrei.ejlmguard.WebAnalysis;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;


public class GUI_swing extends JFrame {
    private JLabel titleLabel;
    private JLabel analyseLabel;
    private JLabel selectFileLabel;
    private JLabel enterPathLabel;
    private JTextField pathTextField;
    private JButton browseButton;
    private JButton analyseButton;
    private JLabel resultLabel;
    private JTextArea resultTextArea;
    private JLabel settingsLabel;
    private JCheckBox watcherCheckBox;
    private JButton stopButton;

    DownloadWatcher downloadwatcher = App.getDownloadWatcher();
    DatabaseHandler db = App.getDatabaseHandler();
    boolean isMalicious = false;
    boolean VirusTotal = false;
    String virusName;

    public GUI_swing() {
        setTitle("EJLMGuard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(650, 373));

        // Set OS Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Top Panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        titleLabel = new JLabel("EJLMGuard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial Bold Italic", Font.PLAIN, 30));
        topPanel.add(titleLabel, BorderLayout.CENTER);
        titleLabel.setBorder(BorderFactory.createLineBorder(Color.black));
        add(topPanel, BorderLayout.NORTH);

        // Center Panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        //centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(centerPanel, BorderLayout.CENTER);

        // Left Panel
        JPanel leftPanel = new JPanel(null);
        leftPanel.setPreferredSize(new Dimension(310, 257));
        centerPanel.add(leftPanel, BorderLayout.WEST);


        analyseLabel = new JLabel("Analyse de fichiers");
        analyseLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        analyseLabel.setBounds(68, 14, 200, 15);
        leftPanel.add(analyseLabel);

        selectFileLabel = new JLabel("Sélectionner le fichier à analyser");
        selectFileLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        selectFileLabel.setBounds(14, 48, 200, 17);
        leftPanel.add(selectFileLabel);

        enterPathLabel = new JLabel("ou entrez le chemin menant au fichier");
        enterPathLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        enterPathLabel.setBounds(14, 69, 228, 17);
        leftPanel.add(enterPathLabel);

        pathTextField = new JTextField();
        pathTextField.setBounds(14, 116, 271, 25);
        leftPanel.add(pathTextField);

        browseButton = new JButton("Parcourir");
        browseButton.setBounds(204, 44, 77, 21);
        browseButton.setFont(new Font("Arial", Font.PLAIN, 10));
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                pathTextField.setText(file.getAbsolutePath());
                
                }
                            //findFile();
                        }
                    });
        leftPanel.add(browseButton);

        analyseButton = new JButton("Analyser");
        analyseButton.setBounds(100, 203, 98, 40);
        analyseButton.setFont(new Font("Arial", Font.PLAIN, 12));
        analyseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                analyse();
            }
        });
        leftPanel.add(analyseButton);

        JLabel pathFormatLabel = new JLabel("sous la forme ..\\..\\..");
        pathFormatLabel.setBounds(14, 86, 228, 17);
        pathFormatLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        leftPanel.add(pathFormatLabel);



        JCheckBox virusTotalCheckBox = new JCheckBox("Activer l'analyse web VirusTotal");
        virusTotalCheckBox.setBounds(57, 156, 200, 20);
        virusTotalCheckBox.setFont(new Font("Arial", Font.PLAIN, 12));
        leftPanel.add(virusTotalCheckBox);
        virusTotalCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                if (virusTotalCheckBox.isSelected()){
                    VirusTotal = true;
                }
                else{
                    VirusTotal = false;
                }
            }
        });


        // Right Panel
        JPanel rightPanel = new JPanel(null);
        rightPanel.setPreferredSize(new Dimension(340, 160));

        centerPanel.add(rightPanel, BorderLayout.EAST);
        watcherCheckBox = new JCheckBox("Activée");
        watcherCheckBox.setBounds(164, 158, 100, 20);
        watcherCheckBox.setFont(new Font("Arial", Font.PLAIN, 12));
        watcherCheckBox.setSelected(true);
        rightPanel.add(watcherCheckBox);

        watcherCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                if (watcherCheckBox.isSelected()){
                    watcherCheckBox.setText("Activée");
                    App.setProtectionStatus(true);
                }
                else{
                    watcherCheckBox.setText("Désactivée");
                    App.setProtectionStatus(false);
                }
            }
        });

        resultLabel = new JLabel("Résultat");
        resultLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        resultLabel.setBounds(122, -1, 200, 25);
        rightPanel.add(resultLabel);

        resultTextArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        resultTextArea.setBorder(BorderFactory.createLineBorder(Color.red));
        scrollPane.setBounds(0, 29, 340, 87);
        rightPanel.add(scrollPane);

        settingsLabel = new JLabel("Paramètres");
        settingsLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        settingsLabel.setBounds(108, 128, 200, 25);
        rightPanel.add(settingsLabel);

        JLabel watcherLabel = new JLabel("  Surveillance en temps réel :");
        watcherLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        watcherLabel.setBounds(14, 158, 200, 17);
        rightPanel.add(watcherLabel);

        stopButton = new JButton("Arreter");
        stopButton.setBounds(125, 205, 98, 40);
        analyseButton.setFont(new Font("Arial", Font.PLAIN, 12));        
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shutDown();
            }
        });
        rightPanel.add(stopButton);


        leftPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        rightPanel.setBorder(BorderFactory.createLineBorder(Color.black));

        // Add vertical struts
Box verticalBox = Box.createVerticalBox();
verticalBox.add(Box.createVerticalStrut(10));
verticalBox.add(this.getContentPane());
verticalBox.add(Box.createVerticalStrut(10));
setContentPane(verticalBox);

        // Add panel border
        JPanel panelBorder = new JPanel();
        panelBorder.setLayout(new BorderLayout());
        panelBorder.add(this.getContentPane(), BorderLayout.CENTER);
        //panelBorder.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(panelBorder);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void findFile() {
        // Logic for finding the file
    }

      public File getFile(String Path){
  
     
      String adapted = Path.replaceAll("\\\\",  "\\\\\\\\");
      String[] name = adapted.split("\\\\");
      File f = new File(adapted);
      //FileInputStream fis = new FileInputStream(f);
      if(f.exists()){
      
      //JOptionPane.showMessageDialog(null, "Analyse de " + name[name.length - 1] +  " en cours");
       JFrame frame = new GUI_ecranChargement(name[name.length - 1]);
        frame.setTitle("");
        frame.setLocationRelativeTo(null);
        frame.setSize(350, 150);
        frame.setVisible(true);
        if (VirusTotal && f.length() < 32 * 1024 * 1024){
          try {
            WebAnalysis wb = new WebAnalysis(Path);
            wb.submitFileForScan();
            isMalicious = wb.getIfMalicious();
            if(isMalicious) virusName = wb.getVirusName();
            frame.setVisible(false);
          } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }else{
          try {
            if(f.length() > 32 * 1024 * 1024) JOptionPane.showMessageDialog(null, "Fichier trop volumineux \n(32 Mb maximum)");
            Thread.sleep(1500);
            frame.setVisible(false);
          } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          
        }
        return f;
      /*File f = new File(adapted);
      FileWriter fw = new FileWriter(f, true); //filewriiter allows to write to the directory
      BufferedWriter bw = new BufferedWriter(fw); //allow to buffer the information from the fw
        
      bw.write("z\n");
      bw.write("------------------------------------------------\n");
      bw.close();*/
      }else {
        throw new IllegalArgumentException("File does not exist");
        
    }


    
  }

  public String resultVirusTotal(){
    if(isMalicious){
      return "VirusTotal: malveillance détectée";
    }
    else{
      return "VirusTotal: pas de malveillance";
    }
  }

    private void analyse() {
        // Logic for analysis
        if(pathTextField.getText() == "" ){
        JOptionPane.showMessageDialog(null, "Text field empty");
        }else{

        try{
            String path = pathTextField.getText();
            File f = getFile(path);
            SignatureUtilities si = new SignatureUtilities(f);
            //System.out.println(si.getMD5() + "\n" + si.getSha1() + "\n" + si.getSha256());
        

            if(db.isHashInDatabase(si.getMD5())){

            resultTextArea.setText("\nBase de donnée: menace détectée \n" + db.findDescription(si.getMD5()) + "\n" + resultVirusTotal());

            //Result.setContent(resultTextArea);

            new ThreatDetectedGUI(db.findDescription(si.getMD5()), f.getPath(), DetectorName.USERSCAN); 
            }else{
            
                resultTextArea.setText("\nBase de donnée: pas de menace\n" + resultVirusTotal());
                            
            //resultTextArea.setText(",opiajdf`\n\nefzdn\nfzed\nzfd\nvec\n\n\nfdzsaiojuhycghdn,koskixuchyghjbzn,dklpxqoiuhcjndz;lxpsiuihy\n\ngdzyuaiszoj_dygucbn,dskxqoduygfbzchjnk,opdsç_yghdbz en,;ldxpoij\n\n\nzgvubcnjx,zsikeuzcugf\n\n\nvzgebhjskizduchy");    
            if (isMalicious) new ThreatDetectedGUI(virusName, f.getPath(), DetectorName.VIRUSTOTAL); 

            //Result.setContent(resultTextArea);
            //System.out.println(db.findDescription(si.getMD5()));
            //System.out.println(db.findDescription(si.getSha1()));
            //System.out.println(db.findDescription(si.getSha256()));
            }
        }catch(IllegalArgumentException e){
                    JOptionPane.showMessageDialog(null, e);

        }
    }
    }

    private void shutDown() {
        // Logic for shutting down
        watcherCheckBox.setEnabled(false);
        stopButton.setEnabled(false);
        downloadwatcher.stop();
        // Message to user
        JOptionPane.showMessageDialog(null, "Arrêt du service de surveillance en temps réelle effectif.\n"+
        "Veuillez fermer l'application pour quitter complètement le programme.","Arrêt du service de surveillance",JOptionPane.INFORMATION_MESSAGE);
        
        
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GUI_swing();
            }
        });
    }
}
