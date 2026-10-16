package util;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 * Couleurs et styles communs de l'application (thème orange).
 */
public class Theme {

    /** Orange principal de l'application (#C2570C). */
    public static final Color ORANGE = new Color(0xC2, 0x57, 0x0C);

    /** Fond général (#FFF7ED). */
    public static final Color FOND = new Color(0xFF, 0xF7, 0xED);

    /** Couleur du texte. */
    public static final Color TEXTE = new Color(0x3B, 0x2A, 0x1A);

    /** Fond des produits à consommer bientôt (orange clair). */
    public static final Color BIENTOT = new Color(255, 226, 184);

    /** Fond des produits périmés (rouge clair). */
    public static final Color PERIME = new Color(255, 201, 192);

    /** Fond orange très clair (bandeau « tout est frais »). */
    public static final Color ORANGE_CLAIR = new Color(255, 237, 213);

    /** Fond des lignes sélectionnées dans un tableau. */
    public static final Color SELECTION = new Color(255, 214, 170);

    /** Police commune de l'application. */
    public static final Font POLICE = new Font("SansSerif", Font.PLAIN, 13);

    /** Police des titres (gras, ~22). */
    public static final Font TITRE = new Font("SansSerif", Font.BOLD, 22);

    /**
     * Crée une petite étiquette colorée (pastille) pour la légende.
     */
    public static JLabel pastille(String texte, Color fond) {
        JLabel label = new JLabel(texte);
        label.setOpaque(true);
        label.setBackground(fond);
        label.setForeground(TEXTE);
        label.setFont(POLICE);
        label.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return label;
    }

    /**
     * Applique le style commun de l'application à un tableau
     * (police, hauteur de ligne, couleurs de sélection).
     */
    public static JTable tableau(JTable table) {
        table.setFont(POLICE);
        table.setRowHeight(24);
        table.setForeground(TEXTE);
        table.setSelectionBackground(SELECTION);
        table.setGridColor(new Color(0xE0, 0xD5, 0xC5));
        table.getTableHeader().setFont(POLICE);
        table.getTableHeader().setReorderingAllowed(false);
        return table;
    }

    /**
     * Place un JTable dans un JScrollPane avec un style sobre.
     */
    public static JScrollPane defilement(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(0xE0, 0xD5, 0xC5)));
        return scroll;
    }
}
