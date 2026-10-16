package service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dao.ProduitDAO;
import dao.RecetteDAO;
import model.Produit;
import model.Recette;
import model.RecetteSuggestion;
import model.RecetteSuggestion.Ingredient;

/**
 * Logique métier des suggestions de recettes.
 * Aucun code Swing ici : uniquement des règles simples sur les données.
 */
public class RecetteService {

    /** Seuil (en jours) à partir duquel un produit doit être consommé vite.
     *  Même valeur que SEUIL_ALERTE dans FrigoPanel. */
    private static final int SEUIL_ALERTE = 3;

    private final RecetteDAO recetteDAO = new RecetteDAO();
    private final ProduitDAO produitDAO = new ProduitDAO();

    /**
     * Propose des recettes en fonction du contenu du frigo.
     *
     * @param seulementRealisables si vrai, on exclut les recettes
     *                             ayant au moins un ingrédient manquant
     * @return la liste des recettes à afficher
     */
    public List<RecetteSuggestion> suggerer(boolean seulementRealisables) throws SQLException {
        List<Recette> recettes = recetteDAO.findAll();
        List<Produit> produits = produitDAO.findAll();

        List<RecetteSuggestion> suggestions = new ArrayList<>();
        for (Recette r : recettes) {
            RecetteSuggestion s = construire(r, produits);
            if (seulementRealisables && !s.estRealisable()) {
                continue;
            }
            suggestions.add(s);
        }
        return suggestions;
    }

    /**
     * Évalue chaque ingrédient de la recette par rapport aux produits du frigo.
     * Correspondance : le nom du produit contient l'ingrédient (insensible à la casse),
     * par exemple « Lait demi-écrémé » correspond à « lait ».
     */
    private RecetteSuggestion construire(Recette recette, List<Produit> produits) {
        List<Ingredient> ingredients = new ArrayList<>();

        String texte = recette.getIngredients() == null ? "" : recette.getIngredients();
        for (String morceau : texte.split(",")) {
            String nom = morceau.trim();
            if (nom.isEmpty()) {
                continue;
            }
            ingredients.add(new Ingredient(nom, evaluer(nom, produits)));
        }

        return new RecetteSuggestion(recette.getTitre(), recette.getTempsMinutes(),
                recette.getInstructions(), ingredients);
    }

    /**
     * Détermine l'état d'un ingrédient : « À consommer vite », « En stock » ou « Manquant ».
     * Si plusieurs produits correspondent, on garde le plus urgent des produits non épuisés.
     */
    private String evaluer(String ingredient, List<Produit> produits) {
        Produit meilleur = null;

        for (Produit p : produits) {
            if (p.getNom() == null
                    || !p.getNom().toLowerCase().contains(ingredient.toLowerCase())) {
                continue;
            }
            // Produit épuisé : il ne peut pas servir
            if (p.getQuantite() <= 0) {
                continue;
            }
            // On garde le produit qui se périme le plus vite
            if (meilleur == null || p.joursRestants() < meilleur.joursRestants()) {
                meilleur = p;
            }
        }

        if (meilleur == null) {
            return Ingredient.MANQUANT;
        }
        if (meilleur.joursRestants() <= SEUIL_ALERTE) {
            return Ingredient.VITE;
        }
        return Ingredient.STOCK;
    }
}
