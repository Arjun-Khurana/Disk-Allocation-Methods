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
            System.out.println(Arrays.toString(whereToPlace));
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
            
            whereToPlace[numBlocks] = 0;

            for (int i = 0; i < numBlocks; i++) {
                byte[] location = {whereToPlace[i+1]};
                dataToPlace[i] = combineArrays(location, blockData[i]);
                this.memory.write(whereToPlace[i], dataToPlace[i]);
            }

            // int where = 0;

            // for (int i = 0; i < bitmap.length; i++) {
            //     boolean foundSpace = true;
            //     if (bitmap[i] == 0) {
            //         for (int j = i; j < i + numBlocks; j++) {
            //             if (bitmap[j] == 1) {
            //                 foundSpace = false;
            //                 break;
            //             }
            //         }
            //         if (foundSpace) {
            //             where = i;
            //             break;
            //         }
            //     }
            // }

            // if (where == 0) {
            //     System.out.println("No space found on disk.");
            //     return 1;
            // }

            // for (int i = where, j = 0; i < where + numBlocks; i++, j++) {
            //     this.memory.write(i, blockData[j]);
            // }

            // if (this.addToFileTable(filename, (byte)where, (byte)numBlocks) != 0) {
            //     return 1;
            // }

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

            // if (this.addToFileTable(filename, (byte)where, (byte)1) != 0) {
            //     return 1;
            // }
        }
        return 0;
    }

    public void displayFile(String name) throws Exception {

    }

    public void printFileTable() throws Exception {

    }

    private int addToFileTable(String filename, byte where, byte length) throws Exception {
        return 0;
    }

    public int simToDisk(Path path, String filename) throws Exception {
        return 0;
    }

    public int deleteFile(String filename) throws Exception {
        return 0;
    }
}
