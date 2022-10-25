package ClassFile.InterCode;

import ClassFile.VarSymbol;

public class VarDecl extends ICode {
    private final String name;
    private final VarSymbol varSymbol;

    public VarDecl(VarSymbol varSymbol, String name) {
        this.name = name;
        this.varSymbol = varSymbol;
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

    public VarSymbol getVarSymbol() {
        return varSymbol;
    }

    public String getName() {
        return name;
    }
}
