package view;

import java.awt.BorderLayout;
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
import javax.swing.table.DefaultTableModel;

import dao.CourseDAO;
import dao.ProduitDAO;
import model.ArticleCourse;
import model.Produit;
import model.Suggestion;
import service.CourseService;
import util.Theme;
import util.Ui;

/**
 * Panneau « Courses » : liste manuelle et facultative d'articles à acheter.
 */
public class CoursesPanel extends JPanel implements Rafraichissable {

    private final CourseDAO dao = new CourseDAO();
    private final ProduitDAO produitDAO = new ProduitDAO();
    private final CourseService service = new CourseService();
    private final JTextField champArticle = new JTextField(16);
    private final JTextField champQuantite = new JTextField(6);
    private final JLabel videLabel = new JLabel();
    private final JLabel besoinLabel = new JLabel();
    private final JButton boutonFrigo = Ui.boutonPlein("Mettre au frigo les articles pris");
    private final DefaultTableModel modele;
    private final JTable table;

    /** Articles actuellement affichés (index = ligne du modèle). */
    private List<ArticleCourse> articles = new ArrayList<>();

    /**
     * Drapeau : true pendant le rechargement du tableau,
     * pour ne pas déclencher setAchete à cause des mises à jour du modèle.
     */
    private boolean majEnCours = false;

    public CoursesPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.FOND);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Bandeau d'information
        JLabel info = new JLabel("Les courses sont facultatives : ajoute un article seulement si tu en as besoin.");
        info.setOpaque(true);
        info.setBackground(Theme.ORANGE_CLAIR);
        info.setForeground(Theme.TEXTE);
        info.setFont(Theme.POLICE);
        info.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        // Bandeau de besoin (suggestions), juste sous le message d'information
        besoinLabel.setOpaque(true);
        besoinLabel.setBackground(Theme.ORANGE_CLAIR);
        besoinLabel.setForeground(Theme.TEXTE);
        besoinLabel.setFont(Theme.POLICE);
        besoinLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        // Ligne de saisie : article + quantité + boutons
        JPanel saisie = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        saisie.setBackground(Theme.FOND);

        JLabel labelArticle = new JLabel("Article :");
        labelArticle.setForeground(Theme.TEXTE);
        JLabel labelQuantite = new JLabel("Quantité :");
        labelQuantite.setForeground(Theme.TEXTE);

        JButton boutonAjouter = Ui.boutonContour("Ajouter à la main");
        JButton boutonSuggerer = Ui.boutonPlein("Suggérer des articles");

        saisie.add(labelArticle);
        saisie.add(champArticle);
        saisie.add(labelQuantite);
        saisie.add(champQuantite);
        saisie.add(boutonAjouter);
        saisie.add(boutonSuggerer);

        JPanel zoneSaisie = new JPanel(new BorderLayout());
        zoneSaisie.setBackground(Theme.FOND);
        zoneSaisie.add(besoinLabel, BorderLayout.NORTH);
        zoneSaisie.add(saisie, BorderLayout.CENTER);

        JPanel haut = new JPanel(new BorderLayout());
        haut.setBackground(Theme.FOND);
        haut.add(info, BorderLayout.NORTH);
        haut.add(zoneSaisie, BorderLayout.CENTER);
        add(haut, BorderLayout.NORTH);

        // Message affiché quand la liste est vide
        videLabel.setText("Ta liste est vide. Rien d'obligatoire : ajoute un article seulement si besoin.");
        videLabel.setHorizontalAlignment(JLabel.CENTER);
        videLabel.setForeground(Theme.TEXTE);
        videLabel.setFont(Theme.POLICE);
        videLabel.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));

        // Tableau : Pris (case à cocher), Article, Quantité, Origine
        modele = new DefaultTableModel(
                new Object[]{"Pris", "Article", "Quantité", "Origine"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }
        };

        table = Theme.tableau(new JTable(modele));
        table.setAutoCreateRowSorter(false); // tableau non triable
        table.getColumnModel().getColumn(0).setMaxWidth(60);

        JPanel centre = new JPanel(new BorderLayout());
        centre.setBackground(Theme.FOND);
        centre.add(videLabel, BorderLayout.NORTH);
        centre.add(Theme.defilement(table), BorderLayout.CENTER);
        add(centre, BorderLayout.CENTER);

        // Boutons du bas
        JButton boutonSupprimer = Ui.boutonContour("Supprimer la sélection");
        JButton boutonRetirer = Ui.boutonContour("Retirer les articles pris");

        JPanel bas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bas.setBackground(Theme.FOND);
        bas.add(boutonSupprimer);
        bas.add(boutonRetirer);
        bas.add(boutonFrigo);
        add(bas, BorderLayout.SOUTH);

        // Écouteurs
        boutonAjouter.addActionListener(e -> ajouter());
        boutonSuggerer.addActionListener(e -> suggerer());
        boutonSupprimer.addActionListener(e -> supprimerSelection());
        boutonRetirer.addActionListener(e -> retirerAchetes());
        boutonFrigo.addActionListener(e -> mettreAuFrigo());

        // Cocher / décocher une case met à jour la base tout de suite
        modele.addTableModelListener(e -> {
            if (majEnCours || e.getColumn() != 0) {
                return;
            }
            int ligne = e.getFirstRow();
            if (ligne < 0 || ligne >= articles.size()) {
                return;
            }
            boolean coche = Boolean.TRUE.equals(modele.getValueAt(ligne, 0));
            try {
                dao.setAchete(articles.get(ligne).getId(), coche);
            } catch (SQLException ex) {
                afficherErreur(ex);
                charger();
            }
            majBoutonFrigo();
        });

        charger();
    }

    /**
     * Recharge la liste des articles depuis la base.
     */
    @Override
    public void charger() {
        majEnCours = true;
        try {
            articles = dao.findAll();
            modele.setRowCount(0);
            for (ArticleCourse a : articles) {
                modele.addRow(new Object[]{
                    a.isAchete(),
                    a.getNom(),
                    a.getQuantite(),
                    a.getOrigine()
                });
            }
            videLabel.setVisible(articles.isEmpty());
            majBoutonFrigo();
            majBandeauBesoin();
        } catch (SQLException e) {
            afficherErreur(e);
        } finally {
            majEnCours = false;
        }
    }

    /**
     * Met à jour le bandeau de besoin à partir des suggestions.
     * Ce bandeau n'ajoute jamais rien : il informe seulement.
     */
    private void majBandeauBesoin() throws SQLException {
        int n = service.suggerer().size();
        if (n > 0) {
            besoinLabel.setText(n + " produit(s) pourraient être à racheter. "
                    + "Clique sur « Suggérer des articles » pour choisir.");
            besoinLabel.setBackground(Theme.BIENTOT);
        } else {
            besoinLabel.setText("Aucun besoin détecté pour le moment.");
            besoinLabel.setBackground(Theme.ORANGE_CLAIR);
        }
    }

    /**
     * Ajoute un article saisi à la main (origine « Manuel »).
     */
    private void ajouter() {
        String nom = champArticle.getText().trim();
        if (nom.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Saisis le nom de l'article.",
                    "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String quantite = champQuantite.getText().trim();
        if (quantite.isEmpty()) {
            quantite = "1";
        }

        try {
            if (dao.existeNonAchete(nom)) {
                JOptionPane.showMessageDialog(this,
                        "Cet article est déjà dans la liste.",
                        "Attention", JOptionPane.WARNING_MESSAGE);
                return;
            }
            dao.insert(new ArticleCourse(nom, quantite, "Manuel"));
            champArticle.setText("");
            champQuantite.setText("");
            charger();
        } catch (SQLException e) {
            afficherErreur(e);
        }
    }

    /**
     * Ouvre la boîte de dialogue des suggestions et ajoute ce que l'utilisateur a coché.
     */
    private void suggerer() {
        try {
            List<Suggestion> liste = service.suggerer();
            if (liste.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Rien à suggérer pour le moment.",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            SuggestionsDialog dialog = new SuggestionsDialog(
                    SwingUtilities.getWindowAncestor(this), liste);
            dialog.setVisible(true);

            List<Suggestion> choisies = dialog.getChoisies();
            if (choisies.isEmpty()) {
                return;
            }

            int n = service.ajouter(choisies);
            JOptionPane.showMessageDialog(this,
                    n + " article(s) ajouté(s).",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            charger();
        } catch (SQLException e) {
            afficherErreur(e);
        }
    }

    /**
     * Supprime les lignes cochées dans le tableau (la sélection).
     */
    private void supprimerSelection() {
        int[] lignes = table.getSelectedRows();
        if (lignes.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "Sélectionne d'abord une ligne.",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Conversion des index de vue en index de modèle, du bas vers le haut
        int[] modeles = new int[lignes.length];
        for (int i = 0; i < lignes.length; i++) {
            modeles[i] = table.convertRowIndexToModel(lignes[i]);
        }
        java.util.Arrays.sort(modeles);

        try {
            for (int i = modeles.length - 1; i >= 0; i--) {
                dao.delete(articles.get(modeles[i]).getId());
            }
            charger();
        } catch (SQLException e) {
            afficherErreur(e);
        }
    }

    /**
     * Retire définitivement les articles déjà pris (avec confirmation).
     */
    private void retirerAchetes() {
        int choix = JOptionPane.showConfirmDialog(this,
                "Retirer tous les articles déjà pris ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);
        if (choix != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            dao.supprimerAchetes();
            charger();
        } catch (SQLException e) {
            afficherErreur(e);
        }
    }

    /**
     * Active le bouton « Mettre au frigo » seulement si au moins un article est coché.
     */
    private void majBoutonFrigo() {
        boolean auMoinsUn = false;
        for (int i = 0; i < modele.getRowCount(); i++) {
            if (Boolean.TRUE.equals(modele.getValueAt(i, 0))) {
                auMoinsUn = true;
                break;
            }
        }
        boutonFrigo.setEnabled(auMoinsUn);
    }

    /**
     * Met au frigo les articles cochés (pris).
     * Pour chacun : ouvre la boîte d'ajout de produit pré-remplie.
     * Si l'utilisateur annule, l'article reste dans la liste.
     */
    private void mettreAuFrigo() {
        // On lit l'état des cases depuis le modèle (source fiable de l'affichage)
        List<Integer> lignes = new ArrayList<>();
        for (int i = 0; i < modele.getRowCount(); i++) {
            if (Boolean.TRUE.equals(modele.getValueAt(i, 0))) {
                lignes.add(i);
            }
        }
        if (lignes.isEmpty()) {
            return;
        }

        int mis = 0;
        for (int ligne : lignes) {
            ArticleCourse article = articles.get(ligne);
            ProduitDialog dialog = new ProduitDialog(SwingUtilities.getWindowAncestor(this));
            dialog.setProduit(null); // mode ajout
            dialog.preremplir(article.getNom(), "1");
            dialog.setVisible(true);

            Produit produit = dialog.getResultat();
            if (produit == null) {
                continue; // annulé : l'article reste dans la liste
            }
            try {
                produitDAO.insert(produit);
                dao.delete(article.getId());
                mis++;
            } catch (SQLException e) {
                afficherErreur(e);
            }
        }

        JOptionPane.showMessageDialog(this,
                mis + " article(s) mis au frigo.",
                "Information", JOptionPane.INFORMATION_MESSAGE);
        charger();
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
