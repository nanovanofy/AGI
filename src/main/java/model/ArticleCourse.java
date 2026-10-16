package model;

/**
 * Un article de la liste de courses (ajout manuel ou facultatif).
 */
public class ArticleCourse {

    private int id;
    private String nom;
    private String quantite;
    private boolean achete;
    private String origine;

    /** Constructeur vide (obligatoire pour la lecture des résultats JDBC). */
    public ArticleCourse() {
    }

    /** Constructeur d'un article non encore acheté. */
    public ArticleCourse(String nom, String quantite, String origine) {
        this.nom = nom;
        this.quantite = quantite;
        this.origine = origine;
        this.achete = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getQuantite() {
        return quantite;
    }

    public void setQuantite(String quantite) {
        this.quantite = quantite;
    }

    public boolean isAchete() {
        return achete;
    }

    public void setAchete(boolean achete) {
        this.achete = achete;
    }

    public String getOrigine() {
        return origine;
    }

    public void setOrigine(String origine) {
        this.origine = origine;
    }
}
