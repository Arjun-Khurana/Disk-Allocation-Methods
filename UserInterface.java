import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class UserInterface {
    public static void main(String[] args) throws Exception {
        DiskDrive drive = new DiskDrive();
        ChainedSystem filesystem = new ChainedSystem(drive);
        byte[] data = new byte[512];

        Scanner in = new Scanner(System.in);
        
        while (true) {
            System.out.println("1) Display a file");
            System.out.println("2) Display the file table");
            System.out.println("3) Display the free space bitmap");
            System.out.println("4) Display a disk block");
            System.out.println("5) Copy a file from the simulation to a file on the real system");
            System.out.println("6) Copy a file from the real system to a file in the simulation");
            System.out.println("7) Delete a file");
            System.out.println("8) Exit");
            
            int input;

            try {
                input = in.nextInt();
            } catch (Exception e) {
                System.out.println("Invalid choice");
                in.nextLine();
                // e.printStackTrace();
                continue;
            }

            System.out.println("Choice: " + input);

            String inputString = "";
            String filename = "";
            Path path = null;
            int result;

            switch (input) {
                case 1:
                    System.out.print("Display which file? ");
                    String choice = in.next();
                    filesystem.displayFile(choice);
                    System.out.println("");
                    break;

                case 2:
                    filesystem.printFileTable();
                    break;

                case 3:
                    filesystem.printBitmap();
                    break;

                case 4:
                    System.out.print("Display which block? ");
                    int block = in.nextInt();
                    filesystem.printBlock(block);
                    break;

                case 5:
                    System.out.print("Copy from: ");
                    filename = in.next();
                    System.out.print("Copy to: ");
                    inputString = in.next();
                    path = Paths.get(inputString);
                    if (filesystem.simToDisk(path, filename) == 0) {
                        System.out.println("Successfully copied to " + inputString);
                    };
                    System.out.println("");
                    break;
                
                case 6:
                    System.out.print("Copy from: ");
                    inputString = in.next();
                    System.out.print("Copy to: ");
                    filename = in.next();
                    path = Paths.get(inputString);
                    if (filesystem.diskToSim(path, filename) == 0) {
                        System.out.println("Successfully copied to " + filename);
                    }
                    System.out.println("");
                    break;

                case 7:
                    System.out.print("Delete which file? ");
                    filename = in.next();
                    if (filesystem.deleteFile(filename) == 0) {
                        System.out.println("Successfully deleted " + filename);
                    }
                    System.out.println("");
                    break;

                case 8:
                    in.close();
                    return;

                default:
                    System.out.println("Invalid choice");
                    break;
            }
        }
    }
}
