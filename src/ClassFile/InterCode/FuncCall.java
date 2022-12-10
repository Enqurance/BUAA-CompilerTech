package ClassFile.InterCode;

import ClassFile.FuncSymbol;
import ClassFile.VarSymbol;

import java.util.ArrayList;

public class FuncCall extends ICode {
    private final String FuncName;
    private final ArrayList<VarSymbol> varSymbols;

    public FuncCall(FuncSymbol funcSymbol) {
        this.FuncName = funcSymbol.getName();
        this.varSymbols = funcSymbol.getVarSymbols();
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

    public String getFuncName() {
        return FuncName;
    }

    public ArrayList<VarSymbol> getVarSymbols() {
        return varSymbols;
    }

    public int GetParamsCount() {
        return varSymbols.size();
    }
}
