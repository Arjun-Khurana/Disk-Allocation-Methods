import java.io.Serializable;

public class FileTable implements Serializable {
    
    public FileEntry[] table;

    public FileTable() {
        table = new FileEntry[10];
    }
}