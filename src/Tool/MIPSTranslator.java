package Tool;

import ClassFile.AddrSym;
import ClassFile.InterCode.ConstDecl;
import ClassFile.InterCode.Exp;
import ClassFile.InterCode.FuncCall;
import ClassFile.InterCode.FuncParam;
import ClassFile.InterCode.FuncPush;
import ClassFile.InterCode.Get;
import ClassFile.InterCode.ICode;
import ClassFile.InterCode.Label;
import ClassFile.InterCode.Printf;
import ClassFile.InterCode.VarDecl;
import ClassFile.MipsInstr.Assign;
import ClassFile.MipsInstr.Calculate;
import ClassFile.MipsInstr.Global;
import ClassFile.MipsInstr.Instr;
import ClassFile.MipsInstr.JumpTo;
import ClassFile.MipsInstr.Load;
import ClassFile.MipsInstr.MDUnit;
import ClassFile.MipsInstr.MipsLabel;
import ClassFile.MipsInstr.Space;
import ClassFile.MipsInstr.Store;
import ClassFile.MipsInstr.Syscall;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class MIPSTranslator {
    private final ICodeStorage storage;
    private final ArrayList<ICode> InterCodes;
    private final ArrayList<ICode> GlobalCodes;
    private final ArrayList<ICode> InitCodes;
    private final HashMap<String, ArrayList<ICode>> FuncLabels;
    private final ArrayList<Instr> MIPSCodes = new ArrayList<>();
    private final Stack<Integer> SPRecord = new Stack<>();
    private int SPOffset = 0;
    private final RegDistributor regDistributor = new RegDistributor();
    private final MIPSTable headTable = new MIPSTable(null);
    private MIPSTable curTable = headTable;
    private final HashMap<String, String> TempReg = new HashMap<>();
    /* 对于某一个临时变量，可能出现这么一种情况：它还没有消亡，就要进入另外一个函数
     * 这样在新的函数中，根据现有的寄存器分配原则，它的值可能会被覆盖。所以设计一个
     * HashMap，用于保存未消亡的临时变量和它的偏移。由于临时变量名称唯一，仅保存
     * 偏移即可。在Fetch的时候加入，Use的时候取出 */
    private final HashSet<String> UnusedTempVar = new HashSet<>();

    public MIPSTranslator(ICodeStorage storage) {
        this.storage = storage;
        this.InterCodes = storage.getInterCodes();
        this.InitCodes = storage.getInitCodes();
        this.GlobalCodes = storage.getGlobalCodes();
        this.FuncLabels = storage.getFuncLabels();
        curTable.setStackTop(SPOffset);
        SPRecord.add(SPOffset);
    }

    public void Translate() {
        TranslateGlobalCodes();
        TranslateInitCodes();
        TranslateMainFuncCode();
        TranslateFuncCode();
    }

    public void TranslateGlobalCodes() {
        MIPSCodes.add(new Space(Space.DATA));
        for (ICode code : GlobalCodes) {
            if (code instanceof ConstDecl && code.GetLSym().contains("$str$")) {
                MIPSCodes.add(new Global(Global.STRING, code.GetLSym(), ((ConstDecl) (code)).GetRSym()));
                curTable.PutSymbol(new AddrSym(code.GetLSym()));
            } else {
                MIPSCodes.add(new Global(Global.INTEGER, code.GetLSym()));
                curTable.PutSymbol(new AddrSym(code.GetLSym()));
            }
        }
    }

    public void TranslateInitCodes() {
        MIPSCodes.add(new Space(Space.TEXT));
        for (ICode code : InitCodes) {
            if (code instanceof Exp) {
                TranslateExp(code);
            }
        }
    }

    public void TranslateMainFuncCode() {
        /* 进入函数，则需要新建符号表 */
        AddTable();
        ArrayList<ICode> MainCodes = FuncLabels.get("$$main$$:");
        MIPSCodes.add(new MipsLabel(MainCodes.get(0).toString()));
        for (ICode code : MainCodes) {
            if (code instanceof VarDecl) {
                TranslateVarDecl(code);
            } else if (code instanceof Exp) {
                TranslateExp(code);
            } else if (code instanceof Printf) {
                TranslatePrintf(code);
            } else if (code instanceof Get) {
                TranslateGet(code);
            } else if (code instanceof FuncCall) {
                TranslateCall(code);
            } else if (code instanceof FuncPush) {
                TranslatePush(code);
            }
        }
        AddAssign("10", RegDistributor.V0Reg, Assign.LI);
        AddSyscall();
        BackTable();
    }

    public void TranslateFuncCode() {
        ArrayList<ArrayList<ICode>> codes = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        for (ICode code : InterCodes) {
            if (!code.toString().equals("$$main$$:") && FuncLabels.containsKey(code.toString())) {
                labels.add(code.toString());
                codes.add(FuncLabels.get(code.toString()));
            }
        }
        for (ArrayList<ICode> childCode : codes) {
            /* 进入函数，新建一个符号表 */
            AddTable();
            TranslateSingleFunc(childCode);
            BackTable();
        }
    }

    public void TranslateSingleFunc(ArrayList<ICode> codes) {
        /* 首先将形参找出，并登记符号表，不需要翻译出Mips语句。形参的值来自之前已经保存到栈中的实参的值 */
        ArrayList<FuncParam> funcParams = new ArrayList<>();
        for (ICode code : codes) {
            if (code instanceof FuncParam) {
                funcParams.add((FuncParam) code);
            }
        }
        TranslateFunParams(funcParams);
        for (ICode code : codes) {
            if (code instanceof Label) {
                AddMipsLabel(code.toString());
            } else if (code instanceof VarDecl) {
                TranslateVarDecl(code);
            } else if (code instanceof Exp) {
                TranslateExp(code);
            } else if (code instanceof Printf) {
                TranslatePrintf(code);
            } else if (code instanceof Get) {
                TranslateGet(code);
            } else if (code instanceof FuncPush) {
                TranslatePush(code);
            }
        }
        AddJumpTo(RegDistributor.RAReg, JumpTo.JR);
    }

    public void TranslateVarDecl(ICode code) {
        /* 遇到变量声明，填写符号表 */
        curTable.PutSymbol(new AddrSym(code.GetLSym(), Integer.toString(SPOffset)));
        StackGrow();
        curTable.setStackTop(SPOffset);
    }

    public void TranslateExp(ICode code) {
        Exp exp = (Exp) code;
        String lSym = exp.getLSym(), rSym1 = exp.getRSym_1(), rSym2 = exp.getRSym_2();
        if (!isEmpty(rSym1) && !isEmpty(rSym2)) {
            /* 形如 a = b + c的表达式 */
            UseSym(lSym, rSym1, rSym2, exp);
        } else if (!isEmpty(rSym2)) {
            /* 形如a = b的表达式 */
            String reg = FetchSym(rSym2);
            UseSym(lSym, reg, exp);
        }
    }

    public void TranslateAssign(String source, String target) {
        if (isReg(source)) {
            MIPSCodes.add(new Assign(source, target, Assign.MOVE));
        } else {
            MIPSCodes.add(new Assign(source, target, Assign.LI));
        }
    }

    public void TranslatePrintf(ICode code) {
        Printf printf = (Printf) code;
        String target = printf.getName();
        if (isTemp(target)) {
            String reg = FetchSym(target);
            AddAssign(reg, RegDistributor.A0Reg, Assign.MOVE);
            AddAssign(Integer.toString(Syscall.PRINT_INTEGER), RegDistributor.V0Reg, Assign.LI);
            AddSyscall();
        } else if (isVar(target)) {
            if (isGlobalStr(target)) {
                AddLoad(RegDistributor.A0Reg, target, Load.LA);
                AddAssign(Integer.toString(Syscall.PRINT_STRING), RegDistributor.V0Reg, Assign.LI);
                AddSyscall();
            } else {
                if (isGlobalVar(target)) {
                    AddLoad(RegDistributor.A0Reg, target, Load.LW);
                } else {
                    AddrSym addrSym = curTable.FindAddrSym(target);
                    AddLoad(RegDistributor.A0Reg, RegDistributor.SPReg, addrSym.getOffset());
                }
                AddAssign(Integer.toString(Syscall.PRINT_INTEGER), RegDistributor.V0Reg, Assign.LI);
                AddSyscall();
            }
        } else if (isDigit(target)) {
            AddAssign(target, RegDistributor.A0Reg, Assign.LI);
            AddAssign(Integer.toString(Syscall.PRINT_INTEGER), RegDistributor.V0Reg, Assign.LI);
            AddSyscall();
        }
    }

    public void TranslateGet(ICode code) {
        Get get = (Get) code;
        String name = get.getName();
        AddrSym addrSym = curTable.FindAddrSym(name);
        AddAssign(Integer.toString(Syscall.READ_INTEGER), RegDistributor.V0Reg, Assign.LI);
        AddSyscall();
        AddStore(RegDistributor.V0Reg, RegDistributor.SPReg, addrSym.getOffset());
    }

    public void TranslateCall(ICode code) {
        FuncCall funcCall = (FuncCall) code;
        String FuncLabel = "$$" + funcCall.getFuncName() + "$$";
        AddCalculate("-", RegDistributor.SPReg, RegDistributor.SPReg, Integer.toString(Math.abs(SPOffset)));
        AddJumpTo(FuncLabel, JumpTo.JAL);
        AddCalculate("+", RegDistributor.SPReg, RegDistributor.SPReg, Integer.toString(Math.abs(SPOffset)));
        PutTempVarAddrSymbol();
    }

    public void TranslateFunParams(ArrayList<FuncParam> funcParams) {
        int size = funcParams.size();
        for (FuncParam param : funcParams) {
            AddrSym newSym = new AddrSym(param.getName(), Integer.toString(size * 4));
            curTable.PutSymbol(newSym);
            size--;
        }
//        PrintAllAddrSym();
    }

    public void TranslatePush(ICode code) {
        FuncPush push = (FuncPush) code;
        String target = FetchSym(push.getTarget());
        PutTempVarAddrSymbol();
        AddStore(target, RegDistributor.SPReg, Integer.toString(SPOffset));
        StackGrow();
    }

    public String DistributeReg(String temp) {
        String tempReg = regDistributor.TempRegDistribute();
        TempReg.put(temp, tempReg);
        return tempReg;
    }

    public String FetchSym(String sym) {    /* 取用一个Sym */
        if (isDigit(sym)) {     /* 数字，直接分发一个寄存器，并存入数字，更新记录 */
            String reg = DistributeReg(sym);
            TranslateAssign(sym, reg);
            return reg;
        } else if (isVar(sym)) {
            if (isGlobalVar(sym)) {     /* 全局变量，分配一个寄存器并使用lw Label */
                String reg = DistributeReg(sym);
                AddLoad(reg, sym, Load.LW);
                return reg;
            } else {    /* 局部变量，分配一个寄存器并使用lw reg offset($sp) */
                AddrSym addrSym = curTable.FindAddrSym(sym);
                String reg = DistributeReg(sym);
                AddLoad(reg, RegDistributor.SPReg, addrSym.getOffset());
                return reg;
            }
        } else if (isTemp(sym)) {   /* 临时变量，如果该临时变量已经被分配，则返回其对应寄存器；否则，为其分配一个寄存器 */
            RemoveTempVar(sym);
            if (curTable.FindSymbol(sym)) {
                AddrSym addrSym = curTable.FindAddrSym(sym);
                String reg = DistributeReg(sym);
                AddLoad(reg, RegDistributor.SPReg, addrSym.getOffset());
                return reg;
            }
            if (TempReg.containsKey(sym)) {
                return TempReg.get(sym);
            }
            return DistributeReg(sym);
        } else if (isFuncRet(sym)) {
            return RegDistributor.V1Reg;
        }
        return null;
    }

    public void UseSym(String lSym, String rSym1, String rSym2, Exp exp) {
        if (isVar(lSym)) {
            if (isGlobalVar(lSym)) {
                /* 左侧使用全局变量，应使用sw Label */
                String reg1 = FetchSym(rSym1);
                String reg2 = FetchSym(rSym2);
                String reg3 = DistributeReg(lSym);
                AddCalculate(exp.getOperator(), reg3, reg1, reg2);
                AddStore(reg3, lSym);
            } else {
                /* 左侧使用局部变量，应当使用sw reg offset($sp) */
                AddrSym addrSym = curTable.FindAddrSym(lSym);
                String reg1 = FetchSym(rSym1);
                String reg2 = FetchSym(rSym2);
                String reg3 = DistributeReg(lSym);
                AddCalculate(exp.getOperator(), reg3, reg1, reg2);
                AddStore(reg3, RegDistributor.SPReg, addrSym.getOffset());
            }
        } else if (isTemp(lSym)) {
            /* 左侧使用临时变量，分配寄存器并保存值，更新寄存器记录 */
            /* 右边使用完的变量可以释放 */
            String reg = DistributeReg(lSym);
            String reg1 = FetchSym(rSym1);
            String reg2 = FetchSym(rSym2);
            TempReg.remove(rSym1);
            TempReg.remove(rSym2);
            MarkTempVar(lSym);
            AddCalculate(exp.getOperator(), reg, reg1, reg2);
        }
    }

    public void UseSym(String lSym, String rSym1, Exp exp) {
        String operator = exp.getOperator();
        if (operator.equals("-")) {
            /* UnaryOp为负，需要手动转换 */
            AddCalculate(operator, rSym1, RegDistributor.ZEROReg, rSym1);
        }
        if (isVar(lSym)) {
            if (isGlobalVar(lSym)) {
                /* 左侧使用全局变量，应使用sw Label */
                AddStore(rSym1, lSym);
            } else {
                /* 左侧使用局部变量，应当使用sw reg offset($sp) */
                AddrSym addrSym = curTable.FindAddrSym(lSym);
                AddStore(rSym1, RegDistributor.SPReg, addrSym.getOffset());
            }
        } else if (isTemp(lSym)) {
            /* 左侧使用临时变量，分配寄存器并保存值，更新寄存器记录 */
            String reg = DistributeReg(lSym);
            AddAssign(rSym1, reg, Assign.MOVE);
            TempReg.put(rSym1, lSym);
            MarkTempVar(lSym);
        } else if (isFuncRet(lSym)) {
            /* 左侧是返回值，向V1中保存右侧值 */
            AddAssign(rSym1, RegDistributor.V1Reg, Assign.MOVE);
        }
    }

    public void AddLoad(String TargetReg, String AddrReg, String Offset) {
        MIPSCodes.add(new Load(TargetReg, AddrReg, Offset));
    }

    public void AddStore(String TargetReg, String AddrReg, String Offset) {
        MIPSCodes.add(new Store(TargetReg, AddrReg, Offset));
    }

    public void AddLoad(String TargetReg, String Label, int type) {
        MIPSCodes.add(new Load(TargetReg, Label, type));
    }

    public void AddStore(String TargetReg, String Label) {
        MIPSCodes.add(new Store(TargetReg, Label));
    }

    public void AddAssign(String source, String target, int type) {
        MIPSCodes.add(new Assign(source, target, type));
    }

    public void AddCalculate(String operator, String t1, String t2, String t3) {
        switch (operator) {
            case "+":
            case "-":
            case "*":
                MIPSCodes.add(new Calculate(operator, t1, t2, t3));
                break;
            case "%":
                MIPSCodes.add(new Calculate(operator, t2, t3));
                MIPSCodes.add(new MDUnit(MDUnit.MFHI, t1));
                break;
            case "/":
                MIPSCodes.add(new Calculate(operator, t2, t3));
                MIPSCodes.add(new MDUnit(MDUnit.MFLO, t1));
                break;
        }
    }

    public void AddMipsLabel(String label) {
        MIPSCodes.add(new MipsLabel(label));
    }

    public void AddSyscall() {
        MIPSCodes.add(new Syscall());
    }

    public void AddJumpTo(String target, int type) {
        MIPSCodes.add(new JumpTo(type, target));
    }

    public void StackGrow() {
        SPOffset -= 4;
    }

    public void StackBack() {
        SPOffset += 4;
    }

    public boolean isEmpty(String string) {
        return string.isEmpty();
    }

    public boolean isTemp(String string) {
        return string.contains("#t");
    }

    public boolean isVar(String string) {
        return string.contains("$");
    }

    public boolean isReg(String string) {
        return string.contains("$");
    }

    public boolean isFuncRet(String string) {
        return string.equals("RET");
    }

    public boolean isGlobalStr(String string) {
        return string.contains("$str$");
    }

    public boolean isGlobalVar(String sym) {
        return headTable.FindSymbol(sym);
    }

    public boolean isDigit(String string) {
        for (int i = string.length() - 1; i >= 0; i--) {
            if (!Character.isDigit(string.charAt(i))) {
                if (i == 0 && string.charAt(i) == '-' && string.length() != 1) {
                    continue;
                }
                return false;
            }
        }
        return true;
    }

    public void MarkTempVar(String temp) {
        UnusedTempVar.add(temp);
        StackGrow();
    }

    public void RemoveTempVar(String temp) {
        UnusedTempVar.remove(temp);
        StackBack();
    }

    public void PutTempVarAddrSymbol() {
        for (String tempVar : UnusedTempVar) {
            curTable.PutSymbol(new AddrSym(tempVar, Integer.toString(SPOffset)));
            AddStore(TempReg.get(tempVar), RegDistributor.SPReg, Integer.toString(SPOffset));
            StackGrow();
        }
        UnusedTempVar.clear();
    }

    public void AddTable() {
        SPRecord.add(SPOffset);
        SPOffset = 0;
        curTable = new MIPSTable(curTable);
    }

    public void BackTable() {
        SPOffset = SPRecord.pop();
        curTable = curTable.getParent();
    }

    public void OutputMipsCode() {
        String path = "mips.txt";
        File file = new File(path);
        try (FileWriter writer = new FileWriter(file)) {
            for (Instr instr : MIPSCodes) {
                writer.write(instr.toString() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void PrintAllAddrSym() {
        System.out.println("This is a MIPSTable...");
        for (AddrSym sym : curTable.getAddrSymbols().values()) {
            System.out.println(sym.getName() + " " + sym.getOffset());
        }
    }

}
