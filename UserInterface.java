import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class UserInterface {
    public static void main(String[] args) throws Exception {
        DiskDrive drive = new DiskDrive();
        ContiguousSystem filesystem = new ContiguousSystem(drive);
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

            switch (input) {
                case 1:
                    System.out.print("Display which file? ");
                    String choice = in.nextLine();
                    filesystem.displayFile(choice);
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
                    break;
                
                case 6:
                    System.out.print("Copy from: ");
                    String inputString = in.nextLine();
                    System.out.print("Copy to: ");
                    String filename = in.nextLine();
                    Path path = Paths.get(inputString);
                    filesystem.storeFile(path, filename);
                    break;

                case 7:
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
