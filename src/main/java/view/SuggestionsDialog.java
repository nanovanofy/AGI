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

import model.Suggestion;
import util.Theme;
import util.Ui;

/**
 * Boîte de dialogue proposant des articles à racheter.
 * L'utilisateur coche ce qu'il veut : rien n'est ajouté sans son accord.
 */
public class SuggestionsDialog extends JDialog {

    private final List<Suggestion> suggestions;
    private final DefaultTableModel modele;

    /** Vrai uniquement si l'utilisateur a validé avec « Ajouter la sélection ». */
    private boolean confirme = false;

    public SuggestionsDialog(Window parent, List<Suggestion> suggestions) {
        super(parent, "Suggestions de courses", ModalityType.APPLICATION_MODAL);
        this.suggestions = suggestions;

        setSize(520, 420);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Theme.FOND);
        setLayout(new BorderLayout());

        // En-tête orange + texte explicatif
        JLabel entete = new JLabel("Suggestions de courses");
        entete.setOpaque(true);
        entete.setBackground(Theme.ORANGE);
        entete.setForeground(Color.WHITE);
        entete.setFont(Theme.TITRE);
        entete.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel texte = new JLabel("Coche ce que tu veux acheter. Rien n'est ajouté sans ton accord.");
        texte.setForeground(Theme.TEXTE);
        texte.setFont(Theme.POLICE);
        texte.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        JPanel haut = new JPanel(new BorderLayout());
        haut.setBackground(Theme.FOND);
        haut.add(entete, BorderLayout.NORTH);
        haut.add(texte, BorderLayout.CENTER);
        add(haut, BorderLayout.NORTH);

        // Tableau des suggestions (case « Choisir » décochée par défaut)
        modele = new DefaultTableModel(
                new Object[]{"Choisir", "Article", "Raison"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }
        };
        for (Suggestion s : suggestions) {
            modele.addRow(new Object[]{Boolean.FALSE, s.getNom(), s.getRaison()});
        }

        JTable table = Theme.tableau(new JTable(modele));
        table.setAutoCreateRowSorter(false);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        add(Theme.defilement(table), BorderLayout.CENTER);

        // Boutons
        JButton boutonToutCocher = Ui.boutonContour("Tout cocher");
        JButton boutonToutDecocher = Ui.boutonContour("Tout décocher");
        JButton boutonAnnuler = Ui.boutonContour("Annuler");
        JButton boutonAjouter = Ui.boutonPlein("Ajouter la sélection");

        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        gauche.setBackground(Theme.FOND);
        gauche.add(boutonToutCocher);
        gauche.add(boutonToutDecocher);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        droite.setBackground(Theme.FOND);
        droite.add(boutonAnnuler);
        droite.add(boutonAjouter);

        JPanel bas = new JPanel(new BorderLayout());
        bas.setBackground(Theme.FOND);
        bas.setBorder(BorderFactory.createEmptyBorder(0, 12, 8, 12));
        bas.add(gauche, BorderLayout.WEST);
        bas.add(droite, BorderLayout.EAST);
        add(bas, BorderLayout.SOUTH);

        // Écouteurs
        boutonToutCocher.addActionListener(e -> toutCocher(true));
        boutonToutDecocher.addActionListener(e -> toutCocher(false));
        boutonAnnuler.addActionListener(e -> dispose());
        boutonAjouter.addActionListener(e -> {
            confirme = true;
            dispose();
        });
    }

    /** Coche ou décoche toutes les lignes. */
    private void toutCocher(boolean valeur) {
        for (int i = 0; i < modele.getRowCount(); i++) {
            modele.setValueAt(valeur, i, 0);
        }
    }

    /**
     * Renvoie les suggestions cochées.
     * Liste vide si l'utilisateur a annulé.
     */
    public List<Suggestion> getChoisies() {
        List<Suggestion> choisies = new ArrayList<>();
        if (!confirme) {
            return choisies;
        }
        for (int i = 0; i < modele.getRowCount(); i++) {
            if (Boolean.TRUE.equals(modele.getValueAt(i, 0))) {
                choisies.add(suggestions.get(i));
            }
        }
        return choisies;
    }
}
