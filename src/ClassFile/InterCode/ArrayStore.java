package ClassFile.InterCode;

public class ArrayStore extends ICode {
    private final String name;
    private final String source;
    private final String index1;
    private final String index2;

    public ArrayStore(String name, String source, String index) {
        this.name = name;
        this.source = source;
        this.index1 = index;
        this.index2 = null;
    }

    public ArrayStore(String name, String source, String index1, String index2) {
        this.name = name;
        this.source = source;
        this.index1 = index1;
        this.index2 = index2;
    }

    @Override
    public String toString() {
        if (index2 == null) {
            return "store " + name + "[" + index1 + "]" + " " + source;
        } else {
            return "store " + name + "[" + index1 + "]" + "[" + index2 + "]" + " " + source;
        }
    }
}
