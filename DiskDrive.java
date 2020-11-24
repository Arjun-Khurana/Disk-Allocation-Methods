public class DiskDrive {
    
    private byte[][] memory;

    public DiskDrive() {
        memory = new byte[256][512]; 
        memory[1][0] = 1;
        memory[1][1] = 1;
    }

    public byte[] read(int index) {
        return memory[index];
    }

    public void write(int index, byte[] data) {
        memory[index] = data;
        this.memory[1][index] = 1;
    }
}
