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
}