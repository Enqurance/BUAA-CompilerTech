package Tool;

import ClassFile.FuncSymbol;
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
import ClassFile.MipsInstr.Branch;
import ClassFile.MipsInstr.JumpTo;
import ClassFile.Node;
import ClassFile.SymbolTable;
import ClassFile.VarSymbol;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

public class Generator {
    private final Node treeHead;
    private final SymbolTable tableHead;
    private Node curTree;
    private SymbolTable curTable;
    private final ArrayList<ICode> iCodes = new ArrayList<>();
    private int variableCount = 0;
    private int whileLabelCount = -1;
    private int ifLabelCount = -1;
    private int strCount = 0;

    public Generator(Node head, SymbolTable table) {
        treeHead = head;
        tableHead = table;
        curTable = tableHead;
        curTree = treeHead;
    }

    public void Generate() {
        AddLabel("Init", false);
        GenCompUnit(treeHead);
    }

    public void GenCompUnit(Node node) {
        for (Node item : node.getChildren()) {
            switch (item.getContext()) {
                case "<Decl>":
                    GenDecl(item);
                    break;
                case "<FuncDef>":
                    GenFuncDef(item);
                    break;
                case "<MainFuncDef>":
                    GenMainFuncDef(item);
                    break;
            }
        }
    }

    public void GenDecl(Node node) {
        for (Node item : node.getChildren()) {
            switch (item.getContext()) {
                case "<ConstDecl>":
                    GenConstDecl(item);
                    break;
                case "<VarDecl>":
                    GenVarDecl(item);
                    break;
            }
        }
    }

    public void GenFuncDef(Node node) {
        ArrayList<Node> children = node.getChildren();
        AddLabel(children.get(1).getContext(), false);
        FuncDecl funcDecl = AddFuncDef(children);
        for (Node item : children) {
            switch (item.getContext()) {
                case "<FuncFParams>":
                    funcDecl.getFuncParams().addAll(GenFuncFParams(item));
                    break;
                case "<Block>":
                    GenBlock(item);
                    break;
            }
        }
    }

    public void GenMainFuncDef(Node node) {
        ArrayList<Node> children = node.getChildren();
        AddLabel(children.get(1).getContext(), false);
        AddMainFuncDef();
        GenBlock(children.get(4));
    }

    public void GenConstDecl(Node node) {
        ArrayList<Node> children = node.getChildren();
        for (Node item : children) {
            if (item.getContext().equals("<ConstDef>")) {
                GenConstDef(item);
            }
        }
    }

    public void GenConstDef(Node node) {
        ArrayList<Node> children = node.getChildren();
        VarSymbol symbol = FindVarSymbolByName(curTable,
                children.get(0).getContext(), children.get(1).getLine());
        if (symbol.getDimension() == 0) {
            String initVal = GenConstInitValForVar(children.get(2));
            VarSymbol var = curTable.FindVarSymbolByName(children.get(0).getContext());
            ConstDecl constDecl = AddConstDecl(IdentifyVarLevel(var), var.isGlobal());
            AddExp(constDecl.GetLSym(), initVal);
            if (isDigit(initVal)) {
                symbol.setValue(initVal);
            }
        } else if (symbol.getDimension() == 1) {
            String DimOne = GenConstExp(children.get(2));
            ArrayDecl arrayDecl = AddArrayDecl(symbol);
            arrayDecl.setSym1(DimOne);
            for (Node item : children) {
                if (item.getContext().equals("<ConstExp>")) {
                    String size = GenConstExp(item);
                    symbol.SetSize(Integer.parseInt(size));
                } else if (item.getContext().equals("<ConstInitVal>")) {
                    GenConstInitValForArrayDimOne(item, symbol, 0);
                }
            }
            if (!symbol.isGlobal()) {
                int length = symbol.getInitVal().size();
                for (int i = 0; i < length; i++) {
                    AddArrayStore(IdentifyVarLevel(symbol), String.valueOf(symbol.getInitVal().get(i)), String.valueOf(i));
                }
            }
        } else if (symbol.getDimension() == 2) {
            String DimOne = GenConstExp(children.get(2));
            String DimTwo = GenConstExp(children.get(5));
            ArrayDecl arrayDecl = AddArrayDecl(symbol);
            arrayDecl.setSym1(DimOne);
            arrayDecl.setSym2(DimTwo);
            for (Node item : children) {
                if (item.getContext().equals("<ConstExp>")) {
                    String size = GenConstExp(item);
                    symbol.SetSize(Integer.parseInt(size));
                } else if (item.getContext().equals("<ConstInitVal>")) {
                    GenConstInitValForArrayDimTwo(item, symbol);
                }
            }
            if (!symbol.isGlobal()) {
                int length = symbol.getInitVal().size();
                for (int i = 0; i < length; i++) {
                    AddArrayStore(IdentifyVarLevel(symbol), String.valueOf(symbol.getInitVal().get(i)), String.valueOf(i));
                }
            }
        }
    }

    public String GenConstInitValForVar(Node node) {
        ArrayList<Node> children = node.getChildren();
        return GenConstExp(children.get(0));
    }

    public void GenConstInitValForArrayDimOne(Node node, VarSymbol array, int start) {
        ArrayList<Node> children = node.getChildren();
        int index = start;
        for (Node item : children) {
            if (item.getContext().equals("<ConstInitVal>")) {
                int value = Integer.parseInt(GenConstInitValForVar(item));
                array.SetArrayValue(index++, value);
            }
        }
    }

    public void GenConstInitValForArrayDimTwo(Node node, VarSymbol array) {
        ArrayList<Node> children = node.getChildren();
        int index = 0;
        for (Node item : children) {
            if (item.getContext().equals("<ConstInitVal>")) {
                GenConstInitValForArrayDimOne(item, array, index);
                index = index + array.getSizeTwo();
            }
        }
    }

    public String GenConstExp(Node node) {
        return GenAddExp(node.getChildren().get(0));
    }

    public ArrayList<FuncParam> GenFuncFParams(Node node) {
        ArrayList<FuncParam> params = new ArrayList<>();
        ArrayList<Node> children = node.getChildren();
        for (Node item : children) {
            if ("<FuncFParam>".equals(item.getContext())) {
                params.add(GenFuncFParam(item));
            }
        }
        return params;
    }

    public FuncParam GenFuncFParam(Node node) {
        ArrayList<Node> children = node.getChildren();
        return AddFuncParam(children);
    }

    public void GenBlock(Node node) {
        ArrayList<Node> children = node.getChildren();
        for (Node item : children) {
            if (item.getContext().equals("<BlockItem>")) {
                GenBlockItem(item);
            }
        }
        curTable = curTable.getParent();
    }

    public void GenBlockItem(Node node) {
        ArrayList<Node> children = node.getChildren();
        for (Node item : children) {
            switch (item.getContext()) {
                case "<Decl>":
                    GenDecl(item);
                    break;
                case "<Stmt>":
                    GenStmt(item);
                    break;
            }
        }
    }

    public void GenVarDecl(Node node) {
        ArrayList<Node> children = node.getChildren();
        for (Node item : children) {
            if (item.getContext().equals("<VarDef>")) {
                GenVarDef(item);
            }
        }
    }

    public void GenVarDef(Node node) {
        ArrayList<Node> children = node.getChildren();
        VarSymbol symbol = FindVarSymbol(children.get(0));
        if (symbol.getDimension() == 0) {
            VarDecl varDecl = AddVarDecl(symbol, IdentifyVarLevel(symbol));
            if (children.get(children.size() - 1).getContext().equals("<InitVal>")) {
                String initVal = GenInitValForVar(children.get(children.size() - 1));
                AddExp(varDecl.getName(), initVal);
            }
        } else if (symbol.getDimension() == 1) {
            String DimOne = GenConstExp(children.get(2));
            ArrayDecl arrayDecl = AddArrayDecl(symbol);
            symbol.SetSize(Integer.parseInt(DimOne));
            arrayDecl.setSym1(DimOne);
            if (symbol.isGlobal()) {
                symbol.InitializeArray(Integer.parseInt(DimOne), 1);
            }
            ArrayList<String> values = new ArrayList<>();
            for (Node item : children) {
                if (item.getContext().equals("<InitVal>")) {
                    values.addAll(GenInitValForArrayDimOne(item));
                }
            }
            int length = values.size();
            for (int i = 0; i < length; i++) {
                AddArrayStore(IdentifyVarLevel(symbol), values.get(i), String.valueOf(i));
            }
        } else if (symbol.getDimension() == 2) {
            String DimOne = GenConstExp(children.get(2));
            String DimTwo = GenConstExp(children.get(5));
            ArrayDecl arrayDecl = AddArrayDecl(symbol);
            symbol.SetSize(Integer.parseInt(DimOne));
            symbol.SetSize(Integer.parseInt(DimTwo));
            VarSymbol varSymbol = FindVarSymbolByName(curTable, GetVarName(DimTwo), node.getLine());
//            if (varSymbol != null) {
//                System.out.println(varSymbol.getValue());
//            }
            arrayDecl.setSym1(DimOne);
            arrayDecl.setSym2(DimTwo);
            if (symbol.isGlobal()) {
                symbol.InitializeArray(Integer.parseInt(DimOne), Integer.parseInt(DimTwo));
            }
            ArrayList<String> values = new ArrayList<>();
            for (Node item : children) {
                if (item.getContext().equals("<InitVal>")) {
                    values.addAll(GenInitValForArrayDimTwo(item));
                }
            }
            int length = values.size();
            for (int i = 0; i < length; i++) {
                AddArrayStore(IdentifyVarLevel(symbol), values.get(i),
                        String.valueOf(i / symbol.getSizeTwo()), String.valueOf(i % symbol.getSizeTwo()));
            }
        }
    }

    public String GenInitValForVar(Node node) {
        return GenExp(node.getChildren().get(0));
    }

    public ArrayList<String> GenInitValForArrayDimOne(Node node) {
        ArrayList<Node> children = node.getChildren();
        ArrayList<String> values = new ArrayList<>();
        for (Node item : children) {
            if (item.getContext().equals("<InitVal>")) {
                values.add(GenInitValForVar(item));
            }
        }
        return values;
    }

    public ArrayList<String> GenInitValForArrayDimTwo(Node node) {
        ArrayList<Node> children = node.getChildren();
        ArrayList<String> values = new ArrayList<>();
        for (Node item : children) {
            if (item.getContext().equals("<InitVal>")) {
                values.addAll(GenInitValForArrayDimOne(item));
            }
        }
        return values;
    }

    public void GenStmt(Node node) {
        ArrayList<Node> children = node.getChildren();
        Node firstNode = children.get(0);
        switch (firstNode.getContext()) {
            case "<LVal>":
                String lVal = GenLVal(firstNode);
                if (!lVal.contains("[") && !lVal.contains("]")) {
                    if (children.get(2).getContext().equals("getint")) {
                        AddGet(lVal);
                    } else if (children.get(2).getContext().equals("<Exp>")) {
                        String rSym = GenExp(children.get(2));
                        AddExp(lVal, rSym);
                    }
                } else {
                    if (children.get(2).getContext().equals("getint")) {
                        String temp = AddGet(lVal, true);
                        AddStore(lVal, temp);
                    } else if (children.get(2).getContext().equals("<Exp>")) {
                        String rSym = GenExp(children.get(2));
                        AddStore(lVal, rSym);
                    }
                }
                break;
            case "<Exp>":
                GenExp(firstNode);
                break;
            case "<Block>":
                curTable = curTable.GetNextChild();
                if (curTable.isWhile()) {
                    curTable.setWhileCount(whileLabelCount);
                }
                GenBlock(firstNode);
                break;
            case "if":
                if (children.size() == 5) {
                    ifLabelCount++;
                    String IfBegin = DistributeIfLabel();
                    String IfEnd = DistributeIfEndLabel();
                    GenCond(children.get(2), IfBegin, IfEnd, 1);
                    AddLabel(IfBegin, true);
                    GenStmt(children.get(4));
                    AddLabel(IfEnd, true);
                } else if (children.size() == 7) {
                    ifLabelCount++;
                    String IfBegin = DistributeIfLabel();
                    String IfEnd = DistributeIfEndLabel();
                    String ElseBegin = DistributeElseLabel();
                    String ElseEnd = DistributeElseEndLabel();
                    GenCond(children.get(2), IfBegin, IfEnd, 1);
                    AddLabel(IfBegin, true);
                    GenStmt(children.get(4));
                    AddJump(ElseEnd, JumpTo.J);
                    AddLabel(IfEnd, true);
                    AddLabel(ElseBegin, true);
                    GenStmt(children.get(6));
                    AddLabel(ElseEnd, true);
                }
                break;
            case "while":
                whileLabelCount++;
                String endLabel = DistributeWhileLabelEnd();
                String beginLabel = DistributeWhileLabel();
                String CondBegin = GenCond(children.get(2), beginLabel, endLabel, 0);
                children.get(0).setWhileCondLabel(CondBegin);   // 将标签存入Node中
                children.get(0).setWhileEndLabel(endLabel);
                AddLabel(beginLabel, true);
                GenStmt(children.get(4));
                AddJump(CondBegin, JumpTo.J);
                AddLabel(endLabel, true);
                break;
            case "break":
                Node test1 = node.getParent().getChildren().get(0);
                if (test1.getContext().equals("while")) {    // 没有块，直接跟break
                    AddJump(test1.getWhileEndLabel(), JumpTo.J);
                    break;
                }
                AddJump(GetBreakLabel(curTable), JumpTo.J);
                break;
            case "continue":
                Node test2 = node.getParent().getChildren().get(0);
                if (test2.getContext().equals("while")) {    // 没有块，直接跟break
                    AddJump(test2.getWhileCondLabel(), JumpTo.J);
                    break;
                }
                AddJump(GetContinueLabel(curTable), JumpTo.J);
                break;
            case "return":
                if (children.size() == 2) {
                    AddJump(RegDistributor.RAReg, JumpTo.JR);
                    break;
                } else {
                    String exp = GenExp(children.get(1));
                    AddExp("RET", exp);
                }
                break;
            case "printf":
                ArrayList<Node> exps = new ArrayList<>();
                for (Node item : children) {
                    if (item.getContext().equals("<Exp>")) {
                        exps.add(item);
                    }
                }
                Node format = children.get(2);
                String context = format.getContext();
                int len = context.length(), i = 1;
                ArrayList<String> slices = new ArrayList<>();
                while (i < len - 1) {
                    StringBuilder slice = new StringBuilder();
                    if (context.charAt(i) != '%') {
                        slice.append(context.charAt(i));
                        i++;
                        while (i < len - 1 && context.charAt(i) != '%') {
                            slice.append(context.charAt(i));
                            i++;
                        }
                    } else {
                        slice.append(context.charAt(i)).append(context.charAt(i + 1));
                        i = i + 2;
                    }
                    slices.add(slice.toString());
                }
                ArrayList<String> Outputs = new ArrayList<>();
                for (Node exp : exps) {
                    Outputs.add(GenExp(exp));
                }
                int OutputCnt = 0;
                for (String str : slices) {
                    if (!str.equals("%d")) {
                        ConstDecl ConstStr = AddConstDecl(str, strCount++, true);
                        AddPrintf(ConstStr.GetLSym());
                    } else {
                        AddPrintf(Outputs.get(OutputCnt++));
                    }
                }
                break;
        }
    }

    public String GenExp(Node node) {
        return GenAddExp(node.getChildren().get(0));
    }

    public String GenAddExp(Node node) {
        if (node.isPushingParams()) {
            node.AdjustPushingParam(true);
        }
        ArrayList<Node> children = node.getChildren();
        ArrayList<String> symbols = new ArrayList<>();
        Stack<String> ResultStack = new Stack<>();
        if (children.size() == 1) {
            String res = GenMulExp(children.get(0));
            if (node.isPushingParams()) {
                node.AdjustPushingParam(true);
            }
            return res;
        }
        for (Node item : children) {
            if (item.getContext().equals("<MulExp>")) {
                symbols.add(GenMulExp(item));
            } else if (!item.getContext().equals("<AddExp>")) { /* 当前Node对应一个运算符 */
                symbols.add(item.getContext());
            }
        }
        MergeCountable(node.getLine(), symbols, ResultStack);
        ReverseStack(ResultStack);
        if (ResultStack.size() != 1) {
            while (ResultStack.size() > 1) {
                String num1 = ResultStack.pop(), op = ResultStack.pop(), num2 = ResultStack.pop();
                String tempVar = DistributeVariable();
                AddExp(op, tempVar, num1, num2);
                ResultStack.add(tempVar);
            }
        }
        if (node.isPushingParams()) {
            node.AdjustPushingParam(false);
        }
        return ResultStack.pop();
    }

    public void MergeCountable(int line, ArrayList<String> symbols, Stack<String> resultStack) {
        /* resultStack是用来维护表达式的 */
        for (String symbol : symbols) {
            if (isDigit(symbol)) {
                if (resultStack.size() != 0) {
                    /* 栈顶为运算符，top-1是一个运算数。临时变量、*/
                    String operator = resultStack.pop(), topVar = resultStack.pop();
                    if (CanMerge(topVar, line, resultStack, operator)) {
                        if (isDigit(topVar)) {
                            int top = Integer.parseInt(topVar), now = Integer.parseInt(symbol);
                            int res = CalculateTwo(operator, top, now);
                            resultStack.add(Integer.toString(res));
                        } else {
                            VarSymbol varSymbol = FindVarSymbolByName(curTable, GetVarName(topVar), line);
                            int top = Integer.parseInt(varSymbol.getValue()), now = Integer.parseInt(symbol);
                            int res = CalculateTwo(operator, top, now);
                            resultStack.add(Integer.toString(res));
                        }
                    } else {
                        resultStack.add(topVar);
                        resultStack.add(operator);
                        resultStack.add(symbol);
                    }
                } else {
                    resultStack.add(symbol);
                }
            } else if (isVar(symbol)) {
                VarSymbol var = FindVarSymbolByName(curTable, GetVarName(symbol), line);
                if (resultStack.size() != 0) {
                    if (var.getValue() != null) {
                        String operator = resultStack.pop(), topVar = resultStack.pop();
                        if (CanMerge(topVar, line, resultStack, operator)) {
                            if (isDigit(topVar)) {
                                int top = Integer.parseInt(topVar), now = Integer.parseInt(var.getValue());
                                int res = CalculateTwo(operator, top, now);
                                resultStack.add(Integer.toString(res));
                            } else {
                                VarSymbol varSymbol = FindVarSymbolByName(curTable, GetVarName(topVar), line);
                                int top = Integer.parseInt(varSymbol.getValue()), now = Integer.parseInt(symbol);
                                int res = CalculateTwo(operator, top, now);
                                resultStack.add(Integer.toString(res));
                            }
                        } else {
                            resultStack.add(topVar);
                            resultStack.add(operator);
                            resultStack.add(symbol);
                        }
                    } else {
                        resultStack.add(symbol);
                    }
                } else {
                    resultStack.add(symbol);
                }
            } else if (isNote(symbol) || isTempVar(symbol)) {
                resultStack.add(symbol);
            }
        }
    }

    public boolean CanMerge(String string, int line, Stack<String> stack, String curOperator) {
        if (!stack.isEmpty()) {
            String preOperator = stack.pop();
            /* 连续的除法或者是模运算不优化 */
            switch (curOperator) {
                case "%":
                case "/":
                    stack.push(preOperator);
                    return false;
                case "*":
                    if (!preOperator.equals("*")) {
                        stack.push(preOperator);
                        return false;
                    }
                    break;
                case "-":
                case "+":    /* 减法也不优化了 */
                    if (!preOperator.equals("+")) {
                        stack.push(preOperator);
                        return false;
                    }
                    break;
            }
            stack.push(preOperator);
        }
        if (isDigit(string)) {
            return true;
        } else if (isTempVar(string)) {
            return false;
        } else if (isVar(string)) {
            VarSymbol varSymbol = FindVarSymbolByName(curTable, GetVarName(string), line);
            if (varSymbol.getDimension() == 0) {
                return varSymbol.getValue() != null;
            } else if (varSymbol.getDimension() == 1) {
                /*TODO:For dim1 array*/
            } else if (varSymbol.getDimension() == 2) {
                /*TODO:For dim2 array*/
            }
        }
        return false;
    }

    public void ReverseStack(Stack<String> stack) {
        ArrayList<String> elements = new ArrayList<>();
        while (!stack.isEmpty()) {
            elements.add(stack.pop());
        }
        for (String element : elements) {
            stack.push(element);
        }
    }

    public String GenMulExp(Node node) {
        if (node.isPushingParams()) {
            node.AdjustPushingParam(true);
        }
        ArrayList<Node> children = node.getChildren();
        ArrayList<String> symbols = new ArrayList<>();
        Stack<String> ResultStack = new Stack<>();
        if (children.size() == 1) {
            String res = GenUnaryExp(children.get(0));
            if (node.isPushingParams()) {
                node.AdjustPushingParam(false);
            }
            return res;
        }
        for (Node item : children) {
            if (item.getContext().equals("<UnaryExp>")) {
                symbols.add(GenUnaryExp(item));
            } else if (!item.getContext().equals("<MulExp>")) {
                symbols.add(item.getContext());
            }
        }
        MergeCountable(node.getLine(), symbols, ResultStack);
        ReverseStack(ResultStack);
        if (ResultStack.size() != 1) {
            while (ResultStack.size() > 1) {
                String num1 = ResultStack.pop(), op = ResultStack.pop(), num2 = ResultStack.pop();
                if (num2.equals("1") && (op.equals("*") || op.equals("/"))) {
                    ResultStack.push(num1);
                    continue;
                }
                String tempVar = DistributeVariable();
                AddExp(op, tempVar, num1, num2);
                ResultStack.add(tempVar);
            }
        }
        if (node.isPushingParams()) {
            node.AdjustPushingParam(false);
        }
        return ResultStack.pop();
    }

    public String GenCond(Node node, String beginLabel, String endLabel, int type) {
        return GenLOrExp(node.getChildren().get(0), beginLabel, endLabel, type);
    }

    public String GenLOrExp(Node node, String beginLabel, String endLabel, int type) {
        int condCnt = 0;
        ArrayList<Node> children = node.getChildren();
        ArrayList<Node> LAndExps = new ArrayList<>();
        for (Node item : children) {
            if (item.getContext().equals("<LAndExp>")) {
                LAndExps.add(item);
            }
        }
        int length = LAndExps.size();
        String CondBegin = DistributeCondLabel(condCnt, type);
        AddLabel(CondBegin, true);
        condCnt++;
        for (int i = 0; i < length - 1; i++) {
            String CondLabel = DistributeCondLabel(condCnt, type);
            GenLAndExp(LAndExps.get(i), beginLabel, CondLabel, false);
            AddLabel(CondLabel, true);
            condCnt++;
        }
        GenLAndExp(LAndExps.get(length - 1), beginLabel, endLabel, true);
        return CondBegin;
    }

    public void GenLAndExp(Node node, String beginLabel, String endLabel, boolean isFinal) {
        ArrayList<Node> children = node.getChildren();
        int length = children.size();
        for (int i = 0; i < length - 1; i++) {
            if (children.get(i).getContext().equals("<EqExp>")) {
                String res = GenEqExp(children.get(i));
                AddCmp(res, endLabel, Branch.BEQ);
            }
        }
        String res = GenEqExp(children.get(length - 1));
        if (isFinal) {
            AddCmp(res, endLabel, Branch.BEQ);
        } else {
            AddCmp(res, beginLabel, Branch.BNE);
        }
    }

    public String GenEqExp(Node node) {
        ArrayList<Node> children = node.getChildren();
        ArrayList<String> symbols = new ArrayList<>();
        Stack<String> ResultStack = new Stack<>();
        if (children.size() == 1) {
            return GenRelExp(children.get(0));
        }
        for (Node item : children) {
            if (item.getContext().equals("<RelExp>")) {
                symbols.add(GenRelExp(item));
            } else if (!item.getContext().equals("<EqExp>")) {
                symbols.add(item.getContext());
            }
        }
        for (String str : symbols) {
            ResultStack.push(str);
        }
        ReverseStack(ResultStack);
        if (ResultStack.size() != 1) {
            while (ResultStack.size() > 1) {
                String num1 = ResultStack.pop(), op = ResultStack.pop(), num2 = ResultStack.pop();
                String tempVar = DistributeVariable();
                AddExp(op, tempVar, num1, num2);
                ResultStack.add(tempVar);
            }
        }
        return ResultStack.pop();
    }

    public String GenRelExp(Node node) {
        ArrayList<Node> children = node.getChildren();
        ArrayList<String> symbols = new ArrayList<>();
        Stack<String> ResultStack = new Stack<>();
        if (children.size() == 1) {
            return GenAddExp(children.get(0));
        }
        for (Node item : children) {
            if (item.getContext().equals("<AddExp>")) {
                symbols.add(GenAddExp(item));
            } else if (!item.getContext().equals("<RelExp>")) {
                symbols.add(item.getContext());
            }
        }
        for (String str : symbols) {
            ResultStack.push(str);
        }
        ReverseStack(ResultStack);
        if (ResultStack.size() != 1) {
            while (ResultStack.size() > 1) {
                String num1 = ResultStack.pop(), op = ResultStack.pop(), num2 = ResultStack.pop();
                String tempVar = DistributeVariable();
                AddExp(op, tempVar, num1, num2);
                ResultStack.add(tempVar);
            }
        }
        return ResultStack.pop();
    }

    public String GenUnaryExp(Node node) {
        if (node.isPushingParams()) {
            node.AdjustPushingParam(true);
        }
        ArrayList<Node> children = node.getChildren();
        Node firstNode = children.get(0);
        if (children.size() == 1) {
            String res = GenPrimaryExp(firstNode);
            if (node.isPushingParams()) {
                node.AdjustPushingParam(false);
            }
            return res;
        }
        if ("<UnaryOp>".equals(firstNode.getContext())) {
            String rSym = GenUnaryExp(children.get(1));
            String note = GenUnaryOp(firstNode);
            if (isDigit(rSym)) {
                if (note.equals("-")) {
                    return String.valueOf(-1 * Integer.parseInt(rSym));
                } else if (!note.equals("!")) {
                    return String.valueOf(Integer.parseInt(rSym));
                }
            }
            String lSym = DistributeVariable();
            AddExp(note, lSym, rSym);
            if (node.isPushingParams()) {
                node.AdjustPushingParam(false);
            }
            return lSym;
        } else {
            FuncSymbol func = (FuncSymbol) tableHead.findName(firstNode.getContext());
            if (children.get(2).getContext().equals("<FuncRParams>")) {
                GenFuncRParams(children.get(2));
            }
            AddFuncCall(func);
            String ret = DistributeVariable();
            if (node.isPushingParams()) {
                node.AdjustPushingParam(false);
            }
            if (!func.getType().equals("void")) {
                AddExp(ret, "RET");
                return ret;
            }
            return null;
        }
    }

    public void GenFuncRParams(Node node) {
        ArrayList<Node> children = node.getChildren();
        ArrayList<String> results = new ArrayList<>();
        for (Node item : children) {
            if (item.getContext().equals("<Exp>")) {
                item.AdjustPushingParam(true);
                String res = GenExp(item);
                results.add(res);
//                iCodes.add(new FuncPush(res));
                item.AdjustPushingParam(false);
            }
        }
        for (String res : results) {
            iCodes.add(new FuncPush(res));
        }
    }

    public String GenUnaryOp(Node node) {
        return node.getChildren().get(0).getContext();
    }

    public String GenPrimaryExp(Node node) {
        StringBuilder temp = new StringBuilder();
        ArrayList<Node> children = node.getChildren();
        switch (children.get(0).getContext()) {
            case "<LVal>":
                temp.append(GenLVal(children.get(0)));
                break;
            case "<Number>":
                temp.append(GenNumber(children.get(0)));
                break;
            case "(":
                temp.append(GenExp(children.get(1)));
                break;
        }
        String res = temp.toString();
        ArrayList<String> divided = DivideArrayString(res);
        VarSymbol varSymbol = FindVarSymbolByName(curTable, GetVarName(divided.get(0)), node.getLine());
        if (varSymbol != null) {
            if (varSymbol.getValue() != null) {
                return varSymbol.getValue();
            }
            if (varSymbol.getDimension() != 0) {
                return AddLoad(res, node.getLine(), node.isPushingParams(), varSymbol);
            }
        }
        return res;
    }

    public String GenNumber(Node node) {
        Node IntConst = node.getChildren().get(0);
        return IntConst.getContext();
    }

    public String GenLVal(Node node) {
        ArrayList<Node> children = node.getChildren();
        VarSymbol varSymbol = FindVarSymbolByName(curTable, children.get(0).getContext(),
                children.get(0).getLine());
        if (children.size() == 1) {
            return IdentifyVarLevel(varSymbol);
        } else {
            StringBuilder name = new StringBuilder(IdentifyVarLevel(varSymbol));
            for (Node item : children) {
                if (item.getContext().equals("[") || item.getContext().equals("]")) {
                    name.append(item.getContext());
                } else if (item.getContext().equals("<Exp>")) {
                    name.append(GenExp(item));
                }
            }
            return name.toString();
        }
    }

    public void OutputAllICode() {
        String path = "intern.txt";
        File file = new File(path);
        try (FileWriter writer = new FileWriter(file)) {
            for (ICode code : iCodes) {
                writer.write(code.toString() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ConstDecl AddConstDecl(String name, boolean isGlobal) {
        ConstDecl constDecl = new ConstDecl(name, ConstDecl.INT, isGlobal);
        iCodes.add(constDecl);
        return constDecl;
    }

    public ConstDecl AddConstDecl(String name, int strCount, boolean isGlobal) {
        ConstDecl constDecl = new ConstDecl(name, ConstDecl.STR, strCount, isGlobal);
        iCodes.add(constDecl);
        return constDecl;
    }


    public FuncDecl AddFuncDef(ArrayList<Node> children) {
        curTable = tableHead.getTableByName(children.get(1).getContext());
        String type = children.get(0).getChildren().get(0).getContext(), name = children.get(1).getContext();
        FuncDecl funcDecl = new FuncDecl(type, name);
        iCodes.add(funcDecl);
        return funcDecl;
    }

    public void AddMainFuncDef() {
        curTable = tableHead.getTableByName("main");
        String type = "int", name = "main";
        FuncDecl funcDecl = new FuncDecl(type, name);
        iCodes.add(funcDecl);
    }

    public FuncParam AddFuncParam(ArrayList<Node> children) {
        VarSymbol varSymbol = FindVarSymbolByName(curTable,
                children.get(1).getContext(), children.get(1).getLine());
        FuncParam funcParam = new FuncParam(varSymbol, IdentifyVarLevel(varSymbol));
        iCodes.add(funcParam);
        for (Node item : children) {
            if (item.getContext().equals("<ConstExp>")) {
                String res = GenConstExp(item);
                funcParam.setDim2(res);
            }
        }
        return funcParam;
    }

    public VarDecl AddVarDecl(VarSymbol symbol, String name) {
        VarDecl varDecl = new VarDecl(symbol, name, symbol.isGlobal());
        iCodes.add(varDecl);
        return varDecl;
    }

    public ArrayDecl AddArrayDecl(VarSymbol symbol) {
        ArrayDecl arrayDecl = new ArrayDecl(symbol, IdentifyVarLevel(symbol), symbol.isGlobal());
        iCodes.add(arrayDecl);
        return arrayDecl;
    }

    public VarSymbol FindVarSymbol(Node node) {
        return (VarSymbol) (curTable.findName(node.getContext()));
    }

    public String AddGet(String lVal) {
        String tempVar = DistributeVariable();
        Get get = new Get(lVal, tempVar);
        iCodes.add(get);
        return tempVar;
    }

    public String AddGet(String lVal, boolean isArray) {
        String tempVar = DistributeVariable();
        Get get = new Get(lVal, tempVar, isArray);
        iCodes.add(get);
        return tempVar;
    }

    public Exp AddExp(String operator, String lSym, String rSym_1, String rSym_2) {
        Exp exp = new Exp(operator, lSym, rSym_1, rSym_2);
        iCodes.add(exp);
        return exp;
    }

    public Exp AddExp(String operator, String lSym, String rSym) {
        Exp exp = new Exp(operator, lSym, rSym);
        iCodes.add(exp);
        return exp;
    }

    public Exp AddExp(String lSym, String rSym) {
        Exp exp = new Exp(lSym, rSym);
        iCodes.add(exp);
        return exp;
    }

    public void AddLabel(String name, boolean isCondLabel) {
        iCodes.add(new Label(name, isCondLabel));
    }

    public void AddPrintf(String name) {
        iCodes.add(new Printf(name));
    }

    public void AddFuncCall(FuncSymbol funcSymbol) {
        iCodes.add(new FuncCall(funcSymbol));
    }

    public void AddArrayLoad(String name, String target, boolean la) {
        iCodes.add(new ArrayLoad(name, target, la));
    }

    public void AddArrayLoad(String name, String target, String index, boolean la) {
        iCodes.add(new ArrayLoad(name, target, index, la));
    }

    public void AddArrayLoad(String name, String target, String index1, String index2, boolean la) {
        iCodes.add(new ArrayLoad(name, target, index1, index2, la));
    }

    public String AddLoad(String element, int line, boolean la, VarSymbol symbol) {
        ArrayList<String> division = DivideArrayString(element);
        String value = FindArrayElement(division, line);
        if (value != null) {
            return value;
        }
        String tempTarget = DistributeVariable();
        if (division.size() == 1) {
            AddArrayLoad(division.get(0), tempTarget, la);
        } else if (division.size() == 2) {
            if (symbol.getDimension() == 1) {
                AddArrayLoad(division.get(0), tempTarget, division.get(1), false);
            } else {
                AddArrayLoad(division.get(0), tempTarget, division.get(1), la);
            }
        } else if (division.size() == 3) {
            if (symbol.getDimension() == 2) {
                AddArrayLoad(division.get(0), tempTarget, division.get(1), division.get(2), false);
            } else {
                AddArrayLoad(division.get(0), tempTarget, division.get(1), division.get(2), la);
            }
        }
        return tempTarget;
    }

    public void AddArrayStore(String name, String source, String index) {
        iCodes.add(new ArrayStore(name, source, index));
    }

    public void AddArrayStore(String name, String source, String index1, String index2) {
        iCodes.add(new ArrayStore(name, source, index1, index2));
    }

    public void AddStore(String element, String source) {
        ArrayList<String> division = DivideArrayString(element);
        if (division.size() == 2) {
            AddArrayStore(division.get(0), source, division.get(1));
        } else {
            AddArrayStore(division.get(0), source, division.get(1), division.get(2));
        }
    }

    public void AddCmp(String source, String label, int type) {
        iCodes.add(new Cmp(source, label, type));
    }

    public void AddJump(String target, int type) {
        iCodes.add(new Jump(target, type));
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

    public String FindArrayElement(ArrayList<String> info, int line) {
        VarSymbol varSymbol = FindVarSymbolByName(curTable, GetVarName(info.get(0)), line);
        if (varSymbol.isConst()) {
            if (varSymbol.getDimension() == 1) {
                if (isDigit(info.get(1)) && Integer.parseInt(info.get(1)) < varSymbol.getInitVal().size()) {
                    return String.valueOf(varSymbol.getInitVal().get(Integer.parseInt(info.get(1))));
                }
            } else {
                String index1 = info.get(1), index2 = info.get(2);
                if (isDigit(index1) && isDigit(index2)) {
                    int index = Integer.parseInt(index1) * varSymbol.getSizeTwo() + Integer.parseInt(index2);
                    if (index < varSymbol.getInitVal().size()) {
                        return String.valueOf(varSymbol.getInitVal().get(index));
                    }
                }
            }
        }
        return null;
    }

    public VarSymbol FindVarSymbolByName(SymbolTable table, String name, int line) {
        if (table == null) {
            return null;
        } else {
            VarSymbol symbol = (VarSymbol) table.findName(name);
            if (symbol != null && line >= symbol.getLine()) {
                return (VarSymbol) table.findName(name);
            }
            return FindVarSymbolByName(table.getParent(), name, line);
        }
    }

    public String IdentifyVarLevel(VarSymbol var) {
        return var.getName() + "$" + var.getIdent();
    }

    public String DistributeVariable() {
        String str = "#t" + variableCount + "#";
        variableCount++;
        return str;
    }

    public String DistributeWhileLabel() {
        return "$WhileLabel_" + whileLabelCount + "$";
    }

    public String GetWhileLabel() {
        return "$WhileLabel_" + whileLabelCount + "$";
    }

    public String DistributeCondLabel(int condCnt, int type) {
        /* type is 0 --> While, type is 1 --> If, type is 2 --> Else */
        if (type == 0) {
            return "$WhileLabel_" + whileLabelCount + "_Cond_" + condCnt + "$";
        } else if (type == 1) {
            return "$IfLabel_" + ifLabelCount + "_Cond_" + condCnt + "$";
        } else {
            return "$ElseLabel_" + ifLabelCount + "_Cond_" + condCnt + "$";
        }
    }

    public String DistributeWhileLabelEnd() {
        return "$WhileLabel_End_" + whileLabelCount + "$";
    }

    public String GetWhileLabelEnd() {
        return "$WhileLabel_End_" + whileLabelCount + "$";
    }

    public String DistributeIfLabel() {
        return "$IfLabel_" + ifLabelCount + "$";
    }

    public String GetIfLabel() {
        return "$IfLabel_" + ifLabelCount + "$";
    }

    public String DistributeIfEndLabel() {
        return "$IfLabel_End_" + ifLabelCount + "$";
    }

    public String DistributeElseLabel() {
        return "$ElseLabel_" + ifLabelCount + "$";
    }

    public String DistributeElseEndLabel() {
        return "$ElseLabel_End_" + ifLabelCount + "$";
    }

    public String GetElseLabel() {
        return "$ElseLabel_" + ifLabelCount + "$";
    }

    public String GetBreakLabel(SymbolTable table) {
        if (table == null) {
            return "Error";
        } else {
            if (table.isWhile()) {
                return "$WhileLabel_End_" + table.getWhileCount() + "$";
            }
            return GetBreakLabel(table.getParent());
        }
    }

    public String GetContinueLabel(SymbolTable table) {
        if (table == null) {
            return "Error";
        } else {
            if (table.isWhile()) {
                return "$WhileLabel_" + table.getWhileCount() + "_Cond_0$";
            }
            return GetContinueLabel(table.getParent());
        }
    }

    public void TravelBlock(int index) {
        curTable = curTable.getChildren().get(index);
    }

    public void BackBlock() {
        curTable = curTable.getParent();
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

    public boolean isNote(String string) {
        return string.equals("+") || string.equals("-") || string.equals("*") ||
                string.equals("/") || string.equals("%") || string.equals("<") ||
                string.equals("<=") || string.equals(">") || string.equals(">=") ||
                string.equals("==") || string.equals("!=") || string.equals("!");
    }

    public boolean isVar(String string) {
        return string.contains("$");
    }

    public boolean isTempVar(String string) {
        return string.contains("#t");
    }

    public int CalculateTwo(String operator, int num1, int num2) {
        switch (operator) {
            case "+":
                return num1 + num2;
            case "-":
                return num1 - num2;
            case "*":
                return num1 * num2;
            case "/":
                return num1 / num2;
            case "%":
                return num1 % num2;
        }
        return 0;
    }

    public String GetVarName(String var) {
        return var.split("\\$")[0];
    }

    public ArrayList<ICode> getCodes() {
        return iCodes;
    }

}
