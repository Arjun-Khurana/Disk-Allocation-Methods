/*
    Arjun Khurana
    CS4348 Project 3

    FileTable class for project3, which contains a serializable class
    with one attribute: an array of FileEntry objects
*/

// Used for serializing and deserializing the objects of this class
import java.io.Serializable;

public class FileTable implements Serializable {
    
    // FileEntry array
    public FileEntry[] table;

    // Constructor initializes the file table
    public FileTable() {
        table = new FileEntry[10];
    }
}