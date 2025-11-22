
package banque;

import static banque.ImportCSV.connectionDB;
import static banque.ImportCSV.logFile;
import com.opencsv.exceptions.CsvException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Properties;

/**
 *
 * @author Yann
 */
public class CalculAttacheTiers {

    // Méthode proncipale
    public static void main(String[] args)
            throws FileNotFoundException, IOException, SQLException, CsvException, ParseException {

        WriteFile.writeFile("INFO", "-------------> CalculAttacheTiers début Main()", logFile);

        // variables de connexion à la base Mysql
        GetBanqueProperties.main(null);
        Properties dbConnProperties = GetBanqueProperties.prop;
        String host = dbConnProperties.getProperty("host");
        String base = dbConnProperties.getProperty("base");
        String username = dbConnProperties.getProperty("username");
        String password = dbConnProperties.getProperty("password");

        // Ouvre la connexion à la base
        connectionDB = DBConnection.getConnection(host, base, username, password);

        CalculAttacheTiers runCalcul = new CalculAttacheTiers();
        runCalcul.razAttacheTiers("update historique set critere = null ", connectionDB);
        runCalcul.calculAttacheTiersInHistory(connectionDB);

        // Ferme la connexion à la base
        connectionDB.close();

        WriteFile.writeFile("INFO", "-------------> CalculAttacheTiers fin Main()", logFile);
    }

    public static void razAttacheTiers(String Sql, Connection connection) throws SQLException {
        // force le commit automatique
        connection.setAutoCommit(true);
        String sql = Sql;
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.addBatch();
        statement.executeBatch();
    }

    public void calculAttacheTiersInHistory(Connection connection) throws SQLException {
        // Charge les Tiers et les critères associés
        String sqlTiers = "SELECT Tiers.IdTiers, Tiers.IdCategorie, Correspondances.Critere FROM Tiers INNER JOIN Correspondances ON Tiers.N° = Correspondances.N°Tiers ";
        try (PreparedStatement statementAttacheTiers = connectionDB.prepareStatement(sqlTiers);
                ResultSet rsTiers = statementAttacheTiers.executeQuery();) {

            while (rsTiers.next()) {
                String critere = rsTiers.getString("Correspondances.Critere");

                WriteFile.writeFile("INFO", "-------------> critere " + critere, logFile);

                // Charge les historiques correspondant au critère en argument
                String sqlHistorique = "select Compte,Date,Libelle,euros,critere "
                        + " from historique where libelle like '" + critere + "' "
                        + " Order by Date";

                try (PreparedStatement statementHistorique = connectionDB.prepareStatement(sqlHistorique);
                        ResultSet rsHistorique = statementHistorique.executeQuery();) {
                    while (rsHistorique.next()) {
                        Double euros = rsHistorique.getDouble("Euros");
                        String compte = rsHistorique.getString("Compte");
                        String libelle = rsHistorique.getString("Libelle");
                        Date date = rsHistorique.getDate("date");
                        // Mise à jour du critère de Tiers correspondant
                        // rsHistorique.updateString("critere", critere);
                        // rsHistorique.updateRow();
                        WriteFile.writeFile("INFO",
                                "-> critere:" + critere + " > update historique set critere = '" + critere
                                        + "' where compte='" + compte + "' and euros=" + euros + " and libelle='"
                                        + libelle + "' ",
                                logFile);
                        razAttacheTiers("update historique set critere = '" + critere + "' where compte='" + compte
                                + "' and euros=" + euros + " and libelle='" + libelle + "' ", connectionDB);

                    }
                    rsHistorique.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }

        }
    }
}
