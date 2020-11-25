/* 
    Arjun Khurana
    CS4348 Project 3

    ChainedSystem class for project 3, which inherits from the 
    generic FileSystem class, and overrides methods for the full functionality 
    of the file system. Utilizes the chained allocation method,
    in which each file block contains a pointer to the next block 
    in the file
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

public class ChainedSystem extends FileSystem {

    // Constructor simply calls the parent constructor with the disk drive
    public ChainedSystem(DiskDrive d) throws Exception {
        super(d);
    }

    // Method to combine arrays
    private byte[] combineArrays(byte[] first, byte[] second) {
        byte[] res = new byte[first.length + second.length];
        System.arraycopy(first, 0, res, 0, first.length);
        System.arraycopy(second, 0, res, first.length, second.length);
        return res;
    }

    // Method to display a particular file from the simulation
    // takes as argument the name of the file to display
    @Override
    public void displayFile(String name) throws Exception {
        // Get the filetable
        FileTable ft = getFileTable();

        int start = 0;
        boolean found = false;

        // Loop over the file table to find the correct file
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

        // Get the initial block of the file
        byte[] toDisplay = this.memory.read(start);

        // While the pointer byte is non-zero, display the
        // current block and get the next block using the pointer
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

    // Method to print the file table   
    @Override
    public void printFileTable() throws Exception {
        // Get the filetable
        FileTable ft = getFileTable();
        System.out.println(
            "File" + "\t" +
            "Start" + "\t" +
            "Length"
        );

        // Iterate over the table and print the non-null elements
        for (int i = 0; i < ft.table.length; i++) {
            if (ft.table[i] == null) {
                continue;
            }

            // Print the name of the file, the starting block, and the length
            System.out.println(
                String.valueOf(ft.table[i].name) + "\t" + 
                (int)ft.table[i].start + "\t" + 
                (int)ft.table[i].length
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

        // Here, the block size is only 511 bytes because we need one byte 
        // for the pointer to the next block
        int blockLength = 511;
        
        // subdivide the file into blocks of length 511
        byte[][] blockData  = subdivideData(blockLength, data);
        
        // initialize arrays for the data to write, and where to write it
        byte[][] dataToPlace = new byte[blockData.length][512];
        byte[] whereToPlace = new byte[blockData.length+1];

        // read the bitmap
        byte[] bitmap = this.memory.read(1);

        // For every block in the file, look for an open block in the
        // bitmap to fit it
        for (int i = 0; i < blockData.length; i++) {
            for (int j = 0; j < bitmap.length; j++) {
                // when we find an open block, add the place where we
                // found it to the array of where to place
                if (bitmap[j] == 0) {
                    whereToPlace[i] = (byte)j;

                    // update the bitmap so we don't choose this block again
                    bitmap[j] = 1;
                    this.memory.write(1, bitmap);
                    break;
                }
            }
        }

        // If any of the blocks don't have a place to go, return with an error
        for (int i = 0; i < blockData.length; i++) {
            if (whereToPlace[i] == 0) {
                System.out.println("Not enough space found on disk.");
                return 1;
            }
        }
        
        // make sure the last block has a 0 pointer
        whereToPlace[blockData.length] = 0;

        // If there is no room in the filetable, return with an error
        if (this.addToFileTable(filename, whereToPlace[0], (byte)blockData.length) != 0) {
            return 1;
        }

        // for each block, write it to memory in the location specified by the
        // whereToPlace array
        for (int i = 0; i < blockData.length; i++) {
            byte[] location = {whereToPlace[i+1]};
            
            // combine the pointer to the next block with this array
            // and write it to the disk
            dataToPlace[i] = combineArrays(location, blockData[i]);
            this.memory.write(whereToPlace[i], dataToPlace[i]);
        }

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

        // Iterate over the filetable to find the correct file
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

        // If the file is not in the table, return with an error
        if (!found) {
            System.out.println("File not found.");
            return 1;
        }

        // Create a file output stream object to write the file to the disk
        File outputFile = path.toFile();
        OutputStream fileOut = new FileOutputStream(outputFile);

        // Get the first block of the file
        byte[] data = this.memory.read(start);

        // While the pointer byte is non-zero, write the
        // current block and get the next block using the pointer
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

    // Method to delete a file from the simulation
    // takes as argument the name of the file to delete
    @Override
    public int deleteFile(String filename) throws Exception {
        // Get the filetable
        FileTable ft = getFileTable();

        int start = 0;
        boolean found = false;

        // Iterate over the file table to get the correct file
        for (int i = 0; i < ft.table.length; i++) {
            if (ft.table[i] == null) {
                continue;
            }
            FileEntry e = ft.table[i];

            // If we find the file, set that entry to null in the table
            if (String.valueOf(e.name).equals(filename)) {
                found = true;
                start = e.start;
                ft.table[i] = null;
                break;
            }
        } 

        // If the file is not in the table, return with an error
        if (!found) {
            System.out.println("File not found.");
            return 1;
        }

        // Write the file back to memory
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(ft);
        oos.flush();
        byte[] ftBytes = bos.toByteArray();
        this.memory.write(0, ftBytes);

        // Get the bitmap and the first block of the file
        byte[] bitmap = this.memory.read(1);
        byte[] data = this.memory.read(start);

        // Set the first block to free in the bitmap
        bitmap[start] = 0;

        // While the pointer byte is non-zero, free the
        // current block in the bitmap and get the next block 
        // using the pointer
        while (true) {
            if (data[0] == 0) {
                break;
            }
            bitmap[data[0]] = 0;
            data = this.memory.read(data[0]);
        }

        // Write the bitmap back to memory
        this.memory.write(1, bitmap);

        return 0;
    }
}
