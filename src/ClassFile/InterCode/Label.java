package ClassFile.InterCode;

public class Label extends ICode {
    private final String labelName;

    public Label(String name) {
        this.labelName = name;
    }

    @Override
    public String toString() {
        return "_#" + labelName + "#_:";
    }

    @Override
    public void PrintString() {
        super.PrintString();
        System.out.println(this);
    }
}
