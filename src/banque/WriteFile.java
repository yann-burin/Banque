/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package banque;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Yann
 */
public class WriteFile {

    /* 
	 --------------------------------------------------------------------------------
	 Fonction: Ecriture dans le fichier en paramètre (scripts, log)
	 --------------------------------------------------------------------------------
     */
    static void writeFile(String level, String text, File fileName) {
        try {
            if (fileName != null) {

                // si le fichier n'existe pas, on le cré
                if (checkFileExists(fileName.toString()) == false) {
                    fileName.createNewFile();
                }

                // la création du fichier a échoué
                if (checkFileExists(fileName.toString()) == true) {
                    FileWriter fw = new FileWriter(fileName.getAbsoluteFile(), true);
                    BufferedWriter bw = new BufferedWriter(fw);

                    if (level == "") {
                        bw.write(text + "\r\n");
                    } else {
                        bw.write("[" + level + "]  -> " + text + "\r\n");
                        if (level == "ERREUR" | level == "ERROR") {
                            System.err.println("[" + level + "]  -> " + text);
                        } else {
                            System.out.println("[" + level + "]  -> " + text);
                        }
                    }
                    bw.close();
                } else {
                    System.err.println(" [ERREUR]  -> Le fichier d'écriture en argument " + fileName + " n'existe pas.");
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur d'écriture de la LOG" + e);
            e.printStackTrace();
        }
    }

    /* 
	 --------------------------------------------------------------------------------
	 Fonction: Création de fichier 
	 --------------------------------------------------------------------------------
     */
    public static void createFile(String filePath) {

        if (filePath == "") {
            System.err.println("L'argument <filePath> est obligatoire.");
        } else {

            try {
                File file = new File(filePath);
                if (checkFileExists(file.getParent()) == false) {
                    new File(file.getParent()).mkdirs();
                }
                if (checkFileExists(file.getPath()) == false) {
                    file.createNewFile();
                }
            } catch (IOException e) {
                System.err.println("Erreur à la création du fichier: " + filePath + ". " + e);
                e.printStackTrace();
            }
        }
    }

        /* 
	 --------------------------------------------------------------------------------
	 Fonction: Suppression de fichier 
	 --------------------------------------------------------------------------------
     */
    public static void deleteFile(String filePath) {

        if (filePath == "") {
            System.err.println("L'argument <filePath> est obligatoire.");
        } else {

            File file = new File(filePath);
            if (checkFileExists(file.getPath()) == true) {
                file.delete();
            }
        }
    }
    
    /* 
	 --------------------------------------------------------------------------------
	 Fonction: Retourne yes/no si le fichier en paramètre existe
	 --------------------------------------------------------------------------------
     */
    static boolean checkFileExists(String file) {
        Boolean bReturn = new File(file).exists();
        return bReturn;
    }

}
