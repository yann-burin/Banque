package banque;

import com.opencsv.exceptions.CsvException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import static banque.DBConnection.*;
import java.io.File;
import java.text.ParseException;
import javax.swing.JFrame;

public class Banque extends javax.swing.JFrame {

    static Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    public Banque() {
        initComponents();

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFrame1 = new javax.swing.JFrame();
        jPanel1 = new javax.swing.JPanel();
        btnCalculerCumuls = new javax.swing.JButton();
        btnVoirMouvements = new javax.swing.JButton();
        btnImporterLesFichiersCSV = new javax.swing.JButton();
        btnCalcDebitsCreditsMensuels = new javax.swing.JButton();
        javax.swing.JButton btnCalculerTiers = new javax.swing.JButton();
        javax.swing.JButton btnCalculerFrequences = new javax.swing.JButton();
        javax.swing.JButton btnRunTest = new javax.swing.JButton();

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
                jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 400, Short.MAX_VALUE));
        jFrame1Layout.setVerticalGroup(
                jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        btnCalculerCumuls.setText("Calculer les cumuls");
        btnCalculerCumuls.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCalculCumulsActionPerformed(evt);
            }
        });

        btnVoirMouvements.setText("Voir les mouvements");
        btnVoirMouvements.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVoirMouvementsActionPerformed(evt);
            }
        });

        btnImporterLesFichiersCSV.setText("Importer les fichiers CSV");
        btnImporterLesFichiersCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImporterLesFichiersCSVActionPerformed(evt);
            }
        });

        btnCalcDebitsCreditsMensuels.setText("Somme des Débits/Crédits mensuels");
        btnCalcDebitsCreditsMensuels.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCalcDebitsCreditsMensuelsbtnNewClassActionPerformed(evt);
            }
        });

        btnCalculerTiers.setText("Calculer les Tiers");
        btnCalculerTiers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCalculerTiersbtnCalculTiersActionPerformed(evt);
            }
        });

        btnCalculerFrequences.setText("Calculer les Fréquences");
        btnCalculerFrequences.setName(""); // NOI18N
        btnCalculerFrequences.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCalculerFrequencesbtnCalculFrequencesActionPerformed(evt);
            }
        });

        btnRunTest.setText("Class de test");
        btnRunTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRunTestbtnCalculTiersActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGroup(jPanel1Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                                false)
                                                        .addComponent(btnVoirMouvements,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(btnCalculerFrequences,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, 158,
                                                                Short.MAX_VALUE)
                                                        .addComponent(btnCalculerTiers,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(btnCalculerCumuls,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addGap(18, 18, 18)
                                                .addComponent(btnRunTest))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(btnImporterLesFichiersCSV)
                                                .addGap(18, 18, 18)
                                                .addComponent(btnCalcDebitsCreditsMensuels)))
                                .addContainerGap()));
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnImporterLesFichiersCSV)
                                        .addComponent(btnCalcDebitsCreditsMensuels))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnCalculerCumuls)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnCalculerTiers)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnCalculerFrequences)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnVoirMouvements)
                                        .addComponent(btnRunTest))
                                .addContainerGap(20, Short.MAX_VALUE)));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(36, 36, 36)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(87, Short.MAX_VALUE)));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(53, 53, 53)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(132, Short.MAX_VALUE)));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRunTestbtnCalculTiersActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnRunTestbtnCalculTiersActionPerformed
        // TODO add your handling code here:
        Test.main();
    }// GEN-LAST:event_btnRunTestbtnCalculTiersActionPerformed

    private void btnCalculerFrequencesbtnCalculFrequencesActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnCalculerFrequencesbtnCalculFrequencesActionPerformed
        // TODO add your handling code here:
        // TODO add your handling code here:
        try {
            CalculFrequenceMvts.main(null);
        } catch (IOException ex) {
            Logger.getLogger(Banque.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Banque.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(Banque.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CsvException ex) {
            Logger.getLogger(Banque.class.getName()).log(Level.SEVERE, null, ex);
        }
    }// GEN-LAST:event_btnCalculerFrequencesbtnCalculFrequencesActionPerformed

    private void btnCalculerTiersbtnCalculTiersActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnCalculerTiersbtnCalculTiersActionPerformed
        // TODO add your handling code here:
        try {
            CalculAttacheTiers.main(null);
        } catch (IOException ex) {
            Logger.getLogger(Banque.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Banque.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(Banque.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CsvException ex) {
            Logger.getLogger(Banque.class.getName()).log(Level.SEVERE, null, ex);
        }
    }// GEN-LAST:event_btnCalculerTiersbtnCalculTiersActionPerformed

    private void btnCalcDebitsCreditsMensuelsbtnNewClassActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnCalcDebitsCreditsMensuelsbtnNewClassActionPerformed

        try {
            CalculDebitCreditMensuel.main(null);
        } catch (IOException ex) {
            Logger.getLogger(Banque.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Banque.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(Banque.class.getName()).log(Level.SEVERE, null, ex);
        }
    }// GEN-LAST:event_btnCalcDebitsCreditsMensuelsbtnNewClassActionPerformed

    private void btnImporterLesFichiersCSVActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnImporterLesFichiersCSVActionPerformed
        try {
            ImportCSV.main(null);
        } catch (IOException ex) {
            Logger.getLogger(Banque.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Banque.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CsvException ex) {
            Logger.getLogger(Banque.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(Banque.class.getName()).log(Level.SEVERE, null, ex);
        }
    }// GEN-LAST:event_btnImporterLesFichiersCSVActionPerformed

    private void btnVoirMouvementsActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnVoirMouvementsActionPerformed
        JFrame frame = new exampleJframe();
        frame.setTitle("Historiques de mouvements");
        frame.setSize(800, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }// GEN-LAST:event_btnVoirMouvementsActionPerformed

    private void btnCalculCumulsActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnCalculCumulsActionPerformed
        // TODO add your handling code here:
        try {
            try {
                CalculCumuls.main(null);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Banque.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(Banque.class.getName()).log(Level.SEVERE, null, ex);
            } catch (CsvException ex) {
                Logger.getLogger(Banque.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(Banque.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(Banque.class.getName()).log(Level.SEVERE, null, ex);
        }
    }// GEN-LAST:event_btnCalculCumulsActionPerformed

    public static Connection ReturnConnexion() {
        return conn;
    }

    /**
     * Point d'entrée de l'application.
     * 
     * @param args les arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        // Utilisation de lambda (Java 8+) pour plus de concision
        java.awt.EventQueue.invokeLater(() -> {
            var banque = new Banque();
            banque.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCalcDebitsCreditsMensuels;
    private javax.swing.JButton btnCalculerCumuls;
    private javax.swing.JButton btnImporterLesFichiersCSV;
    private javax.swing.JButton btnVoirMouvements;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

}
