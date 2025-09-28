import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class DBConfig {
    private String dbpath;
    
    /**
     * Constructeur de la classe DBConfig
     * Initialise une nouvelle configuration de base de données avec le chemin spécifié
     * @param dbpath le chemin vers la base de données
     */
    public DBConfig(String dbpath) {
        this.dbpath = dbpath;
    }

    /**
     * Récupère le chemin de la base de données
     * @return le chemin vers la base de données
     */
    public String getPath() {
        return dbpath;
    }

    /**
     * Charge la configuration de la base de données depuis un fichier
     * Lit le fichier ligne par ligne et cherche la ligne contenant "dbpath = '...'"
     * @param fichier_config le fichier de configuration à lire
     * @return une nouvelle instance de DBConfig avec le chemin trouvé, ou null si non trouvé
     * @throws IOException si une erreur de lecture du fichier survient
     */
    public static DBConfig LoadDBConfig(File fichier_config) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fichier_config));
        String line  = reader.readLine();
        while(line != null) {
            if(line.startsWith("dbpath = '")) {
                int start = line.indexOf("'");
                int end = line.indexOf("'", start + 1);
                String path = line.substring(start + 1, end);
                reader.close();
                return new DBConfig(path);
            }
            line = reader.readLine();
        }
        reader.close();
        return null;
    }

}

    


