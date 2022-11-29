package ClassFile.MipsInstr;

public class Calculate extends Instr {
    private final String operator;
    private final String t1;
    private final String t2;
    private final String t3;

    public Calculate(String operator, String t1, String t2, String t3) {
        this.operator = operator;
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = t3;
    }

    public Calculate(String operator, String t1, String t2) {
        this.operator = operator;
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = null;
    }

    @Override
    public String toString() {
        switch (operator) {
            case "+":
                return "\taddu " + t1 + " " + t2 + " " + t3;
            case "-":
                return "\tsubu " + t1 + " " + t2 + " " + t3;
            case "*":
                return "\tmul " + t1 + " " + t2 + " " + t3;
            case "%":
            case "/":
                return "\tdiv " + t1 + " " + t2;
            case "==":
            case "!":
                return "\tseq " + t1 + " " + t2 + " " + t3;
            case "!=":
                return "\tsne " + t1 + " " + t2 + " " + t3;
            case ">=":
                return "\tsge " + t1 + " " + t2 + " " + t3;
            case "<=":
                return "\tsle " + t1 + " " + t2 + " " + t3;
            case ">":
                return "\tsgt " + t1 + " " + t2 + " " + t3;
            case "<":
                return "\tslt " + t1 + " " + t2 + " " + t3;
        }
        return null;
    }
}
