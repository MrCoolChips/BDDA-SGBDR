package bdda;

public class BufferManager {

    private DBConfig config;
    private DiskManager diskManager;

    public BufferManager(DBConfig config, DiskManager diskManager) {
        this.config = config;
        this.diskManager = diskManager;
    }

    public DBConfig getConfig() {
        return config;
    }

    public DiskManager getDiskManager() {
        return diskManager;
    }
    
}
