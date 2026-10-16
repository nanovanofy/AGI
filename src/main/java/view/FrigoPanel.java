package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import dao.CourseDAO;
import dao.ProduitDAO;
import model.ArticleCourse;
import model.Produit;
import util.Theme;
import util.Ui;

/**
 * Panneau principal affichant la liste des produits du frigo.
 */
public class FrigoPanel extends JPanel implements Rafraichissable {

    /** Seuil (en jours) à partir duquel un produit est signalé « Bientôt ». */
    private static final int SEUIL_ALERTE = 3;

    private final ProduitDAO dao = new ProduitDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final JTextField champRecherche = new JTextField(20);
    private final JLabel bandeau = new JLabel();
    private final DefaultTableModel modele;
    private final JTable table;

    // Produits actuellement affichés (pour retrouver la ligne sélectionnée)
    private List<Produit> produits = new ArrayList<>();

    public FrigoPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.FOND);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Bandeau d'alerte (indépendant de la recherche)
        bandeau.setOpaque(true);
        bandeau.setFont(Theme.POLICE);
        bandeau.setForeground(Theme.TEXTE);
        bandeau.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        // Barre du haut : recherche + boutons (à gauche), légende (à droite)
        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        gauche.setBackground(Theme.FOND);

        JLabel labelRecherche = new JLabel("Rechercher :");
        labelRecherche.setForeground(Theme.TEXTE);
        gauche.add(labelRecherche);
        gauche.add(champRecherche);

        JButton boutonAjouter = Ui.boutonPlein("Ajouter");
        JButton boutonModifier = Ui.boutonContour("Modifier");
        JButton boutonSupprimer = Ui.boutonContour("Supprimer");
        gauche.add(boutonAjouter);
        gauche.add(boutonModifier);
        gauche.add(boutonSupprimer);

        JPanel legende = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        legende.setBackground(Theme.FOND);
        legende.add(Theme.pastille("Périmé", Theme.PERIME));
        legende.add(Theme.pastille("Bientôt (" + SEUIL_ALERTE + " j)", Theme.BIENTOT));
        legende.add(Theme.pastille("Frais", Color.WHITE));

        JPanel barre = new JPanel(new BorderLayout());
        barre.setBackground(Theme.FOND);
        barre.add(gauche, BorderLayout.WEST);
        barre.add(legende, BorderLayout.EAST);

        JPanel haut = new JPanel(new BorderLayout());
        haut.setBackground(Theme.FOND);
        haut.add(bandeau, BorderLayout.NORTH);
        haut.add(barre, BorderLayout.CENTER);
        add(haut, BorderLayout.NORTH);

        // Tableau du centre
        modele = new DefaultTableModel(
                new Object[]{"Produit", "Quantité", "Péremption", "Jours restants", "État"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(modele);
        table.setForeground(Theme.TEXTE);
        table.setDefaultRenderer(Object.class, new RenduLigne());
        JScrollPane scroll = Theme.defilement(table);
        add(scroll, BorderLayout.CENTER);

        // Écouteurs
        boutonAjouter.addActionListener(e -> ajouter());
        boutonModifier.addActionListener(e -> modifier());
        boutonSupprimer.addActionListener(e -> supprimer());
        champRecherche.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                charger();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                charger();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                charger();
            }
        });

        charger();
    }

    /**
     * Recharge le tableau en fonction du texte de recherche.
     */
    public void charger() {
        try {
            produits = dao.rechercher(champRecherche.getText().trim());
            modele.setRowCount(0);
            for (Produit p : produits) {
                modele.addRow(new Object[]{
                    p.getNom(),
                    p.quantiteTexte(),
                    p.getDatePeremption() == null ? "" : p.getDatePeremption().toString(),
                    p.joursRestants(),
                    etat(p)
                });
            }
            majBandeau();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur : " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Met à jour le bandeau d'alerte à partir de tous les produits,
     * indépendamment de la recherche en cours.
     */
    private void majBandeau() throws SQLException {
        List<Produit> bientot = new ArrayList<>();
        for (Produit p : dao.rechercher("")) {
            if (p.joursRestants() <= SEUIL_ALERTE) {
                bientot.add(p);
            }
        }
        int n = bientot.size();
        long perimes = bientot.stream().filter(p -> p.joursRestants() < 0).count();

        if (perimes > 0) {
            bandeau.setText(n + " produit(s) à consommer rapidement dont "
                    + perimes + " déjà périmé(s)");
            bandeau.setBackground(Theme.PERIME);
        } else if (n > 0) {
            bandeau.setText(n + " produit(s) à consommer rapidement");
            bandeau.setBackground(Theme.BIENTOT);
        } else {
            bandeau.setText("Tout est frais : aucun produit à consommer rapidement.");
            bandeau.setBackground(Theme.ORANGE_CLAIR);
        }
    }

    /**
     * Calcule l'état d'un produit selon ses jours restants.
     */
    private String etat(Produit p) {
        long jours = p.joursRestants();
        if (jours < 0) {
            return "Périmé";
        }
        if (jours <= SEUIL_ALERTE) {
            return "Bientôt";
        }
        return "Frais";
    }

    private void ajouter() {
        ProduitDialog dialog = new ProduitDialog(SwingUtilities.getWindowAncestor(this));
        dialog.setProduit(null);
        dialog.setVisible(true);
        Produit resultat = dialog.getResultat();
        if (resultat != null) {
            try {
                dao.insert(resultat);
                charger();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Erreur : " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void modifier() {
        int ligne = table.getSelectedRow();
        if (ligne < 0) {
            JOptionPane.showMessageDialog(this,
                    "Sélectionne d'abord une ligne.",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Produit selectionne = produits.get(ligne);
        ProduitDialog dialog = new ProduitDialog(SwingUtilities.getWindowAncestor(this));
        dialog.setProduit(selectionne);
        dialog.setVisible(true);
        Produit resultat = dialog.getResultat();
        if (resultat != null) {
            try {
                dao.update(resultat);
                charger();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Erreur : " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void supprimer() {
        int ligne = table.getSelectedRow();
        if (ligne < 0) {
            JOptionPane.showMessageDialog(this,
                    "Sélectionne d'abord une ligne.",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Produit selectionne = produits.get(ligne);
        int choix = JOptionPane.showConfirmDialog(this,
                "Supprimer « " + selectionne.getNom() + " » ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);
        if (choix != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            dao.delete(selectionne.getId());
            charger();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur : " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Deuxième question : proposer d'ajouter le produit supprimé à la liste de courses
        int ajout = JOptionPane.showOptionDialog(this,
                "Ajouter « " + selectionne.getNom() + " » à la liste de courses ?",
                "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, new Object[]{"Oui", "Non"}, "Non");
        if (ajout != 0) {
            return;
        }
        try {
            if (!courseDAO.existeNonAchete(selectionne.getNom())) {
                courseDAO.insert(new ArticleCourse(selectionne.getNom(), "1", "Auto : consommé"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur : " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Colorie chaque ligne du tableau selon l'état du produit affiché.
     */
    private class RenduLigne extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object valeur,
                boolean selection, boolean focus, int ligne, int colonne) {
            Component c = super.getTableCellRendererComponent(
                    table, valeur, selection, focus, ligne, colonne);
            c.setForeground(Theme.TEXTE);

            int modeleLigne = table.convertRowIndexToModel(ligne);
            if (selection) {
                c.setBackground(Theme.SELECTION);
            } else if (modeleLigne >= 0 && modeleLigne < produits.size()) {
                long jours = produits.get(modeleLigne).joursRestants();
                if (jours < 0) {
                    c.setBackground(Theme.PERIME);
                } else if (jours <= SEUIL_ALERTE) {
                    c.setBackground(Theme.BIENTOT);
                } else {
                    c.setBackground(Color.WHITE);
                }
            }
            return c;
        }
    }
}
