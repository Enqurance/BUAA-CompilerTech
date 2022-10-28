package ClassFile.MipsInstr;

public class MDUnit extends Instr {
    public static int MFHI = 0;
    public static int MFLO = 1;
    private final int type;
    private final String TargetReg;

    public MDUnit(int type, String TargetReg) {
        this.type = type;
        this.TargetReg = TargetReg;
    }

    @Override
    public String toString() {
        if (type == MFHI) {
            return "\tmfhi " + TargetReg;
        } else {
            return "\tmflo " + TargetReg;
        }
    }
}
