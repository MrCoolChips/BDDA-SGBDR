package BDDA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * DBConfig gère la configuration de la base de données, notamment le chemin vers la base.
 */
public class DBConfig {
    // Chemin vers la base de données
    public String dbpath;
    
    /**
     * Constructeur prenant le chemin de la base de données.
     * @param chemin Le chemin vers la base de données.
     */
    public DBConfig(String chemin) {
        dbpath = chemin;
    }
    
    /**
     * Charge la configuration de la base de données à partir d'un fichier.
     * @param fichier_config Fichier de configuration.
     * @return DBConfig contenant la configuration chargée.
     * @throws IOException Si le fichier n'existe pas ou ne peut être lu.
     * @throws IllegalArgumentException Si le champ 'dbpath' est manquant ou mal formaté.
     */
    public static DBConfig LoadDBConfig(File fichier_config) throws IOException {
        if (fichier_config == null || !fichier_config.exists()) {
            throw new IOException("Fichier de configuration introuvable : " + fichier_config);
        }

        String dbpath = null;

        // Lecture du fichier ligne par ligne
        try (BufferedReader reader = new BufferedReader(new FileReader(fichier_config))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Ignore les lignes vides ou les commentaires
                if (line.isEmpty() || line.startsWith("#")) continue;

                // On suppose que chaque ligne valide est au format clé=valeur
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();

                    // On vérifie la clé dbpath et son format
                    if (key.equals("dbpath")) {
                        if (value.endsWith("/DB’")) {
                            dbpath = value;
                        } else {
                            throw new IllegalArgumentException("Champ 'dbpath' manquant dans le fichier de configuration (/DB)'");
                        }
                    }
                }
            }
        }

        // On lève une exception si dbpath n'a pas été trouvé
        if (dbpath == null) {
            throw new IllegalArgumentException("Champ 'dbpath' manquant dans le fichier de configuration.");
        }

        return new DBConfig(dbpath);
    }
}