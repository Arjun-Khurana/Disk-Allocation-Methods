import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.ObjectInputStream;

public class ContiguousSystem extends FileSystem {

    public ContiguousSystem(DiskDrive d) {
        super(d);
    }

    public void displayFile(String name) throws Exception {
        byte[] data = this.memory.read(0);
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        FileTable ft = (FileTable)is.readObject();

        for (FileEntry e : ft.table) {
            if (String.valueOf(e.name).equals(name)) {
                System.out.println(e.name);
            }
        }
        // System.out.println(ft.table[0].name);
    }

    public void storeFile(Path path, String filename) throws Exception {
        byte[] data = Files.readAllBytes(path);
        if (data.length >= 512) {
            
        }
    }
}