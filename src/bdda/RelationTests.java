package bdda;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * Cette classe a pour but de tester le bon fonctionnement de la classe Relation
 * sans utiliser de bibliothèque de test externe comme JUnit.
 * Les tests sont organisés en fonctions distinctes pour plus de clarté.
 */
public class RelationTests {

    public static void main(String[] args) {
        System.out.println("Lancement de la suite de tests pour la classe Relation...");
        System.out.println("---------------------------------------------------------");

        int testsReussis = 0;
        int testsTotal = 0;

        // Lancer chaque test et compter les succès
        if (testerEcriturePuisLectureSimple()) {
            testsReussis++;
        }
        testsTotal++;

        if (testerDebordementDeTamponALecriture()) {
            testsReussis++;
        }
        testsTotal++;
        
        // D'autres tests peuvent être ajoutés ici sur le même modèle.
        // ex: if (autreTest()) { testsReussis++; } testsTotal++;

        System.out.println("---------------------------------------------------------");
        System.out.println("Résumé des tests :");
        System.out.println(testsReussis + " sur " + testsTotal + " tests ont réussi.");

        if (testsReussis != testsTotal) {
            System.out.println("Des erreurs ont été détectées.");
        } else {
            System.out.println("Tous les tests se sont terminés avec succès.");
        }
    }

    /**
     * Teste le scénario de base : écrire un enregistrement dans un tampon,
     * puis le relire et vérifier que les données sont identiques.
     * @return true si le test réussit, sinon false.
     */
    private static boolean testerEcriturePuisLectureSimple() {
        System.out.println("-> Lancement du test : Ecriture et lecture simples.");

        // Préparation du test
        List<String> colNames = Arrays.asList("ID", "SALAIRE", "NOM");
        List<String> colTypes = Arrays.asList("INT", "FLOAT", "CHAR(10)");
        Relation relation = new Relation("EMPLOYE", colNames, colTypes, null, 0, null, null);

        Record enregistrementOriginal = new Record();
        enregistrementOriginal.addValue(99);
        enregistrementOriginal.addValue(52000.50f);
        enregistrementOriginal.addValue("Martin");

        int tailleEnregistrement = Integer.BYTES + Float.BYTES + 10;
        ByteBuffer tampon = ByteBuffer.allocate(tailleEnregistrement);

        try {
            // Action : Écriture puis lecture
            relation.writeRecordToBuffer(enregistrementOriginal, tampon, 0);
            
            Record enregistrementLu = new Record();
            relation.readFromBuffer(enregistrementLu, tampon, 0);

            // Vérification
            boolean sontEgaux = enregistrementOriginal.getValues().equals(enregistrementLu.getValues());

            if (sontEgaux) {
                System.out.println("   SUCCES : L'enregistrement lu est identique à l'original.");
                return true;
            } else {
                System.err.println("   ECHEC : L'enregistrement lu est différent de l'original.");
                System.err.println("   Original : " + enregistrementOriginal.getValues());
                System.err.println("   Lu       : " + enregistrementLu.getValues());
                return false;
            }

        } catch (Exception e) {
            System.err.println("   ECHEC : Une exception inattendue est survenue : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Teste si la méthode writeRecordToBuffer lève correctement une exception
     * BufferOverflowException lorsque le tampon est trop petit.
     * @return true si le test réussit, sinon false.
     */
    private static boolean testerDebordementDeTamponALecriture() {
        System.out.println("-> Lancement du test : Levee de l'exception BufferOverflowException.");

        // Préparation du test
        List<String> colNames = Arrays.asList("ID", "NOM");
        List<String> colTypes = Arrays.asList("INT", "VARCHAR(20)");
        Relation relation = new Relation("PRODUIT", colNames, colTypes, null, 0, null, null);

        Record enregistrement = new Record();
        enregistrement.addValue(404);
        enregistrement.addValue("Un nom de produit tres long");

        int tailleEnregistrement = Integer.BYTES + 20;
        // Créer un tampon volontairement trop petit
        ByteBuffer tampon = ByteBuffer.allocate(tailleEnregistrement - 5);

        try {
            // Action : Tenter d'écrire dans un tampon trop petit
            relation.writeRecordToBuffer(enregistrement, tampon, 0);
            
            // Si on arrive ici, l'exception n'a pas été levée, c'est un échec.
            System.err.println("   ECHEC : L'exception BufferOverflowException aurait du etre levee mais ne l'a pas ete.");
            return false;

        } catch (BufferOverflowException e) {
            // L'exception attendue a bien été attrapée. C'est un succès.
            System.out.println("   SUCCES : L'exception attendue (BufferOverflowException) a bien ete levee.");
            return true;
        } catch (Exception e) {
            // Une autre exception a été levée, ce qui n'est pas le comportement attendu.
            System.err.println("   ECHEC : Une exception differente de BufferOverflowException a ete levee : " + e.getClass().getName());
            return false;
        }
    }
}