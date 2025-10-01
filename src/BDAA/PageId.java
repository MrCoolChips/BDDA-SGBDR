package BDAA;

public class PageId {

    private int FileIdx;
    private int PageIdx;

    /**
     * Constructeur de PageId
     * Crée un nouvel identifiant de page avec les indices spécifiés
     * @param FileIdx l'identifiant du fichier (le x dans Fx)
     * @param PageIdx l'indice de la page dans le fichier (commence à 0)
     */
    public PageId(int FileIdx, int PageIdx) {
        this.FileIdx = FileIdx;
        this.PageIdx = PageIdx;
    }

    /**
     * Récupère l'identifiant du fichier
     * @return l'identifiant du fichier (le x dans Fx)
     */
    public int getFileIdx() {
        return FileIdx;
    }

    /**
     * Récupère l'indice de la page dans le fichier
     * @return l'indice de la page (0 = première page, 1 = deuxième page, etc.)
     */
    public int getPageIdx() {
        return PageIdx;
    }
}
