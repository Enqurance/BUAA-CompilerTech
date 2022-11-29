package ClassFile.InterCode;

public class ArrayStore extends ICode {
    private final String name;
    private final String source;
    private final String index1;
    private final String index2;
    private final String lSym;
    private final int dim;

    public ArrayStore(String name, String source, String index) {
        this.name = name;
        this.source = source;
        this.index1 = index;
        this.index2 = "0";
        this.lSym = name + "[" + index1 + "]";
        this.dim = 1;
    }

    public ArrayStore(String name, String source, String index1, String index2) {
        this.name = name;
        this.source = source;
        this.index1 = index1;
        this.index2 = index2;
        this.lSym = name + "[" + index1 + "]" + "[" + index2 + "]";
        this.dim = 2;
    }

    @Override
    public String toString() {
        return "store " + lSym + " " + source;
    }

    @Override
    public String GetLSym() {
        return lSym;
    }

//    public int GetIndexOffset() {
//        return -(Integer.parseInt(index1) * 4);
//    }


//    public int GetIndexOffset(int dim) {
//        return -((dim * Integer.parseInt(index1) + Integer.parseInt(index2)) * 4);
//    }

    public int getDim() {
        return dim;
    }

    public String getSource() {
        return source;
    }

    public String getIndex1() {
        return index1;
    }

    public String getIndex2() {
        return index2;
    }
}
