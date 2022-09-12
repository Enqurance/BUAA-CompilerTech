package ClassFile;

public class Token {
    public String context;
    public int value;
    public int line;
    public String type;

    public Token(String context, int line, int typeNote) {
        this.context = context;
        this.line = line;
        setCategoryType(context, typeNote);
    }

    public void setCategoryType(String context, int typeNote) {
        if (typeNote == 1) {
            type = CategoryCode.IDENT;
        } else if (typeNote == 2) {
            type = CategoryCode.TYPEMAP.get(context);
        } else if (typeNote == 3) {
            type = CategoryCode.INTCONST;
        } else if (typeNote == 4) {
            type = CategoryCode.TYPEMAP.get(context);
        } else if (typeNote == 5) {
            type = CategoryCode.FORMATSTRING;
        }
    }
}
