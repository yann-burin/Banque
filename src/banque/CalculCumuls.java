/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package banque;

import static banque.ImportCSV.connectionDB;
import static banque.ImportCSV.consolide_Data;
import static banque.ImportCSV.loadFiles;
import static banque.ImportCSV.logFile;
import static banque.ImportCSV.truncateDataTemp;
import com.opencsv.exceptions.CsvException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Properties;

/**
 *
 * @author Yann
 */
public class CalculCumuls {

    // Méthode proncipale
    public static void main(String[] args)
            throws FileNotFoundException, IOException, SQLException, CsvException, ParseException {

        WriteFile.writeFile("INFO", "-------------> CalculCumuls début Main()", logFile);

        // variables de connexion à la base Mysql
        GetBanqueProperties.main(null);
        Properties dbConnProperties = GetBanqueProperties.prop;
        String host = dbConnProperties.getProperty("host");
        String base = dbConnProperties.getProperty("base");
        String username = dbConnProperties.getProperty("username");
        String password = dbConnProperties.getProperty("password");

        // Ouvre la connexion à la base
        connectionDB = DBConnection.getConnection(host, base, username, password);

        CalculCumuls runCalcul = new CalculCumuls();
        runCalcul.razCumulHistorique(connectionDB);
        runCalcul.calculCumulEurosSaisiFromImportFiles(connectionDB);
        runCalcul.calculCumulEurosCalc(connectionDB);

        // Ferme la connexion à la base
        connectionDB.close();

        WriteFile.writeFile("INFO", "-------------> CalculCumuls fin Main()", logFile);
    }

    public static void razCumulHistorique(Connection connection) throws SQLException {
        // force le commit automatique
        connection.setAutoCommit(true);
        String sql = "update historique set cumul_euros_calc = null, cumul_euros_saisi = null ";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.addBatch();
        statement.executeBatch();
    }

    public void calculCumulEurosCalc(Connection connection) throws SQLException {

        String sep = "\\";
        Double tempEuros = 0.0;
        int i = 0;

        // Charge les historiques de mouvements correspondants aux fichiers importés
        String sqlHistorique = "select num,compte,Date,seqinsameday,Libelle,euros,cumul_euros_calc,cumul_euros_saisi, fichier_import "
                + " from historique "
                + " order by compte,Date asc,seqinsameday desc";
        try (PreparedStatement statementHistorique = connectionDB.prepareStatement(sqlHistorique,
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                ResultSet rsHistorique2 = statementHistorique.executeQuery();) {
            while (rsHistorique2.next()) {
                i = i + 1;
                String compte = rsHistorique2.getString("compte");
                Date date = rsHistorique2.getDate("date");
                int seqinsameday = rsHistorique2.getInt("seqinsameday");
                Double euros = rsHistorique2.getDouble("euros");
                Double cumulEurosCalc = rsHistorique2.getDouble("cumul_euros_calc");
                Double cumulEurosSaisi = rsHistorique2.getDouble("cumul_euros_saisi");

                tempEuros = tempEuros + euros;

                WriteFile.writeFile("INFO", i + " -------------> " + CalculCumuls.class.getName()
                        + "AVANT : date: " + date
                        + ", seqinsameday: " + seqinsameday
                        + ", euros: " + euros
                        + ", cumulEurosCalc: " + cumulEurosCalc
                        + ", tempEuros: " + tempEuros.floatValue()
                        + ", cumulEurosSaisi: " + cumulEurosSaisi, logFile);

                // indique dasn l'historique des mouvements la situation indiquée dans le
                // fichier importé
                rsHistorique2.updateDouble("cumul_euros_calc", tempEuros.floatValue());
                rsHistorique2.updateInt("num", i);
                rsHistorique2.updateRow();
                /*
                 * WriteFile.writeFile("INFO", "-------------> " + CalculCumuls.class.getName()
                 * + "APRES : date: "+date
                 * +", seqinsameday: "+seqinsameday
                 * +", euros: "+euros
                 * +", cumulEurosCalc: "+cumulEurosCalc
                 * +", tempEuros: "+tempEuros
                 * +", cumulEurosSaisi: "+cumulEurosSaisi, logFile);
                 */
            }
            rsHistorique2.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void calculCumulEurosSaisiFromImportFiles(Connection connection) throws SQLException {

        String sep = "\\";

        // Charge les fichiers importés
        String sqlImport = "SELECT compte,repertoire,fichier, date_situation,euros_situation,date_first_mouvement,date_last_mouvement FROM banque.import_fichiers where compte ='65022812330' and is_imported is true order by date_situation asc";
        try (PreparedStatement statementImport = connectionDB.prepareStatement(sqlImport,
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                ResultSet rsImport = statementImport.executeQuery();) {
            while (rsImport.next()) {
                String compteImport = rsImport.getString("compte");
                Date dateLastMouvementImport = rsImport.getDate("date_last_mouvement");
                Double eurosSituationImport = rsImport.getDouble("euros_situation");
                String repertoireImport = rsImport.getString("repertoire");
                String fichierImport = rsImport.getString("fichier");

                // WriteFile.writeFile("INFO", "-------------> " + CalculCumuls.class.getName()
                // + ": fichierImport " + fichierImport, logFile);

                // Charge les historiques de mouvements correspondants aux fichiers importés
                String sqlHistorique = "select compte,Date,seqinsameday,Libelle,euros,cumul_euros_calc,cumul_euros_saisi, fichier_import "
                        + " from historique "
                        + " where compte='" + compteImport + "' "
                        + " and date=date('" + dateLastMouvementImport + "') "
                        + " and fichier_import='" + repertoireImport.replace(sep, sep + sep) + sep + sep + fichierImport
                        + "' "
                        + " and seqinsameday=1 "
                        + " order by compte,Date desc,seqinsameday asc";
                // WriteFile.writeFile("INFO", "-------------> " + CalculCumuls.class.getName()
                // + ": sqlHistorique " + sqlHistorique, logFile);
                try (PreparedStatement statementHistorique = connectionDB.prepareStatement(sqlHistorique,
                        ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                        ResultSet rsHistorique = statementHistorique.executeQuery();) {
                    while (rsHistorique.next()) {
                        String compte = rsHistorique.getString("compte");
                        Date date = rsHistorique.getDate("date");
                        int seqinsameday = rsHistorique.getInt("seqinsameday");
                        Double euros = rsHistorique.getDouble("euros");
                        // indique dasn l'historique des mouvements la situation indiquée dans le
                        // fichier importé
                        rsHistorique.updateDouble("cumul_euros_saisi", eurosSituationImport);
                        rsHistorique.updateRow();

                        // WriteFile.writeFile("INFO", "-------------> " + CalculCumuls.class.getName()
                        // + ": sep " + repertoireImport.replace(sep, sep + sep) + sep + sep +
                        // fichierImport, logFile);

                    }
                    rsHistorique.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }

        }
    }
}
