package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;

import model.RecetteSuggestion;
import model.RecetteSuggestion.Ingredient;
import model.Suggestion;
import service.CourseService;
import service.RecetteService;
import util.Theme;
import util.Ui;

/**
 * Panneau « Recettes » : liste des recettes suggérées selon le contenu du frigo,
 * avec le détail (ingrédients, instructions) de la recette sélectionnée.
 */
public class RecettesPanel extends JPanel implements Rafraichissable {

    /** Police des titres de section (Theme n'a pas de POLICE_GRAS : on dérive POLICE). */
    private static final Font POLICE_GRAS = Theme.POLICE.deriveFont(Font.BOLD, 16f);

    /** Sous-titre : TITRE en plus petit (Theme n'a pas de SOUS_TITRE). */
    private static final Font SOUS_TITRE = Theme.TITRE.deriveFont(Font.PLAIN, 16f);

    /** Bordure du panneau blanc de droite (même couleur que Theme.defilement). */
    private static final Border BORDURE =
            BorderFactory.createLineBorder(new Color(0xE0, 0xD5, 0xC5));

    /** Message affiché quand aucune recette ne correspond. */
    private static final String MESSAGE_VIDE =
            "Aucune recette à proposer. Ajoute des produits à ton frigo ou décoche le filtre.";

    private final RecetteService service = new RecetteService();
    private final CourseService courseService = new CourseService();

    private final JCheckBox caseRealisables =
            new JCheckBox("Seulement les recettes réalisables");

    private final DefaultListModel<RecetteSuggestion> modeleListe = new DefaultListModel<>();
    private final JList<RecetteSuggestion> liste = new JList<>(modeleListe);

    private final DefaultTableModel modeleTableau = new DefaultTableModel(
            new Object[]{"Ingrédient", "Disponibilité"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = Theme.tableau(new JTable(modeleTableau));

    private final JLabel titreDetail = new JLabel();
    private final JLabel tempsLabel = new JLabel();
    private final JTextArea zoneInstructions = new JTextArea();
    private final JButton boutonManquants =
            Ui.boutonPlein("Ajouter les manquants aux courses");

    /** Deux cartes à droite : le détail d'une recette, ou le message « vide ». */
    private final CardLayout cartes = new CardLayout();
    private final JPanel panneauDroit = new JPanel(cartes);

    /** Recettes actuellement affichées (même ordre que la liste). */
    private List<RecetteSuggestion> suggestions = new ArrayList<>();

    public RecettesPanel() {
        setLayout(new BorderLayout(12, 0));
        setBackground(Theme.FOND);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        add(construireGauche(), BorderLayout.WEST);
        add(construireDroit(), BorderLayout.CENTER);

        // Écouteurs
        caseRealisables.addActionListener(e -> charger());
        boutonManquants.addActionListener(e -> ajouterManquants());
        liste.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            RecetteSuggestion choix = liste.getSelectedValue();
            if (choix != null) {
                afficherDetail(choix);
            }
        });

        charger();
    }

    /**
     * Colonne de gauche (largeur 300) : titre, filtre et liste des recettes.
     */
    private JPanel construireGauche() {
        JPanel gauche = new JPanel(new BorderLayout(0, 10));
        gauche.setBackground(Theme.FOND);
        gauche.setPreferredSize(new Dimension(300, 0));

        JLabel titre = new JLabel("Recettes suggérées");
        titre.setFont(POLICE_GRAS);
        titre.setForeground(Theme.TEXTE);

        caseRealisables.setFont(Theme.POLICE);
        caseRealisables.setForeground(Theme.TEXTE);
        caseRealisables.setBackground(Theme.FOND);
        caseRealisables.setSelected(false);

        JPanel haut = new JPanel(new BorderLayout(0, 8));
        haut.setBackground(Theme.FOND);
        haut.add(titre, BorderLayout.NORTH);
        haut.add(caseRealisables, BorderLayout.SOUTH);
        gauche.add(haut, BorderLayout.NORTH);

        liste.setFont(Theme.POLICE);
        liste.setBackground(Color.WHITE);
        liste.setForeground(Theme.TEXTE);
        liste.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        liste.setSelectionBackground(Theme.SELECTION);
        liste.setSelectionForeground(Theme.TEXTE);
        liste.setCellRenderer(new RenduRecette());

        JScrollPane defilement = new JScrollPane(liste);
        defilement.setBorder(BorderFactory.createLineBorder(new Color(0xE0, 0xD5, 0xC5)));
        defilement.getViewport().setBackground(Color.WHITE);
        gauche.add(defilement, BorderLayout.CENTER);

        return gauche;
    }

    /**
     * Colonne de droite : détail de la recette sélectionnée, ou message si liste vide.
     */
    private JPanel construireDroit() {
        panneauDroit.setBackground(Color.WHITE);
        panneauDroit.setBorder(BORDURE);

        // Carte 1 : détail
        JPanel detail = new JPanel(new BorderLayout(0, 10));
        detail.setBackground(Color.WHITE);
        detail.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JPanel enTete = new JPanel(new BorderLayout(8, 0));
        enTete.setBackground(Color.WHITE);
        titreDetail.setFont(SOUS_TITRE);
        titreDetail.setForeground(Theme.TEXTE);
        tempsLabel.setFont(Theme.POLICE);
        tempsLabel.setForeground(Theme.TEXTE);
        tempsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        enTete.add(titreDetail, BorderLayout.CENTER);
        enTete.add(tempsLabel, BorderLayout.EAST);
        detail.add(enTete, BorderLayout.NORTH);

        table.setAutoCreateRowSorter(false);
        detail.add(Theme.defilement(table), BorderLayout.CENTER);

        zoneInstructions.setEditable(false);
        zoneInstructions.setFont(Theme.POLICE);
        zoneInstructions.setForeground(Theme.TEXTE);
        zoneInstructions.setBackground(Color.WHITE);
        zoneInstructions.setLineWrap(true);
        zoneInstructions.setWrapStyleWord(true);
        zoneInstructions.setRows(5);
        zoneInstructions.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JScrollPane defilementTexte = new JScrollPane(zoneInstructions);
        defilementTexte.setBorder(BORDURE);
        defilementTexte.getViewport().setBackground(Color.WHITE);

        // Le bouton s'active seulement si la recette a des ingrédients manquants
        boutonManquants.setEnabled(false);

        JPanel bas = new JPanel(new BorderLayout(0, 8));
        bas.setBackground(Color.WHITE);
        bas.add(defilementTexte, BorderLayout.CENTER);
        bas.add(boutonManquants, BorderLayout.SOUTH);
        detail.add(bas, BorderLayout.SOUTH);

        // Carte 2 : aucune recette
        JLabel videLabel = new JLabel(MESSAGE_VIDE, SwingConstants.CENTER);
        videLabel.setFont(Theme.POLICE);
        videLabel.setForeground(Theme.TEXTE);
        videLabel.setOpaque(true);
        videLabel.setBackground(Color.WHITE);
        videLabel.setBorder(BorderFactory.createEmptyBorder(20, 16, 20, 16));

        panneauDroit.add(detail, "detail");
        panneauDroit.add(videLabel, "vide");
        return panneauDroit;
    }

    /**
     * Recharge la liste des recettes suggérées.
     * La recette sélectionnée est conservée si elle fait encore partie des résultats,
     * sinon on sélectionne la première.
     */
    @Override
    public void charger() {
        try {
            RecetteSuggestion selectionAvant = liste.getSelectedValue();
            suggestions = service.suggerer(caseRealisables.isSelected());

            modeleListe.clear();
            for (RecetteSuggestion s : suggestions) {
                modeleListe.addElement(s);
            }

            if (suggestions.isEmpty()) {
                boutonManquants.setEnabled(false);
                cartes.show(panneauDroit, "vide");
                return;
            }

            // Index de la recette à afficher : celle qui était sélectionnée, sinon la première
            int index = 0;
            if (selectionAvant != null) {
                for (int i = 0; i < suggestions.size(); i++) {
                    if (suggestions.get(i).getTitre().equals(selectionAvant.getTitre())) {
                        index = i;
                        break;
                    }
                }
            }

            cartes.show(panneauDroit, "detail");
            liste.setSelectedIndex(index);
            afficherDetail(suggestions.get(index));
        } catch (SQLException e) {
            afficherErreur(e);
        }
    }

    /**
     * Affiche le détail (titre, temps, ingrédients, instructions) d'une recette.
     */
    private void afficherDetail(RecetteSuggestion r) {
        titreDetail.setText(r.getTitre());
        tempsLabel.setText("Temps : " + r.getTempsMinutes() + " min");

        modeleTableau.setRowCount(0);
        for (Ingredient ing : r.getIngredients()) {
            modeleTableau.addRow(new Object[]{ing.getNom(), ing.getDisponibilite()});
        }

        String texte = r.getInstructions() == null ? "" : r.getInstructions();
        zoneInstructions.setText(texte);
        zoneInstructions.setCaretPosition(0);

        majBoutonManquants(r);
    }

    /**
     * Active le bouton « Ajouter les manquants » et indique le nombre d'ingrédients manquants.
     * Le bouton est désactivé si la recette n'a aucun manquant.
     */
    private void majBoutonManquants(RecetteSuggestion r) {
        int n = r.getManquants().size();
        boutonManquants.setEnabled(n > 0);
        if (n == 1) {
            boutonManquants.setText("Ajouter le manquant aux courses");
        } else {
            boutonManquants.setText("Ajouter les " + n + " manquants aux courses");
        }
    }

    /**
     * Ouvre la boîte de dialogue des ingrédients manquants de la recette sélectionnée.
     * L'utilisateur choisit ce qu'il veut ajouter : rien n'est ajouté sans son accord.
     */
    private void ajouterManquants() {
        RecetteSuggestion r = liste.getSelectedValue();
        if (r == null || r.getManquants().isEmpty()) {
            return;
        }

        // Une suggestion par ingrédient manquant (quantité « 1 », raison « Recette »)
        List<Suggestion> propositions = new ArrayList<>();
        for (String nom : r.getManquants()) {
            propositions.add(new Suggestion(nom, "1", "Recette"));
        }

        SuggestionsDialog dialog = new SuggestionsDialog(
                SwingUtilities.getWindowAncestor(this), propositions);
        dialog.setVisible(true);

        List<Suggestion> choisies = dialog.getChoisies();
        if (choisies.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Rien n'a été ajouté.",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            int n = courseService.ajouter(choisies);
            JOptionPane.showMessageDialog(this,
                    n + " article(s) ajouté(s) aux courses.",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            afficherErreur(e);
        }
    }

    /**
     * Rendu d'une ligne de la liste : « Titre  (3/5) » suivi de « · anti-gaspi »
     * si la recette utilise des produits urgents.
     */
    private static class RenduRecette extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object valeur,
                int index, boolean selection, boolean focus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, valeur, index, selection, focus);

            if (valeur instanceof RecetteSuggestion r) {
                String texte = r.getTitre() + "  (" + r.nbDisponibles()
                        + "/" + r.nbIngredients() + ")";
                if (r.utiliseUrgent()) {
                    texte += " · anti-gaspi";
                }
                label.setText(texte);
            }
            label.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
            label.setOpaque(true);
            if (selection) {
                label.setBackground(Theme.SELECTION);
                label.setForeground(Theme.TEXTE);
            }
            return label;
        }
    }

    /**
     * Affiche une erreur SQL sans faire planter l'application.
     */
    private void afficherErreur(SQLException e) {
        JOptionPane.showMessageDialog(this,
                "Erreur : " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}
