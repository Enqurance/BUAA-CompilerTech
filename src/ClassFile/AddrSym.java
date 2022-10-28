package ClassFile;

public class AddrSym {
    private final String name;
    private String offset;
    private String reg;

    public AddrSym(String name, String offset, String reg) {
        this.name = name;
        this.offset = offset;
        this.reg = reg;
    }

    public AddrSym(String name, String offset) {
        this.name = name;
        this.offset = offset;
        this.reg = null;
    }

    public AddrSym(String name) {
        this.name = name;
        this.offset = null;
        this.reg = null;
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
}
