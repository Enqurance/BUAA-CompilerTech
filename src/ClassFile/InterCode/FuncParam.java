package ClassFile.InterCode;

import ClassFile.VarSymbol;

import java.util.ArrayList;

public class FuncParam extends ICode {
    private final VarSymbol paramSymbol;
    private final String name;
    private final ArrayList<Exp> exps = new ArrayList<>();

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
        }
        return context.toString();
    }

    @Override
    public void PrintString() {
        super.PrintString();
        System.out.println(this);
    }
}
