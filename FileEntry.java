/*
    Arjun Khurana
    CS4348 Project 3

    FileEntry class for project3, which contains a serializable class
    with attributes relating to elements of the file allocation table
*/

// Used for serializing and deserializing the class
import java.io.Serializable;

public class FileEntry implements Serializable {
    // Attributes for the name, starting block, and number of blocks
    public char[] name;
    public byte start;
    public byte length;   

    // Constructor takes as arguments a string and two bytes
    // corresponding to the attributes
    public FileEntry(String n, byte s, byte l) {
        this.name = n.toCharArray();
        this.start = s;
        this.length = l;
    }
}

