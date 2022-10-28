package ClassFile.MipsInstr;

public class Syscall extends Instr {
    public static int PRINT_INTEGER = 1;
    public static int PRINT_STRING = 4;
    public static int READ_INTEGER = 5;
    public static int EXIT = 10;

    @Override
    public String toString() {
        return "\tsyscall";
    }
}
