import java.util.Scanner;

public class UserInterface {
    public static void main(String[] args) {
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
                    break;

                case 2:
                    break;

                case 3:
                    break;

                case 4:
                    break;

                case 5:
                    break;
                
                case 6:
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
