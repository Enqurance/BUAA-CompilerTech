package ClassFile.InterCode;

public class Label extends ICode {
    private final String labelName;
    private final boolean isCondLabel;

    public Label(String name, boolean isCondLabel) {
        this.labelName = name;
        this.isCondLabel = isCondLabel;
    }


    @Override
    public String toString() {
        if (isCondLabel) {
            return labelName + ":";
        }
        return "$$" + labelName + "$$:";
    }

    @Override
    public void PrintString() {
        super.PrintString();
        System.out.println(this);
    }
}
