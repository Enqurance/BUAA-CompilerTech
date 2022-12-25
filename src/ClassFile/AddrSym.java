package ClassFile;

public class AddrSym {
    private final String name;
    private final boolean isGlobal;
    private String offset;
    private String reg;
    private int dim1 = 0;
    private int dim2 = 0;
    private int dimension = 0;
    public static boolean Global = true;
    private int AbsAddr;
    private boolean isParam = false;
    private int times;

    public AddrSym(String name, String offset, String reg, boolean isGlobal) {
        this.name = name;
        this.offset = offset;
        this.reg = reg;
        this.isGlobal = isGlobal;
    }

    public AddrSym(String name, String offset, boolean isGlobal) {
        this.name = name;
        this.offset = offset;
        this.reg = null;
        this.isGlobal = isGlobal;
    }

    public AddrSym(String name, boolean isGlobal) {
        this.name = name;
        this.offset = null;
        this.reg = null;
        this.isGlobal = isGlobal;
    }

    public String getName() {
        return name;
    }

    public String getOffset() {
        return offset;
    }

    public String getReg() {
        return reg;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public void setReg(String reg) {
        this.reg = reg;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setDim1(int dim1) {
        this.dim1 = dim1;
    }

    public void setDim2(int dim2) {
        this.dim2 = dim2;
    }

    public int getDim1() {
        return dim1;
    }

    public int getDim2() {
        return dim2;
    }

    public void setDim(int dim1, int dim2) {
        setDim1(dim1);
        setDim2(dim2);
    }

    public void setAbsAddr(int absAddr) {
        AbsAddr = absAddr;
    }

    public int getAbsAddr() {
        return AbsAddr;
    }

    public boolean isParam() {
        return isParam;
    }

    public void setParam(boolean param) {
        isParam = param;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public int getTimes() {
        return times;
    }

    public void addTimes() {
        this.times++;
    }

    public void clearTimes() {
        this.times = 0;
    }
}
