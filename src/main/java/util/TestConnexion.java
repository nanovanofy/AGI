package util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Test rapide de la connexion à la base.
 */
public class TestConnexion {

    public static void main(String[] args) {
        try (Connection connexion = DBConnection.getConnection();
             Statement st = connexion.createStatement()) {

            // Version du serveur PostgreSQL
            try (ResultSet rs = st.executeQuery("SELECT version()")) {
                if (rs.next()) {
                    System.out.println("Version : " + rs.getString(1));
                }
            }

            // Nombre de lignes de la table produit
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM produit")) {
                if (rs.next()) {
                    System.out.println("Nombre de produits : " + rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
            System.exit(1);
        }
    }
}
