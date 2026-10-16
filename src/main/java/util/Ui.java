package util;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JButton;

/**
 * Petits utilitaires d'interface (boutons) pour garder un style cohérent.
 */
public class Ui {

    /**
     * Bouton plein : fond orange, texte blanc.
     */
    public static JButton boutonPlein(String texte) {
        JButton bouton = new JButton(texte);
        bouton.setBackground(Theme.ORANGE);
        bouton.setForeground(Color.WHITE);
        bouton.setOpaque(true);
        bouton.setBorderPainted(false);
        bouton.setFocusPainted(false);
        return bouton;
    }

    /**
     * Bouton contour : fond clair, contour et texte orange.
     */
    public static JButton boutonContour(String texte) {
        JButton bouton = new JButton(texte);
        bouton.setBackground(Theme.FOND);
        bouton.setForeground(Theme.ORANGE);
        bouton.setOpaque(true);
        bouton.setBorder(BorderFactory.createLineBorder(Theme.ORANGE));
        bouton.setFocusPainted(false);
        return bouton;
    }
}
