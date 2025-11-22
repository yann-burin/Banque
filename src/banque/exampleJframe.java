/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package banque;

/**
 *
 * @author Yann
 */
import static banque.ImportCSV.connectionDB;
import static banque.ImportCSV.logFile;
import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.Properties;

public class exampleJframe extends JFrame {

    DefaultTableModel model = new DefaultTableModel();
    Container cnt = this.getContentPane();
    JTable jtbl = new JTable(model);

    public exampleJframe() {
        cnt.setLayout(new FlowLayout(FlowLayout.LEFT));
        cnt.setBackground(Color.LIGHT_GRAY);
        cnt.setEnabled(rootPaneCheckingEnabled);

        model.addColumn("N°");
        model.addColumn("Date");
        model.addColumn("Euros");
        model.addColumn("Cumul");
        model.addColumn("Fréquence");
        model.addColumn("Libellé");
        model.addColumn("Tiers");
        model.addColumn("Catégorie");
        model.isCellEditable(1800, 1800);

        try {

            // variables de connexion à la base Mysql
            GetBanqueProperties.main(null);
            Properties dbConnProperties = GetBanqueProperties.prop;
            String host = dbConnProperties.getProperty("host");
            String base = dbConnProperties.getProperty("base");
            String username = dbConnProperties.getProperty("username");
            String password = dbConnProperties.getProperty("password");
            String sSql = "";

            // Ouvre la connexion à la base
            connectionDB = DBConnection.getConnection(host, base, username, password);

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/banque", "root",
                    "Administrator*");

            sSql = "SELECT historique.num, historique.date,historique.euros,round(historique.Cumul_Euros_calc,2), ";
            sSql = sSql + "historique.Frequence, historique.libelle ,Tiers.LblTiers, Categories.LblCategorie ";
            sSql = sSql
                    + "FROM historique historique, Correspondances Correspondances, Tiers Tiers, Categories Categories ";
            sSql = sSql + "Where `historique`.`critere` = `Correspondances`.`critere` ";
            sSql = sSql + "And `Correspondances`.`N°Tiers` = `Tiers`.`N°` ";
            sSql = sSql + "And `Categories`.`IdCategorie` = `Tiers`.`IdCategorie` ";
            sSql = sSql + "ORDER BY historique.Date DESC , historique.SeqInSameDay asc";
            PreparedStatement pstm = con.prepareStatement(sSql);
            ResultSet Rs = pstm.executeQuery();
            int iCount = 0;
            while (Rs.next()) {
                model.addRow(new Object[] {
                        Rs.getInt("historique.num"), Rs.getDate("historique.date"),
                        Rs.getDouble("historique.euros"), Rs.getDouble("round(historique.Cumul_Euros_calc,2)"),
                        Rs.getString("historique.Frequence"), Rs.getString("historique.libelle"),
                        Rs.getString("Tiers.LblTiers"), Rs.getString("Categories.LblCategorie")
                });
                iCount++;
            }
            WriteFile.writeFile("INFO", "count:" + String.valueOf(iCount), logFile);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        JScrollPane pg = new JScrollPane(jtbl);
        cnt.add(pg);
        // model.setNumRows(20);
        jtbl.setSize(2000, 2000);
        pg.setSize(2000, 2000);
        cnt.setSize(2000, 2000);
        this.pack();
    }
}
