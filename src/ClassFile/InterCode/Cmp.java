package ClassFile.InterCode;

import ClassFile.MipsInstr.Branch;

public class Cmp extends ICode {
    private final int type;
    private final String source;
    private final String label;

    public Cmp(String source, String label, int type) {
        this.source = source;
        this.label = label;
        this.type = type;
    }

    @Override
    public String toString() {
        if (type == Branch.BNE) {
            return "if " + source + " goto " + label;
        }
        return "if not " + source + " goto " + label;
    }

    @Override
    public void PrintString() {
        super.PrintString();
        System.out.println(this);
    }

    public int getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String GetLSym() {
        return source;
    }
}
