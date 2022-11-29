package Tool;

import ClassFile.AddrSym;
import ClassFile.InterCode.ArrayDecl;
import ClassFile.InterCode.ArrayLoad;
import ClassFile.InterCode.ArrayStore;
import ClassFile.InterCode.Cmp;
import ClassFile.InterCode.ConstDecl;
import ClassFile.InterCode.Exp;
import ClassFile.InterCode.FuncCall;
import ClassFile.InterCode.FuncDecl;
import ClassFile.InterCode.FuncParam;
import ClassFile.InterCode.FuncPush;
import ClassFile.InterCode.Get;
import ClassFile.InterCode.ICode;
import ClassFile.InterCode.Jump;
import ClassFile.InterCode.Label;
import ClassFile.InterCode.Printf;
import ClassFile.InterCode.VarDecl;
import ClassFile.MipsInstr.Assign;
import ClassFile.MipsInstr.Branch;
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
import ClassFile.VarSymbol;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class MIPSTranslator {
    private final ArrayList<ICode> InterCodes;
    private final ArrayList<ICode> GlobalCodes;
    private final ArrayList<ICode> InitCodes;
    private final HashMap<String, ArrayList<ICode>> FuncLabels;
    private final ArrayList<Instr> MIPSCodes = new ArrayList<>();
    private final Stack<Integer> SPRecord = new Stack<>();
    private int SPOffset = 0;
    private int SPAddr = 0x7fffeffc;
    private final RegDistributor regDistributor = new RegDistributor();
    private final MIPSTable headTable = new MIPSTable(null);
    private MIPSTable curTable = headTable;
    private String curFunc;
    private final HashMap<String, String> TempReg = new HashMap<>();
    /* 对于某一个临时变量，可能出现这么一种情况：它还没有消亡，就要进入另外一个函数
     * 这样在新的函数中，根据现有的寄存器分配原则，它的值可能会被覆盖。所以设计一个
     * HashMap，用于保存未消亡的临时变量和它的偏移。由于临时变量名称唯一，仅保存
     * 偏移即可。在Fetch的时候加入，Use的时候取出 */


    public MIPSTranslator(ICodeStorage storage) {
        this.InterCodes = storage.getInterCodes();
        this.InitCodes = storage.getInitCodes();
        this.GlobalCodes = storage.getGlobalCodes();
        this.FuncLabels = storage.getFuncLabels();
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
                curTable.PutSymbol(new AddrSym(code.GetLSym(), AddrSym.Global));
            } else if (code instanceof ConstDecl) {
                MIPSCodes.add(new Global(Global.INTEGER, code.GetLSym()));
                curTable.PutSymbol(new AddrSym(code.GetLSym(), AddrSym.Global));
            } else if (code instanceof VarDecl) {
                MIPSCodes.add(new Global(Global.INTEGER, code.GetLSym()));
                curTable.PutSymbol(new AddrSym(code.GetLSym(), AddrSym.Global));
            } else if (code instanceof ArrayDecl) {
                ArrayDecl decl = (ArrayDecl) code;
                if (decl.getSym1() != null && decl.getSym2() == null) {
                    MIPSCodes.add(new Global(Global.ArrayOne, decl.GetLSym(), decl.getVarSymbol().getInitVal()));
                } else if (decl.getSym1() != null && decl.getSym2() != null) {
                    MIPSCodes.add(new Global(Global.ArrayTwo, decl.GetLSym(), decl.getVarSymbol().getInitVal()));
                }
                AddrSym sym = new AddrSym(code.GetLSym(), AddrSym.Global);
                sym.setDim(decl.getVarSymbol().getSizeOne(), decl.getVarSymbol().getSizeTwo());
                curTable.PutSymbol(sym);
            }
        }
        for (String key : FuncLabels.keySet()) {
            for (ICode code : FuncLabels.get(key)) {
                if (code instanceof ConstDecl) {
                    MIPSCodes.add(new Global(Global.INTEGER, code.GetLSym()));
                }
            }
        }
    }

    public void TranslateInitCodes() {
        MIPSCodes.add(new Space(Space.TEXT));
        for (ICode code : InitCodes) {
            if (code instanceof Exp) {
                TranslateExp(code);
            } else if (code instanceof ArrayLoad) {
                TranslateArrayLoad(code);
            } else if (code instanceof ArrayStore) {
                TranslateArrayStore(code);
            }
        }
    }

    public void TranslateMainFuncCode() {
        /* 进入函数，则需要新建符号表 */
        AddTable();
        ArrayList<ICode> MainCodes = FuncLabels.get("$$main$$:");
        curFunc = "$$main$$:";
        for (ICode code : MainCodes) {
            if (code instanceof VarDecl) {
                TranslateVarDecl(code);
            } else if (code instanceof ConstDecl) {
                TranslateConstDecl(code);
            } else if (code instanceof ArrayDecl) {
                TranslateArrayDecl(code);
            } else if (code instanceof ArrayLoad) {
                TranslateArrayLoad(code);
            } else if (code instanceof ArrayStore) {
                TranslateArrayStore(code);
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
            } else if (code instanceof Label) {
                TranslateLabel(code);
            } else if (code instanceof Jump) {
                TranslateJump(code);
            } else if (code instanceof Cmp) {
                TranslateCmp(code);
            }
        }
        AddAssign("10", RegDistributor.V0Reg, Assign.LI);
        AddSyscall();
        BackTable();
        curFunc = "NotMain";
    }

    public void TranslateFuncCode() {
        ArrayList<ArrayList<ICode>> codes = new ArrayList<>();
        for (ICode code : InterCodes) {
            if (!code.toString().equals("$$main$$:") && FuncLabels.containsKey(code.toString())) {
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
        TranslateLabel(codes.get(0));
        codes.remove(0);
        for (ICode code : codes) {
            if (code instanceof FuncCall) {
                if (curFunc.equals("NotMain")) {
                    if (!curTable.FindSymbol(RegDistributor.RAReg)) {
                        curTable.PutSymbol(new AddrSym(RegDistributor.RAReg, String.valueOf(SPOffset), false));
                        AddStore(RegDistributor.RAReg, RegDistributor.SPReg, String.valueOf(SPOffset));
                        StackGrow();
                    }
                }
            }
        }
        for (ICode code : codes) {
            if (code instanceof Label) {
                TranslateLabel(code);
            } else if (code instanceof VarDecl) {
                TranslateVarDecl(code);
            } else if (code instanceof ConstDecl) {
                TranslateConstDecl(code);
            } else if (code instanceof ArrayDecl) {
                TranslateArrayDecl(code);
            } else if (code instanceof ArrayLoad) {
                TranslateArrayLoad(code);
            } else if (code instanceof ArrayStore) {
                TranslateArrayStore(code);
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
            } else if (code instanceof Jump) {
                TranslateJump(code);
            } else if (code instanceof Cmp) {
                TranslateCmp(code);
            }
        }
        for (ICode code : codes) {
            if (code instanceof FuncDecl && ((FuncDecl) code).getFuncType().equals("void")) {
                /* 到达函数的末尾，需要安排跳转语句 */
                if (!(MIPSCodes.get(MIPSCodes.size() - 1) instanceof JumpTo)) {
                    if (curTable.FindSymbol(RegDistributor.RAReg)) {
                        AddrSym ra = curTable.FindAddrSym(RegDistributor.RAReg);
                        AddLoad(RegDistributor.RAReg, RegDistributor.SPReg, ra.getOffset());
                    }
                    AddJumpTo(RegDistributor.RAReg, JumpTo.JR);
                }
                break;
            }
        }
    }

    public void TranslateConstDecl(ICode code) {
        ConstDecl constDecl = (ConstDecl) code;
        curTable.PutConstSymbol(new AddrSym(constDecl.GetLSym(), AddrSym.Global));
    }

    public void TranslateVarDecl(ICode code) {
        /* 遇到变量声明，填写符号表 */
        VarDecl decl = (VarDecl) code;
        curTable.PutSymbol(new AddrSym(decl.GetLSym(), Integer.toString(SPOffset), decl.isGlobal()));
        StackGrow();
    }

    public void TranslateExp(ICode code) {
        Exp exp = (Exp) code;
        String lSym = exp.getLSym(), rSym1 = exp.getRSym_1(), rSym2 = exp.getRSym_2();
        if (!isEmpty(rSym1) && !isEmpty(rSym2)) {
            /* 形如 a = b + c的表达式 */
            UseSym(lSym, rSym1, rSym2, exp);
        } else if (!isEmpty(rSym2)) {
            /* 形如a = b的表达式 */
            UseSym(lSym, rSym2, exp);
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
                if (isGlobalVar(target) || isConstVar(target)) {
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
        AddrSym addrSym;
        AddAssign(Integer.toString(Syscall.READ_INTEGER), RegDistributor.V0Reg, Assign.LI);
        AddSyscall();
        if (get.isArrayEle()) {
            String reg = DistributeReg(get.getTempVar());
            AddAssign(RegDistributor.V0Reg, reg, Assign.MOVE);
        } else {
            addrSym = curTable.FindAddrSym(name);
            if (isGlobalVar(name) || isConstVar(name)) {
                AddStore(RegDistributor.V0Reg, name);
            } else {
                AddStore(RegDistributor.V0Reg, RegDistributor.SPReg, addrSym.getOffset());
            }
        }
    }

    public void TranslateCall(ICode code) {
        PutTempVarAddrSymbol();
        FuncCall funcCall = (FuncCall) code;
        String FuncLabel = "$$" + funcCall.getFuncName() + "$$";
        AddCalculate("-", RegDistributor.SPReg, RegDistributor.SPReg, Integer.toString(Math.abs(SPOffset)));
        SPAddr -= SPOffset;
        AddJumpTo(FuncLabel, JumpTo.JAL);
        AddCalculate("+", RegDistributor.SPReg, RegDistributor.SPReg, Integer.toString(Math.abs(SPOffset)));
        SPAddr += SPOffset;
    }

    public void TranslateFunParams(ArrayList<FuncParam> funcParams) {
        int size = funcParams.size();
        for (FuncParam param : funcParams) {
            AddrSym sym = new AddrSym(param.getName(), Integer.toString(size * 4), !AddrSym.Global);
            sym.setParam(true);
            if (param.getParamSymbol().getDimension() != 0) {
                sym.setDimension(param.getParamSymbol().getDimension());
                if (sym.getDimension() == 2) {
                    sym.setDim2(Integer.parseInt(param.getDim2()));
                }
            }
            curTable.PutSymbol(sym);
            size--;
        }
//        PrintAllAddrSym();
    }

    public void TranslatePush(ICode code) {
        /* 若传递数组，则只Push一个地址，并且要修改对应的AddrSym的Offset */
        FuncPush push = (FuncPush) code;
        String target = FetchSym(push.getTarget());
        PutTempVarAddrSymbol(); /* 将尚未使用的临时变量存入栈中 */
        AddStore(target, RegDistributor.SPReg, Integer.toString(SPOffset));
        StackGrow();
    }

    public void TranslateArrayDecl(ICode code) {
        ArrayDecl arrayDecl = (ArrayDecl) code;
        VarSymbol array = arrayDecl.getVarSymbol();
        StackGrow(array.GetSize());
        AddrSym sym = new AddrSym(arrayDecl.GetLSym(), Integer.toString(SPOffset + 4), array.isGlobal());
        sym.setDim(arrayDecl.getVarSymbol().getSizeOne(), arrayDecl.getVarSymbol().getSizeTwo());
        sym.setDimension(arrayDecl.getVarSymbol().getDimension());
        sym.setAbsAddr(SPAddr + SPOffset + 4);
        curTable.PutSymbol(sym);
    }

    public void TranslateArrayLoad(ICode code) {
        ArrayLoad arrayLoad = (ArrayLoad) code;
        ArrayList<String> divided = DivideArrayString(arrayLoad.GetLSym());    /* Size of divided: 2 or 3 */
        AddrSym sym = curTable.FindAddrSym(divided.get(0));
        AddArrayLoad(sym, arrayLoad);
    }

    public void TranslateArrayStore(ICode code) {
        ArrayStore arrayStore = (ArrayStore) code;
        ArrayList<String> divided = DivideArrayString(arrayStore.GetLSym());    /* Size of divided: 2 or 3 */
        AddrSym sym = curTable.FindAddrSym(divided.get(0));
        String source = FetchSym(arrayStore.getSource());
        AddArrayStore(sym, arrayStore, source);
    }

    public void TranslateLabel(ICode code) {
        Label label = (Label) code;
        AddMipsLabel(label.toString());
    }

    public void TranslateJump(ICode code) {
        Jump jump = (Jump) code;
        if (jump.getType() == JumpTo.JR) {
            if (curTable.FindSymbol(RegDistributor.RAReg)) {
                AddrSym ra = curTable.FindAddrSym(RegDistributor.RAReg);
                AddLoad(RegDistributor.RAReg, RegDistributor.SPReg, ra.getOffset());
            }
        }
        AddJumpTo(jump.getTarget(), jump.getType());
    }

    public void TranslateCmp(ICode code) {
        Cmp cmp = (Cmp) code;
        String reg = FetchSym(cmp.GetLSym());
        AddBranch(reg, RegDistributor.ZEROReg, cmp.getLabel(), cmp.getType());
    }

    public void AddArrayLoad(AddrSym sym, ArrayLoad arrayLoad) {
        /* 访问数组时，需要区别数组是参数还是变量 */
        String reg = DistributeReg(arrayLoad.getTarget());
        if (arrayLoad.isLoadAddress()) {
            if (sym.isParam()) {    /* 如果是参数数组，那么地址从内存中取出 */
                AddLoad(reg, RegDistributor.SPReg, sym.getOffset());
            } else if (sym.isGlobal()) {    /* 如果是全局数组，那么取其标签地址 */
                AddLoad(reg, sym.getName(), Load.LA);
            } else {    /* 如果是局部数组，那么取其偏移 */
                AddAssign("0x" + Integer.toHexString(sym.getAbsAddr()), reg, Assign.LI);    /* 取出数组的绝对地址 */
            }
            if (sym.getDimension() == 1) {
                /* 实参是1维的 */
                if (!arrayLoad.getIndex1().equals("-1")) {
                    /* 形参是0维的，传递数值 */
                    String index1 = FetchSym(arrayLoad.getIndex1());
                    AddCalculate("*", index1, index1, "4"); /* 元素偏移 */
                    AddCalculate("+", reg, reg, index1);    /* 数组地址+元素偏移 */
                    AddLoad(reg, reg, "0");
                }
                /* 形参是1维的，直接传递地址 */
            } else if (sym.getDimension() == 2) {
                /* 实参是2维的 */
                if (!arrayLoad.getIndex1().equals("-1") && !arrayLoad.getIndex2().equals("-1")) {
                    /* 形参是0维的，传递数值 */
                    String index1 = FetchSym(arrayLoad.getIndex1());
                    String index2 = FetchSym(arrayLoad.getIndex2());
                    AddCalculate("*", index1, index1, String.valueOf(sym.getDim2()));
                    AddCalculate("+", index1, index1, index2);
                    AddCalculate("*", index1, index1, "4"); /* 元素偏移 */
                    AddCalculate("+", reg, reg, index1);    /* 数组地址+元素偏移 */
                    AddLoad(reg, reg, "0");
                } else if (!arrayLoad.getIndex1().equals("-1")) {
                    /* 形参是1维的，传递对应行的起始地址 */
                    String index1 = FetchSym(arrayLoad.getIndex1());
                    AddCalculate("*", index1, index1, String.valueOf(sym.getDim2()));
                    AddCalculate("*", index1, index1, "4"); /* 行偏移 */
                    AddCalculate("+", reg, reg, index1);    /* 数组地址+行偏移 */
                }
                /* 形参是2维的，直接传递起始地址 */
            }
        } else {
            String index1 = FetchSym(arrayLoad.getIndex1());
            if (arrayLoad.getDim() == 1) {
                if (sym.isGlobal()) {
                    /* 全局数组都使用标签+偏移进行访问 */
                    /* lw $t1 label($index_offset) */
                    AddCalculate("*", index1, index1, "4"); /* 元素偏移 */
                    AddLoad(reg, sym.getName(), index1, Load.LW);   /* 全局标签+偏移 */
                } else {
                    /* 局部数组需要区分到底是参数还是变量 */
                    if (sym.isParam()) {
                        /* 参数访问：lw $t1 0($abs + $index_offset) */
                        AddCalculate("*", index1, index1, "4");     /* 元素偏移 */
                        AddLoad(reg, RegDistributor.SPReg, sym.getOffset());    /* 参数数组的绝对地址 */
                        AddCalculate("+", reg, reg, index1);    /* 参数数组的绝对地址+元素偏移 */
                        AddLoad(reg, reg, "0");
                    } else {
                        /* 变量访问：lw $t1 0($sp + $offset + $index_offset) */
                        /* offset为相对sp的偏移，是负数 */
                        AddCalculate("*", index1, index1, "4");     /* 元素偏移 */
                        AddCalculate("+", index1, index1, sym.getOffset()); /* 局部数组偏移+元素偏移 */
                        AddCalculate("+", index1, RegDistributor.SPReg, index1); /* 栈指针+局部数组偏移+元素偏移 */
                        AddLoad(reg, index1, "0");
                    }
                }
            } else if (arrayLoad.getDim() == 2) {
                String index2 = FetchSym(arrayLoad.getIndex2());
                AddCalculate("*", index1, index1, String.valueOf(sym.getDim2()));
                AddCalculate("+", index1, index1, index2);
                AddCalculate("*", index1, index1, "4"); /* 元素偏移 */
                if (sym.isGlobal()) {
                    /* lw $t1 label($index_offset), index_offset = (index1 * col + index2) * 4 */
                    AddLoad(reg, sym.getName(), index1, Load.LW);   /* 全局标签+元素偏移 */
                } else {
                    if (sym.isParam()) {
                        /* 参数访问：lw $t1 0($abs + $index_offset) */
                        AddLoad(reg, RegDistributor.SPReg, sym.getOffset());    /* 参数数组绝对地址 */
                        AddCalculate("+", reg, reg, index1);    /* 参数数组绝对地址+元素偏移 */
                        AddLoad(reg, reg, "0");
                    } else {
                        /* 变量访问：lw $t1 0($sp + $offset + $index_offset) */
                        AddCalculate("+", index1, index1, sym.getOffset()); /* 局部数组偏移+元素偏移 */
                        AddCalculate("+", index1, RegDistributor.SPReg, index1); /*栈指针+局部数组偏移+元素偏移 */
                        AddLoad(reg, index1, "0");
                    }
                }
            }
        }
    }

    public void AddArrayStore(AddrSym sym, ArrayStore arrayStore, String source) {
        String index1 = FetchSym(arrayStore.getIndex1());
        if (arrayStore.getDim() == 1) {
            if (sym.isGlobal()) {
                /* sw $t1, label($index_offset) */
                AddCalculate("*", index1, index1, "4"); /* 元素偏移 */
                AddStore(source, index1, sym.getName());    /* 标签+元素偏移 */
            } else {
                if (sym.isParam()) {
                    /* 参数访问sw $t1, 0($abs + $index_offset) */
                    String abs = DistributeReg();
                    AddCalculate("*", index1, index1, "4"); /* 元素偏移 */
                    AddLoad(abs, RegDistributor.SPReg, sym.getOffset());    /* 参数数组绝对地址 */
                    AddCalculate("+", index1, abs, index1); /* 参数数组绝对地址+元素偏移 */
                    AddStore(source, index1, "0");
                } else {
                    /* 变量访问sw $t1, 0($sp + $offset + $index_offset) */
                    AddCalculate("*", index1, index1, "4"); /* 元素偏移 */
                    AddCalculate("+", index1, index1, sym.getOffset()); /* 局部数组偏移+元素偏移 */
                    AddCalculate("+", index1, RegDistributor.SPReg, index1); /* 栈指针+局部数组偏移+元素偏移 */
                    AddStore(source, index1, "0");
                }
            }
        } else if (arrayStore.getDim() == 2) {
            String index2 = FetchSym(arrayStore.getIndex2());
            AddCalculate("*", index1, index1, String.valueOf(sym.getDim2()));
            AddCalculate("+", index1, index1, index2);
            AddCalculate("*", index1, index1, "4"); /* 元素偏移 */
            if (sym.isGlobal()) {
                /* sw $t1, label($index_offset) */
                AddStore(source, index1, sym.getName());    /* 全局标签+元素偏移 */
            } else {
                if (sym.isParam()) {
                    /* 参数访问：sw $t1 0($abs + $index_offset) */
                    String abs = DistributeReg();
                    AddLoad(abs, RegDistributor.SPReg, sym.getOffset());    /* 参数数组绝对地址 */
                    AddCalculate("+", index1, abs, index1); /* 参数数组绝对地址+元素偏移 */
                    AddStore(source, index1, "0");
                } else {
                    /* 变量访问：sw $t1 0($sp + $offset + $index_offset */
                    AddCalculate("+", index1, index1, sym.getOffset()); /* 局部数组偏移+元素偏移 */
                    AddCalculate("+", index1, RegDistributor.SPReg, index1); /* 栈指针+局部数组偏移+元素偏移 */
                    AddStore(source, index1, "0");
                }
            }
        }
    }


    public String DistributeReg(String temp) {
        String tempReg = regDistributor.TempRegDistribute();
        /* 分配寄存器的时候，如果遇到某一个寄存器已经被占用，则将该寄存器内容弹出
         * 并登记符号表，记录偏移 */
        CheckRegUsage(tempReg);
        TempReg.put(temp, tempReg);
        return tempReg;
    }

    public String DistributeReg() {
        String tempReg = regDistributor.TempRegDistribute();
        CheckRegUsage(tempReg);
        return tempReg;
    }

    public void CheckRegUsage(String reg) {
        String result = null;
        for (String key : TempReg.keySet()) {
            if (TempReg.get(key).equals(reg)) {
                AddStore(TempReg.get(key), RegDistributor.SPReg, String.valueOf(SPOffset));
                curTable.PutSymbol(new AddrSym(key, Integer.toString(SPOffset), !AddrSym.Global));
                StackGrow();
                result = key;
                break;
            }
        }
        if (result != null) {
            TempReg.remove(result);
        }
    }


    public String FetchSym(String sym) {    /* 取用一个Sym */
        if (isDigit(sym)) {     /* 数字，直接分发一个寄存器 */
            String reg = DistributeReg();
            TranslateAssign(sym, reg);
            return reg;
        } else if (isVar(sym)) {    /* 右侧要求全局和局部变量，都是马上使用，不用登记 */
            if (isGlobalVar(sym) || isConstVar(sym)) {     /* 全局变量，分配一个寄存器并使用lw Label */
                String reg = DistributeReg();
                AddLoad(reg, sym, Load.LW);
                return reg;
            } else {    /* 局部变量，分配一个寄存器并使用lw reg offset($sp) */
                AddrSym addrSym = curTable.FindAddrSym(sym);
                String reg = DistributeReg();
                AddLoad(reg, RegDistributor.SPReg, addrSym.getOffset());
                return reg;
            }
        } else if (isTemp(sym)) {
            /* 临时变量，如果该临时变量已经被分配到临时寄存器，则返回其对应寄存器；
             * 否则，该临时变量应当被存入了符号表，需要查表 */
            if (curTable.FindSymbol(sym)) {
                AddrSym addrSym = curTable.FindAddrSym(sym);
                String reg = DistributeReg(sym);
                AddLoad(reg, RegDistributor.SPReg, addrSym.getOffset());
                return reg;
            }
            if (TempReg.containsKey(sym)) {
                String reg = TempReg.get(sym);
                TempReg.remove(sym);
                return reg;
            }
            return DistributeReg(sym);
        } else if (isFuncRet(sym)) {
            return RegDistributor.V1Reg;
        }
        return null;
    }

    public void UseSym(String lSym, String rSym1, String rSym2, Exp exp) {
        if (isVar(lSym)) {
            String reg3 = DistributeReg();  /* 这里的寄存器分配属于原子操作，分配完了马上使用，故而不需要登记 */
            String reg1 = FetchSym(rSym1);
            TempReg.put(rSym1, reg1);
            String reg2 = FetchSym(rSym2);
            if (reg1.equals(reg2)) {
                reg1 = FetchSym(rSym1);
            }
            if (isGlobalVar(lSym) || isConstVar(lSym)) {
                /* 左侧使用全局变量或常量，应使用sw Label */
                AddCalculate(exp.getOperator(), reg3, reg1, reg2);
                AddStore(reg3, lSym);
            } else {
                /* 左侧使用局部变量，应当使用sw reg offset($sp) */
                AddrSym addrSym = curTable.FindAddrSym(lSym);
                AddCalculate(exp.getOperator(), reg3, reg1, reg2);
                AddStore(reg3, RegDistributor.SPReg, addrSym.getOffset());
            }
            TempReg.remove(rSym1);
            TempReg.remove(rSym2);
        } else if (isTemp(lSym)) {
            /* 左侧使用临时变量，分配寄存器并保存值，更新寄存器记录 */
            /* 右边使用完的变量可以释放 */
            String reg = DistributeReg(lSym);
            String reg1 = FetchSym(rSym1);
            TempReg.put(rSym1, reg1);
            String reg2 = FetchSym(rSym2);
            if (reg1.equals(reg2)) {
                reg1 = FetchSym(rSym1);
            }
            TempReg.remove(rSym1);
            TempReg.remove(rSym2);
            AddCalculate(exp.getOperator(), reg, reg1, reg2);
        }
    }

    public void UseSym(String lSym, String rSym, Exp exp) {
        String reg = FetchSym(rSym);
        String operator = exp.getOperator();
        if (operator.equals("-")) {
            /* UnaryOp为负，需要手动转换 */
            AddCalculate(operator, reg, RegDistributor.ZEROReg, reg);
        } else if (operator.equals("!")) {
            AddCalculate(operator, reg, reg, RegDistributor.ZEROReg);
        }
        if (isVar(lSym)) {
            if (isGlobalVar(lSym) || isConstVar(lSym)) {
                /* 左侧使用全局变量或常量，应使用sw Label */
                AddStore(reg, lSym);
            } else {
                /* 左侧使用局部变量，应当使用sw reg offset($sp) */
                AddrSym addrSym = curTable.FindAddrSym(lSym);
                AddStore(reg, RegDistributor.SPReg, addrSym.getOffset());
            }
        } else if (isTemp(lSym)) {
            /* 左侧使用临时变量，分配寄存器并保存值，更新寄存器记录 */
            String tReg = DistributeReg(lSym);
            AddAssign(reg, tReg, Assign.MOVE);
        } else if (isFuncRet(lSym)) {
            /* 左侧是返回值，向V1中保存右侧值
             * 注意，出现返回值表示可能存在的函数返回，需要安排跳转语句 */
            AddAssign(reg, RegDistributor.V1Reg, Assign.MOVE);
            if (!curFunc.equals("$$main$$:")) {
                if (curTable.FindSymbol(RegDistributor.RAReg)) {
                    AddrSym ra = curTable.FindAddrSym(RegDistributor.RAReg);
                    AddLoad(RegDistributor.RAReg, RegDistributor.SPReg, ra.getOffset());
                }
                AddJumpTo(RegDistributor.RAReg, JumpTo.JR);
            }
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

    public void AddLoad(String TargetReg, String Label, String addrReg, int type) {
        MIPSCodes.add(new Load(TargetReg, Label, addrReg, type));
    }

    public void AddAssign(String source, String target, int type) {
        MIPSCodes.add(new Assign(source, target, type));
    }

    public void AddCalculate(String operator, String t1, String t2, String t3) {
        switch (operator) {
            case "+":
            case "-":
            case "*":
            case "==":
            case "!=":
            case ">=":
            case "<=":
            case ">":
            case "<":
            case "!":
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

    public void AddBranch(String t1, String t2, String label, int type) {
        MIPSCodes.add(new Branch(t1, t2, label, type));
    }

    public void StackGrow() {
        SPOffset -= 4;
    }

    public void StackGrow(int length) {
        SPOffset -= length;
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
//        System.out.println(sym);
//        System.out.println(headTable.FindSymbol(sym));
        return headTable.FindSymbol(sym);
    }

    public boolean isConstVar(String sym) {
        return curTable.FindConstSymbol(sym);
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

    public ArrayList<String> DivideArrayString(String origin) {
        ArrayList<String> division = new ArrayList<>();
        int length = origin.length();
        for (int i = 0; i < length; i++) {
            StringBuilder substring = new StringBuilder();
            while (i < length && origin.charAt(i) != '[' && origin.charAt(i) != ']') {
                substring.append(origin.charAt(i));
                i++;
            }
            if (!substring.toString().equals("")) {
                division.add(substring.toString());
            }
        }
        return division;
    }

    public void PutTempVarAddrSymbol() {
        for (String tempVar : TempReg.keySet()) {
            curTable.PutSymbol(new AddrSym(tempVar, Integer.toString(SPOffset), !AddrSym.Global));
            AddStore(TempReg.get(tempVar), RegDistributor.SPReg, Integer.toString(SPOffset));
            StackGrow();
        }
        TempReg.clear();
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
