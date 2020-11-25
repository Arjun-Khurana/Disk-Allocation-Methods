/*
    Arjun Khurana
    CS4348 Project 3

    FileSystem class for project 3, which serves as a parent class for each
    of the three types of file systems. Contains some methods which are 
    universal to all file systems, and some methods which are overridden
    by the child classes. This object is not a fully functional file system 
    itself, it only provides a baseline for the child file systems.
*/

// Used to serialize the file table object
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

// Used to get the path of disk files
import java.nio.file.Path;

// Used for array operations
import java.util.Arrays;


public class FileSystem {

    // DiskDrive object is only visible by this class and its children
    protected DiskDrive memory;

    // Constructor takes as argument a disk drive
    public FileSystem(DiskDrive d) throws Exception {
        this.memory = d;

        // Initializing a file table and setting the first block to the
        // empty table
        FileTable ft = new FileTable();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(ft);
        oos.flush();
        byte[] data = bos.toByteArray();
        this.memory.write(0, data);
    }

    // Method to print a particular block
    public void printBlock(int block) {
        // Get the block from memory
        byte[] data = this.memory.read(block);

        // Try to print out the block byte by byte
        // If some of the block is null, catch and exit
        for (int i = 0; i < 512; i++) {
            try {
                System.out.print(data[i]);
                if ((i+1) % 32 == 0) {
                    System.out.println();
                }
            } catch (Exception E) {
                // E.printStackTrace();
                break;
            }
        }
    }

    // Printing the bitmap is just printing the second block
    public void printBitmap() {
        printBlock(1);
    }

    // Child classes will use this method to add a new entry to the file table
    protected int addToFileTable(String filename, byte where, byte length) throws Exception {
        // Read the file table from memory and deserialize it
        byte[] ftBytes = this.memory.read(0);
        ByteArrayInputStream in = new ByteArrayInputStream(ftBytes);
        ObjectInputStream is = new ObjectInputStream(in);
        FileTable ft = (FileTable)is.readObject();

        // Try to find an empty space in the file table
        boolean ftSpace = false;

        for (int i = 0; i < ft.table.length; i++) {
            if (ft.table[i] == null) {
                ft.table[i] = new FileEntry(filename, (byte)where, (byte)length);
                ftSpace = true;
                break;
            }
        } 

        // If there is no space in the file table, return with an error
        if (!ftSpace) {
            System.out.println("No space found in filetable.");
            return 1;
        }

        // Serialize the file table and write it back to memory
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(ft);
        oos.flush();
        ftBytes = bos.toByteArray();
        this.memory.write(0, ftBytes);
        return 0;
    }

    // Child classes will use this method to obtain the file table object
    protected FileTable getFileTable() throws Exception {
        // Reading the file table from memory and deserializing
        // it into an object
        byte[] ftBytes = this.memory.read(0);
        ByteArrayInputStream in = new ByteArrayInputStream(ftBytes);
        ObjectInputStream is = new ObjectInputStream(in);

        // Returning the object
        return (FileTable)is.readObject();
    }

    // Child classes will use this method to break up large files into block-size chunks 
    protected byte[][] subdivideData(int blockLength, byte[] data) {
        // the leftover bytes after subdividing equally will go in the last block
        int leftover = data.length % blockLength;

        // assigning the number of blocks and initializing the 2d array
        int numBlocks = data.length/blockLength + (leftover > 0 ? 1 : 0);
        byte[][] blockData  = new byte[numBlocks][blockLength];

        // assigning the 2d array with blocks from the original array
        for (int i = 0; i < (leftover > 0 ? numBlocks - 1 : numBlocks); i++) {
            blockData[i] = Arrays.copyOfRange(data, i*blockLength, i*blockLength + blockLength);
        }

        // assigning the leftover
        if (leftover > 0) {
            blockData[numBlocks - 1] = Arrays.copyOfRange(data, (numBlocks-1)*blockLength, (numBlocks-1)*blockLength + leftover); 
        }
        return blockData;
    }

    // Method stubs which will be overridden in the child classes for the other
    // functionalities of the file system. 
    public int diskToSim(Path path, String filename) throws Exception {return 0;}
    public void printFileTable() throws Exception {}
    public int simToDisk(Path path, String filename) throws Exception {return 0;}
    public void displayFile(String name) throws Exception {}
    public int deleteFile(String filename) throws Exception {return 0;}
}