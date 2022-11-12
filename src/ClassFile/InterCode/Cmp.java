package ClassFile.InterCode;

public class Cmp extends ICode {
    private final boolean target;
    private final String source;
    private final String label;

    public Cmp(String source, String label, boolean target) {
        this.source = source;
        this.label = label;
        this.target = target;
    }

    @Override
    public String toString() {
        if (target) {
            return "if " + source + " goto " + label;
        }
        return "if not " + source + " goto " + label;
    }

    @Override
    public void PrintString() {
        super.PrintString();
        System.out.println(this);
    }
}
