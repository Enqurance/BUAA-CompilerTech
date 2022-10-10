package ClassFile;

public class Symbol {
    private final Token token;
    private final int line;
    private String name;

    public Symbol(Token token) {
        this.line = token.line;
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    public String getName() {
        return name;
    }

    public int getLine() {
        return line;
    }

    public void setName(String name) {
        this.name = name;
    }
}
