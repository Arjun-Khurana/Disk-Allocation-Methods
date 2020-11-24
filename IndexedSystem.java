import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.io.ObjectInputStream;

public class IndexedSystem extends FileSystem {

    public IndexedSystem(DiskDrive d) throws Exception {
        super(d);
    }

    @Override
    public int diskToSim(Path path, String filename) throws Exception {
        byte[] data = Files.readAllBytes(path);
        byte[] bitmap = this.memory.read(1);
        int indexBlock = 0;

        for (int i = 0; i < bitmap.length; i++) {
            if (bitmap[i] == 0) {
                bitmap[i] = 1;
                indexBlock = i;
                break;
            }
        }

        if (indexBlock == 0) {
            System.out.println("Not enough space found on disk.");
            return 1;
        }

        byte[][] blockData = subdivideData(512, data);
        byte[] toIndex = new byte[blockData.length];
        System.out.println(blockData.length);

        for (int i = 0; i < blockData.length; i++) {
            for (int j = 0; j < bitmap.length; j++) {
                if (bitmap[j] == 0) {
                    bitmap[j] = 1;
                    toIndex[i] = (byte)j;
                    this.memory.write(j, blockData[i]);
                    break;
                }
            }
        }

        for (int i = 0; i < toIndex.length; i++) {
            if (toIndex[i] == 0) {
                System.out.println("Not enough space found on disk.");
                for (int j = i-1; j >= 0; j--) {
                    bitmap[j] = 0;
                }
                return 1;
            }
        }
        
        this.memory.write(indexBlock, toIndex);
        this.memory.write(1, bitmap);

        if (this.addToFileTable(filename, (byte)indexBlock, (byte)0) != 0) {
            return 1;
        }

        return 0;
    }

    @Override
    public int simToDisk(Path path, String filename) throws Exception {
        return 0;
    }

    @Override
    public void displayFile(String name) throws Exception {
        byte[] data = this.memory.read(0);
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        FileTable ft = (FileTable)is.readObject();

        int start = 0;
        boolean found = false;

        for (int i = 0; i < ft.table.length; i++) {
            if (ft.table[i] == null) {
                continue;
            }
            FileEntry e = ft.table[i];
            if (String.valueOf(e.name).equals(name)) {
                found = true;
                start = e.start;
                break;
            }
        } 

        if (!found) {
            System.out.println("File not found.");
            return;
        }

        byte[] indices = this.memory.read(start);
        byte[] toDisplay;

        for (byte b : indices) {
            toDisplay = this.memory.read((int)b);
            for (int i = 0; i < toDisplay.length; i++) {
                System.out.print(toDisplay[i]);
            }
        }
    }

    @Override
    public void printFileTable() throws Exception {
        byte[] ftBytes = this.memory.read(0);
        ByteArrayInputStream in = new ByteArrayInputStream(ftBytes);
        ObjectInputStream is = new ObjectInputStream(in);
        FileTable ft = (FileTable)is.readObject();
        for (int i = 0; i < ft.table.length; i++) {
            if (ft.table[i] == null) {
                continue;
            }
            System.out.println(
                String.valueOf(ft.table[i].name) + "\t" + 
                (int)ft.table[i].start
            );
        }
    }   

    @Override
    public int deleteFile(String filename) throws Exception {
        return 0;
    }
}
