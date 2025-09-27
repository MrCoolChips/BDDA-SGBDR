package BDDA;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DBConfigTest {

    @TempDir
    File tempDir;

    /**
     * Test de chargement réussi avec un fichier de configuration valide.
     */
    @Test
    void testLoadDBConfig_ValidFile() throws IOException {
        File configFile = new File(tempDir, "config.txt");
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write("dbpath=/home/user/DB’\n");
        }

        DBConfig config = DBConfig.LoadDBConfig(configFile);
        assertEquals("/home/user/DB’", config.dbpath);
    }

    /**
     * Test d'exception si le fichier n'existe pas.
     */
    @Test
    void testLoadDBConfig_FileNotFound() {
        File configFile = new File(tempDir, "notfound.txt");
        assertThrows(IOException.class, () -> DBConfig.LoadDBConfig(configFile));
    }

    /**
     * Test d'exception si le champ dbpath est absent.
     */
    @Test
    void testLoadDBConfig_MissingDbPath() throws IOException {
        File configFile = new File(tempDir, "config.txt");
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write("name=test\n");
        }

        assertThrows(IllegalArgumentException.class, () -> DBConfig.LoadDBConfig(configFile));
    }

    /**
     * Test d'exception si dbpath ne termine pas par /DB’
     */
    @Test
    void testLoadDBConfig_WrongDbPathFormat() throws IOException {
        File configFile = new File(tempDir, "config.txt");
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write("dbpath=/home/user/database\n");
        }

        assertThrows(IllegalArgumentException.class, () -> DBConfig.LoadDBConfig(configFile));
    }

    /**
     * Test de gestion des lignes vides et commentaires.
     */
    @Test
    void testLoadDBConfig_IgnoreCommentsAndWhitespace() throws IOException {
        File configFile = new File(tempDir, "config.txt");
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write("# Ceci est un commentaire\n");
            writer.write("\n");
            writer.write("dbpath=/tmp/DB’\n");
        }

        DBConfig config = DBConfig.LoadDBConfig(configFile);
        assertEquals("/tmp/DB’", config.dbpath);
    }
}