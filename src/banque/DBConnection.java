package banque;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * Gestionnaire de connexion à la base de données MySQL.
 * Utilise JDBC pour établir les connexions.
 * 
 * Note: Pour une application en production, considérez l'utilisation d'un pool
 * de connexions
 * comme HikariCP pour de meilleures performances.
 */
public class DBConnection {

    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());
    private static final int DEFAULT_PORT = 3306;

    /**
     * Établit une connexion à la base de données MySQL.
     * Le pilote JDBC MySQL est chargé automatiquement depuis JDBC 4.0.
     * 
     * @param host l'hôte de la base de données
     * @param base le nom de la base de données
     * @param user le nom d'utilisateur
     * @param pwd  le mot de passe
     * @return Connection ou null en cas d'erreur
     */
    public static Connection getConnection(String host, String base, String user, String pwd) {
        // Le chargement explicite du driver n'est plus nécessaire depuis JDBC 4.0
        String url = String.format("jdbc:mysql://%s:%d/%s?serverTimezone=UTC&useSSL=false",
                host, DEFAULT_PORT, base);

        try {
            Connection conn = DriverManager.getConnection(url, user, pwd);
            LOGGER.log(Level.INFO, "Connexion à la base de données établie avec succès");
            return conn;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la connexion à la base de données", e);
            JOptionPane.showMessageDialog(null,
                    "Erreur de connexion à la base de données: " + e.getMessage(),
                    "Erreur de connexion",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /**
     * Ferme proprement une connexion.
     * 
     * @param conn la connexion à fermer
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                LOGGER.log(Level.INFO, "Connexion fermée avec succès");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture de la connexion", e);
            }
        }
    }
}