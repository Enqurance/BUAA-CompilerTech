package ClassFile.InterCode;

import ClassFile.VarSymbol;

import java.util.ArrayList;

public class ConstDecl extends ICode {
    public static int INT = 1;
    public static int STR = 2;
    private final String leftSym;
    private String rightSym;
    private final int type;
    private final ArrayList<VarSymbol> symbols = new ArrayList<>();

    public ConstDecl(String name, int type) {
        this.leftSym = name;
        this.type = type;
    }

    public ConstDecl(String name, int type, int strCount) {
        this.leftSym = "str$" + strCount;
        this.type = type;
        this.rightSym = name;
    }

    @Override
    public String toString() {
        if (type == 1) {
            return "const int " + leftSym + " = " + rightSym;
        } else {
            return "const str " + leftSym + " = " + rightSym;
        }
    }

    @Override
    public void PrintString() {
        super.PrintString();
        System.out.println(this);
    }

    public void setRightSym(String rightSym) {
        this.rightSym = rightSym;
    }

    public ArrayList<VarSymbol> getSymbols() {
        return symbols;
    }

    public String getLeftSym() {
        return leftSym;
    }
}
