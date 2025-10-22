package bdda;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * Représente une relation (ou table) dans un système de base de données.
 * Cette classe contient les métadonnées d'une relation, telles que son nom,
 * les noms et les types de ses colonnes. Elle fournit également des méthodes
 * pour sérialiser et désérialiser des enregistrements ({@link Record})
 * dans un {@link ByteBuffer}.
 */
public class Relation {
    /** Le nom de la relation (table). */
    private String name;
    
    /** La liste ordonnée des noms des colonnes. */
    private List<String> columnNames;
    
    /** La liste ordonnée des types des colonnes (ex: "INT", "FLOAT", "CHAR(10)", "VARCHAR(255)"). */
    private List<String> columnTypes;
    
    private PageId headerPageId;
    private int recordPerPage;
    private DiskManager diskManager;
    private BufferManager bufferManager;

    /**
     * Construit une nouvelle instance de Relation.
     *
     * @param name Le nom de la relation.
     * @param columnNames La liste des noms de colonnes.
     * @param columnTypes La liste des types de colonnes, correspondant à la liste des noms.
     */
    public Relation(String name, List<String> columnNames, List<String> columnTypes, PageId headerPageId, int recordPerPage,DiskManager diskManager, BufferManager bufferManager) {
        this.name = name;
        this.columnNames = columnNames;
        this.columnTypes = columnTypes;
        this.headerPageId = headerPageId;
        this.recordPerPage = recordPerPage;
        this.diskManager = diskManager;
        this.bufferManager = bufferManager;
    }

    /**
     * @return Le nom de la relation.
     */
    public String getName() {
        return name;
    }

    /**
     * @return La liste des noms de colonnes.
     */
    public List<String> getColumnNames() {
        return columnNames;
    }

    /**
     * @return La liste des types de colonnes.
     */
    public List<String> getColumnTypes() {
        return columnTypes;
    }

    /**
     * Écrit les données d'un enregistrement (record) dans un ByteBuffer à une position spécifiée.
     * La sérialisation se fait en fonction des types de colonnes définis dans la relation.
     *
     * @param record L'enregistrement contenant les valeurs à écrire. Ne doit pas être null.
     * @param buff Le buffer dans lequel écrire les données. Ne doit pas être null.
     * @param pos La position de départ dans le buffer pour l'écriture.
     * @throws IllegalArgumentException si le record, le buffer ou un type de colonne est null,
     * ou si la position est invalide.
     * @throws IllegalStateException si les types de colonnes n'ont pas été initialisés.
     * @throws BufferOverflowException si le buffer n'a pas assez d'espace pour écrire les données.
     */
    public void writeRecordToBuffer(Record record, ByteBuffer buff, int pos) {
        if (record == null) {
            throw new IllegalArgumentException("Le record ne doit pas être null.");
        }
        if (buff == null) {
            throw new IllegalArgumentException("Le buffer ne doit pas être null.");
        }
        if (pos < 0 || pos >= buff.capacity()) {
            throw new IllegalArgumentException("Position invalide: " + pos + " (capacité=" + buff.capacity() + ")");
        }
        if (columnTypes == null) {
            throw new IllegalStateException("La liste columnTypes n'est pas initialisée.");
        }
        
        List<Object> values = record.getValues();
        if (values == null) {
            throw new IllegalArgumentException("La méthode getValues() du record a retourné null.");
        }

        buff.position(pos);

        for (int i = 0; i < columnTypes.size(); i++) {
            String type = columnTypes.get(i);
            if (type == null) {
                throw new IllegalArgumentException("Le type de la colonne à l'index " + i + " est null.");
            }
            
            Object val = values.get(i);

            if (type.startsWith("INT")) {
                buff.putInt(Integer.parseInt(val.toString()));
            } else if (type.startsWith("FLOAT")) {
                buff.putFloat(Float.parseFloat(val.toString()));
            } else if (type.startsWith("CHAR")) {
                int size = Integer.parseInt(type.substring(type.indexOf('(') + 1, type.indexOf(')')));
                if (size < 0) {
                    throw new IllegalArgumentException("La taille pour CHAR ne peut pas être négative : " + size);
                }
                if (buff.remaining() < size) {
                    throw new BufferOverflowException();
                }
                
                String str = val.toString();
                for (int j = 0; j < size; j++) {
                    // Remplit avec le caractère ou avec un espace si la chaîne est plus courte
                    buff.put((byte) (j < str.length() ? str.charAt(j) : ' '));
                }
            } else if (type.startsWith("VARCHAR")) {
                String str = val.toString();
                int max = Integer.parseInt(type.substring(type.indexOf('(') + 1, type.indexOf(')')));
                if (max < 0) {
                    throw new IllegalArgumentException("La taille pour VARCHAR ne peut pas être négative : " + max);
                }
                if (buff.remaining() < max) {
                    throw new BufferOverflowException();
                }
                
                byte[] bytes = str.getBytes();
                if (bytes.length > max) {
                    // Tronque la chaîne si elle est trop longue
                    buff.put(bytes, 0, max);
                } else {
                    // Écrit la chaîne et remplit le reste avec des espaces
                    buff.put(bytes);
                    for (int j = bytes.length; j < max; j++) {
                        buff.put((byte) ' ');
                    }
                }
            } else {
                throw new IllegalArgumentException("Type de colonne non géré : " + type);
            }
        }
    }

    /**
     * Lit les données d'un ByteBuffer à partir d'une position spécifiée et remplit un enregistrement (record).
     * La désérialisation se fait en fonction des types de colonnes définis dans la relation.
     *
     * @param record L'enregistrement à remplir avec les données lues. Il sera vidé avant le remplissage. Ne doit pas être null.
     * @param buff Le buffer depuis lequel lire les données. Ne doit pas être null.
     * @param pos La position de départ dans le buffer pour la lecture.
     * @throws IllegalArgumentException si le record, le buffer ou un type de colonne est null,
     * ou si la position est invalide.
     * @throws IllegalStateException si les types de colonnes n'ont pas été initialisés.
     * @throws BufferUnderflowException si le buffer ne contient pas assez de données pour lire un champ complet.
     */
    public void readFromBuffer(Record record, ByteBuffer buff, int pos) {
        if (record == null) {
            throw new IllegalArgumentException("Le record ne doit pas être null.");
        }
        if (buff == null) {
            throw new IllegalArgumentException("Le buffer ne doit pas être null.");
        }
        if (pos < 0 || pos >= buff.capacity()) {
            throw new IllegalArgumentException("Position invalide: " + pos + " (capacité=" + buff.capacity() + ")");
        }
        if (columnTypes == null) {
            throw new IllegalStateException("La liste columnTypes n'est pas initialisée.");
        }

        buff.position(pos);
        for (String type : columnTypes) {
            if (type == null) {
                throw new IllegalArgumentException("Un type de colonne est null.");
            }

            if (type.startsWith("INT")) {
                if (buff.remaining() < Integer.BYTES) {
                    throw new BufferUnderflowException();
                }
                record.addValue(buff.getInt());
            } else if (type.startsWith("FLOAT")) {
                if (buff.remaining() < Float.BYTES) {
                    throw new BufferUnderflowException();
                }
                record.addValue(buff.getFloat());
            } else if (type.startsWith("CHAR")) {
                int size = Integer.parseInt(type.substring(type.indexOf('(') + 1, type.indexOf(')')));
                if (size < 0) {
                    throw new IllegalArgumentException("La taille pour CHAR ne peut pas être négative : " + size);
                }
                if (buff.remaining() < size) {
                    throw new BufferUnderflowException();
                }
                
                byte[] strBytes = new byte[size];
                buff.get(strBytes);
                record.addValue(new String(strBytes).trim());
            } else if (type.startsWith("VARCHAR")) {
                int max = Integer.parseInt(type.substring(type.indexOf('(') + 1, type.indexOf(')')));
                if (max < 0) {
                    throw new IllegalArgumentException("La taille pour VARCHAR ne peut pas être négative : " + max);
                }
                if (buff.remaining() < max) {
                    throw new BufferUnderflowException();
                }
                
                byte[] strBytes = new byte[max];
                buff.get(strBytes);
                record.addValue(new String(strBytes).trim());
            } else {
                throw new IllegalArgumentException("Type de colonne non géré : " + type);
            }
        }
    }
    

    public void addDataPage() throws IOException {
        // Allouer une nouvelle page de données
        PageId newPage = diskManager.allocPage();

        // Ouvrir la Header Page via le BufferManager
        byte[] headerBuf = bufferManager.GetPage(headerPageId);
        ByteBuffer hb = ByteBuffer.wrap(headerBuf);

        // Lire le nombre courant d’entrées
        final int pageSize = bufferManager.getConfig().getPageSize();
        if (pageSize < 12) {
            bufferManager.FreePage(headerPageId, false);
            throw new IOException("Taille de page insuffisante pour stocker la Header Page.");
        }

        int count = hb.getInt(0);
        int entryOffset = 4 + (count * 8);

        // Vérifier qu’on ne dépasse pas la taille de page
        if (entryOffset + 8 > pageSize) {
            bufferManager.FreePage(headerPageId, false);
            throw new IOException("Header Page pleine : impossible d’ajouter une nouvelle entrée (offset=" + entryOffset + ", pageSize=" + pageSize + ").");
        }

        // Écrire la nouvelle entrée (fileIdx, pageIdx), puis incrémenter count
        hb.putInt(entryOffset, newPage.getFileIdx());
        hb.putInt(entryOffset + 4, newPage.getPageIdx());
        hb.putInt(0, count + 1);

        // Libérer la page d’en-tête (dirty = true car on a modifié son contenu)
        bufferManager.FreePage(headerPageId, true);
    }
    
    public PageId getFreeDataPageId(int sizeRecord) throws IOException {
        
    	if (sizeRecord < 0) {
            throw new IllegalArgumentException("sizeRecord ne peut pas être négatif.");
        }

        final int pageSize = bufferManager.getConfig().getPageSize();

        // Charger la Header Page via le BufferManager
        byte[] headerBuf = bufferManager.GetPage(headerPageId);
        ByteBuffer hb = ByteBuffer.wrap(headerBuf);

        // Lire le nombre de pages référencées
        if (pageSize < 4) {
            bufferManager.FreePage(headerPageId, false);
            throw new IOException("Taille de page insuffisante pour une Header Page valide.");
        }
        int count = hb.getInt(0);

        // Parcourir les entrées (8 octets chacune)
        for (int i = 0; i < count; i++) {
            int entryOffset = 4 + i * 8;
            if (entryOffset + 8 > pageSize) {
                // Protection contre une header corrompue
                bufferManager.FreePage(headerPageId, false);
                throw new IOException("Header Page corrompue: entrée hors limites (index=" + i + ").");
            }

            int fileIdx = hb.getInt(entryOffset);
            int pageIdx = hb.getInt(entryOffset + 4);
            PageId pid = new PageId(fileIdx, pageIdx);

            // Lire la Data Page candidate et calculer l’espace libre réel
            byte[] dataBuf = bufferManager.GetPage(pid);
            ByteBuffer db = ByteBuffer.wrap(dataBuf);

            if (pageSize < 8) {
                bufferManager.FreePage(pid, false);
                bufferManager.FreePage(headerPageId, false);
                throw new IOException("Taille de page insuffisante pour un footer valide.");
            }

            int nbRecords = db.getInt(pageSize - 8);
            int freePos   = db.getInt(pageSize - 4);

            // Directory déjà utilisé + footer 8 octets
            int directoryUsed = nbRecords * 8;
            int freeBytes = pageSize - freePos - 8 - directoryUsed; // 8 pour la future entrée (pos,taille)

            bufferManager.FreePage(pid, false);

            // Assez de place ? (payload + entrée répertoire)
            if (freeBytes >= sizeRecord + 8) {
                bufferManager.FreePage(headerPageId, false);
                return pid;
            }
        }

        // Aucune page ne convient
        bufferManager.FreePage(headerPageId, false);
        return null;
    }

	
}