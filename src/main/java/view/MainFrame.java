package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import util.Theme;
import util.Ui;

public class MainFrame extends JFrame {

    public MainFrame() {
        super("Anti-gaspillage intelligent");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setMinimumSize(new Dimension(800, 520));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.FOND);

        JPanel entete = new JPanel(new BorderLayout());
        entete.setBackground(Theme.ORANGE);
        entete.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        entete.setOpaque(true);

        JLabel titreLabel = new JLabel("Anti-gaspillage intelligent");
        titreLabel.setFont(Theme.TITRE);
        titreLabel.setForeground(Color.WHITE);

        JButton boutonQuitter = Ui.boutonContour("Quitter");
        // Le bouton contour sur fond orange : garder lisibilité
        boutonQuitter.setBackground(Color.WHITE);
        boutonQuitter.setForeground(Theme.ORANGE);
        boutonQuitter.setBorder(BorderFactory.createLineBorder(Theme.ORANGE));
        boutonQuitter.setOpaque(true);
        boutonQuitter.setFocusPainted(false);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        droite.setBackground(Theme.ORANGE);
        droite.add(boutonQuitter);

        entete.add(titreLabel, BorderLayout.WEST);
        entete.add(droite, BorderLayout.EAST);

        boutonQuitter.addActionListener(e -> {
            int choix = JOptionPane.showConfirmDialog(this,
                    "Voulez-vous vraiment quitter ?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION);
            if (choix == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        JTabbedPane onglets = new JTabbedPane();
        onglets.setFont(Theme.POLICE);
        onglets.setBackground(Theme.FOND);
        onglets.addTab("Mon frigo", new FrigoPanel());
        onglets.addTab("Courses", new CoursesPanel());
        onglets.addTab("Recettes", new RecettesPanel());

        onglets.addChangeListener(e -> {
            Component c = onglets.getSelectedComponent();
            if (c instanceof Rafraichissable r) {
                r.charger();
            }
        });

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(entete, BorderLayout.NORTH);
        getContentPane().add(onglets, BorderLayout.CENTER);
    }
}
