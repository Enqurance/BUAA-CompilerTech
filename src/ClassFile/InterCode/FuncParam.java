package ClassFile.InterCode;

import ClassFile.VarSymbol;

import java.util.ArrayList;

public class FuncParam extends ICode {
    private final VarSymbol paramSymbol;
    private final String name;
    private String dim2 = null;

    public FuncParam(VarSymbol varSymbol, String name) {
        this.paramSymbol = varSymbol;
        this.name = name;
    }

    @Override
    public String toString() {
        StringBuilder context = new StringBuilder("para int ").append(name);
        if (paramSymbol.getDimension() == 0) {
            return context.toString();
        } else if (paramSymbol.getDimension() == 1) {
            context.append("[]");
        } else if (paramSymbol.getDimension() == 2) {
            /*TODO: Add Exp Later*/
            context.append("[]");
            context.append("[").append(dim2).append("]");
        }
        return context.toString();
    }

    @Override
    public void PrintString() {
        super.PrintString();
        System.out.println(this);
    }

    public String getName() {
        return name;
    }

    public VarSymbol getParamSymbol() {
        return paramSymbol;
    }

    public String getDim2() {
        return dim2;
    }

    public void setDim2(String dim2) {
        this.dim2 = dim2;
    }
}
