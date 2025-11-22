package banque;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fenêtre de recherche et visualisation des mouvements bancaires avec filtres.
 * Permet de filtrer par date (début/fin) et par libellé.
 * 
 * @author Yann
 */
public class HistoriqueSearchFrame extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(HistoriqueSearchFrame.class.getName());
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private DefaultTableModel model;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    
    // Composants de recherche
    private JTextField txtLibelle;
    private JTextField txtDateDebut;
    private JTextField txtDateFin;
    private JButton btnSearch;
    private JButton btnReset;
    private JLabel lblResultCount;
    
    private Connection connection;

    public HistoriqueSearchFrame() {
        initComponents();
        loadData();
    }

    /**
     * Initialise les composants de l'interface graphique.
     */
    private void initComponents() {
        setTitle("Historique des mouvements bancaires");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Panel de recherche en haut
        JPanel searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.NORTH);

        // Table au centre
        createTable();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(1200, 600));
        add(scrollPane, BorderLayout.CENTER);

        // Panel de statut en bas
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Crée le panel de recherche avec les filtres.
     */
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Critères de recherche"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Libellé
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Libellé :"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtLibelle = new JTextField(20);
        txtLibelle.setToolTipText("Rechercher dans le libellé");
        panel.add(txtLibelle, gbc);

        // Date début
        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(new JLabel("Date début :"), gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.5;
        txtDateDebut = new JTextField(10);
        txtDateDebut.setToolTipText("Format: YYYY-MM-DD");
        panel.add(txtDateDebut, gbc);

        // Date fin
        gbc.gridx = 4;
        gbc.weightx = 0;
        panel.add(new JLabel("Date fin :"), gbc);

        gbc.gridx = 5;
        gbc.weightx = 0.5;
        txtDateFin = new JTextField(10);
        txtDateFin.setToolTipText("Format: YYYY-MM-DD");
        panel.add(txtDateFin, gbc);

        // Boutons
        gbc.gridx = 6;
        gbc.weightx = 0;
        btnSearch = new JButton("Rechercher");
        btnSearch.addActionListener(e -> searchData());
        panel.add(btnSearch, gbc);

        gbc.gridx = 7;
        btnReset = new JButton("Réinitialiser");
        btnReset.addActionListener(e -> resetSearch());
        panel.add(btnReset, gbc);

        return panel;
    }

    /**
     * Crée la table pour afficher les données.
     */
    private void createTable() {
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Table en lecture seule
            }
        };

        // Définition des colonnes
        model.addColumn("N°");
        model.addColumn("Date");
        model.addColumn("Euros");
        model.addColumn("Cumul");
        model.addColumn("Fréquence");
        model.addColumn("Libellé");
        model.addColumn("Tiers");
        model.addColumn("Catégorie");

        table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);
        
        // Configuration des largeurs de colonnes
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // N°
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // Date
        table.getColumnModel().getColumn(2).setPreferredWidth(80);  // Euros
        table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Cumul
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Fréquence
        table.getColumnModel().getColumn(5).setPreferredWidth(300); // Libellé
        table.getColumnModel().getColumn(6).setPreferredWidth(150); // Tiers
        table.getColumnModel().getColumn(7).setPreferredWidth(150); // Catégorie

        // Tri sur les colonnes
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
    }

    /**
     * Crée le panel de statut en bas.
     */
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblResultCount = new JLabel("0 résultat(s)");
        panel.add(lblResultCount);
        return panel;
    }

    /**
     * Établit la connexion à la base de données.
     */
    private void connectToDatabase() {
        try {
            GetBanqueProperties propLoader = new GetBanqueProperties();
            propLoader.getPropValues();
            Properties dbConnProperties = GetBanqueProperties.prop;
            
            String host = dbConnProperties.getProperty("host");
            String base = dbConnProperties.getProperty("base");
            String username = dbConnProperties.getProperty("username");
            String password = dbConnProperties.getProperty("password");

            connection = DBConnection.getConnection(host, base, username, password);
            
            if (connection == null) {
                throw new SQLException("Impossible d'établir la connexion à la base de données");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur de connexion à la base de données", e);
            JOptionPane.showMessageDialog(this,
                "Erreur de connexion à la base de données:\n" + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Charge toutes les données de l'historique.
     */
    private void loadData() {
        loadData(null, null, null);
    }

    /**
     * Charge les données avec les filtres spécifiés.
     */
    private void loadData(String libelle, String dateDebut, String dateFin) {
        connectToDatabase();
        
        if (connection == null) {
            return;
        }

        // Vider la table
        model.setRowCount(0);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT h.num, h.date, h.euros, ROUND(h.Cumul_Euros_calc, 2) as cumul, ");
        sql.append("h.Frequence, h.libelle, t.LblTiers, c.LblCategorie ");
        sql.append("FROM historique h ");
        sql.append("LEFT JOIN Correspondances cor ON h.critere = cor.critere ");
        sql.append("LEFT JOIN Tiers t ON cor.`N°Tiers` = t.`N°` ");
        sql.append("LEFT JOIN Categories c ON t.IdCategorie = c.IdCategorie ");
        sql.append("WHERE 1=1 ");

        try (PreparedStatement pstm = connection.prepareStatement(sql.toString() + buildWhereClause(libelle, dateDebut, dateFin))) {
            
            // Définir les paramètres
            int paramIndex = 1;
            if (libelle != null && !libelle.trim().isEmpty()) {
                pstm.setString(paramIndex++, "%" + libelle.trim() + "%");
            }
            if (dateDebut != null && !dateDebut.trim().isEmpty()) {
                pstm.setString(paramIndex++, dateDebut.trim());
            }
            if (dateFin != null && !dateFin.trim().isEmpty()) {
                pstm.setString(paramIndex++, dateFin.trim());
            }

            sql.append("ORDER BY h.Date DESC, h.SeqInSameDay ASC");
            
            try (ResultSet rs = pstm.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("num"),
                        rs.getDate("date"),
                        String.format("%.2f", rs.getDouble("euros")),
                        String.format("%.2f", rs.getDouble("cumul")),
                        rs.getString("Frequence"),
                        rs.getString("libelle"),
                        rs.getString("LblTiers"),
                        rs.getString("LblCategorie")
                    });
                    count++;
                }
                
                lblResultCount.setText(count + " résultat(s)");
                LOGGER.log(Level.INFO, "Chargement de {0} enregistrement(s)", count);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des données", e);
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des données:\n" + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            DBConnection.closeConnection(connection);
        }
    }

    /**
     * Construit la clause WHERE en fonction des filtres.
     */
    private String buildWhereClause(String libelle, String dateDebut, String dateFin) {
        StringBuilder where = new StringBuilder();

        if (libelle != null && !libelle.trim().isEmpty()) {
            where.append("AND h.libelle LIKE ? ");
        }
        if (dateDebut != null && !dateDebut.trim().isEmpty()) {
            where.append("AND h.date >= ? ");
        }
        if (dateFin != null && !dateFin.trim().isEmpty()) {
            where.append("AND h.date <= ? ");
        }

        where.append("ORDER BY h.Date DESC, h.SeqInSameDay ASC");
        return where.toString();
    }

    /**
     * Effectue une recherche avec les critères saisis.
     */
    private void searchData() {
        String libelle = txtLibelle.getText();
        String dateDebut = txtDateDebut.getText();
        String dateFin = txtDateFin.getText();

        // Validation des dates
        if (!dateDebut.isEmpty() && !isValidDate(dateDebut)) {
            JOptionPane.showMessageDialog(this,
                "Format de date début invalide. Utilisez le format YYYY-MM-DD",
                "Erreur de validation",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!dateFin.isEmpty() && !isValidDate(dateFin)) {
            JOptionPane.showMessageDialog(this,
                "Format de date fin invalide. Utilisez le format YYYY-MM-DD",
                "Erreur de validation",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        loadData(libelle, dateDebut, dateFin);
    }

    /**
     * Réinitialise les filtres et recharge toutes les données.
     */
    private void resetSearch() {
        txtLibelle.setText("");
        txtDateDebut.setText("");
        txtDateFin.setText("");
        loadData();
    }

    /**
     * Valide le format d'une date.
     */
    private boolean isValidDate(String date) {
        try {
            DATE_FORMAT.setLenient(false);
            DATE_FORMAT.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Point d'entrée pour tester la fenêtre.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            var frame = new HistoriqueSearchFrame();
            frame.setVisible(true);
        });
    }
}
