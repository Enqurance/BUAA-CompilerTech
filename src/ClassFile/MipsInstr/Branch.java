package ClassFile.MipsInstr;

public class Branch extends Instr {
    public static int BEQ = 0;
    public static int BNE = 1;
    private final int type;
    private final String t1;
    private final String t2;
    private final String label;

    public Branch(String t1, String t2, String label, int type) {
        this.type = type;
        this.t1 = t1;
        this.t2 = t2;
        this.label = label;
    }

    @Override
    public String toString() {
        if (type == BEQ) {
            return "\tbeq " + t1 + " " + t2 + " " + label;
        } else {
            return "\tbne " + t1 + " " + t2 + " " + label;
        }
    }
}
