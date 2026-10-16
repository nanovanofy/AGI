package model;

/**
 * Une suggestion de courses proposée à l'utilisateur.
 * Elle n'est jamais ajoutée automatiquement : c'est l'utilisateur qui choisit.
 */
public class Suggestion {

    private final String nom;
    private final String quantite;
    private final String raison;

    /**
     * @param raison « Périmé » ou « Épuisé »
     */
    public Suggestion(String nom, String quantite, String raison) {
        this.nom = nom;
        this.quantite = quantite;
        this.raison = raison;
    }

    public String getNom() {
        return nom;
    }

    public String getQuantite() {
        return quantite;
    }

    public String getRaison() {
        return raison;
    }
}
