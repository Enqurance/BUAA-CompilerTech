package ClassFile.MipsInstr;

public class Load extends Instr {
    public static int LW = 0;
    public static int LA = 1;
    private final String TargetReg;
    private final String AddrReg;
    private final int type;
    private final String Label;
    private final String Offset;

    public Load(String TargetReg, String AddrReg, String Offset) {
        this.TargetReg = TargetReg;
        this.AddrReg = AddrReg;
        this.type = LW;
        this.Offset = Offset;
        this.Label = null;
    }

    public Load(String TargetReg, String Label, int type) {
        this.TargetReg = TargetReg;
        this.AddrReg = null;
        this.type = type;
        this.Offset = null;
        this.Label = Label;
    }

    public Load(String TargetReg, String Label, String addrReg, int type) {
        this.TargetReg = TargetReg;
        this.AddrReg = addrReg;
        this.type = type;
        this.Offset = null;
        this.Label = Label;
    }

    @Override
    public String toString() {
        if (type == LW) {
            if (Label == null) {
                return "\tlw " + TargetReg + " " + Offset + "(" + AddrReg + ")";
            } else if (AddrReg == null) {
                return "\tlw " + TargetReg + " " + Label;
            } else {
                return "\tlw " + TargetReg + " " + Label + "(" + AddrReg + ")";
            }
        } else {
            return "\tla " + TargetReg + " " + Label;
        }
    }
}
