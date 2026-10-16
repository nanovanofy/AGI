package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import model.Produit;
import util.Theme;
import util.Ui;

/**
 * Boîte de dialogue demandant quels produits ont été utilisés pour une recette.
 * L'utilisateur coche ce qu'il a entièrement consommé : rien n'est retiré sans son accord.
 */
public class ConsommationDialog extends JDialog {

    private final List<Produit> produits;
    private final DefaultTableModel modele;

    /** Vrai uniquement si l'utilisateur a validé avec « Valider ». */
    private boolean confirme = false;

    public ConsommationDialog(Window parent, List<Produit> produits) {
        super(parent, "Retirer du frigo", ModalityType.APPLICATION_MODAL);
        this.produits = produits;

        setSize(520, 420);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Theme.FOND);
        setLayout(new BorderLayout());

        // En-tête orange + texte explicatif
        JLabel entete = new JLabel("Retirer du frigo");
        entete.setOpaque(true);
        entete.setBackground(Theme.ORANGE);
        entete.setForeground(Color.WHITE);
        entete.setFont(Theme.TITRE);
        entete.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel texte = new JLabel("Coche les produits que tu as entièrement utilisés. "
                + "Pour une utilisation partielle, utilise « Modifier » dans Mon frigo.");
        texte.setForeground(Theme.TEXTE);
        texte.setFont(Theme.POLICE);
        texte.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        JPanel haut = new JPanel(new BorderLayout());
        haut.setBackground(Theme.FOND);
        haut.add(entete, BorderLayout.NORTH);
        haut.add(texte, BorderLayout.CENTER);
        add(haut, BorderLayout.NORTH);

        // Tableau des produits (case « Retirer » décochée par défaut)
        modele = new DefaultTableModel(
                new Object[]{"Retirer", "Produit", "Quantité", "Péremption"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }
        };
        for (Produit p : produits) {
            modele.addRow(new Object[]{
                Boolean.FALSE,
                p.getNom(),
                p.quantiteTexte(),
                p.getDatePeremption() == null ? "" : p.getDatePeremption().toString()
            });
        }

        JTable table = Theme.tableau(new JTable(modele));
        table.setAutoCreateRowSorter(false);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        add(Theme.defilement(table), BorderLayout.CENTER);

        // Boutons
        JButton boutonAnnuler = Ui.boutonContour("Annuler");
        JButton boutonValider = Ui.boutonPlein("Valider");

        JPanel bas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        bas.setBackground(Theme.FOND);
        bas.setBorder(BorderFactory.createEmptyBorder(0, 12, 8, 12));
        bas.add(boutonAnnuler);
        bas.add(boutonValider);
        add(bas, BorderLayout.SOUTH);

        // Écouteurs
        boutonAnnuler.addActionListener(e -> dispose());
        boutonValider.addActionListener(e -> {
            confirme = true;
            dispose();
        });
    }

    /**
     * Renvoie les produits cochés.
     * Liste vide si l'utilisateur a annulé.
     */
    public List<Produit> getChoisis() {
        List<Produit> choisis = new ArrayList<>();
        if (!confirme) {
            return choisis;
        }
        for (int i = 0; i < modele.getRowCount(); i++) {
            if (Boolean.TRUE.equals(modele.getValueAt(i, 0))) {
                choisis.add(produits.get(i));
            }
        }
        return choisis;
    }
}
