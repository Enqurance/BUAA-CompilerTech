package ClassFile.InterCode;

public class Label extends ICode {
    private final String labelName;

    public Label(String name) {
        this.labelName = name;
    }

    @Override
    public String toString() {
        return "$$" + labelName + "$$:";
    }

    @Override
    public void PrintString() {
        super.PrintString();
        System.out.println(this);
    }
}
