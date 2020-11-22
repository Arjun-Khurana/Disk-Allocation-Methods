public class DiskDrive {
    
    private byte[][] memory;

    public DiskDrive() {
        memory = new byte[256][512]; 
    }

    public byte[] read(int index) {
        return memory[index];
    }

    public void write(int index, byte[] data) {
        memory[index] = data;
    }
}
