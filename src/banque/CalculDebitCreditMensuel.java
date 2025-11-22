/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package banque;

import static banque.ImportCSV.connectionDB;
import static banque.ImportCSV.getDateSqlFormat;
import static banque.ImportCSV.logFile;
import static banque.ImportCSV.truncateDataTemp;
import static banque.ImportCSV.truncateHistorique;
import static banque.ImportCSV.truncateImportFichiers;
import com.opencsv.exceptions.CsvException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Properties;

/**
 *
 * @author Yann
 */
public class CalculDebitCreditMensuel {

    // Méthode proncipale
    public static void main(String[] args) throws IOException, SQLException, ParseException {

        WriteFile.writeFile("INFO", "-------------> Calcul somme débits crédits mensuels début Main()", logFile);

        // variables de connexion à la base Mysql
        GetBanqueProperties.main(null);
        Properties dbConnProperties = GetBanqueProperties.prop;
        String host = dbConnProperties.getProperty("host");
        String base = dbConnProperties.getProperty("base");
        String username = dbConnProperties.getProperty("username");
        String password = dbConnProperties.getProperty("password");

        // Ouvre la connexion à la base
        connectionDB = DBConnection.DBConnection(host, base, username, password);

        truncateSommeDebitCreditMensuels(connectionDB);

        SimpleDateFormat format_annee = new SimpleDateFormat("yyyy");
        SimpleDateFormat format_mois = new SimpleDateFormat("MM");
        int annee = 0;
        int mois = 0;
        Double solde=0.0;
        Double credit = 0.0;
        Double debit = 0.0;

        // Charge les historiques de mouvements correspondants aux fichiers importés
        String sqlHistorique = "select num,compte,Date,seqinsameday,Libelle,euros,cumul_euros_calc,cumul_euros_saisi, fichier_import "
                + " from historique "
                //                + " where num<1900 "
                + " order by compte,Date asc,seqinsameday desc";
        try ( PreparedStatement statementHistorique = connectionDB.prepareStatement(sqlHistorique,
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);  ResultSet rsHistorique = statementHistorique.executeQuery();) {
            while (rsHistorique.next()) {
                String compte = rsHistorique.getString("compte");
                Date date = rsHistorique.getDate("date");
                int seqinsameday = rsHistorique.getInt("seqinsameday");
                Double euros = rsHistorique.getDouble("euros");
                Double cumulEurosCalc = rsHistorique.getDouble("cumul_euros_calc");
                Double cumulEurosSaisi = rsHistorique.getDouble("cumul_euros_saisi");

                if ((Integer.parseInt(format_mois.format(date)) != mois || Integer.parseInt(format_annee.format(date)) != annee)) {
                    WriteFile.writeFile("INFO", "-------------> date: " + date + ", annee: " + annee + ", mois: " + mois + ", cumulEurosCalc: " + cumulEurosCalc + ",  debit: " + debit + ", credit: " + credit, logFile);

                    if (debit < 0) {
                        debit = debit * -1.0;
                    };
                    
                    solde=credit-debit;

                    // WriteFile.writeFile("INFO", "-------------> insert =>  debit: " + debit + " - credit: " + credit, logFile);

                    MajImportFichiers(connectionDB, compte, mois, annee, debit, credit, solde);
                    
                    
                    annee = Integer.parseInt(format_annee.format(date));
                    mois = Integer.parseInt(format_mois.format(date));
                    credit = 0.0;
                    debit = 0.0;
                    solde=0.0;

                }

                if (euros >= 0.0) {
                    credit = credit + euros;
                } else {
                    debit = debit + euros;
                };

                annee = Integer.parseInt(format_annee.format(date));
                mois = Integer.parseInt(format_mois.format(date));

                //WriteFile.writeFile("INFO", "-------------> date: "+date+", annee: "+annee+", mois: "+mois, logFile);
            }
        }

        WriteFile.writeFile("INFO", "-------------> Calcul somme débits crédits mensuels fin Main()", logFile);

    }

    public static void truncateSommeDebitCreditMensuels(Connection connection) throws SQLException {
        // force le commit automatique
        connection.setAutoCommit(true);
        String sql = "Truncate table Somme_Debit_Credit_Mensuels";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.addBatch();
        statement.executeBatch();
    }

    public static void MajImportFichiers(Connection connection, String compte, int mois, int annee, Double sommeDebits, Double sommeCredits, Double solde) throws SQLException {
        int count = 0;
        int batchSize = 20;

        String sql = "insert into Somme_Debit_Credit_Mensuels (Compte, mois, annee, Somme_Debits, Somme_Credits, solde) " 
                    + "values  "
                    + "(?, ?, ?, ?, ?, ?) " ;
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, compte);
        statement.setInt(2, mois);
        statement.setInt(3, annee);
        statement.setDouble(4, sommeDebits);
        statement.setDouble(5, sommeCredits);
        statement.setDouble(6, solde);

        // execute the remaining queries
        statement.addBatch();
        if (count % batchSize == 0) {
            statement.executeBatch();
        }

    }

}
