package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import model.Recette;
import util.Theme;
import util.Ui;

/**
 * Boîte de dialogue pour créer une nouvelle recette.
 * Les ingrédients sont saisis librement puis stockés en texte séparé par des virgules.
 */
public class RecetteDialog extends JDialog {

    private final JTextField champTitre = new JTextField(28);
    private final JTextField champTemps = new JTextField(28);
    private final JTextArea champIngredients = new JTextArea(4, 28);
    private final JTextArea champInstructions = new JTextArea(6, 28);

    // Résultat renvoyé : null si l'utilisateur annule
    private Recette resultat;

    public RecetteDialog(Window parent) {
        super(parent, "Nouvelle recette", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        construire();
        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * Renvoie la recette validée, ou null si la saisie a été annulée.
     */
    public Recette getResultat() {
        return resultat;
    }

    private void construire() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.FOND);
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        ajouterLigne(form, c, 0, "Titre :", champTitre);
        ajouterLigne(form, c, 1, "Temps (minutes) :", champTemps);

        champIngredients.setFont(Theme.POLICE);
        champIngredients.setLineWrap(true);
        champIngredients.setWrapStyleWord(true);
        ajouterLigne(form, c, 2, "Ingrédients :",
                new JScrollPane(champIngredients));

        champInstructions.setFont(Theme.POLICE);
        champInstructions.setLineWrap(true);
        champInstructions.setWrapStyleWord(true);
        ajouterLigne(form, c, 3, "Instructions :",
                new JScrollPane(champInstructions));

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

    private void ajouterLigne(JPanel form, GridBagConstraints c, int ligne,
            String libelle, java.awt.Component champ) {
        c.gridx = 0;
        c.gridy = ligne;
        JLabel label = new JLabel(libelle);
        label.setForeground(Theme.TEXTE);
        form.add(label, c);

        c.gridx = 1;
        form.add(champ, c);
    }

    private void valider() {
        String titre = champTitre.getText().trim();
        String tempsTexte = champTemps.getText().trim();
        String ingredients = normaliserIngredients(champIngredients.getText());
        String instructions = champInstructions.getText().trim();

        if (titre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le titre est obligatoire.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int temps;
        try {
            temps = Integer.parseInt(tempsTexte);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Le temps doit être un nombre entier.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (temps < 0) {
            JOptionPane.showMessageDialog(this, "Le temps ne peut pas être négatif.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (ingredients.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Indique au moins un ingrédient.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        resultat = new Recette(0, titre, ingredients,
                instructions.isEmpty() ? null : instructions, temps);
        dispose();
    }

    /**
     * Transforme la saisie libre en texte CSV « a,b,c » :
     * on accepte les virgules et les retours à la ligne comme séparateurs.
     */
    private String normaliserIngredients(String texte) {
        StringBuilder sb = new StringBuilder();
        for (String morceau : texte.split("[,\\n\\r]+")) {
            String nom = morceau.trim();
            if (nom.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(nom);
        }
        return sb.toString();
    }
}
