public class FileSystem {

    protected DiskDrive memory;
    private FileTable filetable;

    public FileSystem(DiskDrive d) {
        this.memory = d;
    }

    public void printBlock(int block) {
        byte[] data = this.memory.read(block);

        for (int i = 0; i < 512; i++) {
            System.out.print(data[i]);
            if ((i+1) % 32 == 0) {
                System.out.println();
            }
        }
    }

    public void printFileTable() {
        printBlock(0);
    }

    public void printBitmap() {
        printBlock(1);
    }
}