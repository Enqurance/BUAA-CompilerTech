package ClassFile.InterCode;

import java.util.ArrayList;

public class FuncDecl extends ICode {
    private final String funcType;
    private final String funcName;
    private final ArrayList<FuncParam> funcParams = new ArrayList<>();

    public FuncDecl(String funcType, String funcName) {
        this.funcName = funcName;
        this.funcType = funcType;
    }

    @Override
    public String toString() {
        return funcType + " " + funcName;
    }

    @Override
    public void PrintString() {
        super.PrintString();
        System.out.println(this);
    }

    public ArrayList<FuncParam> getFuncParams() {
        return funcParams;
    }

    public String getFuncType() {
        return funcType;
    }
}
