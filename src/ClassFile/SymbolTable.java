package ClassFile;

import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {
    private final boolean isGlobal;
    private final boolean isWhile;
    private String tableName;
    private SymbolTable parent;
    private final ArrayList<SymbolTable> children = new ArrayList<>();
    private int childrenCount = 0;
    private final HashMap<String, FuncSymbol> funcSymbols = new HashMap<>();
    private final HashMap<String, VarSymbol> varSymbols = new HashMap<>();
    private int WhileCount = 0;

    public SymbolTable(boolean isGlobal, boolean isWhile) {
        this.isGlobal = isGlobal;
        this.isWhile = isWhile;
        this.tableName = null;
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
                varSymbols.put(symbol.getName(), (VarSymbol) symbol);   /* 填符号表 */
                ((VarSymbol) symbol).SearchLastSymbol(this.getParent());    /* 查看该符号是否来自外层，从上一层开始查找 */
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

    public VarSymbol FindVarSymbolByName(String name) {
        return varSymbols.get(name);
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

    public ArrayList<SymbolTable> getChildren() {
        return children;
    }

    public HashMap<String, FuncSymbol> getFuncSymbols() {
        return funcSymbols;
    }

    public SymbolTable getTableByName(String name) {
        for (SymbolTable table : children) {
            if (table.tableName != null && table.tableName.equals(name)) {
                return table;
            }
        }
        return null;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
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

    public SymbolTable GetNextChild() {
        if (childrenCount < children.size()) {
            return children.get(childrenCount++);
        }
        return null;
    }

    public HashMap<String, VarSymbol> getVarSymbols() {
        return varSymbols;
    }

    public int getWhileCount() {
        return WhileCount;
    }


    public void setWhileCount(int whileCount) {
        WhileCount = whileCount;
    }
}
