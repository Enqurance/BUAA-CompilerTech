package ClassFile.InterCode;

public class Jump extends ICode {
    private final String target;
    private final int type;

    public Jump(String target, int type) {
        this.target = target;
        this.type = type;
    }

    @Override
    public String toString() {
        return "goto " + target;
    }

    public String getTarget() {
        return target;
    }

    public int getType() {
        return type;
    }
}
