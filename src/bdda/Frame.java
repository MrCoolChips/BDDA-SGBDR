package bdda;

public class Frame {

    PageId pageId;
    byte[] buffer;
    boolean dirty;
    int pinCount;
    long lastAccess;

    Frame(int pageSize) {
        this.pageId = null;
        this.buffer = new byte[pageSize];
        this.dirty = false;
        this.pinCount = 0;
        this.lastAccess = System.currentTimeMillis();
    }
}
