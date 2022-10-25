package ClassFile.InterCode;

public class Printf extends ICode {
    private final String name;

    public Printf(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "printf " + name;
    }

    @Override
    public void PrintString() {
        super.PrintString();
        System.out.println(this);
    }
}
