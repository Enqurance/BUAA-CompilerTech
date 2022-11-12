package ClassFile.InterCode;

public class ArrayLoad extends ICode {
    private final String name;
    private final String target;
    private final String index1;
    private final String index2;

    public ArrayLoad(String name, String target, String index) {
        this.name = name;
        this.target = target;
        this.index1 = index;
        this.index2 = null;
    }

    public ArrayLoad(String name, String target, String index1, String index2) {
        this.name = name;
        this.target = target;
        this.index1 = index1;
        this.index2 = index2;
    }

    @Override
    public String toString() {
        if (index2 == null) {
            return "load " + target + " " + name + "[" + index1 + "]";
        } else {
            return "load " + target + " " + name + "[" + index1 + "]" + "[" + index2 + "]";
        }
    }

    @Override
    public void PrintString() {
        super.PrintString();
        System.out.println(this);
    }
}
