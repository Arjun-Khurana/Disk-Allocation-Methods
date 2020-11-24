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

public class IndexedSystem extends FileSystem {

    public IndexedSystem(DiskDrive d) throws Exception {
        super(d);
    }

    public int diskToSim(Path path, String filename) throws Exception {
        return 0;
    }

    public int simToDisk(Path path, String filename) throws Exception {
        return 0;
    }

    public void displayFile(String name) throws Exception {
        
    }

    public void printFileTable() throws Exception {

    }   

    public int deleteFile(String filename) throws Exception {
        return 0;
    }
}
