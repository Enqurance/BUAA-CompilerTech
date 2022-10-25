package ClassFile.InterCode;

public class Get extends ICode {
    private final String name;
    private final String tempVar;

    public Get(String Name, String tempVar) {
        this.name = Name;
        this.tempVar = tempVar;
    }

    @Override
    public String toString() {
        return "scanf " + tempVar + "\n" + name + " = " + tempVar;
    }

    @Override
    public void PrintString() {
        super.PrintString();
        System.out.println(this);
    }
}
