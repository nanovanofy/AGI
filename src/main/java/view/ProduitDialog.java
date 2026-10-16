package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import model.Produit;
import util.Theme;
import util.Ui;

/**
 * Boîte de dialogue pour ajouter ou modifier un produit.
 */
public class ProduitDialog extends JDialog {

    private final JTextField champNom = new JTextField(20);
    private final JTextField champQuantite = new JTextField(20);
    private final JTextField champUnite = new JTextField(20);
    private final JTextField champDate = new JTextField(20);

    // Produit en cours d'édition (null lorsqu'on ajoute)
    private Produit produit;
    // Résultat renvoyé : null si l'utilisateur annule
    private Produit resultat;

    public ProduitDialog(Window parent) {
        super(parent, "Produit", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        construire();
        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * Prépare la boîte de dialogue : produit null = ajout, sinon modification.
     */
    public void setProduit(Produit produit) {
        this.produit = produit;
        this.resultat = null;
        if (produit == null) {
            champNom.setText("");
            champQuantite.setText("");
            champUnite.setText("");
            champDate.setText(LocalDate.now().toString());
        } else {
            champNom.setText(produit.getNom());
            champQuantite.setText(String.valueOf(produit.getQuantite()));
            champUnite.setText(produit.getUnite() == null ? "" : produit.getUnite());
            champDate.setText(produit.getDatePeremption() == null ? "" : produit.getDatePeremption().toString());
        }
    }

    /**
     * Pré-remplit le nom et la quantité (utilisable en mode ajout).
     * Les autres champs (unité, péremption) ne sont pas modifiés.
     */
    public void preremplir(String nom, String quantite) {
        champNom.setText(nom == null ? "" : nom);
        champQuantite.setText(quantite == null ? "" : quantite);
    }

    /**
     * Renvoie le produit validé, ou null si la saisie a été annulée.
     */
    public Produit getResultat() {
        return resultat;
    }

    private void construire() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.FOND);
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        ajouterLigne(form, c, 0, "Nom :", champNom);
        ajouterLigne(form, c, 1, "Quantité :", champQuantite);
        ajouterLigne(form, c, 2, "Unité :", champUnite);
        ajouterLigne(form, c, 3, "Péremption (AAAA-MM-JJ) :", champDate);

        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        boutons.setBackground(Theme.FOND);
        boutons.setBorder(BorderFactory.createEmptyBorder(0, 20, 16, 20));
        JButton annuler = Ui.boutonContour("Annuler");
        JButton valider = Ui.boutonPlein("Valider");
        annuler.addActionListener(e -> dispose());
        valider.addActionListener(e -> valider());
        boutons.add(annuler);
        boutons.add(valider);

        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.FOND);
        add(form, BorderLayout.CENTER);
        add(boutons, BorderLayout.SOUTH);
    }

    private void ajouterLigne(JPanel form, GridBagConstraints c, int ligne, String libelle, JTextField champ) {
        c.gridx = 0;
        c.gridy = ligne;
        JLabel label = new JLabel(libelle);
        label.setForeground(Theme.TEXTE);
        form.add(label, c);

        c.gridx = 1;
        form.add(champ, c);
    }

    private void valider() {
        String nom = champNom.getText().trim();
        String quantiteTexte = champQuantite.getText().trim();
        String unite = champUnite.getText().trim();
        String dateTexte = champDate.getText().trim();

        if (nom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le nom est obligatoire.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double quantite;
        try {
            quantite = Double.parseDouble(quantiteTexte);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La quantité doit être un nombre.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateTexte);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "La date doit être au format AAAA-MM-JJ.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Produit nouveau = new Produit(nom, quantite, unite.isEmpty() ? null : unite, date);
        if (produit != null) {
            nouveau.setId(produit.getId());
        }
        resultat = nouveau;
        dispose();
    }
}
