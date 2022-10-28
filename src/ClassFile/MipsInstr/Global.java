package ClassFile.MipsInstr;

public class Global extends Instr {
    public static int INTEGER = 0;
    public static int STRING = 1;
    private final int type;
    private final String name;
    private final String context;

    public Global(int type, String name) {
        this.type = type;
        this.name = name;
        this.context = null;
    }

    public Global(int type, String name, String context) {
        this.type = type;
        this.name = name;
        this.context = context;
    }

    @Override
    public String toString() {
        if (type == 0) {
            return "\t" + name + ": .word 0";
        } else {
            return "\t" + name + ": .asciiz \"" + context + "\"";
        }
    }
}
