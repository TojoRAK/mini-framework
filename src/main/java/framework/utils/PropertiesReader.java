package framework.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {

    private String uploadLocation;

    public Properties readProperties() throws Exception {
        Properties prop = new Properties();

        try (InputStream input = PropertiesReader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new Exception("Impossible de trouver application.properties");
            }
        
            prop.load(input);
        
            return prop;
        
        } catch (IOException ex) {
            ex.printStackTrace();
            throw ex;
        }

    }

    public static String getProperty(String propertyName) throws Exception {
        PropertiesReader reader = new PropertiesReader();
        Properties prop = reader.readProperties();
        return prop.getProperty(propertyName);
    }

    public String getUploadLocation() {
        return uploadLocation;
    }

    public void setUploadLocation(String uploadLocation) {
        this.uploadLocation = uploadLocation;
    }

}
