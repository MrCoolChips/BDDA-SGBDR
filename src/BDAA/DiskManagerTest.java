package BDAA;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class DiskManagerTest {

    public static void main(String[] args) {
        try {
            // Configuration de base de données : dossier "db_test", pagesize 16 octets, max 64 octets par fichier
            File fichier = new File("src/config.txt");
            DBConfig config = DBConfig.LoadDBConfig(fichier);
            DiskManager dm = new DiskManager(config);

            // ️Allocation de 3 pages
            PageId p1 = dm.allocPage();
            PageId p2 = dm.allocPage();
            PageId p3 = dm.allocPage();

            System.out.println("Pages allouées:");
            System.out.println(p1);
            System.out.println(p2);
            System.out.println(p3);

            // ️Écriture de données dans la page 1
            byte[] data = new byte[config.getPageSize()];
            Arrays.fill(data, (byte) 42); // remplir avec la valeur 42
            dm.WritePage(p1, data);

            // Lecture depuis la page 1
            byte[] readBuffer = new byte[config.getPageSize()];
            dm.ReadPage(p1, readBuffer);
            System.out.println("Contenu lu depuis page 1: " + Arrays.toString(readBuffer));

            // Désallocation de la page 2
            dm.DeallocPage(p2);
            System.out.println("Page 2 désallouée.");

            // Sauvegarde de l'état
            dm.SaveState();
            System.out.println("État sauvegardé dans dm.save.");

            // Recharge de l'état
            DiskManager dm2 = new DiskManager(config);
            dm2.LoadState();
            System.out.println("État chargé depuis dm.save, pages libres:");
            dm2.getFreePages();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
