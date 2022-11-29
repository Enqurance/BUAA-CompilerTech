package ClassFile.MipsInstr;

import java.util.ArrayList;

public class Global extends Instr {
    public static int INTEGER = 0;
    public static int STRING = 1;
    public static int ArrayOne = 2;
    public static int ArrayTwo = 3;
    private final int type;
    private final String name;
    private final String context;
    private final ArrayList<Integer> values = new ArrayList<>();

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

    public Global(int type, String name, ArrayList<Integer> values) {
        this.type = type;
        this.name = name;
        this.context = null;
        this.values.addAll(values);
    }

    @Override
    public String toString() {
        if (type == 0) {
            /* type 0 is Integer */
            return "\t" + name + ": .word 0";
        } else if (type == 1) {
            /* type 1 is String */
            return "\t" + name + ": .asciiz \"" + context + "\"";
        } else{
            /* type is 2 or 3 is Array */
            StringBuilder str = new StringBuilder("\t" + name + ":");
            for (Integer integer : values) {
                str.append("\n\t.word ").append(integer);
            }
            return str.toString();
        }
    }
}
