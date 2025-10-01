package BDAA;

import java.io.File;
import java.io.IOException;

/**
 * Classe de test pour DBConfig
 * Permet de tester le chargement de configuration depuis un fichier
 */
public class DBConfigTest {

    /**
     * Méthode principale pour tester la classe DBConfig
     * Charge la configuration depuis le fichier config.txt et affiche le chemin trouvé
     * @param args arguments de ligne de commande (non utilisés)
     * @throws IOException si une erreur de lecture du fichier survient
     */
    public static void main(String[] args) throws IOException {
        File fichier = new File("src/config.txt");
        DBConfig bd = DBConfig.LoadDBConfig(fichier);
        try {
            System.out.println(bd.getPath() + " " +  bd.getMaxFileSize() + " " + bd.getPageSize());
        } catch (NullPointerException e) {
            System.out.println("Erreur : Configuration non trouvée ou incomplète !");
            System.out.println("Vérifiez que le fichier contient : dbpath, pagesize, dm_maxfilesize");
        }
    }
}
