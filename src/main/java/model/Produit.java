package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Représente un produit du frigo.
 */
public class Produit {

    private int id;
    private String nom;
    private double quantite;
    private String unite;
    private LocalDate datePeremption;

    public Produit() {
    }

    public Produit(String nom, double quantite, String unite, LocalDate datePeremption) {
        this.nom = nom;
        this.quantite = quantite;
        this.unite = unite;
        this.datePeremption = datePeremption;
    }

    public Produit(int id, String nom, double quantite, String unite, LocalDate datePeremption) {
        this.id = id;
        this.nom = nom;
        this.quantite = quantite;
        this.unite = unite;
        this.datePeremption = datePeremption;
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

    public double getQuantite() {
        return quantite;
    }

    public void setQuantite(double quantite) {
        this.quantite = quantite;
    }

    public String getUnite() {
        return unite;
    }

    public void setUnite(String unite) {
        this.unite = unite;
    }

    public LocalDate getDatePeremption() {
        return datePeremption;
    }

    public void setDatePeremption(LocalDate datePeremption) {
        this.datePeremption = datePeremption;
    }

    /**
     * Quantité au format texte, par exemple « 4 pièce(s) ».
     */
    public String quantiteTexte() {
        String nombre;
        if (quantite == Math.floor(quantite)) {
            nombre = String.valueOf((long) quantite);
        } else {
            nombre = String.valueOf(quantite);
        }
        String u = (unite == null || unite.isBlank()) ? "" : unite + "(s)";
        return u.isEmpty() ? nombre : nombre + " " + u;
    }

    /**
     * Nombre de jours restants avant la date de péremption (négatif si dépassée).
     */
    public long joursRestants() {
        if (datePeremption == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), datePeremption);
    }
}
