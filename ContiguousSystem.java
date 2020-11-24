import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.ObjectInputStream;

public class ContiguousSystem extends FileSystem {

    public ContiguousSystem(DiskDrive d) throws Exception {
        super(d);
    }

    public void displayFile(String name) throws Exception {
        byte[] data = this.memory.read(0);
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        FileTable ft = (FileTable)is.readObject();

        int start = 0;
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
    }

    public void printFileTable() throws Exception{
        byte[] ftBytes = this.memory.read(0);
        ByteArrayInputStream in = new ByteArrayInputStream(ftBytes);
        ObjectInputStream is = new ObjectInputStream(in);
        FileTable ft = (FileTable)is.readObject();
        for (int i = 0; i < ft.table.length; i++) {
            if (ft.table[i] == null) {
                break;
            }
            System.out.println(
                String.valueOf(ft.table[i].name) + "\t" + 
                (int)ft.table[i].start + "\t" + 
                (int)ft.table[i].length
            );
        }
    }

    public void storeFile(Path path, String filename) throws Exception {
        byte[] data = Files.readAllBytes(path);
        if (data.length >= 512) {
            System.out.println("TODO");
        } else {
            int where = 0;
            byte[] bitmap = this.memory.read(1);
            for (int i = 0; i < bitmap.length; i++) {
                if (bitmap[i] == 0) {
                    System.out.println(bitmap[i]);
                    this.memory.write(i, data);
                    where = i;
                    break;
                }
            }

            if (where == 0) {
                System.out.println("No space found on disk.");
                return;
            }
            
            // System.out.println(data.length);
            byte[] ftBytes = this.memory.read(0);
            ByteArrayInputStream in = new ByteArrayInputStream(ftBytes);
            ObjectInputStream is = new ObjectInputStream(in);
            FileTable ft = (FileTable)is.readObject();

            boolean ftSpace = false;

            for (int i = 0; i < ft.table.length; i++) {
                if (ft.table[i] == null) {
                    ft.table[i] = new FileEntry(filename, (byte)where, (byte)1);
                    ftSpace = true;
                    break;
                }
            } 

            if (!ftSpace) {
                System.out.println("No space found in filetable.");
                return;
            }
            
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(ft);
            oos.flush();
            ftBytes = bos.toByteArray();
            this.memory.write(0, ftBytes);
        }
    }
}