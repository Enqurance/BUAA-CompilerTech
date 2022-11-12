package ClassFile.InterCode;

public class Jump extends ICode {
    private final String target;

    public Jump(String target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return "goto " + target;
    }
}
