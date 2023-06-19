package com.efrei.ejlmguard;


import java.awt.Font;
import java.awt.event.*;
import javax.swing.*;

public class GUI_ecranChargement extends JFrame {


    JLabel lblLastname, lblfirstname, lblmissed,lbltempted;
    JProgressBar bar;

    GUI_ecranChargement(String string){
        JPanel panel = new JPanel();

        panel.add(lblfirstname = new JLabel("Analyse de " + string + " en cours\n"));
        panel.add(bar = new JProgressBar());

        lblfirstname.setFont(new Font("Arial", Font.PLAIN, 18));

        bar.setIndeterminate(true);
        add(panel);

    }
}
