import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

public class Test {

    public static void main(String[] args) throws Exception {

        DiskDrive d = new DiskDrive();
        byte[] b = d.read(1);
        System.out.println(b[0] == 0);
        // FileTable ft = new FileTable();

        // FileEntry e = new FileEntry("abcdefgh", (byte)1, (byte)2);
        // for (int i = 0; i < 10; i++) {
        //     ft.table[i] = new FileEntry("abcdefgh", (byte)1, (byte)2);
        // }

        // // byte[] data = new byte[512];

        // ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // ObjectOutputStream oos = new ObjectOutputStream(bos);
        // oos.writeObject(ft);
        // oos.flush();
        // byte[] data = bos.toByteArray();
        // System.out.println(data.length);

        // ByteArrayInputStream in = new ByteArrayInputStream(data);
        // ObjectInputStream is = new ObjectInputStream(in);
        // FileTable ft2 = (FileTable)is.readObject();
        // System.out.println(ft2.table[10].name);

        // for (int i = 0; i < 512; i++) {
        //     System.out.print(data[i]);
        //     if ((i+1) % 32 == 0) {
        //         System.out.println();
        //     }
        // }
    }
    
}
