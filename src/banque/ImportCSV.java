/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package banque;

/**
 *
 * @author Yann
 */
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.Connection;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

// http://opencsv.sourceforge.net/
public class ImportCSV {

    // variables
    //public static String csvFilePath = "C:\\Users\\Yann\\Documents\\Banque\\dev\\csv\\CA_Historique.CSV";
    public static String previousDate = "";
    public static int iSeqInSameDay = 1;
    public static Connection connectionDB = null;
    public static Pattern pattern;
    public static Matcher matcher;
    public static Boolean recursivePath = true;
    //public static String patternAvalaibleFileName = "(?i)^(CA)(.*)(\\.csv)$";
    public static int indexRegistre = 0;
    public static String rootPath = "C:\\Users\\Yann\\Documents\\Banque\\dev\\csv\\";
    public static File logFile = new File(rootPath + "log.txt");

    // Méthode proncipale
    public static void main(String[] args) throws FileNotFoundException, IOException, SQLException, CsvException, ParseException {

        WriteFile.writeFile("INFO", "-------------> ImportCSV début Main()", logFile);
        JOptionPane.showMessageDialog(null, "Début d'import");
        
        
        // variables de connexion à la base Mysql
        GetBanqueProperties.main(null);
        Properties dbConnProperties=GetBanqueProperties.prop;
        String host = dbConnProperties.getProperty("host");
        String base = dbConnProperties.getProperty("base");
        String username = dbConnProperties.getProperty("username");
        String password = dbConnProperties.getProperty("password");

        // Ouvre la connexion à la base
        connectionDB = DBConnection.DBConnection(host, base, username, password);
        
        // Réinitialise la LOG
        WriteFile.deleteFile(logFile.getAbsolutePath());
        WriteFile.createFile(logFile.getAbsolutePath());
        
        // Charge la configuration des imports des fichiers csv
        String sql = "select compte,n°,repertoire,patternFichier, patternEnTete,patternCompte,patternSolde from import_configuration";
        WriteFile.writeFile("INFO", ImportCSV.class.getName() + "sql : " + sql, logFile);

        // Purge les tables d'import avant de relancer 
        truncateDataTemp(connectionDB);
        //truncateHistorique(connectionDB);
        //truncateImportFichiers(connectionDB);

        try ( PreparedStatement statement = connectionDB.prepareStatement(sql,
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);  ResultSet rs = statement.executeQuery();) {

                while (rs.next()) {
                    String compte=rs.getString("compte");
                    Double num = rs.getDouble("n°");
                    String repertoire=rs.getString("repertoire");
                    String patternFichier=rs.getString("patternFichier");
                    String patternEnTete=rs.getString("patternEnTete");
                    String patternCompte=rs.getString("patternCompte");
                    String patternSolde=rs.getString("patternSolde");
                    
                    WriteFile.writeFile("INFO", "-------------> config: compte: " 
                            + compte 
                            + ", n°: "+num 
                            + ", patternFichier: "+patternFichier
                            + ", patternEnTete: "+patternEnTete
                            + ", patternCompte: "+patternCompte
                            + ", patternSolde: "+patternSolde, logFile);
           
                    // Charge les fichiers à importer
                    HashMap<BigDecimal, File> hmapFiles = loadFiles(connectionDB, repertoire, patternFichier,compte);
                    // Purge les tables d'import avant de relancer 
                    truncateDataTemp(connectionDB);
                    truncateHistorique(connectionDB);
                    truncateImportFichiers(connectionDB);
      
                    // Import des données des fichiers CSV dans la base Mysql                    
                    importData(connectionDB, hmapFiles,patternEnTete,patternCompte,patternSolde);
                    consolide_Data(connectionDB);
                    
            }
        
        // Ecrit les données importées dans la table "historique"
        consolide_Data(connectionDB);
        
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Ferme la connexion à la base
        connectionDB.close();
        
        WriteFile.writeFile("INFO", "-------------> ImportCSV fin Main()", logFile);
        JOptionPane.showMessageDialog(null, "Fin d'import");
    }

    /* 
	--------------------------------------------------------------------------------
	Fonction: Charge la liste des CSV à importer
	--------------------------------------------------------------------------------
     */
    public static HashMap loadFiles(Connection connection,String dir, String patternFichier, String compte) throws IOException, SQLException {
        File file = new File(dir);
        File[] files = file.listFiles();
        HashMap<Integer, File> hmapFiles = new HashMap<Integer, File>();
        if (files != null) {
            // D'abord on liste tous les fichiers
            for (int i = 0; i < files.length; i++) {
                // Dans le cas d'un fichier de type "répertoire", on fait un appel récursif
                if (files[i].isDirectory() == true && recursivePath == true) {
                    loadFiles(connection,files[i].getAbsolutePath(),patternFichier,compte);
                // Si c'est un fichier
                } else {
                    System.out.println("banque.ImportCSV.loadFiles(): fichier : " + files[i].getName().toString() + " - pattern: "+patternFichier);
                    if (!"".equals(getRegexp(patternFichier,files[i].getName().toString(),0))) {
                        if (isFileExists(connection, files[i].getName().toString())!=true) {
                            hmapFiles.put(i, files[i]);
                            MajImportFichiers(
                                    connection, // connection
                                    compte, // compte
                                    files[i].getName(), // fichier
                                    dir, // repertoire
                                    null, // dateSituation
                                    0.0, // eurosSituation
                                    null, // dateFirstMouvement
                                    null, // dateLastMouvement
                                    false, // isImported
                                    0.0, // nbMvtsImportes
                                    null, // errTxt
                                    0.0, // fileSize
                                    null, // fileCreation
                                    null, // fileLastUpdate
                                    new Timestamp(System.currentTimeMillis()) // dtImport
                                );
                            
                            WriteFile.writeFile("INFO", "pattern de fichier valide et jamais importé: " + files[i], logFile);
                        } else {
                            WriteFile.writeFile("INFO", "pattern de fichier valide et mais déjà importé, on ne le reprend pas: " + files[i], logFile);
                        }
                    }
                }
            }
        }
        return hmapFiles;
    }
    
    /* 
	--------------------------------------------------------------------------------
	Fonction: Retourne la chaine du groupe regexp qui match avec le pattern en argument
	--------------------------------------------------------------------------------
     */
    public static String getRegexp(String pattern, String str, int groupNum) {
        String sReturn = "";
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        if (matcher.matches()) {
            sReturn = matcher.group(groupNum).trim();
        }
        return sReturn;
    }
    /* 
	--------------------------------------------------------------------------------
	Fonction: Retourne VRAI si le fichier en argument a déjà été importé
	--------------------------------------------------------------------------------
     */
    public static boolean isFileExists(Connection connection, String fichier) throws SQLException {
        boolean bReturn = false;
        String sql = "select count(1) as count from import_fichiers where fichier='"+fichier+"'";
        WriteFile.writeFile("INFO", ImportCSV.class.getName() + "sql : " + sql, logFile);
        try ( PreparedStatement statement = connection.prepareStatement(sql,
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);  ResultSet rs = statement.executeQuery();) {
            while (rs.next()) {
                Double fNbRows = rs.getDouble("count");
                WriteFile.writeFile("INFO", "-------------> isFichierExists fNbRows: " + fNbRows, logFile);
                if (!fNbRows.equals(0.0)) {
                    bReturn = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bReturn;
    }        
            
    public static boolean isHistoriqueExists(Connection connection, String compte, Date date, String libelle, Double euros, String fichierImport) throws SQLException {
        boolean bReturn = false;
        //String sql = "select count(1) as count from historique where compte='"+compte+"' and date=to_date('"+date+"','yyyy-mm-dd hh24:mi:ss') and libelle='"+libelle+"' and euros="+euros;
        // String sql = "select count(1) as count from historique where compte='" + compte + "' and date='" + date + "' and libelle='" + libelle.replace("'", " ") + "' and euros=" + euros;
        String sql = "select count(1) as count from historique where "
                + " compte='" + compte + "' "
                + " and date='" + date + "' "
                + " and euros=" + euros + " "
                + "and fichier_import !='"+fichierImport.replace("\\","\\\\")+"'";
        WriteFile.writeFile("INFO", ImportCSV.class.getName() + "sql : " + sql, logFile);
        try ( PreparedStatement statement = connection.prepareStatement(sql,
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);  ResultSet rs = statement.executeQuery();) {
            while (rs.next()) {
                Double fNbRows = rs.getDouble("count");
                WriteFile.writeFile("INFO", "-------------> isHistoriqueExists fNbRows: " + fNbRows, logFile);
                if (!fNbRows.equals(0.0)) {
                    bReturn = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bReturn;
    }

        public static void MajImportFichiers(Connection connection, String compte,String fichier,String repertoire,String dateSituation,Double eurosSituation,Date dateFirstMouvement,Date dateLastMouvement,Boolean isImported,Double nbMvtsImportes,String errTxt,Double fileSize,Timestamp fileCreation,Timestamp fileLastUpdate,Timestamp dtImport) throws SQLException {
        int count = 0;
        int batchSize = 20;
        String sql = "select count(1) as count from import_fichiers where compte='" + compte + "' and fichier='" + fichier + "'";
        try ( PreparedStatement statement = connection.prepareStatement(sql,
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);  ResultSet rs = statement.executeQuery();) {
            while (rs.next()) {
                Double fNbRows = rs.getDouble("count");
                
                // Si le registre n'existe pas, alors on fait un INSERT
                if (!fNbRows.equals(0.0)) {
                    WriteFile.writeFile("INFO", ImportCSV.class.getName()+" le registre existe déjà - compte='" + compte + "' and fichier='" + fichier + "' - count: " + fNbRows, logFile);
                    String sql2 = "update import_fichiers set Repertoire=?, Date_Situation=?, Euros_Situation=?, Date_First_Mouvement=?, Date_Last_Mouvement=?, Is_Imported=?, "                           
                    + "Nb_Mvts_Importes=?, Err_Txt=?, File_Size=?, File_Creation=?, File_Last_Update=?, Dt_Import=?"
                    + "Where  Compte=? and Fichier=? "
                    ;                    
                    PreparedStatement statement2 = connection.prepareStatement(sql2);
                    statement2.setString(13, compte);
                    statement2.setString(14, fichier);
                    statement2.setString(1, repertoire);                
                    statement2.setDate(2, getDateSqlFormat(dateSituation));
                    statement2.setDouble(3, eurosSituation);
                    statement2.setDate(4, dateFirstMouvement);
                    statement2.setDate(5, dateLastMouvement);
                    statement2.setBoolean(6, isImported);
                    statement2.setDouble(7, nbMvtsImportes);
                    statement2.setString(8, errTxt);
                    statement2.setDouble(9, fileSize);
                    statement2.setTimestamp(10, fileCreation);
                    statement2.setTimestamp(11, fileLastUpdate);
                    statement2.setTimestamp(12, dtImport);

                    // execute the remaining queries
                    statement2.addBatch();
                    if (count % batchSize == 0) {
                        statement2.executeBatch();
                    }
                    
              // Si le registre existe déjà, alors on fait un UPDATE
              } else {
                    WriteFile.writeFile("INFO", ImportCSV.class.getName()+" le registre n'existe pas - compte='" + compte + "' and fichier='" + fichier + "' - count: " + fNbRows, logFile);
                    String sql2 = "insert into import_fichiers (Compte, Fichier, Repertoire, Date_Situation, Euros_Situation, Date_First_Mouvement, Date_Last_Mouvement, "
                    + "Is_Imported, Nb_Mvts_Importes, Err_Txt, File_Size, File_Creation, File_Last_Update, Dt_Import) " 
                    + "values  "
                    + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " 
                    ;
                    PreparedStatement statement2 = connection.prepareStatement(sql2);
                    statement2.setString(1, compte);
                    statement2.setString(2, fichier);
                    statement2.setString(3, repertoire);                
                    statement2.setDate(4, getDateSqlFormat(dateSituation));
                    statement2.setDouble(5, eurosSituation);
                    statement2.setDate(6, dateFirstMouvement);
                    statement2.setDate(7, dateLastMouvement);
                    statement2.setBoolean(8, isImported);
                    statement2.setDouble(9, nbMvtsImportes);
                    statement2.setString(10, errTxt);
                    statement2.setDouble(11, fileSize);
                    statement2.setTimestamp(12, fileCreation);
                    statement2.setTimestamp(13, fileLastUpdate);
                    statement2.setTimestamp(14, dtImport);

                    // execute the remaining queries
                    statement2.addBatch();
                    if (count % batchSize == 0) {
                        statement2.executeBatch();
                    }
                }                
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void truncateHistorique(Connection connection) throws SQLException {
        // force le commit automatique
        connection.setAutoCommit(true);
        String sql = "Truncate table historique";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.addBatch();
        statement.executeBatch();
    }
            
    public static void truncateImportFichiers(Connection connection) throws SQLException {
        // force le commit automatique
        connection.setAutoCommit(true);
        String sql = "Truncate table import_fichiers";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.addBatch();
        statement.executeBatch();
    }
                    
        
    public static void truncateDataTemp(Connection connection) throws SQLException {
        // force le commit automatique
        connection.setAutoCommit(true);
        String sql = "Truncate table import_data_temp";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.addBatch();
        statement.executeBatch();
    }

//    public static void convertFileToUtf8(String file) throws IOException {
//        Path p = Paths.get(file);
//        ByteBuffer bb = ByteBuffer.wrap(Files.readAllBytes(p));
//        CharBuffer cb = Charset.forName("UTF-8").decode(bb);
//        bb = Charset.forName("UTF-8").encode(cb);
//        Files.write(p, bb.array());
//    }

    public static void importData(Connection connection, HashMap hmapFiles, String patternEnTete,String patternCompte,String patternSolde) throws FileNotFoundException, IOException, CsvException, SQLException, ParseException {
        //https://mkyong.com/java/how-to-read-and-parse-csv-file-in-java/
        int batchSize = 20;
        indexRegistre = getMaxIndexTableTemp(connection);
        String csvDateMvt = "";
        String csvLibelle = "";
        String csvEurosDebit = "";
        String csvEurosCredit = "";
        boolean bIsMvtOkToImport = false;
        boolean bFlagEndFile = false;
        String regexDate="(?i)^([0-9]*)(\\/)([0-9]*)(\\/)([0-9]*)$";

        // calcule la date du jour            
        LocalDateTime localDate = LocalDateTime.now(); // fixed: LocalDateTime
        DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");

        // force le commit automatique
        connection.setAutoCommit(true);

		// Prépare la syntaxe d'INSERT du mouvement
        String sql = "INSERT INTO import_data_temp (n°,compte,date,libelle,euros,seqInSameDay,fichier_import,dt_create) VALUES (?, ?, ?, ?, ?, ? ,?, ?)";
        PreparedStatement statement = connection.prepareStatement(sql);

        // =============================== //
        // === Boucle sur les fichiers === //
        // =============================== //
        for (Iterator fileIterator = hmapFiles.entrySet().iterator(); fileIterator.hasNext();) {
            Map.Entry fileMapEntry = (Map.Entry) fileIterator.next();

            File fileToRead= new File(fileMapEntry.getValue().toString());
            BasicFileAttributes fileToReadAttributes = Files.readAttributes(fileToRead.toPath(), BasicFileAttributes.class);
            
            // Les informations valables pour toutes les lignes du fichier courant
            String csvCompte = ""; // le numero de compte
            String csvSoldeEuros = ""; // Le solde de compte à la date de situation
            String csvDateSituation = ""; // La date de situation 
            String csvFichier = new File(fileMapEntry.getValue().toString()).getName(); // Le nom du fichier en court de lecture
            String csvRepertoire = new File(fileMapEntry.getValue().toString()).getParent(); // Le répertoire du fichier en court de lecture
            Double csvFileSize = Double.valueOf(Files.size(fileToRead.toPath())) ; // la taille du fichier
            Timestamp csvFileLastUpdate = new Timestamp(fileToReadAttributes.lastModifiedTime().toMillis()) ; // la date de dernière modification du fichier
            Timestamp csvFileCreation = new Timestamp(fileToReadAttributes.creationTime().toMillis()); // la date de création du fichier
            Double nbMvtsImportes = 0.0; // le nombre de mouvements importés
            Date dateFirstMouvement=null; 
            Date dateLastMouvement=null; 
            // ========================================= //
            // === Charge en mémoire tout le fichier === //
            // ========================================= //       
            
            try ( CSVReader reader = new CSVReader(new FileReader(fileToRead.toString()))) {
                int count = 0;
                int ligneNbr=0;
                bIsMvtOkToImport = false;
                bFlagEndFile = false;
                WriteFile.writeFile("INFO", " ################## debut lecture contenu du fichier courant : " + fileToRead.toString() + ", indexRegistre: " + indexRegistre, logFile);

                List<String[]> r = reader.readAll();

                // ========================================== //
                // === Boucle sur chaque ligne du fichier === //
                // ========================================== //
                for (String[] row : r) {

                    String ligneTmp = Arrays.toString(row);                    
                    ligneNbr=ligneNbr+1;
                    // On formate la ligne en cours de lecture en UTF8
                    byte[] ligneBytes = ligneTmp.getBytes();
                    String ligne = new String(ligneBytes, StandardCharsets.UTF_8);

                    ligne = ligne.replaceAll("^\\[", ""); // on enlève le caractère "[" de début de ligne;
                    ligne = ligne.replaceAll("\\]$", ""); // on enlève le caractère "]" de fin de ligne;

                    // Si la ligne n'est pas vide                            
                    if ((!"".equals(ligne.replace(" ", ""))) && (!ligne.isEmpty()) && (!ligne.replace(" ", "").equals("[]"))) {

                        ligne = ligne.replaceAll("\"", ""); // on enlève les guillemets;        
                        ligne = ligne.replaceAll(";;", "; ;"); // force un espace entre 2 ";" si champ vide (sion le "length" ne le compte pas !!)

                        // Le numéro de compte, la valeur du solde et la date de situation sont indiqués en début de fichier avant les historiques de mouvements
                        if (bIsMvtOkToImport == false  && (bFlagEndFile == false)) {
					
                            WriteFile.writeFile("INFO", " ligne: "+ligne, logFile);
                            
                            // Cherche le numero de compte
                            if (csvCompte =="" ) {
                                csvCompte = getRegexp(patternCompte,ligne,4);
                                if (csvCompte!="") {
                                    csvCompte=csvCompte.replace(" ","");
                                }
                                WriteFile.writeFile("INFO", " Compte: "+csvCompte, logFile);
                            }    
                            // Cherche le solde à la date de situation
                            if (csvSoldeEuros =="" ) {
                                csvSoldeEuros = getRegexp(patternSolde,ligne,4);
                                csvSoldeEuros = csvSoldeEuros.replaceAll(" ", "");
                                csvSoldeEuros = csvSoldeEuros.replaceAll("�", ""); // 
                                csvSoldeEuros = csvSoldeEuros.replaceAll(" ", "");                                
                                csvSoldeEuros = csvSoldeEuros.replaceAll(" ", ""); // ceci est un "espace/blanc"
                                csvSoldeEuros = csvSoldeEuros.replace(",", ".");
                                csvSoldeEuros = csvSoldeEuros.replaceAll("[^0-9.]", "");
                                WriteFile.writeFile("INFO", " Solde: "+csvSoldeEuros, logFile);                                                              
                            }                             
                            // Cherche la date de situation du fichier
                            if (csvDateSituation =="" ) {                            
                                csvDateSituation=getRegexp(patternSolde,ligne,2);
                                String tmpJour=getRegexp(regexDate,csvDateSituation,1);
                                String tmpMois=getRegexp(regexDate,csvDateSituation,3);
                                String tmpAnnee=getRegexp(regexDate,csvDateSituation,5);   
                                // WriteFile.writeFile("INFO", " csvDateSituation (avant): "+csvDateSituation, logFile);
                                if (tmpAnnee.length()==2) {
                                    tmpAnnee="20"+tmpAnnee;
                                }
                                if (tmpJour!="" & tmpMois!="" & tmpAnnee!="") {
                                    csvDateSituation=tmpJour+"/"+tmpMois+"/"+tmpAnnee;
                                }                                
                                // WriteFile.writeFile("INFO", " csvDateSituation (apres): "+csvDateSituation, logFile);                                
                            }        
                        }
                        
                        // La ligne d'en-tête a déjà été trouvée et pas la fin de fichier, on importe la ligne
                        if ((bIsMvtOkToImport == true) && (bFlagEndFile == false)) {

                            // WriteFile.writeFile("INFO", " ========= ligne OK dans en tête", logFile);

                            // Contournement sur BUG de format du fichier lorsque la ligne d'historique ne se termine pas par ";" (dans le cas des mouvements de crédit !!)
                            // Ici on force un 4eme ";" pour remmettre dans le bon format du CSV (car un mouvement "Crédit Agricole" contient 4 séparateurs ";")
                            if (ligne.split(";").length == 3) {
                                ligne = ligne + " ;";
                            }

                            // Split la ligne en éléments avec séparateur ";"
                            String[] data = ligne.split(";");

                            // Une ligne conforme contient 4 champs
                            if (data.length >= 4) {
                                
                                csvDateMvt = data[0]; // Le champs n°1 : la date du mouvement								
                                // /!\ Le champs "libellé" peut contenir des ";" (dans ce cas il ne faut pas l'interpréter comme un séparateur de champ). 
                                // Dans ce cas on "merge" tous les champs qui constituent le "libellé", entre le champs 2 et l'avant dernier champs
                                // Pour rappel, le champ1 est "Date", le champ2 est "libelle", le champs avant dernier est "debit", champs dernier "credit"
                                csvLibelle="";
                                for (int u=1; u<=data.length-3; u++) {
                                    csvLibelle=csvLibelle+data[u];
                                }
                                csvLibelle=csvLibelle.replace("'"," "); // Le champs n°3 : si le mouvement est un débit								
                                csvLibelle=csvLibelle.replace(";"," "); // Le champs n°3 : si le mouvement est un débit            
                                csvEurosDebit = data[data.length-2].replaceAll(" ", ""); // !! attention ceci n'est pas un "espace/blanc"
                                csvEurosDebit = csvEurosDebit.replaceAll("�", ""); // 
                                csvEurosDebit = csvEurosDebit.replaceAll(" ", ""); // ceci est un "espace/blanc"								
                                csvEurosCredit = data[data.length-1].replaceAll(" ", ""); // Le champs n°4 : si le mouvement est un crédit
                                csvEurosCredit = csvEurosCredit.replaceAll("�", ""); // 
                                csvEurosCredit = csvEurosCredit.replaceAll(" ", ""); // ceci est un "espace/blanc"

                                // On complète la syntaxe d'INSERT du mouvement avec les champs CSV récupérés
                                try {

                                    // la PK n° (champs technique)
                                    indexRegistre = indexRegistre + 1;
                                    Double sqlDoubleIndex = Double.valueOf(indexRegistre);
                                    statement.setDouble(1, sqlDoubleIndex);

                                    // compte
                                    statement.setString(2, csvCompte);

                                    // la date du mouvement      
                                    statement.setDate(3, getDateSqlFormat(csvDateMvt));

                                    // le libelle du mouvement
                                    statement.setString(4, csvLibelle);

                                    // la valeur du mouvement (soit une crédit soit un débit)
                                    WriteFile.writeFile("INFO", "csvEurosCredit: "+csvEurosCredit + " - csvEurosDebit: "+csvEurosDebit, logFile);                                    
                                    Double sqlDoubleEuros;
                                    if (csvEurosDebit.isEmpty()) {
                                        sqlDoubleEuros = Double.valueOf(csvEurosCredit.replace(",", ".").replace("�", ""));
                                    } else {
                                        sqlDoubleEuros = Double.valueOf(csvEurosDebit.replace(",", ".").replace("�", "")) * -1;
                                    }
                                    statement.setDouble(5, sqlDoubleEuros);

                                    // on séquence les mouvements ayant la même date de mouvement
                                    if (previousDate.equals(csvDateMvt)) {
                                        iSeqInSameDay = iSeqInSameDay + 1;
                                    } else {
                                        iSeqInSameDay = 1;
                                    }
                                    previousDate = csvDateMvt;
                                    Double sqlDoubleSeqInSameDay = Double.valueOf(iSeqInSameDay);
                                    statement.setDouble(6, sqlDoubleSeqInSameDay);

                                    // le fichier d'import                                
                                    statement.setString(7, fileToRead.toString());

                                    // date de l'import    
                                    Timestamp sqlNow = new java.sql.Timestamp(System.currentTimeMillis());
                                    statement.setTimestamp(8, sqlNow);
                                    
                                    // nombre de mouvements importés
                                    nbMvtsImportes=nbMvtsImportes+1;

                                    // execute the remaining queries
                                    statement.addBatch();
                                    if (count % batchSize == 0) {
                                        statement.executeBatch();                                        
                                    }                                   
                                    
                                    // Récupère les dates de premier et de dernier mouvement pour la tables d' import des fichiers
                                    if (dateLastMouvement==null) {
                                        dateLastMouvement=getDateSqlFormat(csvDateMvt); // la date de premier mouvement
                                    }
                                    dateFirstMouvement=getDateSqlFormat(csvDateMvt); // la date de dernier mouvement
                                    

                                } catch (SQLException ex) {
                                    WriteFile.writeFile("ERROR", ImportCSV.class.getName() + ": " + ex, logFile);
                                    Logger.getLogger(ImportCSV.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            } else {
                                    WriteFile.writeFile("WARNING", ImportCSV.class.getName() + ": ligne non conforme car data.length <> de 5 - data.length: "+data.length +" - ligne :"+ligne, logFile);
                            }

                        } // La ligne d'en-tête a déjà été trouvée
                            
                        // on cherche la première ligne des mouvements => le marqueur de début d'import des mouvements est la ligne des en-têtes de colonnes 
                        if (getRegexp(patternEnTete,ligne,1)!="") {
                            if (bIsMvtOkToImport == false) {
                                WriteFile.writeFile("INFO", "La ligne d'en tête a été trouvée, on commence l'import de ce fichier", logFile);                            
                                bIsMvtOkToImport = true;
                        // Une seconde ligne d'en tête est trouvée (-> c'est le détail des mvts carte, il ne faut les prendre)
                            } else {
                                WriteFile.writeFile("INFO", "Une seconde ligne d'en tête a été trouvée, on arrête l'import de ce fichier", logFile);
                                //bIsMvtOkToImport = false;
                                bFlagEndFile = true; 
                            }
                        }

                        // on cherche la fin des mouvements à importer lorsque la ligne n'est plus formattée de 4 séparateurs ";"
                        if ((ligne.split(";").length < 3) && (bIsMvtOkToImport == true)) {
                            WriteFile.writeFile("INFO", "Le formatage de la ligne indique une fin de fichier, on arrête l'import de ce fichier", logFile);
                            bIsMvtOkToImport = false;
                            bFlagEndFile = true;
                        }
                     
                    } // Si la ligne n'est pas vide 
                        WriteFile.writeFile("INFO", " ========= ("+ligneNbr+") bIsMvtOkToImport: "+bIsMvtOkToImport+", bFlagEndFile: "+bFlagEndFile, logFile);
                } // fin de boucle sur les lignes
                
                // Met à jour la table des imports de fichiers
                MajImportFichiers(
                        connection, // connection
                        csvCompte, // compte
                        csvFichier, // fichier
                        csvRepertoire, // repertoire
                        csvDateSituation, // dateSituation
                        Double.valueOf(csvSoldeEuros), // eurosSituation
                        dateFirstMouvement, // dateFirstMouvement
                        dateLastMouvement, // dateLastMouvement
                        true, // isImported
                        nbMvtsImportes, // nbMvtsImportes
                        null, // errTxt
                        csvFileSize, // fileSize
                        csvFileCreation, // fileCreation
                        csvFileLastUpdate, // fileLastUpdate
                        new Timestamp(System.currentTimeMillis())  // dtImport
                    );
                  
                //System.out.println(" ################## fin lecture contenu du fichier courant : " + fileMapEntry.getValue().toString());
                WriteFile.writeFile("INFO", " ################## fin lecture contenu du fichier courant : " + fileToRead.toString(), logFile);

            } // fin du try de CSVReader mise en mémoire du fichier courant

        } // fin de boucle sur les fichiers

        WriteFile.writeFile("INFO", "fin import data temp", logFile);
    }



    public static void consolide_Data(Connection connection) throws FileNotFoundException, IOException, CsvException, SQLException {
        int batchSize = 20;
        int count = 0;
        connection.setAutoCommit(true);
        String sqlDataTemp = "select * from Import_Data_Temp order by compte, Fichier_Import, Date asc, SeqInSameDay asc";
        try (
                 PreparedStatement statement = connection.prepareStatement(sqlDataTemp,
                        ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);  ResultSet rs = statement.executeQuery();) {
            while (rs.next()) {
                // lecture des données de la table TEMP
                String fCompte = rs.getString("Compte");
                Date fDate = rs.getDate("date");
                String fLibelle = rs.getString("libelle");
                Double fEuros = rs.getDouble("euros");
                Double fSeqInSameDay = rs.getDouble("seqInSameDay");
                String fFichierImport = rs.getString("Fichier_Import");
                Timestamp fDtCreate = rs.getTimestamp("dt_create");
                System.out.println("fDate: " + fDate + ", fLibelle: " + fLibelle + ", fEuros: " + fEuros + ", fFichierImport: " + fFichierImport + ", fCompte: " + fCompte + ", fSeqInSameDay: " + fSeqInSameDay + ", dt_create: " + fDtCreate);

                if (isHistoriqueExists(connection, fCompte, fDate, fLibelle, fEuros, fFichierImport) != true) {
                    // Ecriture des données TEMP dans la table cible
                    String sql = "INSERT INTO Historique (compte,date,libelle,euros,seqInSameDay,fichier_import,dt_create) VALUES (?, ?, ?, ?, ? ,?, ?)";
                    PreparedStatement statement2 = connection.prepareStatement(sql);
                    statement2.setString(1, fCompte);
                    statement2.setDate(2, fDate);
                    statement2.setString(3, fLibelle);
                    statement2.setDouble(4, fEuros);
                    statement2.setDouble(5, fSeqInSameDay);
                    statement2.setString(6, fFichierImport);
                    statement2.setTimestamp(7, fDtCreate);

                    // execute the remaining queries
                    statement2.addBatch();
                    if (count % batchSize == 0) {
                        statement2.executeBatch();
                    }
                }
            }

            // commit transaction
            //connection.commit();
            //connection.close();

        } catch (SQLException e) {
            WriteFile.writeFile("ERROR", ImportCSV.class.getName() + ": " + e, logFile);
            e.printStackTrace();
        }
        System.out.println("fin");
    }

    public static java.sql.Date getDateSqlFormat(String date) {
        java.util.Date today;
        java.sql.Date rv = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            today = format.parse(date);
            rv = new java.sql.Date(today.getTime());
        } catch (Exception e) {
            System.out.println("getDateSqlFormat Exception: " + e.getMessage());
        } finally {
            return rv;
        }
    }

    public static int getMaxIndexTableTemp(Connection connection) throws FileNotFoundException, IOException, CsvException, SQLException {
        String sqlDataTemp = "select count(1) from Import_Data_Temp";
        int iReturn = 0;
        try (
                PreparedStatement statement = connection.prepareStatement(sqlDataTemp,
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);  ResultSet rs = statement.executeQuery();) {
            while (rs.next()) {
                iReturn = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return iReturn;
    }

    public static void test5() throws FileNotFoundException, IOException, CsvException, SQLException {
        String csvFilePath = "C:\\Users\\Yann\\Documents\\Banque\\dev\\csv\\CA_Historique.CSV";
        String fileName = csvFilePath;
        String host = "localhost";
        String base = "banque";
        String username = "root";
        String password = "Julienb1000*";
        int batchSize = 20;
        Connection connection = null;
        try {
            //Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":3306/" + base, username, password);
            connection.setAutoCommit(false);

            String sql = "INSERT INTO import_data_temp (n°,compte,date,libelle,euros) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);

            int count = 0;

            try {
                statement.setDouble(1, 1);
                statement.setString(2, "65022812330");
                Timestamp sqlTimestamp = Timestamp.valueOf("2022-4-19 00:00:00");
                statement.setTimestamp(3, sqlTimestamp);
                statement.setString(4, "VIREMENT EN VOTRE FAVEUR");
                Double sqlDoubleEuros = Double.valueOf("20.69");
                statement.setDouble(5, sqlDoubleEuros);

                statement.addBatch();
                if (count % batchSize == 0) {
                    statement.executeBatch();
                }

            } catch (SQLException ex) {
                Logger.getLogger(ImportCSV.class.getName()).log(Level.SEVERE, null, ex);

            }

            //statement.executeBatch(); 
            connection.commit();
            connection.close();

            System.out.println("fin");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void test3() throws FileNotFoundException, IOException, CsvException {
String csvFilePath = "C:\\Users\\Yann\\Documents\\Banque\\dev\\csv\\CA_Historique.CSV";
        String fileName = csvFilePath;
        try ( CSVReader reader = new CSVReader(new FileReader(fileName))) {
            int[] index = {0};
            List<String[]> r = reader.readAll();
            //r.forEach(x -> System.out.println("x: "+Arrays.toString(x).split(";")[2]));
            r.forEach(x -> {
                int currentIndex = index[0];
                String[] data = Arrays.toString(x).split(";");
                String compte = data[0];
                String date = data[1];
                String libelle = data[2];
                String euros = data[3];

                System.out.println("currentIndex : " + currentIndex + " --> compte: " + compte + ", date: " + date + ", libellé: " + libelle + ", euros: " + euros);

                index[0]++;
            });

        }
    }

    public static void test1() {

        System.out.println("Fonction test 1");
String csvFilePath = "C:\\Users\\Yann\\Documents\\Banque\\dev\\csv\\CA_Historique.CSV";
        //String csvFilePath = "C:\\Users\\Yann\\Documents\\Banque\\dev\\Historique.csv";
        System.out.println("1");
        BufferedReader br = null;
        try {
            File file = new File(csvFilePath); // java.io.File
            FileReader fr = new FileReader(file); // java.io.FileReader
            br = new BufferedReader(fr); // java.io.BufferedReader
            String line;
            System.out.println("99");
            while ((line = br.readLine()) != null) {
                // process the line
                System.out.println("0 read line: " + line);

                String[] data = line.split(";");
                String compte = data[0];
                String date = data[1];
                String libelle = data[2];
                String euros = data[3];

                System.out.println("01: compte: " + compte);
                System.out.println("02: date: " + date);
                System.out.println("03: libelle: " + libelle);
                System.out.println("04: euros: " + euros);
                System.out.println("05 compte: " + compte + ", date: " + date + ", libellé: " + libelle + ", euros: " + euros);
                System.out.println("fin ligne");

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void test2() throws FileNotFoundException {
        System.out.println("Fonction test 2");
String csvFilePath = "C:\\Users\\Yann\\Documents\\Banque\\dev\\csv\\CA_Historique.CSV";
        File csvFile = new File(csvFilePath);

        System.out.println("csvFile size: " + csvFile.length());

        Scanner scanner = new Scanner(csvFile);
        scanner.useDelimiter("[\n\r]");
        scanner.next();
        while (scanner.hasNext()) {
            String temp = scanner.next();
            System.out.println(temp);
//          dataStorage.enterRow(temp);

        }
    }

    public static Double StringToDouble(String str) {
        Double str1 = null;
        // Converting the above string into Double
        try {
            str1 = Double.parseDouble(str);
            // Printing string as Double type
            System.out.println(str1);
            return str1;
        } catch (Exception e) {
            System.err.println(e);
            return str1;
        }
    }

    public static void test0() {

//            LocalDateTime localDate = LocalDateTime.now(); // fixed: LocalDateTime
//            DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MMM-dd hh:mm:ss");
//            String date = dtFormatter.format(localDate);
//            System.out.println(date);
        String compte = "\"[65022812330\"";
        System.out.println(compte);
        compte = compte.replaceFirst("\\[", "");
        System.out.println(compte);
    }

    public static void test99(Connection connection, HashMap hmapFiles) throws FileNotFoundException, IOException, CsvException, SQLException {
        //https://mkyong.com/java/how-to-read-and-parse-csv-file-in-java/
        //String fileName = csvFilePath;
        int batchSize = 20;
        indexRegistre = getMaxIndexTableTemp(connection);
        String colDate = "Date";
        String colLibelle = "Libellé";
        String colEurosDebit = "Débit Euros";
        String colEurosCredit = "Crédit Euros";
        String valDate = "";
        String valLibelle = "";
        String valEurosDebit = "";
        String valEurosCredit = "";
        //String ligneEnTete = colDate+";"+colLibelle+";"+colEurosDebit+";"+colEurosCredit+";";
        String ligneEnTete = "Date;Libell�;D�bit Euros;Cr�dit Euros;";
        boolean bFlagImport = false;

        try {

            // calcule la date du jour            
            LocalDateTime localDate = LocalDateTime.now(); // fixed: LocalDateTime
            DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");

            // force le commit automatique
            connection.setAutoCommit(false);

            String sql = "INSERT INTO import_data_temp (n°,compte,date,libelle,euros,seqInSameDay,fichier_import,dt_create) VALUES (?, ?, ?, ?, ?, ? ,?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);

            // =============================== //
            // === Boucle sur les fichiers === //
            // =============================== //
            for (Iterator fileIterator = hmapFiles.entrySet().iterator(); fileIterator.hasNext();) {
                Map.Entry fileMapEntry = (Map.Entry) fileIterator.next();

                // ========================================= //
                // === Charge en mémoire tout le fichier === //
                // ========================================= //
                try ( CSVReader reader = new CSVReader(new FileReader(fileMapEntry.getValue().toString()))) {
                    int count = 0;
                    System.out.println(" ################## debut lecture contenu du fichier courant : " + fileMapEntry.getValue().toString() + ", indexRegistre: " + indexRegistre);
                    List<String[]> r = reader.readAll();
                    for (String[] row : r) {
                        String ligne = Arrays.toString(row).toString();
                        String[] data = ligne.split(";");
//                        System.out.print("-->"+data[0]);
                        System.out.print("-------------------------------------------------------------------" + "\t");
                        for (String tt : data) {
                            System.out.print("-->" + tt + "<--" + "\t");
                        }
                        System.out.println();
                    }
                }
                //System.out.println("Key: " + fileMapEntry.getKey() + " & Value: " + fileMapEntry.getValue());
                System.out.println(" ################## fin lecture contenu du fichier courant : " + fileMapEntry.getValue().toString());
            }

            //---------------------------------------------------------- fin de boucle sur liste de fichiers à importer
            // commit transaction
            connection.commit();
            //connection.close();

            System.out.println("fin");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void test100() throws ParseException {
        String valDate = "01/09/2015"; //-> 2015-09-01
        System.out.println("valDate: " + valDate);
        Date sqlTimestamp = Date.valueOf(valDate);
        System.out.println("sqlTimestamp: " + sqlTimestamp);

        DateFormat sourceFormat = new SimpleDateFormat("dd/MM/yyyy");
        String dateAsString = "25/12/2010";
        java.util.Date date = sourceFormat.parse(dateAsString);
        System.out.println("date: " + date);

        // new SimpleDateFormat("dd MMMM yyyy HH:mm:ss").format(new Date())
        //System.out.println("3-1: date: " + valDate);
        //DateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd");
        //statement.setDate(3, java.sql.Date.valueOf(sourceFormat.toString()));
    }
}
