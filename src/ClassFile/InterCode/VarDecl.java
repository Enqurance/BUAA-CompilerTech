package ClassFile.InterCode;

import ClassFile.VarSymbol;

public class VarDecl extends ICode {
    private final String name;
    private final VarSymbol varSymbol;
    private final boolean isGlobal;

    public VarDecl(VarSymbol varSymbol, String name, boolean isGlobal) {
        this.name = name;
        this.varSymbol = varSymbol;
        this.isGlobal = isGlobal;
    }

    @Override
    public String toString() {
        return "var int " + name;
    }

    @Override
    public void PrintString() {
        super.PrintString();
        System.out.println(this);
    }

    @Override
    public String GetLSym() {
        return name;
    }

    public VarSymbol getVarSymbol() {
        return varSymbol;
    }

    public String getName() {
        return name;
    }

    public boolean isGlobal() {
        return isGlobal;
    }
}
