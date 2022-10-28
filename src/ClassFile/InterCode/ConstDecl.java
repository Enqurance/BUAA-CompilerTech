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
    private final boolean isGlobal;

    public ConstDecl(String name, int type, boolean isGlobal) {
        this.leftSym = name;
        this.type = type;
        this.isGlobal = isGlobal;
    }

    public ConstDecl(String context, int type, int strCount, boolean isGlobal) {
        this.leftSym = "$str$" + strCount;
        this.type = type;
        this.rightSym = context;
        this.isGlobal = isGlobal;
    }

    @Override
    public String toString() {
        if (type == 1) {
            return "const int " + leftSym;
        } else {
            return "const str " + leftSym + " = " + rightSym;
        }
    }

    @Override
    public void PrintString() {
        super.PrintString();
        System.out.println(this);
    }

    @Override
    public String GetLSym() {
        return leftSym;
    }

    public String GetRSym() {
        return rightSym;
    }

    public ArrayList<VarSymbol> getSymbols() {
        return symbols;
    }

    public boolean isGlobal() {
        return isGlobal;
    }
}
