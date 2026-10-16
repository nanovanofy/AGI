package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Produit;
import util.DBConnection;

/**
 * Accès aux données de la table produit.
 */
public class ProduitDAO {

    /**
     * Recherche les produits dont le nom contient le texte donné.
     * Un texte vide renvoie tous les produits.
     */
    public List<Produit> rechercher(String texte) throws SQLException {
        List<Produit> resultats = new ArrayList<>();
        String sql;
        if (texte == null || texte.isBlank()) {
            sql = "SELECT id, nom, quantite, unite, date_peremption FROM produit ORDER BY id";
        } else {
            sql = "SELECT id, nom, quantite, unite, date_peremption FROM produit "
                    + "WHERE nom ILIKE ? ORDER BY id";
        }

        try (Connection connexion = DBConnection.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {

            if (texte != null && !texte.isBlank()) {
                ps.setString(1, "%" + texte + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultats.add(lireProduit(rs));
                }
            }
        }
        return resultats;
    }

    /**
     * Liste tous les produits du frigo.
     */
    public List<Produit> findAll() throws SQLException {
        return rechercher("");
    }

    /**
     * Insère un nouveau produit.
     */
    public void insert(Produit p) throws SQLException {
        String sql = "INSERT INTO produit (nom, quantite, unite, date_peremption) VALUES (?, ?, ?, ?)";
        try (Connection connexion = DBConnection.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setDouble(2, p.getQuantite());
            ps.setString(3, p.getUnite());
            ps.setObject(4, p.getDatePeremption());
            ps.executeUpdate();
        }
    }

    /**
     * Met à jour un produit existant (identifié par son id).
     */
    public void update(Produit p) throws SQLException {
        String sql = "UPDATE produit SET nom = ?, quantite = ?, unite = ?, date_peremption = ? WHERE id = ?";
        try (Connection connexion = DBConnection.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setDouble(2, p.getQuantite());
            ps.setString(3, p.getUnite());
            ps.setObject(4, p.getDatePeremption());
            ps.setInt(5, p.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Supprime un produit par son identifiant.
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM produit WHERE id = ?";
        try (Connection connexion = DBConnection.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Construit un Produit à partir de la ligne courante du ResultSet.
     */
    private Produit lireProduit(ResultSet rs) throws SQLException {
        Produit p = new Produit();
        p.setId(rs.getInt("id"));
        p.setNom(rs.getString("nom"));
        p.setQuantite(rs.getDouble("quantite"));
        p.setUnite(rs.getString("unite"));
        java.sql.Date date = rs.getDate("date_peremption");
        if (date != null) {
            p.setDatePeremption(date.toLocalDate());
        }
        return p;
    }
}
