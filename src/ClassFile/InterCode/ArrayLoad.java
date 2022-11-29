package ClassFile.InterCode;

import ClassFile.MipsInstr.Load;

public class ArrayLoad extends ICode {
    private final String name;
    private final String target;
    private final String index1;
    private final String index2;
    private final String lSym;
    private final int dim;
    private boolean LoadAddress;

    public ArrayLoad(String name, String target, boolean la) {
        this.name = name;
        this.target = target;
        this.index1 = "-1";
        this.index2 = "-1";
        this.lSym = name;
        this.dim = 1;
        this.LoadAddress = la;
    }

    public ArrayLoad(String name, String target, String index, boolean la) {
        this.name = name;
        this.target = target;
        this.index1 = index;
        this.index2 = "-1";
        this.lSym = name + "[" + index1 + "]";
        this.dim = 1;
        this.LoadAddress = la;
    }


    public ArrayLoad(String name, String target, String index1, String index2, boolean la) {
        this.name = name;
        this.target = target;
        this.index1 = index1;
        this.index2 = index2;
        this.lSym = name + "[" + index1 + "]" + "[" + index2 + "]";
        this.dim = 2;
        this.LoadAddress = la;
    }

    @Override
    public String toString() {
        if (LoadAddress) {
            return "load address " + target + " " + lSym;
        }
        return "load " + target + " " + lSym;
    }

    @Override
    public void PrintString() {
        super.PrintString();
        System.out.println(this);
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

    public String getTarget() {
        return target;
    }

    public String getIndex1() {
        return index1;
    }

    public String getIndex2() {
        return index2;
    }

    public boolean isLoadAddress() {
        return LoadAddress;
    }

    public void setLoadAddress(boolean loadAddress) {
        LoadAddress = loadAddress;
    }
}
