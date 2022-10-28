package ClassFile.MipsInstr;

public class MipsLabel extends Instr {
    private final String LabelName;

    public MipsLabel(String LabelName) {
        this.LabelName = LabelName;
    }

    @Override
    public String toString() {
        return LabelName;
    }
}
