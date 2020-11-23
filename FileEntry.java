import java.io.Serializable;

public class FileEntry implements Serializable {
    public char[] name;
    public byte start;
    public byte length;   

    public FileEntry(String n, byte s, byte l) {
        this.name = new char[8];
        this.name = n.toCharArray();
        this.start = s;
        this.length = l;
    }
}

