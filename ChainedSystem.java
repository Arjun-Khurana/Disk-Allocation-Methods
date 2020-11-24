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


public class ChainedSystem extends FileSystem {

    public ChainedSystem(DiskDrive d) throws Exception {
        super(d);
    }

    private byte[] combineArrays(byte[] first, byte[] second) {
        byte[] res = new byte[first.length + second.length];
        System.arraycopy(first, 0, res, 0, first.length);
        System.arraycopy(second, 0, res, first.length, second.length);
        return res;
    }

    public int diskToSim(Path path, String filename) throws Exception {
        byte[] data = Files.readAllBytes(path);
        if (data.length >= 511) {
            int blockLength = 511;
            int leftover = data.length % blockLength;

            int numBlocks = data.length/blockLength + (leftover > 0 ? 1 : 0);
            byte[][] blockData  = new byte[numBlocks][blockLength];
            for (int i = 0; i < (leftover > 0 ? numBlocks - 1 : numBlocks); i++) {
                blockData[i] = Arrays.copyOfRange(data, i*blockLength, i*blockLength + blockLength);
            }
            if (leftover > 0) {
                blockData[numBlocks - 1] = Arrays.copyOfRange(data, (numBlocks-1)*blockLength, (numBlocks-1)*blockLength + leftover); 
            }

            byte[][] dataToPlace = new byte[numBlocks][512];
            byte[] whereToPlace = new byte[numBlocks+1];
            byte[] bitmap = this.memory.read(1);

            for (int i = 0; i < numBlocks; i++) {
                for (int j = 0; j < bitmap.length; j++) {
                    if (bitmap[j] == 0) {
                        whereToPlace[i] = (byte)j;
                        bitmap[j] = 1;
                        this.memory.write(1, bitmap);
                        break;
                    }
                }
            }

            for (int i = 0; i < numBlocks; i++) {
                if (whereToPlace[i] == 0) {
                    System.out.println("No space found on disk.");
                    return 1;
                }
            }
            
            whereToPlace[numBlocks] = 0;

            for (int i = 0; i < numBlocks; i++) {
                byte[] location = {whereToPlace[i+1]};
                dataToPlace[i] = combineArrays(location, blockData[i]);
                this.memory.write(whereToPlace[i], dataToPlace[i]);
            }

            if (this.addToFileTable(filename, whereToPlace[0], (byte)0) != 0) {
                return 1;
            }

        } else {
            int where = 0;
            byte[] bitmap = this.memory.read(1);
            for (int i = 0; i < bitmap.length; i++) {
                if (bitmap[i] == 0) {
                    byte[] location = {0};
                    this.memory.write(i, combineArrays(location, data));
                    where = i;
                    break;
                }
            }

            if (where == 0) {
                System.out.println("No space found on disk.");
                return 1;
            }

            if (this.addToFileTable(filename, (byte)where, (byte)0) != 0) {
                return 1;
            }
        }
        return 0;
    }

    public void displayFile(String name) throws Exception {
        byte[] data = this.memory.read(0);
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        FileTable ft = (FileTable)is.readObject();

        int start = 0;
        boolean found = false;

        for (int i = 0; i < ft.table.length; i++) {
            if (ft.table[i] == null) {
                break;
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

        byte[] toDisplay = this.memory.read(start);

        while (true) {
            for (int i = 1; i < toDisplay.length; i++) {
                System.out.print(toDisplay[i]);
            }
            if (toDisplay[0] == 0) {
                break;
            }
            toDisplay = this.memory.read(toDisplay[0]);
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
                (int)ft.table[i].start
            );
        }
    }

    public int simToDisk(Path path, String filename) throws Exception {
        byte[] ftBytes = this.memory.read(0);
        ByteArrayInputStream in = new ByteArrayInputStream(ftBytes);
        ObjectInputStream is = new ObjectInputStream(in);
        FileTable ft = (FileTable)is.readObject();

        int start = 0;
        boolean found = false;

        for (int i = 0; i < ft.table.length; i++) {
            if (ft.table[i] == null) {
                break;
            }
            FileEntry e = ft.table[i];
            if (String.valueOf(e.name).equals(filename)) {
                found = true;
                start = e.start;
                break;
            }
        } 

        if (!found) {
            System.out.println("File not found.");
            return 1;
        }

        File outputFile = path.toFile();
        OutputStream fileOut = new FileOutputStream(outputFile);

        byte[] data = this.memory.read(start);

        while (true) {
            byte[] toOut = new byte[data.length - 1];
            System.arraycopy(data, 1, toOut, 0, data.length-1);
            fileOut.write(toOut);
            if (data[0] == 0) {
                break;
            }
            data = this.memory.read(data[0]);
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
        boolean found = false;

        for (int i = 0; i < ft.table.length; i++) {
            if (ft.table[i] == null) {
                break;
            }
            FileEntry e = ft.table[i];
            if (String.valueOf(e.name).equals(filename)) {
                found = true;
                start = e.start;
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
        byte[] data = this.memory.read(start);
        bitmap[start] = 0;

        while (true) {
            if (data[0] == 0) {
                break;
            }
            bitmap[data[0]] = 0;
            data = this.memory.read(data[0]);
        }

        this.memory.write(1, bitmap);

        return 0;
    }
}
