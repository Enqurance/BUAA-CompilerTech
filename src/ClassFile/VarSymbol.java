package ClassFile;

import java.util.ArrayList;

public class VarSymbol extends Symbol {
    private final boolean isGlobal;
    private final boolean isConst;
    private final int dimension;
    private int ident;
    private int sizeOne = 0;
    private int sizeTwo = 0;
    private final boolean isRealParam;
    private final boolean hasValue;
    private String value = null;
    private final int line;
    private final ArrayList<Integer> initVal = new ArrayList<>();

    public VarSymbol(Token token, boolean isConst, int dimension,
                     ArrayList<ArrayList<Integer>> initVal, boolean isGlobal, int line) {
        super(token);
        setName(token.context);
        this.isConst = isConst;
        this.dimension = dimension;
        this.isRealParam = false;
        this.hasValue = true;
        this.isGlobal = isGlobal;
        this.line = line;
    }

    public VarSymbol(Token token, boolean isConst, int dimension, boolean isGlobal, int line) {
        super(token);
        setName(token.context);
        this.isConst = isConst;
        this.dimension = dimension;
        this.isRealParam = true;
        this.hasValue = false;
        this.isGlobal = isGlobal;
        this.line = line;
    }

    public boolean isConst() {
        return isConst;
    }

    public int getDimension() {
        return dimension;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void SearchLastSymbol(SymbolTable table) {
        if (table == null) {
            return;
        }
        Symbol symbol = table.findName(getName());
        if (symbol instanceof VarSymbol) {
            ident = ((VarSymbol) symbol).getIdent() + 1;
        } else {
            SearchLastSymbol(table.getParent());
        }
    }

    public int getIdent() {
        return ident;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    @Override
    public int getLine() {
        return line;
    }

    public void SetSize(int size) {
        if (sizeOne == 0) {
            sizeOne = size;
            InitializeArray(sizeOne, 1);
        } else if (sizeTwo == 0) {
            sizeTwo = size;
            InitializeArray(sizeOne, sizeTwo);
        }
    }

    public void InitializeArray(int size1, int size2) {
        int MaxSize = size1 * size2;
        if (size1 * size2 > initVal.size()) {
            while (initVal.size() < MaxSize) {
                initVal.add(0);
            }
        }
    }

    public void SetArrayValue(int index, int value) {
        initVal.set(index, value);
    }

    public ArrayList<Integer> getInitVal() {
        return initVal;
    }

    public int getSizeOne() {
        return sizeOne;
    }

    public int getSizeTwo() {
        return sizeTwo;
    }
}
