public class UserInterface {
    public static void main(String[] args) {
        DiskDrive drive = new DiskDrive();
        ContiguousSystem filesystem = new ContiguousSystem(drive);
        byte[] data = new byte[512];        
    }

}
