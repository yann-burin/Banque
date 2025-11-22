/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Yann
 */
public class CalculFrequenceMvts {

    // Méthode proncipale
    public static void main(String[] args)
            throws FileNotFoundException, IOException, SQLException, CsvException, ParseException {

        WriteFile.writeFile("INFO", "-------------> CalculFrequenceMvts début Main()", logFile);

        // variables de connexion à la base Mysql
        GetBanqueProperties.main(null);
        Properties dbConnProperties = GetBanqueProperties.prop;
        String host = dbConnProperties.getProperty("host");
        String base = dbConnProperties.getProperty("base");
        String username = dbConnProperties.getProperty("username");
        String password = dbConnProperties.getProperty("password");

        // Ouvre la connexion à la base
        connectionDB = DBConnection.getConnection(host, base, username, password);

        CalculFrequenceMvts runCalcul = new CalculFrequenceMvts();
        runCalcul.razAttacheTiers("update historique set frequence = null ", connectionDB);
        runCalcul.calculFrequenceMvtsInHistory(connectionDB);

        // Ferme la connexion à la base
        connectionDB.close();

        WriteFile.writeFile("INFO", "-------------> CalculFrequenceMvts fin Main()", logFile);
    }

    public static void razAttacheTiers(String Sql, Connection connection) throws SQLException {
        // force le commit automatique
        connection.setAutoCommit(true);
        String sql = Sql;
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.addBatch();
        statement.executeBatch();
    }

    public static double diffDate(String date1, String date2) throws ParseException {
        // Attention, ici, le format de date est : yyyy-MM-dd
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);
        java.util.Date firstDate = sdf.parse(date1);
        java.util.Date secondDate = sdf.parse(date2);
        long diffInMillies = Math.abs(secondDate.getTime() - firstDate.getTime());
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        // System.out.println ("diff: " + diff);
        return diff;
    }

    public static void updateFreq(String Sql, Connection connection) throws SQLException {
        // force le commit automatique
        connection.setAutoCommit(true);
        String sql = Sql;
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.addBatch();
        statement.executeBatch();
    }

    public void calculFrequenceMvtsInHistory(Connection connection) throws SQLException, ParseException {

        String sOldDate;
        double iJoursDiff;
        // iJoursDiff = 0;
        double iOldJoursDiff = 0;
        // DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        // Charge les Tiers et les critères associés
        String sqlTiers = "SELECT Tiers.IdTiers, Tiers.IdCategorie, Correspondances.Critere FROM Tiers INNER JOIN Correspondances ON Tiers.N° = Correspondances.N°Tiers ";

        try (PreparedStatement statementAttacheTiers = connectionDB.prepareStatement(sqlTiers);
                ResultSet rsTiers = statementAttacheTiers.executeQuery();) {

            while (rsTiers.next()) {
                String critere = rsTiers.getString("Correspondances.Critere");

                WriteFile.writeFile("INFO", "-------------> critere " + critere, logFile);

                // Charge les historiques correspondant au critère en argument
                String sqlHistorique = "select Compte, Date,Libelle,euros,critere, Frequence "
                        + " from historique where critere = '" + critere + "' "
                        + " Order by Date asc, SeqInSameDay asc";

                try (PreparedStatement statementHistorique = connectionDB.prepareStatement(sqlHistorique);
                        ResultSet rsHistorique = statementHistorique.executeQuery();) {

                    sOldDate = "01/01/1800";
                    while (rsHistorique.next()) {
                        Double euros = rsHistorique.getDouble("Euros");
                        String compte = rsHistorique.getString("Compte");
                        String libelle = rsHistorique.getString("Libelle");
                        String sdateHisto = rsHistorique.getDate("Date").toString();

                        // WriteFile.writeFile("INFO", sOldDate + " - " + sdateHisto , logFile);

                        if ("01/01/1800".equals(sOldDate)) {
                            iJoursDiff = 0;
                        } else if (sOldDate == null ? sdateHisto != null : !sOldDate.equals(sdateHisto)) {
                            iJoursDiff = diffDate(sOldDate, sdateHisto);
                        } else {
                            iJoursDiff = iOldJoursDiff;
                        }

                        // razAttacheTiers(,connectionDB);
                        WriteFile.writeFile("INFO",
                                sdateHisto + " - " + sdateHisto + " - " + libelle + " =--=> " + iJoursDiff, logFile);

                        updateFreq("update historique set Frequence = " + iJoursDiff + " where compte='" + compte
                                + "' and euros=" + euros + " and libelle='" + libelle + "' ", connectionDB);

                        sOldDate = sdateHisto;
                        iOldJoursDiff = iJoursDiff;

                    }
                    rsHistorique.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }

        }
    }
}
