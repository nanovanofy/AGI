package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Une recette proposée à l'utilisateur, enrichie de la disponibilité
 * réelle de ses ingrédients dans le frigo.
 */
public class RecetteSuggestion {

    /** Un ingrédient de la recette avec son état dans le frigo. */
    public static class Ingredient {

        /** États possibles d'un ingrédient. */
        public static final String VITE = "À consommer vite";
        public static final String STOCK = "En stock";
        public static final String MANQUANT = "Manquant";

        private final String nom;
        private final String disponibilite;

        public Ingredient(String nom, String disponibilite) {
            this.nom = nom;
            this.disponibilite = disponibilite;
        }

        public String getNom() {
            return nom;
        }

        public String getDisponibilite() {
            return disponibilite;
        }

        /** Vrai si l'ingrédient est en quantité insuffisante. */
        public boolean estManquant() {
            return MANQUANT.equals(disponibilite);
        }

        /** Vrai si l'ingrédient doit être consommé rapidement. */
        public boolean estUrgent() {
            return VITE.equals(disponibilite);
        }
    }

    private final String titre;
    private final int tempsMinutes;
    private final String instructions;
    private final List<Ingredient> ingredients;

    public RecetteSuggestion(String titre, int tempsMinutes, String instructions,
            List<Ingredient> ingredients) {
        this.titre = titre;
        this.tempsMinutes = tempsMinutes;
        this.instructions = instructions;
        this.ingredients = ingredients;
    }

    public String getTitre() {
        return titre;
    }

    public int getTempsMinutes() {
        return tempsMinutes;
    }

    public String getInstructions() {
        return instructions;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    /** Nombre total d'ingrédients de la recette. */
    public int nbIngredients() {
        return ingredients.size();
    }

    /** Nombre d'ingrédients disponibles (en stock ou à consommer vite). */
    public int nbDisponibles() {
        int n = 0;
        for (Ingredient i : ingredients) {
            if (!i.estManquant()) {
                n++;
            }
        }
        return n;
    }

    /** Vrai si aucun ingrédient n'est manquant. */
    public boolean estRealisable() {
        for (Ingredient i : ingredients) {
            if (i.estManquant()) {
                return false;
            }
        }
        return true;
    }

    /** Vrai si la recette utilise au moins un produit à consommer vite. */
    public boolean utiliseUrgent() {
        for (Ingredient i : ingredients) {
            if (i.estUrgent()) {
                return true;
            }
        }
        return false;
    }

    /** Liste des ingrédients manquants (utile pour l'étape 8C). */
    public List<String> getManquants() {
        List<String> manquants = new ArrayList<>();
        for (Ingredient i : ingredients) {
            if (i.estManquant()) {
                manquants.add(i.getNom());
            }
        }
        return manquants;
    }
}
