/*
    Arjun Khurana
    CS4348 Project 3

    UserInterface class for project 3, contains the main menu loop which
    continuously prompts the user for input and invokes methods on the filesystem
    to satisfy the user's requests.
*/

// Used for getting path of disk files
import java.nio.file.Path; 
import java.nio.file.Paths;

// Used for getting user input
import java.util.Scanner;

public class UserInterface {
    public static void main(String[] args) throws Exception {
        // Check to make sure the user has entered on command line argument
        if (args.length != 1) {
            System.out.println("Expected one argument.");
            return;
        }

        // Initialize the disk drive and filesystem objects, as well as 
        // a scanner for reading user input
        DiskDrive drive = new DiskDrive();
        FileSystem filesystem;
        Scanner in = new Scanner(System.in);

        // Assign the filesystem based on the command line argument
        switch(args[0]) {
            case "contiguous":
                filesystem = new ContiguousSystem(drive);
                break;
            
            case "chained":
                filesystem = new ChainedSystem(drive);
                break;

            case "indexed":
                filesystem = new IndexedSystem(drive);
                break;

            // if the user does not enter a correct argument, exit
            default:
                System.out.println("Invalid argument.");
                in.close();
                return;
        }
        
        // Loop forever until the user enters choice 8
        while (true) {
            // Display the menu
            System.out.println("1) Display a file");
            System.out.println("2) Display the file table");
            System.out.println("3) Display the free space bitmap");
            System.out.println("4) Display a disk block");
            System.out.println("5) Copy a file from the simulation to a file on the real system");
            System.out.println("6) Copy a file from the real system to a file in the simulation");
            System.out.println("7) Delete a file");
            System.out.println("8) Exit");
            
            int input;

            // Try to obtain user input, catch bad input and continue
            try {
                input = in.nextInt();
            } catch (Exception e) {
                System.out.println("Invalid choice");
                in.nextLine();
                continue;
            }

            // Notify user of their choice
            System.out.println("Choice: " + input);

            // Initializing some local variables 
            String inputString = "";
            String filename = "";
            Path path = null;

            // Switching on user input
            switch (input) {
                // Display a file
                case 1:
                    System.out.print("Display which file? ");
                    String choice = in.next();
                    filesystem.displayFile(choice);
                    System.out.println("");
                    break;

                // Print the file table
                case 2:
                    filesystem.printFileTable();
                    System.out.println("");
                    break;

                // Print the bitmap
                case 3:
                    filesystem.printBitmap();
                    System.out.println("");
                    break;

                // Print a particular disk block
                case 4:
                    System.out.print("Display which block? ");
                    int block = in.nextInt();
                    filesystem.printBlock(block);
                    System.out.println("");
                    break;

                // Copy a file from the simulation to the disk
                case 5:
                    System.out.print("Copy from: ");
                    filename = in.next();
                    System.out.print("Copy to: ");
                    inputString = in.next();
                    path = Paths.get(inputString);
                    if (filesystem.simToDisk(path, filename) == 0) {
                        System.out.println("Successfully copied to " + inputString);
                    }
                    System.out.println("");
                    break;
                
                // Copy a file from the disk to the simulation
                case 6:
                    System.out.print("Copy from: ");
                    inputString = in.next();
                    System.out.print("Copy to: ");
                    filename = in.next();
                    if (filename.length() > 8 || filename.contains(".")) {
                        System.out.println("Invalid filename");
                        System.out.println("");
                        break;
                    }
                    path = Paths.get(inputString);
                    if (filesystem.diskToSim(path, filename) == 0) {
                        System.out.println("Successfully copied to " + filename);
                    }
                    System.out.println("");
                    break;

                // Delete a file
                case 7:
                    System.out.print("Delete which file? ");
                    filename = in.next();
                    if (filesystem.deleteFile(filename) == 0) {
                        System.out.println("Successfully deleted " + filename);
                    }
                    System.out.println("");
                    break;

                // Exit
                case 8:
                    in.close();
                    System.out.println("Exiting.");
                    return;

                // Invalid choice
                default:
                    System.out.println("Invalid choice");
                    System.out.println("");
                    break;
            }
        }
    }
}
