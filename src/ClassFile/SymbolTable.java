package ClassFile;

import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {
    private final boolean isGlobal;
    private final boolean isWhile;
    private SymbolTable parent;
    private final ArrayList<SymbolTable> children = new ArrayList<>();
    private final HashMap<String, FuncSymbol> funcSymbols = new HashMap<>();
    private final HashMap<String, VarSymbol> varSymbols = new HashMap<>();

    public SymbolTable(boolean isGlobal, boolean isWhile) {
        this.isGlobal = isGlobal;
        this.isWhile = isWhile;
    }

    public boolean addSymbol(Symbol symbol) {
        if (isGlobal) {
            if (varSymbols.containsKey(symbol.getName()) || funcSymbols.containsKey(symbol.getName())) {
                error(symbol);
                return false;
            }
        }
        if (symbol instanceof VarSymbol) {
            if (varSymbols.containsKey(symbol.getName())) {
                error(symbol);
            } else {
                varSymbols.put(symbol.getName(), (VarSymbol) symbol);
            }
        } else if (symbol instanceof FuncSymbol) {
            if (funcSymbols.containsKey(symbol.getName())) {
                error(symbol);
            } else {
                funcSymbols.put(symbol.getName(), (FuncSymbol) symbol);
            }
        }
        return true;
    }

    public Symbol findName(String name) {
        if (varSymbols.containsKey(name)) {
            return varSymbols.get(name);
        } else if (funcSymbols.containsKey(name)) {
            return funcSymbols.get(name);
        }
        return null;
    }

    public int matchFunc(String name, ArrayList<ArrayList<Symbol>> tables) {
        if (!funcSymbols.containsKey(name)) {
            return 3;
        }
        FuncSymbol symbol = funcSymbols.get(name);
        return symbol.checkFuncParams(tables);
    }

    public void error(Symbol symbol) {
        Error.addErrorMessage(symbol.getLine(), "b");
    }

    public void addChild(SymbolTable symbolTable) {
        children.add(symbolTable);
        symbolTable.addParent(this);
    }

    public void addParent(SymbolTable symbolTable) {
        this.parent = symbolTable;
    }

    public SymbolTable getParent() {
        return parent;
    }

    public boolean isWhile() {
        return isWhile;
    }

    public void printSymbols() {
        System.out.println("Table");
        for (String str : funcSymbols.keySet()) {
            System.out.println("FuncSym " + str + " type is " + funcSymbols.get(str).getType());
        }
        for (String str : varSymbols.keySet()) {
            System.out.println("VarSym " + str + " dimension is " + varSymbols.get(str).getDimension());
        }
        for (SymbolTable table : children) {
            table.printSymbols();
        }
    }
}
