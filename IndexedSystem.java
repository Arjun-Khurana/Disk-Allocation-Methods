/* 
    Arjun Khurana
    CS4348 Project 3

    IndexedSystem class for project 3, which inherits from the 
    generic FileSystem class, and overrides methods for the full functionality 
    of the file system. Utilizes the indexed allocation method,
    in which each file has a block dedicated to holding indices of 
    blocks which actually contain file data
*/

// Used for serializing the file table
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

// Used to read and write to and from the disk, and the println
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class IndexedSystem extends FileSystem {

    // Constructor simply calls the parent constructor with the disk drive
    public IndexedSystem(DiskDrive d) throws Exception {
        super(d);
    }

    // Method to display a particular file from the simulation
    // takes as argument the name of the file to display
    @Override
    public void displayFile(String name) throws Exception {
        // Get the filetable
        FileTable ft = getFileTable();

        int start = 0;
        boolean found = false;

        // Iterate over the table to find the correct file
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

        // If the file is not in the table, return with an error
        if (!found) {
            System.out.println("File not found.");
            return;
        }

        // Get the index block and initialize a display block
        byte[] indices = this.memory.read(start);
        byte[] toDisplay;

        // for every index in the index block, read that block
        // from memory and display it byte by byte
        for (byte b : indices) {
            toDisplay = this.memory.read((int)b);
            for (int i = 0; i < toDisplay.length; i++) {
                System.out.print(toDisplay[i]);
            }
        }
    }
    
    // Method to print the file table
    @Override
    public void printFileTable() throws Exception {
        // Get the filetable
        FileTable ft = getFileTable();
        System.out.println(
            "File" + "\t" +
            "Start" + "\t"
        );

        // Iterate over the file table and print every non-null element
        for (int i = 0; i < ft.table.length; i++) {
            if (ft.table[i] == null) {
                continue;
            }

            // Print the name and starting block
            // The length field is irrelevant for the indexed scheme
            // with fixed length partitions
            System.out.println(
                String.valueOf(ft.table[i].name) + "\t" + 
                (int)ft.table[i].start
            );
        }
    }   

    // Method to copy a file from the disk to the simulation
    // Takes as argument the path to the disk file, and the 
    // filename to associate it with in the simulation
    @Override
    public int diskToSim(Path path, String filename) throws Exception {
        // Try to read the file specified by the path, or return
        // with an error
        byte[] data;
        try {
            data = Files.readAllBytes(path);
        } catch (Exception E) {
            System.out.println("File not found.");
            return 1;
        }
        
        // Read the bitmap to a local block and initialize a variable
        // to hold the location of the index block
        byte[] bitmap = this.memory.read(1);
        int indexBlock = 0;

        // Search the bitmap for the first available block to hold the index block
        for (int i = 0; i < bitmap.length; i++) {
            if (bitmap[i] == 0) {
                bitmap[i] = 1;
                indexBlock = i;
                break;
            }
        }

        // If there is not enough space, return with an error
        if (indexBlock == 0) {
            System.out.println("Not enough space found on disk.");
            return 1;
        }

        // If there is not enough room in the file table, return with an error
        if (this.addToFileTable(filename, (byte)indexBlock, (byte)0) != 0) {
            return 1;
        }

        // Subdivide the file into blocks of length 512 bytes
        byte[][] blockData = subdivideData(512, data);

        // initialize an array to hold the bytes which will go
        // to the index block
        byte[] toIndex = new byte[blockData.length];

        // search for available blocks to place the file blocks
        for (int i = 0; i < blockData.length; i++) {
            for (int j = 0; j < bitmap.length; j++) {
                // if we find an empty block, indicate where it is in the
                // index array and write it to the memory
                if (bitmap[j] == 0) {
                    bitmap[j] = 1;
                    toIndex[i] = (byte)j;
                    this.memory.write(j, blockData[i]);
                    break;
                }
            }
        }

        // if all the indices are not allocated, free up the memory
        // that was allocated and return with an error
        for (int i = 0; i < toIndex.length; i++) {
            if (toIndex[i] == 0) {
                System.out.println("Not enough space found on disk.");
                for (int j = i-1; j >= 0; j--) {
                    bitmap[j] = 0;
                }
                return 1;
            }
        }
        
        // write the index block and updated bitmap to memory
        this.memory.write(indexBlock, toIndex);
        this.memory.write(1, bitmap);

        return 0;
    }

    // Method to copy a file from the simulation to the disk
    // takes as argument the path to the disk location, and the
    // filename in the simulation
    @Override
    public int simToDisk(Path path, String filename) throws Exception {
        // Get the filetable
        FileTable ft = getFileTable();

        int start = 0;
        boolean found = false;

        // Iterate over the file table to find the correct file
        for (int i = 0; i < ft.table.length; i++) {
            if (ft.table[i] == null) {
                continue;
            }
            FileEntry e = ft.table[i];
            if (String.valueOf(e.name).equals(filename)) {
                found = true;
                start = e.start;
                break;
            }
        } 

        // If the file isnt in the table, return with an error
        if (!found) {
            System.out.println("File not found.");
            return 1;
        }

        // Create a file output stream object to write the file to the disk
        File outputFile = path.toFile();
        OutputStream fileOut = new FileOutputStream(outputFile);

        // get the index block from memory
        byte[] indices = this.memory.read(start);

        // For every index in the index block, write the memory block corresponding
        // to that index out to the file stream
        for (byte b : indices) {
            fileOut.write(this.memory.read((int)b));
        }

        fileOut.close();
        return 0;
    }
    
    // Method to delete a file from the simulation
    // takes as argument the name of the file to delete
    @Override
    public int deleteFile(String filename) throws Exception {
        // Get the filetable
        FileTable ft = getFileTable();

        int start = 0;
        boolean found = false;

        // Iterate over the filetable to find the correct file
        for (int i = 0; i < ft.table.length; i++) {
            if (ft.table[i] == null) {
                continue;
            }
            FileEntry e = ft.table[i];

            // if we find it, set that entry to null
            if (String.valueOf(e.name).equals(filename)) {
                found = true;
                start = e.start;
                ft.table[i] = null;
                break;
            }
        } 

        // If the file isnt in the table, return with an error
        if (!found) {
            System.out.println("File not found.");
            return 1;
        }

        // Serialize the filetable and write it back to memory
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(ft);
        oos.flush();
        byte[] ftBytes = bos.toByteArray();
        this.memory.write(0, ftBytes);

        // Get the bitmap and the index block
        byte[] bitmap = this.memory.read(1);
        byte[] indices = this.memory.read(start);

        // Set the index block to free
        bitmap[start] = 0;

        // Set every index in the index block to free inthe bitmap
        for (byte b : indices) {
            bitmap[(int)b] = 0;
        }

        // Write the bitmap back to memory
        this.memory.write(1, bitmap);

        return 0;
    }
}
