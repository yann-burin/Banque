/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package banque;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

/**
 *
 * @author Yann
 */
public class GetBanqueProperties {

    public static Properties prop;    
    InputStream inputStream;

    public static void main(String[] args) throws IOException {
        GetBanqueProperties getProperties = new GetBanqueProperties();
        getProperties.getPropValues();
    }
    public Properties getPropValues() throws IOException {
        try {
            prop = new Properties();
            String propFileName = "banque.properties";
            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            inputStream.close();
        }
        return prop;
    }

}
