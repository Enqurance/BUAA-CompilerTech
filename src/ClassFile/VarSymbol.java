package ClassFile;

import java.util.ArrayList;

public class VarSymbol extends Symbol {
    private final boolean isConst;
    private final int dimension;
    private final int sizeOne = 0;
    private final int sizeTwo = 1;
    private final boolean isRealParam;
    private final boolean hasValue;
    private int value;
    private final ArrayList<Integer> oneDimArray = new ArrayList<>();
    private final ArrayList<ArrayList<Integer>> twoDimArray = new ArrayList<>();

    public VarSymbol(Token token, boolean isConst, int dimension, ArrayList<ArrayList<Integer>> initVal) {
        super(token);
        setName(token.context);
        this.isConst = isConst;
        this.dimension = dimension;
        this.isRealParam = false;
        this.hasValue = true;
    }

    public VarSymbol(Token token, boolean isConst, int dimension) {
        super(token);
        setName(token.context);
        this.isConst = isConst;
        this.dimension = dimension;
        this.isRealParam = true;
        this.hasValue = false;
    }

    public boolean isConst() {
        return isConst;
    }

    public int getDimension() {
        return dimension;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
