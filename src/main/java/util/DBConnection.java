package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utilitaire de connexion à la base PostgreSQL.
 */
public class DBConnection {

    // Valeurs par défaut si les variables d'environnement ne sont pas définies
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/antigaspi";
    private static final String DEFAULT_USER = "antigaspi_user";
    private static final String DEFAULT_PASSWORD = "adminpg123";

    /**
     * Ouvre une connexion en lisant DB_URL, DB_USER et DB_PASSWORD.
     */
    public static Connection getConnection() throws SQLException {
        String url = System.getenv().getOrDefault("DB_URL", DEFAULT_URL);
        String user = System.getenv().getOrDefault("DB_USER", DEFAULT_USER);
        String password = System.getenv().getOrDefault("DB_PASSWORD", DEFAULT_PASSWORD);
        return DriverManager.getConnection(url, user, password);
    }
}
