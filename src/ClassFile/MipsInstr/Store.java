package ClassFile.MipsInstr;

public class Store extends Instr {
    private final String TargetReg;
    private final String AddrReg;
    private final String Label;
    private final String Offset;

    public Store(String TargetReg, String AddrReg, String Offset) {
        this.TargetReg = TargetReg;
        this.AddrReg = AddrReg;
        this.Offset = Offset;
        this.Label = null;
    }

    public Store(String TargetReg, String Label) {
        this.TargetReg = TargetReg;
        this.AddrReg = null;
        this.Offset = null;
        this.Label = Label;
    }

    @Override
    public String toString() {
        if (Label == null) {
            return "\tsw " + TargetReg + " " + Offset + "(" + AddrReg + ")";
        }
        return "\tsw " + TargetReg + " " + Label;
    }
}
