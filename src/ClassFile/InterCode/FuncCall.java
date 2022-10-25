package ClassFile.InterCode;

public class FuncCall extends ICode {
    private final String FuncName;

    public FuncCall(String FuncName) {
        this.FuncName = FuncName;
    }

    @Override
    public String toString() {
        return "call " + FuncName;
    }

    @Override
    public void PrintString() {
        super.PrintString();
        System.out.println(this);
    }
}
