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

        for (int i = 0; i < ft.table.length; i++) {
            if (ft.table[i] == null) {
                break;
            }
            FileEntry e = ft.table[i];
            if (String.valueOf(e.name).equals(name)) {
                found = true;
                start = e.start;
                length = e.length;
                break;
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

    public void printFileTable() throws Exception {
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

    public int diskToSim(Path path, String filename) throws Exception {
        byte[] data = Files.readAllBytes(path);
        if (data.length >= 512) {
            int blockLength = 512;
            int leftover = data.length % blockLength;

            int numBlocks = data.length/blockLength + (leftover > 0 ? 1 : 0);
            byte[][] blockData  = new byte[numBlocks][blockLength];
            for (int i = 0; i < (leftover > 0 ? numBlocks - 1 : numBlocks); i++) {
                blockData[i] = Arrays.copyOfRange(data, i*blockLength, i*blockLength + blockLength);
            }
            if (leftover > 0) {
                blockData[numBlocks - 1] = Arrays.copyOfRange(data, (numBlocks-1)*blockLength, (numBlocks-1)*blockLength + leftover); 
            }

            byte[] bitmap = this.memory.read(1);
            int where = 0;

            for (int i = 0; i < bitmap.length; i++) {
                boolean foundSpace = true;
                if (bitmap[i] == 0) {
                    for (int j = i; j < i + numBlocks; j++) {
                        if (bitmap[j] == 1) {
                            foundSpace = false;
                            break;
                        }
                    }
                    if (foundSpace) {
                        where = i;
                        break;
                    }
                }
            }

            if (where == 0) {
                System.out.println("No space found on disk.");
                return 1;
            }

            for (int i = where, j = 0; i < where + numBlocks; i++, j++) {
                this.memory.write(i, blockData[j]);
            }

            if (this.addToFileTable(filename, (byte)where, (byte)numBlocks) != 0) {
                return 1;
            }

        } else {
            int where = 0;
            byte[] bitmap = this.memory.read(1);
            for (int i = 0; i < bitmap.length; i++) {
                if (bitmap[i] == 0) {
                    this.memory.write(i, data);
                    where = i;
                    break;
                }
            }

            if (where == 0) {
                System.out.println("No space found on disk.");
                return 1;
            }

            if (this.addToFileTable(filename, (byte)where, (byte)1) != 0) {
                return 1;
            }
        }
        return 0;
    }

    public int simToDisk(Path path, String filename) throws Exception {
        byte[] ftBytes = this.memory.read(0);
        ByteArrayInputStream in = new ByteArrayInputStream(ftBytes);
        ObjectInputStream is = new ObjectInputStream(in);
        FileTable ft = (FileTable)is.readObject();

        int start = 0;
        int length = 0;
        boolean found = false;

        for (int i = 0; i < ft.table.length; i++) {
            if (ft.table[i] == null) {
                break;
            }
            FileEntry e = ft.table[i];
            if (String.valueOf(e.name).equals(filename)) {
                found = true;
                start = e.start;
                length = e.length;
                break;
            }
        } 

        if (!found) {
            System.out.println("File not found.");
            return 1;
        }

        File outputFile = path.toFile();
        OutputStream fileOut = new FileOutputStream(outputFile);

        if (length > 1) {
            for (int i = 0; i < length; i++) {
                byte[] data = this.memory.read(start + i);
                fileOut.write(data);
            }
        } else {
            byte[] data = this.memory.read(start);
            fileOut.write(data);
        }

        fileOut.close();
        return 0;
    }

    public int deleteFile(String filename) throws Exception {
        byte[] ftBytes = this.memory.read(0);
        ByteArrayInputStream in = new ByteArrayInputStream(ftBytes);
        ObjectInputStream is = new ObjectInputStream(in);
        FileTable ft = (FileTable)is.readObject();

        int start = 0;
        int length = 0;
        boolean found = false;

        for (int i = 0; i < ft.table.length; i++) {
            if (ft.table[i] == null) {
                break;
            }
            FileEntry e = ft.table[i];
            if (String.valueOf(e.name).equals(filename)) {
                found = true;
                start = e.start;
                length = e.length;
                ft.table[i] = null;
                break;
            }
        } 

        if (!found) {
            System.out.println("File not found.");
            return 1;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(ft);
        oos.flush();
        ftBytes = bos.toByteArray();
        this.memory.write(0, ftBytes);

        byte[] bitmap = this.memory.read(1);

        for (int i = start; i < start + length; i++) {
            bitmap[i] = 0;
        }

        this.memory.write(1, bitmap);

        return 0;
    }
}