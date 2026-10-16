package view;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import util.Theme;

public class PanneauEnConstruction extends JPanel {

    public PanneauEnConstruction(String titre, String message) {
        setLayout(new BorderLayout());
        setBackground(Theme.FOND);
        setOpaque(true);

        JPanel centre = new JPanel();
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
        centre.setBackground(Theme.FOND);

        JLabel labelTitre = new JLabel(titre);
        labelTitre.setFont(Theme.TITRE);
        labelTitre.setForeground(Theme.ORANGE);
        labelTitre.setAlignmentX(CENTER_ALIGNMENT);

        JLabel labelMessage = new JLabel(message);
        labelMessage.setFont(Theme.POLICE);
        labelMessage.setForeground(Theme.TEXTE);
        labelMessage.setAlignmentX(CENTER_ALIGNMENT);

        centre.add(Box.createVerticalGlue());
        centre.add(labelTitre);
        centre.add(Box.createVerticalStrut(8));
        centre.add(labelMessage);
        centre.add(Box.createVerticalGlue());

        add(centre, BorderLayout.CENTER);
    }
}
