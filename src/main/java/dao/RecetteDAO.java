package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Recette;
import util.DBConnection;

/**
 * Accès aux données de la table recette.
 */
public class RecetteDAO {

    /**
     * Liste toutes les recettes, par identifiant croissant.
     */
    public List<Recette> findAll() throws SQLException {
        List<Recette> resultats = new ArrayList<>();
        String sql = "SELECT id, titre, ingredients, instructions, temps_minutes "
                + "FROM recette ORDER BY id";

        try (Connection connexion = DBConnection.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                resultats.add(lireRecette(rs));
            }
        }
        return resultats;
    }

    /**
     * Construit une Recette à partir de la ligne courante du ResultSet.
     */
    private Recette lireRecette(ResultSet rs) throws SQLException {
        Recette r = new Recette();
        r.setId(rs.getInt("id"));
        r.setTitre(rs.getString("titre"));
        r.setIngredients(rs.getString("ingredients"));
        r.setInstructions(rs.getString("instructions"));
        r.setTempsMinutes(rs.getInt("temps_minutes"));
        return r;
    }
}
