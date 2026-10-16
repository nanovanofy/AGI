package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.ArticleCourse;
import util.DBConnection;

/**
 * Accès aux données de la table article_course.
 */
public class CourseDAO {

    /**
     * Liste tous les articles : les non achetés d'abord, puis par identifiant.
     */
    public List<ArticleCourse> findAll() throws SQLException {
        List<ArticleCourse> resultats = new ArrayList<>();
        String sql = "SELECT id, nom, quantite, achete, origine FROM article_course "
                + "ORDER BY achete, id";

        try (Connection connexion = DBConnection.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                resultats.add(lireArticle(rs));
            }
        }
        return resultats;
    }

    /**
     * Insère un article et renseigne son identifiant grâce à RETURNING id.
     */
    public void insert(ArticleCourse a) throws SQLException {
        String sql = "INSERT INTO article_course (nom, quantite, achete, origine) "
                + "VALUES (?, ?, ?, ?) RETURNING id";

        try (Connection connexion = DBConnection.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {

            ps.setString(1, a.getNom());
            ps.setString(2, a.getQuantite());
            ps.setBoolean(3, a.isAchete());
            ps.setString(4, a.getOrigine());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    a.setId(rs.getInt(1));
                }
            }
        }
    }

    /**
     * Coche ou décoche un article (état acheté).
     */
    public void setAchete(int id, boolean achete) throws SQLException {
        String sql = "UPDATE article_course SET achete = ? WHERE id = ?";

        try (Connection connexion = DBConnection.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {

            ps.setBoolean(1, achete);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /**
     * Supprime un article par son identifiant.
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM article_course WHERE id = ?";

        try (Connection connexion = DBConnection.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Supprime tous les articles déjà achetés.
     */
    public void supprimerAchetes() throws SQLException {
        String sql = "DELETE FROM article_course WHERE achete = TRUE";

        try (Connection connexion = DBConnection.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {

            ps.executeUpdate();
        }
    }

    /**
     * Indique si un article non acheté porte déjà ce nom
     * (comparaison insensible à la casse).
     */
    public boolean existeNonAchete(String nom) throws SQLException {
        String sql = "SELECT 1 FROM article_course "
                + "WHERE LOWER(nom) = LOWER(?) AND achete = FALSE";

        try (Connection connexion = DBConnection.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {

            ps.setString(1, nom);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Construit un ArticleCourse à partir de la ligne courante du ResultSet.
     */
    private ArticleCourse lireArticle(ResultSet rs) throws SQLException {
        ArticleCourse a = new ArticleCourse();
        a.setId(rs.getInt("id"));
        a.setNom(rs.getString("nom"));
        a.setQuantite(rs.getString("quantite"));
        a.setAchete(rs.getBoolean("achete"));
        a.setOrigine(rs.getString("origine"));
        return a;
    }
}
