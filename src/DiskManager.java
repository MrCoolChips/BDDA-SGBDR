import java.util.ArrayList;
import java.util.List;

public class DiskManager {

    private DBConfig config;
    private List<PageId> freePages;

    public DiskManager(DBConfig config) {
        this.config = config;
        this.freePages = new ArrayList<>();
    }
    
    public DBConfig getDBConfig() {
        return this.config;
    }

    public PageId AllocPage() {
        if(!this.freePages.isEmpty()) {
            return freePages.remove(0);
        }
        // TODO: Si aucune page libre disponible créer une nouvelle page dans un fichier
        // TODO: Si il nous reste plus d'espace dans un fichier creer un nouveau fichier + une page 
        return null;
    }
}
