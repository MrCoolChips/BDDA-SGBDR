package bdda;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.Queue;


public class DiskManager {
    
    private DBConfig config;
    private Queue<PageId> freePages;

    /**
     * Constructeur du DiskManager.
     * Initialise le gestionnaire avec la configuration fournie et
     * crée une queue vide pour les pages libres.
     * 
     * @param config configuration de la base de données contenant
     *               le chemin, la taille des pages et le nombre max de fichiers
     */
    public DiskManager(DBConfig config) {
        this.config = config;
        this.freePages = new LinkedList<>();
    }

    /**
     * Retourne la configuration actuelle du DiskManager.
     * 
     * @return l'objet DBConfig utilisé par ce gestionnaire
     */
    public DBConfig getConfig() {
        return config;
    }
    
    /**
     * Alloue une nouvelle page pour stockage.
     * Si une page précédemment désallouée est disponible, elle est réutilisée.
     * Sinon, une nouvelle page est créée à la fin d'un fichier existant
     * ou dans un nouveau fichier si nécessaire.
     * 
     * @return PageId identifiant unique de la page allouée
     * @throws IOException si impossible de créer le fichier ou d'écrire la page,
     *                     ou si la limite maximale de fichiers est atteinte
     */
    public PageId allocPage() throws IOException {

        if (!freePages.isEmpty()) {
            return freePages.poll();
        }
        
        int maxFiles = config.getMaxFileCount(); 
        
        for (int fileIdx = 0; fileIdx < maxFiles; fileIdx++) {
            File f = new File(config.getPath(), "Data" + fileIdx + ".bin");
            if (!f.exists()) {
                f.createNewFile();
            }

            try (RandomAccessFile raf = new RandomAccessFile(f, "rw")) {
                long pageIdx = raf.length() / config.getPageSize();
                raf.seek(raf.length());
                // Écrit une page vide (remplie de zéros) pour réserver l'espace
                raf.write(new byte[config.getPageSize()]);
                return new PageId(fileIdx, (int) pageIdx);
            }
        }
        
        throw new IOException("Limite de fichiers atteinte (" + maxFiles + ")");
    }

    /**
     * Désalloue une page en l'ajoutant à la liste des pages libres.
     * La page pourra être réutilisée lors du prochain appel à allocPage().
     * Vérifie que la page existe avant de la désallouer.
     * 
     * @param pageId identifiant de la page à désallouer
     * @throws IOException si la page n'existe pas ou si le fichier est inaccessible
     */
    public void DeallocPage(PageId pageId) throws IOException {
        File f = getFile(pageId);
        // Vérifie que la page existe
        getOffset(pageId, f);
        freePages.add(pageId);
    }

    /**
     * Lit le contenu d'une page et le copie dans le buffer fourni.
     * Le buffer doit avoir exactement la taille d'une page.
     * 
     * @param pageId identifiant de la page à lire
     * @param buff buffer de destination (doit faire config.getPageSize() octets)
     * @throws IOException si la page n'existe pas, le fichier est inaccessible,
     *                     ou si la taille du buffer est incorrecte
     */
    public void ReadPage(PageId pageId, byte[] buff) throws IOException {

        if (buff.length != config.getPageSize()) {
            throw new IOException("Taille du buffer (" + buff.length + ") différente de la taille d'une page (" + config.getPageSize() + ")");
        }

        File f = getFile(pageId);

        try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
            long offset = getOffset(pageId, f);
            raf.seek(offset);
            raf.readFully(buff);
        }
    }

    /**
     * Écrit le contenu du buffer dans la page spécifiée.
     * Le buffer doit avoir exactement la taille d'une page.
     * 
     * @param pageId identifiant de la page où écrire
     * @param buff buffer contenant les données à écrire (doit faire config.getPageSize() octets)
     * @throws IOException si la page n'existe pas, le fichier est inaccessible,
     *                     ou si la taille du buffer est incorrecte
     */
    public void WritePage(PageId pageId, byte[] buff) throws IOException {

        if (buff.length != config.getPageSize()) {
            throw new IOException("Taille du buffer (" + buff.length + ") différente de la taille d'une page (" + config.getPageSize() + ")");
        }

        File f = getFile(pageId);

        try (RandomAccessFile raf = new RandomAccessFile(f, "rw")) {
            long offset = getOffset(pageId, f);
            raf.seek(offset);
            raf.write(buff);
        }
    }

    /**
     * Retourne l'objet File correspondant au PageId fourni.
     * Vérifie que le fichier existe sur le disque.
     * 
     * @param pageId identifiant de la page
     * @return objet File correspondant au fichier contenant cette page
     * @throws IOException si le fichier n'existe pas
     */
    private File getFile(PageId pageId) throws IOException {
        File f = new File(config.getPath(), "Data" + pageId.getFileIdx() + ".bin");
        
        if (!f.exists()) {
            throw new IOException("Fichier inexistant : " + f.getAbsolutePath());
        }
        
        return f;
    }

    /**
     * Calcule l'offset (position en octets) d'une page dans son fichier.
     * Vérifie que la page existe réellement dans le fichier.
     * 
     * @param pageId identifiant de la page
     * @param f fichier contenant la page
     * @return position en octets du début de la page dans le fichier
     * @throws IOException si la page dépasse la taille actuelle du fichier
     */
    private long getOffset(PageId pageId, File f) throws IOException {
        long offset = (long) pageId.getPageIdx() * config.getPageSize();

        if (offset + config.getPageSize() > f.length()) {
            throw new IOException("Page " + pageId.getPageIdx() + 
                    " inexistante dans le fichier " + f.getName());
        }
        
        return offset;
    }

}
