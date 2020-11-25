/*
    Arjun Khurana
    CS4348 Project 3

    DiskDrive class for project 3, which contains only methods relating
    to reading and writing to and from the main memory. Memory consists
    of 256 blocks of 512 bytes each. 
*/

public class DiskDrive {
    
    // Private memory 2d array
    private byte[][] memory;

    // Constructor initializes the memory array
    // and sets the first and second blocks to full in the bitmap
    public DiskDrive() {
        memory = new byte[256][512]; 
        memory[1][0] = 1;
        memory[1][1] = 1;
    }

    // Method to get a particular block from memory
    public byte[] read(int index) {
        return memory[index];
    }

    // Method to write a block to a block index in memory
    public void write(int index, byte[] data) {
        memory[index] = data;
        // Also updates the bitmap
        this.memory[1][index] = 1;
    }
}
