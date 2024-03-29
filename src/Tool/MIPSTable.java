package Tool;

import ClassFile.AddrSym;

import java.util.HashMap;

public class MIPSTable {
    private final MIPSTable parent;
    private final HashMap<String, AddrSym> AddrSymbols = new HashMap<>();
    private final HashMap<String, AddrSym> ConstAddrSymbols = new HashMap<>();
    private MIPSTable children;

    public MIPSTable(MIPSTable table) {
        this.parent = table;
    }

    public MIPSTable getParent() {
        return parent;
    }

    public MIPSTable getChildren() {
        return children;
    }

    public void setChildren(MIPSTable children) {
        this.children = children;
    }

    public void PutSymbol(AddrSym sym) {
        AddrSymbols.put(sym.getName(), sym);
    }

    public void PutConstSymbol(AddrSym sym) {
        ConstAddrSymbols.put(sym.getName(), sym);
        AddrSymbols.put(sym.getName(), sym);
    }

    public boolean FindSymbol(String sym) {
        return AddrSymbols.containsKey(sym);
    }

    public boolean FindConstSymbol(String sym) {
        return ConstAddrSymbols.containsKey(sym);
    }

    public void RemoveSymbol(AddrSym sym) {
        AddrSymbols.remove(sym.getName());
    }

    public AddrSym FindAddrSym(String name) {
        if (AddrSymbols.containsKey(name)) {
            return AddrSymbols.get(name);
        } else {
            if (parent == null) {
                return null;
            }
            return parent.FindAddrSym(name);
        }
    }

    public HashMap<String, AddrSym> getAddrSymbols() {
        return AddrSymbols;
    }

    public void PrintAllAddrSymbols() {
        for (String name : AddrSymbols.keySet()) {
            System.out.println(name);
        }
    }
}
