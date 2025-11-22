package banque;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Yann
 */

import static banque.ImportCSV.getRegexp;
import static banque.ImportCSV.indexRegistre;
import static banque.ImportCSV.logFile;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

public class Test {
  
  public static void main() {
    System.out.println ("hello test");
  }
    
  public static double diffDatessssss(String date1, String date2)  throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
    java.util.Date firstDate = sdf.parse(date1);
    java.util.Date secondDate = sdf.parse(date2);
    long diffInMillies = Math.abs(secondDate.getTime() - firstDate.getTime());
    long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
    System.out.println ("diff: " + diff);
    return diff;
}

////////////////////////////////////


        
        
    public static void main3( ) throws FileNotFoundException, IOException, CsvException, SQLException, ParseException {
               
        String csvDateMvt = "";
        String csvLibelle = "";
        String csvEurosDebit = "";
        String csvEurosCredit = "";
        String ligne="";
        ligne="28/05/2024;VIREMENT EN ;VOTRE FAVEUR VEOL;IA WATE;R INFORMATION; SYSTEMS SALAIRE MAI 2024;A0242024-05-28 663795704-30;123456;4�730,62;";
        String[] data = ligne.split(";");
        
    System.out.println("data.length: " + data.length);

    if(data.length >= 4) {
            csvDateMvt = data[0]; // Le champs n°1 : la date du mouvement
            System.out.println("++ csvDateMvt:"+csvDateMvt);
            // ----
            for (int u=1; u<=data.length-3; u++) {
                csvLibelle=csvLibelle+data[u];
            //System.out.println("++ libelle:"+data[u]);
            }
            
            //csvLibelle = data[1]+data[2]; // Le champs n°2 : le libellé du mouvement
            csvLibelle=csvLibelle.replace("'"," "); // Le champs n°3 : si le mouvement est un débit								
            csvLibelle=csvLibelle.replace(";"," "); // Le champs n°3 : si le mouvement est un débit
            
            //// 
            csvEurosDebit = data[data.length-2].replaceAll(" ", ""); // !! attention ceci n'est pas un "espace/blanc"
            csvEurosDebit = csvEurosDebit.replaceAll("�", ""); // 
            csvEurosDebit = csvEurosDebit.replaceAll(" ", ""); // ceci est un "espace/blanc"								
            csvEurosCredit = data[data.length-1].replaceAll(" ", ""); // Le champs n°4 : si le mouvement est un crédit
            csvEurosCredit = csvEurosCredit.replaceAll("�", ""); // 
            csvEurosCredit = csvEurosCredit.replaceAll(" ", ""); // ceci est un "espace/blanc"
            
            System.out.println("++***** csvLibelle:"+csvLibelle);
            System.out.println("++ csvEurosDebit:"+csvEurosDebit);
            System.out.println("++ csvEurosCredit:"+csvEurosCredit);
             
             
        }
    }
    
    public static void main2( ) throws FileNotFoundException, IOException, CsvException, SQLException, ParseException {

        String patternEnTete="(?i)^(Date;Libell.;D.bit Euros;Cr.dit Euros;)$";
        String patternCompte="(?i)^((CCHQ.*no)|(Compte de D.p.t carte n.))(.*)(;)$";
        String patternSolde="(?i)^(Solde au )([0-9/]*)( | : )([0-9,.  �]*)(EUR|€|�)$";

        int batchSize = 20;
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


        // =============================== //
        // === Boucle sur les fichiers === //
        // =============================== //
//        for (Iterator fileIterator = hmapFiles.entrySet().iterator(); fileIterator.hasNext();) {
//            Map.Entry fileMapEntry = (Map.Entry) fileIterator.next();

            File fileToRead= new File("E:\\perso\\Temp\\testbanque.csv");
            BasicFileAttributes fileToReadAttributes = Files.readAttributes(fileToRead.toPath(), BasicFileAttributes.class);
            
            // Les informations valables pour toutes les lignes du fichier courant
            String csvCompte = ""; // le numero de compte
            String csvSoldeEuros = ""; // Le solde de compte à la date de situation
            String csvDateSituation = ""; // La date de situation 
//            String csvFichier = new File(fileMapEntry.getValue().toString()).getName(); // Le nom du fichier en court de lecture
//            String csvRepertoire = new File(fileMapEntry.getValue().toString()).getParent(); // Le répertoire du fichier en court de lecture
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
                                //WriteFile.writeFile("INFO", " Compte: "+csvCompte, logFile);
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
                                //WriteFile.writeFile("INFO", " Solde: "+csvSoldeEuros, logFile);                                                              
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

                            // Contournement sur BUG de format du fichier lorsque la ligne d'historique ne se termine pas par ";" (dans le cas des mouvements de crédit !!)
                            // Ici on force un 4eme ";" pour remmettre dans le bon format du CSV (car un mouvement "Crédit Agricole" contient 4 séparateurs ";")
                            if (ligne.split(";").length == 3) {
                                ligne = ligne + " ;";
                            }

                            // Split la ligne en éléments avec séparateur ";"
                            String[] data = ligne.split(";");

                            // Une ligne conforme contient 4 champs
                            if (data.length == 4) {
                                
                                csvDateMvt = data[0]; // Le champs n°1 : la date du mouvement								
                                csvLibelle = data[1]; // Le champs n°2 : le libellé du mouvement
                                csvLibelle=csvLibelle.replace("'"," "); // Le champs n°3 : si le mouvement est un débit								
                                csvEurosDebit = data[2].replaceAll(" ", ""); // !! attention ceci n'est pas un "espace/blanc"
                                csvEurosDebit = csvEurosDebit.replaceAll("�", ""); // 
                                csvEurosDebit = csvEurosDebit.replaceAll(" ", ""); // ceci est un "espace/blanc"								
                                csvEurosCredit = data[3].replaceAll(" ", ""); // Le champs n°4 : si le mouvement est un crédit
                                csvEurosCredit = csvEurosCredit.replaceAll("�", ""); // 
                                csvEurosCredit = csvEurosCredit.replaceAll(" ", ""); // ceci est un "espace/blanc"

                                WriteFile.writeFile("INFO", ImportCSV.class.getName() + ": ligne conforme car data.length = 4 - data.length: "+data.length +" - ligne :"+ligne, logFile);

                            } 
                            // Cas des lignes ayant + de 5 ";" 
                            // Dans ce cas on considère que c'est le champ "libellé" qui contient des ";" de syntaxe de texte et PAS comme délimitteur de champ
                            // Donc ici on remplace les ";" par des espaces 
                            else if(data.length >= 4) {
                                WriteFile.writeFile("WARNING", ImportCSV.class.getName() + ": ligne non conforme car data.length >= de 4 - data.length: "+data.length +" - ligne :"+ligne, logFile);
//                              WriteFile.writeFile("WARNING","data.length: " +data.length, logFile);                                    

     csvDateMvt = data[0]; // Le champs n°1 : la date du mouvement								
     csvLibelle = data[1]+data[2]; // Le champs n°2 : le libellé du mouvement
     csvLibelle=csvLibelle.replace("'"," "); // Le champs n°3 : si le mouvement est un débit								
     csvLibelle=csvLibelle.replace(";"," "); // Le champs n°3 : si le mouvement est un débit								
     csvEurosDebit = data[3].replaceAll(" ", ""); // !! attention ceci n'est pas un "espace/blanc"
     csvEurosDebit = csvEurosDebit.replaceAll("�", ""); // 
     csvEurosDebit = csvEurosDebit.replaceAll(" ", ""); // ceci est un "espace/blanc"								
     csvEurosCredit = data[4].replaceAll(" ", ""); // Le champs n°4 : si le mouvement est un crédit
     csvEurosCredit = csvEurosCredit.replaceAll("�", ""); // 
     csvEurosCredit = csvEurosCredit.replaceAll(" ", ""); // ceci est un "espace/blanc"

                                System.out.println("---------------------------------(libellé):"+csvLibelle);

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
                        //WriteFile.writeFile("INFO", " ========= ("+ligneNbr+") bIsMvtOkToImport: "+bIsMvtOkToImport+", bFlagEndFile: "+bFlagEndFile, logFile);
                } // fin de boucle sur les lignes

                //System.out.println(" ################## fin lecture contenu du fichier courant : " + fileMapEntry.getValue().toString());
                WriteFile.writeFile("INFO", " ################## fin lecture contenu du fichier courant : " + fileToRead.toString(), logFile);

            } // fin du try de CSVReader mise en mémoire du fichier courant

        } // fin de boucle sur les fichiers

    private static void elseif(boolean b) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }




 
  public  void frameTest()  {
    JFrame f = new JFrame("Test JTabbedPane");
    f.setSize(320, 150);
    JPanel pannel = new JPanel();

    JTabbedPane onglets = new JTabbedPane(SwingConstants.TOP);

    JPanel onglet1 = new JPanel();
    JLabel titreOnglet1 = new JLabel("Onglet 1");
    onglet1.add(titreOnglet1);
    onglet1.setPreferredSize(new Dimension(300, 80));
    onglets.addTab("onglet1", onglet1);

    JPanel onglet2 = new JPanel();
    JLabel titreOnglet2 = new JLabel("Onglet 2");
    onglet2.add(titreOnglet2);
    onglets.addTab("onglet2", onglet2);

    onglets.setOpaque(true);
    pannel.add(onglets);
    f.getContentPane().add(pannel);
    f.setVisible(true);

  }

  
}