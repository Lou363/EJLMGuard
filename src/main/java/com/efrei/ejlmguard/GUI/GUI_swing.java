package com.efrei.ejlmguard.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


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

    public GUI_swing() {
        setTitle("EJLMGuard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        titleLabel = new JLabel("EJLMGuard");
        titleLabel.setFont(new Font("Arial Bold Italic", Font.PLAIN, 30));
        topPanel.add(titleLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Center Panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(centerPanel, BorderLayout.CENTER);

        // Left Panel
        JPanel leftPanel = new JPanel(null);
        leftPanel.setPreferredSize(new Dimension(308, 257));
        centerPanel.add(leftPanel, BorderLayout.WEST);

        analyseLabel = new JLabel("Analyse de fichiers");
        analyseLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        analyseLabel.setBounds(68, 14, 200, 25);
        leftPanel.add(analyseLabel);

        selectFileLabel = new JLabel("Sélectionner le fichier à analyser");
        selectFileLabel.setBounds(14, 48, 200, 17);
        leftPanel.add(selectFileLabel);

        enterPathLabel = new JLabel("ou entrez le chemin menant au fichier");
        enterPathLabel.setBounds(14, 69, 228, 17);
        leftPanel.add(enterPathLabel);

        pathTextField = new JTextField();
        pathTextField.setBounds(14, 116, 271, 25);
        leftPanel.add(pathTextField);

        browseButton = new JButton("Parcourir");
        browseButton.setBounds(204, 44, 77, 21);
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findFile();
            }
        });
        leftPanel.add(browseButton);

        analyseButton = new JButton("Analyser");
        analyseButton.setBounds(100, 203, 98, 40);
        analyseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                analyse();
            }
        });
        leftPanel.add(analyseButton);

        JLabel pathFormatLabel = new JLabel("sous la forme ..\\..\\..");
        pathFormatLabel.setBounds(14, 86, 228, 17);
        leftPanel.add(pathFormatLabel);

        watcherCheckBox = new JCheckBox("Activée");
        watcherCheckBox.setBounds(164, 158, 100, 20);
        watcherCheckBox.setSelected(true);
        leftPanel.add(watcherCheckBox);

        JCheckBox virusTotalCheckBox = new JCheckBox("Activer l'analyse web VirusTotal");
        virusTotalCheckBox.setBounds(57, 156, 200, 20);
        leftPanel.add(virusTotalCheckBox);

        // Right Panel
        JPanel rightPanel = new JPanel(null);
        rightPanel.setPreferredSize(new Dimension(316, 160));
        centerPanel.add(rightPanel, BorderLayout.EAST);

        resultLabel = new JLabel("Résultat");
        resultLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        resultLabel.setBounds(122, -1, 200, 25);
        rightPanel.add(resultLabel);

        resultTextArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        scrollPane.setBounds(0, 29, 316, 87);
        rightPanel.add(scrollPane);

        settingsLabel = new JLabel("Paramètres");
        settingsLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        settingsLabel.setBounds(108, 128, 200, 25);
        rightPanel.add(settingsLabel);

        JLabel watcherLabel = new JLabel("Surveillance en temps réel :");
        watcherLabel.setBounds(14, 158, 200, 17);
        rightPanel.add(watcherLabel);

        stopButton = new JButton("Arreter");
        stopButton.setBounds(125, 217, 63, 32);
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shutDown();
            }
        });
        rightPanel.add(stopButton);

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

    private void analyse() {
        // Logic for analysis
    }

    private void shutDown() {
        // Logic for shutting down
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
