package ClassFile;


import java.util.ArrayList;

public class FuncSymbol extends Symbol {
    private final ArrayList<VarSymbol> varSymbols = new ArrayList<>();
    private final String type;

    public FuncSymbol(Token ident, String type) {
        super(ident);
        setName(ident.context);
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public ArrayList<VarSymbol> getVarSymbols() {
        return varSymbols;
    }

    public int checkFuncParams(ArrayList<ArrayList<Symbol>> params) {
        if (params.size() != varSymbols.size()) {       // Size no match
            return 1;
        }
        int len = varSymbols.size();
        for (int i = 0; i < len; i++) {
            for (Symbol symbol : params.get(i)) {
                if (varSymbols.get(i).getDimension() == 0) {        // Type no match
                    if (((symbol instanceof FuncSymbol) && ((FuncSymbol) (symbol)).getType().equals("void")) ||
                            ((symbol instanceof VarSymbol) && ((VarSymbol) (symbol)).getDimension() != 0)) {
                        return 2;
                    }
                } else if (varSymbols.get(i).getDimension() == 1) {
                    if (!((symbol instanceof VarSymbol) && ((VarSymbol) (symbol)).getDimension() == 1)) {
                        return 2;
                    }
                } else if (varSymbols.get(i).getDimension() == 2) {
                    if (!((symbol instanceof VarSymbol) && ((VarSymbol) (symbol)).getDimension() == 2)) {
                        return 2;
                    }
                }
            }
        }
        return 0;
    }
}
