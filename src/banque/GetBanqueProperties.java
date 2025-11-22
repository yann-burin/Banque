package banque;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestionnaire de propriétés pour l'application bancaire.
 * Charge le fichier banque.properties depuis le classpath.
 * 
 * @author Yann
 */
public class GetBanqueProperties {

    private static final Logger LOGGER = Logger.getLogger(GetBanqueProperties.class.getName());
    private static final String PROP_FILE_NAME = "banque.properties";
    public static Properties prop;

    public static void main(String[] args) {
        try {
            var properties = new GetBanqueProperties();
            properties.getPropValues();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des propriétés", e);
        }
    }

    /**
     * Charge les propriétés depuis le fichier banque.properties.
     * Utilise try-with-resources pour une gestion automatique des ressources.
     * 
     * @return Properties chargées depuis le fichier
     * @throws IOException si le fichier n'est pas trouvé ou ne peut être lu
     */
    public Properties getPropValues() throws IOException {
        prop = new Properties();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PROP_FILE_NAME)) {
            if (inputStream == null) {
                throw new FileNotFoundException(
                        "Le fichier '" + PROP_FILE_NAME + "' est introuvable dans le classpath");
            }
            prop.load(inputStream);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la lecture du fichier de propriétés", e);
            throw e;
        }

        return prop;
    }

    /**
     * Retourne les propriétés chargées.
     * 
     * @return Properties ou null si non chargées
     */
    public static Properties getProperties() {
        return prop;
    }
}
