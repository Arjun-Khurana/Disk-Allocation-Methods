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

public class FileSystem {

    protected DiskDrive memory;

    public FileSystem(DiskDrive d) throws Exception {
        this.memory = d;
        FileTable ft = new FileTable();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(ft);
        oos.flush();
        byte[] data = bos.toByteArray();
        this.memory.write(0, data);
    }

    public void printBlock(int block) {
        byte[] data = this.memory.read(block);

        for (int i = 0; i < 512; i++) {
            try {
                System.out.print(data[i]);
                if ((i+1) % 32 == 0) {
                    System.out.println();
                }
            } catch (Exception E) {
                E.printStackTrace();
                break;
            }
        }
    }

    public void printBitmap() {
        printBlock(1);
    }

    protected int addToFileTable(String filename, byte where, byte length) throws Exception {
        byte[] ftBytes = this.memory.read(0);
        ByteArrayInputStream in = new ByteArrayInputStream(ftBytes);
        ObjectInputStream is = new ObjectInputStream(in);
        FileTable ft = (FileTable)is.readObject();

        boolean ftSpace = false;

        for (int i = 0; i < ft.table.length; i++) {
            if (ft.table[i] == null) {
                ft.table[i] = new FileEntry(filename, (byte)where, (byte)length);
                ftSpace = true;
                break;
            }
        } 

        if (!ftSpace) {
            System.out.println("No space found in filetable.");
            return 1;
        }
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(ft);
        oos.flush();
        ftBytes = bos.toByteArray();
        this.memory.write(0, ftBytes);
        return 0;
    }

    protected byte[][] subdivideData(int blockLength, byte[] data) {
        int leftover = data.length % blockLength;

        int numBlocks = data.length/blockLength + (leftover > 0 ? 1 : 0);
        byte[][] blockData  = new byte[numBlocks][blockLength];
        for (int i = 0; i < (leftover > 0 ? numBlocks - 1 : numBlocks); i++) {
            blockData[i] = Arrays.copyOfRange(data, i*blockLength, i*blockLength + blockLength);
        }
        if (leftover > 0) {
            blockData[numBlocks - 1] = Arrays.copyOfRange(data, (numBlocks-1)*blockLength, (numBlocks-1)*blockLength + leftover); 
        }
        return blockData;
    }

    public int diskToSim(Path path, String filename) throws Exception {
        System.out.println("Unimplemented");
        return 0;
    }
    public void printFileTable() throws Exception {
        System.out.println("Unummplemented");
    }
    public int simToDisk(Path path, String filename) throws Exception {return 0;}
    public void displayFile(String name) throws Exception {}
    public int deleteFile(String filename) throws Exception {return 0;}
}