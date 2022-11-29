package ClassFile.InterCode;

public class Get extends ICode {
    private final String name;
    private final String tempVar;
    private final boolean isArrayEle;

    public Get(String Name, String tempVar) {
        this.name = Name;
        this.tempVar = tempVar;
        this.isArrayEle = false;
    }

    public Get(String Name, String tempVar, boolean isArrayEle) {
        this.name = Name;
        this.tempVar = tempVar;
        this.isArrayEle = isArrayEle;
    }


    @Override
    public String toString() {
        if (isArrayEle) {
            return "scanf " + tempVar;
        }
        return "scanf " + tempVar + "\n" + name + " = " + tempVar;
    }

    @Override
    public void PrintString() {
        super.PrintString();
        System.out.println(this);
    }

    public String getName() {
        return name;
    }

    public String getTempVar() {
        return tempVar;
    }

    public boolean isArrayEle() {
        return isArrayEle;
    }
}
