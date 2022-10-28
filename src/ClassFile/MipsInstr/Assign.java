package ClassFile.MipsInstr;

public class Assign extends Instr {
    private final String source;
    private final String target;
    private final int type;
    public static int MOVE = 0;
    public static int LI = 1;

    public Assign(String source, String target, int type) {
        this.source = source;
        this.target = target;
        this.type = type;
    }

    @Override
    public String toString() {
        if (type == MOVE) {
            return "\tmove " + target + " " + source;
        } else {
            return "\tli " + target + " " + source;
        }
    }
}
