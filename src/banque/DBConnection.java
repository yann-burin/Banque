package banque;

import java.sql.Connection;
import java.sql.DriverManager;
import javax.swing.JOptionPane;

public class DBConnection {
    Connection con = null;
    public static Connection DBConnection(String host, String base, String user, String pwd){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://"+host+":3306/"+base,user,pwd);            
//            JOptionPane.showMessageDialog(null, "Connected to Database");
            return conn;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
            return null;
        }
    }
}