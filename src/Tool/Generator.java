package Tool;

import ClassFile.FuncSymbol;
import ClassFile.InterCode.ArrayDecl;
import ClassFile.InterCode.ArrayLoad;
import ClassFile.InterCode.ArrayStore;
import ClassFile.InterCode.ConstDecl;
import ClassFile.InterCode.Exp;
import ClassFile.InterCode.FuncCall;
import ClassFile.InterCode.FuncDecl;
import ClassFile.InterCode.FuncParam;
import ClassFile.InterCode.FuncPush;
import ClassFile.InterCode.Get;
import ClassFile.InterCode.ICode;
import ClassFile.InterCode.Label;
import ClassFile.InterCode.Printf;
import ClassFile.InterCode.VarDecl;
import ClassFile.Node;
import ClassFile.SymbolTable;
import ClassFile.VarSymbol;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Generator {
    private final Node treeHead;
    private final SymbolTable tableHead;
    private Node curTree;
    private SymbolTable curTable;
    private final ArrayList<ICode> iCodes = new ArrayList<>();
    private int variableCount = 0;
    private int strCount = 0;

    public Generator(Node head, SymbolTable table) {
        treeHead = head;
        tableHead = table;
        curTable = tableHead;
        curTree = treeHead;
    }

    public void Generate() {
        AddLabel("Init");
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
        AddLabel(children.get(1).getContext());
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
        AddLabel(children.get(1).getContext());
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
        } else if (symbol.getDimension() == 1) {
            String DimOne = GenConstExp(children.get(2));
            ArrayDecl arrayDecl = AddArrayDecl(symbol);
            arrayDecl.setSym1(DimOne);
            GenConstInitValForArrayDimOne(children.get(5), symbol);
        } else if (symbol.getDimension() == 2) {
            String DimOne = GenConstExp(children.get(2));
            String DimTwo = GenConstExp(children.get(5));
            ArrayDecl arrayDecl = AddArrayDecl(symbol);
            arrayDecl.setSym1(DimOne);
            arrayDecl.setSym2(DimTwo);
            GenConstInitValForArrayDimTwo(children.get(8), symbol);
        }
    }

    public String GenConstInitValForVar(Node node) {
        ArrayList<Node> children = node.getChildren();
        return GenConstExp(children.get(0));
    }

    public void GenConstInitValForArrayDimOne(Node node, VarSymbol array) {
        ArrayList<Node> children = node.getChildren();
        for (Node item : children) {
            if (item.getContext().equals("<ConstInitVal>")) {
                String val = GenConstInitValForVar(item);
            }
        }
    }

    public void GenConstInitValForArrayDimTwo(Node node, VarSymbol array) {
        ArrayList<Node> children = node.getChildren();
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
            arrayDecl.setSym1(DimOne);
        } else if (symbol.getDimension() == 2) {
            String DimOne = GenConstExp(children.get(2));
            String DimTwo = GenConstExp(children.get(5));
            ArrayDecl arrayDecl = AddArrayDecl(symbol);
            arrayDecl.setSym1(DimOne);
            arrayDecl.setSym2(DimTwo);
        }
    }

    public String GenInitValForVar(Node node) {
        return GenExp(node.getChildren().get(0));
    }

    public void GenStmt(Node node) {
        ArrayList<Node> children = node.getChildren();
        Node firstNode = children.get(0);
        switch (firstNode.getContext()) {
            case "<LVal>":
                /*TODO:lVal is what*/
                String lVal = GenLVal(firstNode);
                if (children.get(2).getContext().equals("getint")) {
                    Get get = AddGet(lVal);
                } else if (children.get(2).getContext().equals("<Exp>")) {
                    String rSym = GenExp(children.get(2));
                    AddExp(lVal, rSym);
                }
                break;
            case "<Exp>":
                GenExp(firstNode);
                break;
            case "<Block>":
                curTable = curTable.GetNextChild();
                GenBlock(firstNode);
                break;
            case "<if>":
                break;
            case "<while>":
                break;
            case "<break>":
                break;
            case "return":
                if (children.size() == 2) {
                    break;
                } else {
                    String exp = GenExp(children.get(1));
                    AddExp("RET", exp);
                }
                break;
            case "printf":
                ArrayList<Node> exps = new ArrayList<>();
                int ExpCount = 0;
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
                for (String str : slices) {
                    if (!str.equals("%d")) {
                        ConstDecl ConstStr = AddConstDecl(str, strCount++, true);
                        AddPrintf(ConstStr.GetLSym());
                    } else {
                        String tempVar = GenExp(exps.get(ExpCount++));
                        AddPrintf(tempVar);
                    }
                }
                break;
        }
    }

    public String GenExp(Node node) {
        return GenAddExp(node.getChildren().get(0));
    }

    public String GenAddExp(Node node) {
        ArrayList<Node> children = node.getChildren();
        if (children.size() == 1) {
            return GenMulExp(children.get(0));
        }
        String tempVar = "", symA = "", symB, note = "";
        boolean isFirst = false;
        for (Node item : children) {
            if (item.getContext().equals("<MulExp>")) {
                if (!isFirst) {
                    symA = GenMulExp(item);
                    isFirst = true;
                    continue;
                }
                symB = GenMulExp(item);
                tempVar = DistributeVariable();
                Exp exp = AddExp(note, tempVar, symA, symB);
                symA = tempVar;
            } else {
                note = item.getContext();
            }
        }
        return tempVar;
    }

    public String GenMulExp(Node node) {
        ArrayList<Node> children = node.getChildren();
        if (children.size() == 1) {
            return GenUnaryExp(children.get(0));
        }
        String tempVar = "", symA = "", symB, note = "";
        boolean isFirst = false;
        for (Node item : children) {
            if (item.getContext().equals("<UnaryExp>")) {
                if (!isFirst) {
                    symA = GenUnaryExp(item);
                    isFirst = true;
                    continue;
                }
                symB = GenUnaryExp(item);
                tempVar = DistributeVariable();
                Exp exp = AddExp(note, tempVar, symA, symB);
                symA = tempVar;
            } else {
                note = item.getContext();
            }
        }
        return tempVar;
    }

    public String GenUnaryExp(Node node) {
        ArrayList<Node> children = node.getChildren();
        Node firstNode = children.get(0);
        if (children.size() == 1) {
            return GenPrimaryExp(firstNode);
        }
        if ("<UnaryOp>".equals(firstNode.getContext())) {
            String lSym = DistributeVariable();
            String note = GenUnaryOp(firstNode);
            String rSym = GenUnaryExp(children.get(1));
            AddExp(note, lSym, rSym);
            return lSym;
        } else {
            FuncSymbol func = (FuncSymbol) tableHead.findName(firstNode.getContext());
            if (children.get(2).getContext().equals("<FuncRParams>")) {
                GenFuncRParams(children.get(2));
            }
            AddFuncCall(func.getName());
            String ret = DistributeVariable();
            if (!func.getType().equals("void")) {
                AddExp(ret, "RET");
                return ret;
            }
            return null;
        }
    }

    public void GenFuncRParams(Node node) {
        ArrayList<Node> children = node.getChildren();
        for (Node item : children) {
            if (item.getContext().equals("<Exp>")) {
                String res = GenExp(item);
                iCodes.add(new FuncPush(res));
            }
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
        return temp.toString();
    }

    public String GenNumber(Node node) {
        Node IntConst = node.getChildren().get(0);
        return IntConst.getContext();
    }

    public String GenLVal(Node node) {
        ArrayList<Node> children = node.getChildren();
//        System.out.println(children.get(0).getContext());
        if (children.size() == 1) {
            VarSymbol varSymbol = FindVarSymbolByName(curTable, children.get(0).getContext(),
                    children.get(0).getLine());
//            System.out.println(varSymbol.getIdent() + varSymbol.getName());
            return IdentifyVarLevel(varSymbol);
        }
        /*TODO:Add Array*/
        return "LVal";
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
        return funcParam;
    }

    public VarDecl AddVarDecl(VarSymbol symbol, String name) {
        VarDecl varDecl = new VarDecl(symbol, name, symbol.isGlobal());
        iCodes.add(varDecl);
        return varDecl;
    }

    public ArrayDecl AddArrayDecl(VarSymbol symbol) {
        ArrayDecl arrayDecl = new ArrayDecl(symbol, IdentifyVarLevel(symbol));
        iCodes.add(arrayDecl);
        return arrayDecl;
    }

    public VarSymbol FindVarSymbol(Node node) {
        return (VarSymbol) (curTable.findName(node.getContext()));
    }

    public Get AddGet(String lVal) {
        Get get = new Get(lVal, DistributeVariable());
        iCodes.add(get);
        return get;
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

    public void AddLabel(String name) {
        iCodes.add(new Label(name));
    }

    public void AddPrintf(String name) {
        iCodes.add(new Printf(name));
    }

    public void AddFuncCall(String name) {
        iCodes.add(new FuncCall(name));
    }

    public ArrayLoad AddArrayLoad() {
        return new ArrayLoad();
    }

    public ArrayStore AddArrayStore() {
        return new ArrayStore();
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

    public void TravelBlock(int index) {
        curTable = curTable.getChildren().get(index);
    }

    public void BackBlock() {
        curTable = curTable.getParent();
    }

    public ArrayList<ICode> getCodes() {
        return iCodes;
    }
}
