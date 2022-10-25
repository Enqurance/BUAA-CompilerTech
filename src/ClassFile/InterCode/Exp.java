package ClassFile.InterCode;

public class Exp extends ICode {
    private final String lSym;
    private final String rSym_1;
    private final String operator;
    private final String rSym_2;

    public Exp(String operator, String lSym, String rSym_1, String rSym_2) {
        this.operator = operator;
        this.lSym = lSym;
        this.rSym_1 = rSym_1;
        this.rSym_2 = rSym_2;
    }

    public Exp(String operator, String lSym, String rSym_1) {
        this.operator = operator;
        this.lSym = lSym;
        this.rSym_1 = "";
        this.rSym_2 = rSym_1;
    }

    public Exp(String lSym, String rSym_1) {
        this.operator = "";
        this.lSym = lSym;
        this.rSym_1 = "";
        this.rSym_2 = rSym_1;
    }

    @Override
    public String toString() {
        if (operator.equals("")) {
            return lSym + " = " + rSym_2;
        }
        if (rSym_1.equals("")) {
            return lSym + " = " + operator + " " + rSym_2;
        }
        return lSym + " = " + rSym_1 + " " + operator + " " + rSym_2;
    }

    @Override
    public void PrintString() {
        super.PrintString();
        System.out.println(this);
    }
}
