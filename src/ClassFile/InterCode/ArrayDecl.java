package ClassFile.InterCode;

import ClassFile.VarSymbol;

import java.util.ArrayList;

public class ArrayDecl extends ICode {
    private final String name;
    private final VarSymbol varSymbol;
    private String sym1;
    private String sym2;
    private final ArrayList<ArrayLoad> load = new ArrayList<>();


    public ArrayDecl(VarSymbol symbol, String name) {
        this.name = name;
        this.varSymbol = symbol;
    }

    @Override
    public String toString() {
        if (varSymbol.getDimension() == 1) {
            return "arr int " + name + "[" + sym1 + "]";
        } else {
            return "arr int " + name + "[" + sym1 + "]" + "[" + sym2 + "]";
        }
    }

    @Override
    public void PrintString() {
        super.PrintString();
        System.out.println(this);
    }

    public void setSym1(String sym1) {
        this.sym1 = sym1;
    }

    public void setSym2(String sym2) {
        this.sym2 = sym2;
    }
}
