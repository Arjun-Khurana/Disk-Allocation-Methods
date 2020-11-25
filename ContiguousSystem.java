/*
    Arjun Khurana
    CS4348 Project 3

    ContiguousSystem class for project 3, which inherits from the 
    generic FileSystem class, and overrides methods for the full functionality
    of the file system. Utilizes the contiguous allocation method,
    in which each file is stored sequentially in memory.
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

public class ContiguousSystem extends FileSystem {

    // Constructor simply calls the parent constructor with the disk drive
    public ContiguousSystem(DiskDrive d) throws Exception {
        super(d);
    }

    // Method to display a particular file from the simulation
    // takes as argument the name of the file to display
    @Override
    public void displayFile(String name) throws Exception {
        // Get the filetable
        FileTable ft = getFileTable();

        int start = 0;
        int length = 0;
        boolean found = false;

        // Loop through the file table until we find the file we are looking for
        for (int i = 0; i < ft.table.length; i++) {
            if (ft.table[i] == null) {
                continue;
            }
            FileEntry e = ft.table[i];
            if (String.valueOf(e.name).equals(name)) {
                found = true;
                start = e.start;
                length = e.length;
                break;
            }
        } 

        // If the file is not in the table, return with an error
        if (!found) {
            System.out.println("File not found.");
            return;
        }

        // Initialize an empty block
        byte[] toDisplay = new byte[512];

        // For every block in the file, read that block into the local 
        // block variable and print it out byte by byte 
        for (int i = 0; i < length; i++) {
            toDisplay = this.memory.read(start + i);
            for (int j = 0; j < toDisplay.length; j++) {
                System.out.print(toDisplay[j]);
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
            "Start" + "\t" +
            "Length"
        );

        // Loop over the file table and print every non-null element
        for (int i = 0; i < ft.table.length; i++) {
            if (ft.table[i] == null) {
                continue;
            }

            // Print the name, start block, and number of blocks
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

        // Read the bitmap into local variable
        byte[] bitmap = this.memory.read(1);

        // Subdivide the data into blocks of length 512
        byte[][] blockData = subdivideData(512, data);
        int numBlocks = blockData.length;

        int where = 0;

        // Try to find space by iterating over the bitmap to find a 
        // contiguous stretch which fits the file
        for (int i = 0; i < bitmap.length; i++) {
            // When we find an open block, iterate until the number of
            // blocks to make sure the whole file can fit
            if (bitmap[i] == 0) {
                boolean foundSpace = true;
                for (int j = i; j < i + numBlocks; j++) {
                    if (j >= bitmap.length || bitmap[j] == 1) {
                        foundSpace = false;
                        break;
                    }
                }

                // If there is enough space, keep where the space starts
                if (foundSpace) {
                    where = i;
                    break;
                }
            }
        }

        // If not enough space, return with an error
        if (where == 0) {
            System.out.println("Not enough space found on disk.");
            return 1;
        }

        // If there is not enough space in the filetable, return with an error
        if (this.addToFileTable(filename, (byte)where, (byte)numBlocks) != 0) {
            return 1;
        }

        // Finally, write each block of the file to memory starting with the first
        // block and continuing sequentially
        for (int i = where, j = 0; i < where + numBlocks; i++, j++) {
            this.memory.write(i, blockData[j]);
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
        int length = 0;
        boolean found = false;

        // Try to find the file in the file table
        for (int i = 0; i < ft.table.length; i++) {
            if (ft.table[i] == null) {
                continue;
            }
            FileEntry e = ft.table[i];
            if (String.valueOf(e.name).equals(filename)) {
                found = true;
                start = e.start;
                length = e.length;
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

        // Write each block from the file in the simulation to the
        // output stream
        for (int i = 0; i < length; i++) {
            byte[] data = this.memory.read(start + i);
            fileOut.write(data);
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
        int length = 0;
        boolean found = false;

        // Try to find the file in the file table
        for (int i = 0; i < ft.table.length; i++) {
            if (ft.table[i] == null) {
                continue;
            }
            FileEntry e = ft.table[i];

            // If we find the file, set that entry to null in the table
            if (String.valueOf(e.name).equals(filename)) {
                System.out.println(e.name);
                found = true;
                start = e.start;
                length = e.length;
                ft.table[i] = null;
                break;
            }
        } 

        // If the file isn't found, return with an error
        if (!found) {
            System.out.println("File not found.");
            return 1;
        }

        // Serialize the file table object and write it back to memory
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(ft);
        oos.flush();
        byte[] ftBytes = bos.toByteArray();
        this.memory.write(0, ftBytes);

        // Update the bitmap to reflect the file deletion
        byte[] bitmap = this.memory.read(1);

        for (int i = start; i < start + length; i++) {
            bitmap[i] = 0;
        }

        // Write the bitmap back to memory
        this.memory.write(1, bitmap);

        return 0;
    }
}