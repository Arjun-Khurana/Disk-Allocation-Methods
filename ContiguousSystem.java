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

        int start = 0, 
        int length = 0;
        boolean found = false;

        for (FileEntry e : ft.table) {
            if (String.valueOf(e.name).equals(name)) {
                found = true;
                start = e.start;
                length = e.length;
            }
        }

        if (!found) {
            System.out.println("File not found.");
            return;
        }

        byte[] toDisplay = new byte[512];

        if (length > 1) {
            for (int i = 0; i < length; i++) {
                toDisplay = this.memory.read(start + i);
                for (int j = 0; j < toDisplay.length; j++) {
                    System.out.print(toDisplay[j]);
                }
            }
        } else {
            toDisplay = this.memory.read(start);
            for (int j = 0; j < toDisplay.length; j++) {
                System.out.print(toDisplay[j]);
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