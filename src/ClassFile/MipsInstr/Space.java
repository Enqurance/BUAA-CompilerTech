package ClassFile.MipsInstr;

public class Space extends Instr {
    public static int DATA = 0;
    public static int TEXT = 1;
    private final int type;

    public Space(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        if (type == 0) {
            return ".data";
        } else {
            return ".text";
        }
    }
}
