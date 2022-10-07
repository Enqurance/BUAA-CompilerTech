package ClassFile;

import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {
    private SymbolTable parent;
    private ArrayList<SymbolTable> children = new ArrayList<>();
    private HashMap<String, Node> symbols = new HashMap<>();

    public void addSymbol(Node node) {
        if (symbols.containsKey(node.getContext())) {
            error();
        } else {
            symbols.put(node.getContext(), node);
        }
    }

    public void error() {

    }

    public void addChild(SymbolTable symbolTable) {
        children.add(symbolTable);
        symbolTable.addParent(this);
    }

    public void addParent(SymbolTable symbolTable) {
        this.parent = symbolTable;
    }
}
