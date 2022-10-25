package ClassFile.InterCode;

public class FuncPush extends ICode {
    private final String target;

    public FuncPush(String target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return "push " + target;
    }

    @Override
    public void PrintString() {
        super.PrintString();
        System.out.println(this);
    }
}
