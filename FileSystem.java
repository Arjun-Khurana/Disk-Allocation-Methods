import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
}