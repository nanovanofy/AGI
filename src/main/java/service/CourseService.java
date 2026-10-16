package service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dao.CourseDAO;
import dao.ProduitDAO;
import model.ArticleCourse;
import model.Produit;
import model.Suggestion;

/**
 * Logique métier des suggestions de courses.
 * Aucun code Swing ici : uniquement des règles simples sur les données.
 */
public class CourseService {

    private final ProduitDAO produitDAO = new ProduitDAO();
    private final CourseDAO courseDAO = new CourseDAO();

    /**
     * Propose des articles à racheter :
     * produits périmés ou épuisés, qui ne sont pas déjà dans la liste de courses,
     * sans doublon de nom. La quantité proposée est toujours « 1 ».
     */
    public List<Suggestion> suggerer() throws SQLException {
        List<Suggestion> suggestions = new ArrayList<>();
        Set<String> nomsVus = new HashSet<>();

        for (Produit p : produitDAO.findAll()) {
            String nom = p.getNom();
            String cle = nom == null ? "" : nom.toLowerCase();

            // Déjà dans la liste de courses (non acheté) : inutile de suggérer
            if (courseDAO.existeNonAchete(nom)) {
                continue;
            }
            // Un seul enregistrement par nom (insensible à la casse)
            if (!nomsVus.add(cle)) {
                continue;
            }

            // Détermine la raison : périmé d'abord, sinon épuisé
            String raison;
            if (p.joursRestants() < 0) {
                raison = "Périmé";
            } else if (p.getQuantite() <= 0) {
                raison = "Épuisé";
            } else {
                nomsVus.remove(cle); // pas de suggestion : on libère le nom
                continue;
            }

            suggestions.add(new Suggestion(nom, "1", raison));
        }
        return suggestions;
    }

    /**
     * Ajoute les suggestions choisies par l'utilisateur.
     * Re-vérifie les doublons juste avant l'insertion.
     *
     * @return le nombre d'articles réellement ajoutés
     */
    public int ajouter(List<Suggestion> choisies) throws SQLException {
        int ajoutes = 0;
        for (Suggestion s : choisies) {
            // Re-vérification des doublons au moment de l'ajout
            if (courseDAO.existeNonAchete(s.getNom())) {
                continue;
            }
            String origine;
            if ("Recette".equals(s.getRaison())) {
                origine = "Auto : recette";
            } else if ("Périmé".equals(s.getRaison())) {
                origine = "Auto : périmé";
            } else {
                origine = "Auto : épuisé";
            }
            courseDAO.insert(new ArticleCourse(s.getNom(), s.getQuantite(), origine));
            ajoutes++;
        }
        return ajoutes;
    }
}
