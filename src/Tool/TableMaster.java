package Tool;

import ClassFile.Error;
import ClassFile.FuncSymbol;
import ClassFile.Node;
import ClassFile.Symbol;
import ClassFile.SymbolTable;
import ClassFile.Token;
import ClassFile.VarSymbol;

import java.util.ArrayList;
import java.util.HashSet;

public class TableMaster {
    private final HashSet<SymbolTable> symbolTables = new HashSet<>();
    private final SymbolTable headTable;
    private final Node treeHead;
    private SymbolTable curTable;
    private boolean isWhile;


    public TableMaster(Node treeHead) {
        this.treeHead = treeHead;
        headTable = new SymbolTable(true, false);
        curTable = headTable;
        symbolTables.add(headTable);
    }

    public void Build() {
        CompUnit(treeHead);
    }

    public void CompUnit(Node node) {
        for (Node item : node.getChildren()) {
            switch (item.getContext()) {
                case "<Decl>":
                    Decl(item);
                    break;
                case "<FuncDef>":
                    FuncDef(item);
                    break;
                case "<MainFuncDef>":
                    MainFuncDef(item);
                    break;
            }
        }
    }

    public void Decl(Node node) {
        for (Node item : node.getChildren()) {
            switch (item.getContext()) {
                case "<ConstDecl>":
                    ConstDecl(item);
                    break;
                case "<VarDecl>":
                    VarDecl(item);
                    break;
            }
        }
    }

    public void FuncDef(Node node) {
        ArrayList<Node> children = node.getChildren();
        String type = FuncType(children.get(0));
        Token ident = Ident(children.get(1));
        FuncSymbol funcSymbol = new FuncSymbol(ident, type);
        if (curTable.addSymbol(funcSymbol)) {
            CreateTable(curTable);
            curTable.setTableName(ident.context);
            for (Node item : children) {
                if (item.getType() == 1) {
                    switch (item.getContext()) {
                        case "<FuncFParams>":
                            ArrayList<VarSymbol> symbols = FuncFParams(item);
                            funcSymbol.getVarSymbols().addAll(symbols);
                            break;
                        case "<Block>":
                            Block(item);
                            break;
                    }
                }
            }
            if (type.equals("void")) {
                Node stmtNode = SearchReturn(node);
                VoidReturn(stmtNode);
            } else if (type.equals("int")) {
                IntReturn(node);
            }
            BackTable();
        }
    }

    public void MainFuncDef(Node node) {
        CreateTable(curTable);
        curTable.setTableName("main");
        ArrayList<Node> children = node.getChildren();
        for (Node item : children) {
            if (item.getType() == 1 && item.getContext().equals("<Block>")) {
                Block(item);
            }
        }
        IntReturn(node);
        BackTable();
    }

    public void ConstDecl(Node node) {
        ArrayList<Node> children = node.getChildren();
        for (Node item : children) {
            if (item.getType() == 1 && item.getContext().equals("<ConstDef>")) {
//                System.out.println(symbol.getIdent() + " " + symbol.getName());
//                curTable.addSymbol(symbol);
                curTable.addSymbol(ConsDef(item));
            }
        }
    }

    public VarSymbol ConsDef(Node node) {
        ArrayList<Node> children = node.getChildren();
        Token ident = Ident(children.get(0));
        ArrayList<ArrayList<Integer>> initVal = new ArrayList<>();
        int dimension = 0;
        for (Node item : children) {
            if (item.getType() == 0) {
                if (item.getContext().equals("[")) {
                    dimension++;
                }
            } else if (item.getType() == 1) {
                if (item.getContext().equals("<ConstExp>")) {
                    ConstExp(item);
                } else if (item.getContext().equals("<ConstInitVal>")) {
                    ConstInitVal(item, 0, 0, initVal);
                }
            }
        }
        if (curTable.equals(headTable)) {
            return new VarSymbol(ident, true, dimension, initVal, true, ident.line);
        }
        return new VarSymbol(ident, true, dimension, initVal, false, ident.line);
    }

    public void ConstInitVal(Node node, int depth, int line, ArrayList<ArrayList<Integer>> array) {
        ArrayList<Node> children = node.getChildren();
        if (depth == 0) {
            int count = 0;
            for (Node item : children) {
                if (item.getType() == 1 && item.getContext().equals("<ConstExp>")) {
                    ArrayList<Integer> newLine = new ArrayList<>();
                    array.add(newLine);
                    newLine.add(ConstExp(item));
                } else if (item.getType() == 1 && item.getContext().equals("<ConstInitVal>")) {
                    ArrayList<Integer> newLine = new ArrayList<>();
                    array.add(newLine);
                    ConstInitVal(item, depth + 1, count, array);
                    count++;
                }
            }
        } else if (depth == 1) {
            for (Node item : children) {
                if (item.getType() == 1 && item.getContext().equals("<ConstExp>")) {
                    array.get(0).add(ConstExp(item));
                } else if (item.getType() == 1 && item.getContext().equals("<ConstInitVal>")) {
                    ConstInitVal(item, depth + 1, line, array);
                }
            }
        } else if (depth == 2) {
            for (Node item : children) {
                if (item.getType() == 1 && item.getContext().equals("<ConstExp>")) {
                    array.get(line).add(ConstExp(item));
                }
            }
        }
    }

    public void VarDecl(Node node) {
        ArrayList<Node> children = node.getChildren();
        for (Node item : children) {
            if (item.getType() == 1 && item.getContext().equals("<VarDef>")) {
                curTable.addSymbol(VarDef(item));
            }
        }
    }

    public VarSymbol VarDef(Node node) {
        ArrayList<Node> children = node.getChildren();
        Token ident = Ident(children.get(0));
        int dimension = 0;
        boolean hasInitValue = false;
        ArrayList<ArrayList<Integer>> initValue = new ArrayList<>();
        for (Node item : children) {
            if (item.getType() == 0) {
                if (item.getContext().equals("[")) {
                    dimension++;
                }
            } else if (item.getType() == 1) {
                if (item.getContext().equals("<ConstExp>")) {
                    ConstExp(item);
                } else if (item.getContext().equals("<InitVal>")) {
                    hasInitValue = true;
                    InitValue(item, 0, 0, initValue);
                }
            }
        }
        if (hasInitValue) {
            if (curTable.equals(headTable)) {
                return new VarSymbol(ident, false, dimension, initValue, true, ident.line);
            }
            return new VarSymbol(ident, false, dimension, initValue, false, ident.line);
        }
        if (curTable.equals(headTable)) {
            return new VarSymbol(ident, false, dimension, true, ident.line);
        }
        return new VarSymbol(ident, false, dimension, false, ident.line);
    }

    public void InitValue(Node node, int depth, int line, ArrayList<ArrayList<Integer>> array) {
        ArrayList<Node> children = node.getChildren();
        if (depth == 0) {
            int count = 0;
            for (Node item : children) {
                if (item.getType() == 1 && item.getContext().equals("<Exp>")) {
                    ArrayList<Symbol> symbols = new ArrayList<>(Exp(item));
                    for (Symbol symbol : symbols) {
                        if (symbol == null) {

                        }
                    }
                } else if (item.getType() == 1 && item.getContext().equals("<InitVal>")) {
                    ArrayList<Integer> newLine = new ArrayList<>();
                    array.add(newLine);
                    ConstInitVal(item, depth + 1, count, array);
                    count++;
                }
            }
        } else if (depth == 1) {
            for (Node item : children) {
//                if (item.getType() == 1 && item.getContext().equals("<Exp>")) {
//                    array.get(0).add(Exp(item));
//                } else
                if (item.getType() == 1 && item.getContext().equals("<InitVal>")) {
                    InitValue(item, depth + 1, line, array);
                }
            }
        } else if (depth == 2) {
//            for (Node item : children) {
//                if (item.getType() == 1 && item.getContext().equals("<Exp>")) {
//                    array.get(line).add(Exp(item));
//                }
//            }
        }
    }

    public ArrayList<Symbol> Exp(Node node) {
        return new ArrayList<>(AddExp(node.getChildren().get(0)));
    }

    public ArrayList<Symbol> AddExp(Node node) {
        ArrayList<Symbol> symbols = new ArrayList<>();
        ArrayList<Node> children = node.getChildren();
        for (Node item : children) {
            if (item.getType() == 1 & item.getContext().equals("<MulExp>")) {
                symbols.addAll(MulExp(item));
            }
        }
        return symbols;
    }

    public ArrayList<Symbol> MulExp(Node node) {
        ArrayList<Symbol> symbols = new ArrayList<>();
        ArrayList<Node> children = node.getChildren();
        for (Node item : children) {
            if (item.getType() == 1 & item.getContext().equals("<UnaryExp>")) {
                symbols.addAll(UnaryExp(item));
            }
        }
        return symbols;
    }

    public void UnaryOp(Node node) {

    }

    public ArrayList<Symbol> PrimaryExp(Node node) {
        ArrayList<Symbol> symbols = new ArrayList<>();
        ArrayList<Node> children = node.getChildren();
        switch (children.get(0).getContext()) {
            case "(":
                Exp(children.get(1));
                break;
            case "<LVal>":
                symbols.add(LVal(children.get(0)));
                break;
            case "<Number>":
                symbols.add(Number(children.get(0)));
                break;
        }
        return symbols;
    }

    public Symbol Number(Node node) {
        return new VarSymbol(Ident(node.getChildren().get(0)),
                false, 0, false, node.getChildren().get(0).getLine());
    }

    public ArrayList<Symbol> UnaryExp(Node node) {
//        node.printChildren();
        ArrayList<Symbol> symbols = new ArrayList<>();
        ArrayList<Node> children = node.getChildren();
        if (children.get(0).getType() == 1) {
            switch (children.get(0).getContext()) {
                case "<PrimaryExp>":
                    symbols.addAll(PrimaryExp(children.get(0)));
                    break;
                case "<UnaryOp>":
                    UnaryOp(children.get(0));
                    symbols.addAll(UnaryExp(children.get(1)));
                    break;
            }
        } else {
            Token ident = Ident(children.get(0));
            symbols.add(SearchUp(children.get(0), curTable));
            ArrayList<ArrayList<Symbol>> symbolList = new ArrayList<>();
            for (Node item : children) {
                if (item.getContext().equals("<FuncRParams>")) {
                    symbolList.addAll(FuncRParams(item));
                }
            }
            int result = SearchForFuncCall(ident.context, curTable, symbolList);
            if (result == 1) {
                Error.addErrorMessage(ident.line, "d");
            } else if (result == 2) {
                Error.addErrorMessage(ident.line, "e");
            } else if (result == 3) {
                Error.addErrorMessage(ident.line, "c");
            }
        }
        return symbols;
    }

    public ArrayList<ArrayList<Symbol>> FuncRParams(Node node) {
        ArrayList<Node> children = node.getChildren();
        ArrayList<ArrayList<Symbol>> symbolList = new ArrayList<>();
        for (Node item : children) {
            if (item.getType() == 1 && item.getContext().equals("<Exp>")) {
                ArrayList<Symbol> symbols = new ArrayList<>(Exp(item));
                symbolList.add(symbols);
            }
        }
        return symbolList;
    }

    public String FuncType(Node node) {
        return node.getChildren().get(0).getContext();
    }

    public ArrayList<VarSymbol> FuncFParams(Node node) {
        ArrayList<VarSymbol> params = new ArrayList<>();
        ArrayList<Node> children = node.getChildren();
        for (Node item : children) {
            if (item.getType() == 1) {
                if ("<FuncFParam>".equals(item.getContext())) {
                    VarSymbol varSymbol = FuncFParam(item);
                    curTable.addSymbol(varSymbol);
                    params.add(varSymbol);
                }
            }
        }
        return params;
    }

    public VarSymbol FuncFParam(Node node) {
        ArrayList<Node> children = node.getChildren();
//        printAllNode(children);
        Token ident = Ident(children.get(1));
        int dimension = 0;
        for (Node item : children) {
            if (item.getType() == 0) {
                if (item.getContext().equals("[")) {
                    dimension++;
                }
            } else if (item.getType() == 1) {
                if (item.getContext().equals("<ConstExp>")) {
                    ConstExp(item);
                }
            }
        }
        return new VarSymbol(ident, false, dimension, false, children.get(1).getLine());
    }

    public void Block(Node node) {
        ArrayList<Node> children = node.getChildren();
        for (Node item : children) {
            if (item.getType() == 1 && item.getContext().equals("<BlockItem>")) {
                BlockItem(item);
            }
        }
    }

    public Token Ident(Node node) {
        return node.getToken();
    }

    public int ConstExp(Node node) {
        return 0;
    }

    public void BlockItem(Node node) {
        ArrayList<Node> children = node.getChildren();
        for (Node item : children) {
            if (item.getType() == 1 && item.getContext().equals("<Decl>")) {
                Decl(item);
            } else if (item.getType() == 1 && item.getContext().equals("<Stmt>")) {
                Stmt(item);
            }
        }
    }

    public void Stmt(Node node) {
        ArrayList<Node> children = node.getChildren();
        switch (children.get(0).getContext()) {
            case "<LVal>":
                for (Node item : children) {
                    if (item.getType() == 1) {
                        if (item.getContext().equals("<LVal>")) {
                            Symbol symbol = SearchUp(item.getChildren().get(0), curTable);
                            if (symbol instanceof VarSymbol && ((VarSymbol) (symbol)).isConst()) {
                                Error.addErrorMessage(item.getChildren().get(0).getLine(), "h");
                            }
                            LVal(item);
                        } else if (item.getContext().equals("<Exp>")) {
                            Exp(item);
                        }
                    }
                }
                break;
            case "<Exp>":
                Exp(children.get(0));
                break;
            case "if":
                for (Node item : children) {
                    if (item.getType() == 1) {
                        if (node.getContext().equals("<Cond>")) {
                            Cond(item);
                        } else if (node.getContext().equals("<Stmt>")) {
                            Stmt(item);
                        }
                    }
                }
                break;
            case "<Block>":
                CreateTable(curTable);
                Block(children.get(0));
                BackTable();
                break;
            case "while":
                isWhile = true;
                for (Node item : children) {
                    if (item.getType() == 1) {
                        if (item.getContext().equals("<Cond>")) {
                            Cond(item);
                        } else if (item.getContext().equals("<Stmt>")) {
                            Stmt(item);
                        }
                    }
                }
                break;
            case "break":
                if (SearchWhile(curTable) == null) {
                    Error.addErrorMessage(children.get(0).getLine(), "m");
                }
            case "continue":
                if (SearchWhile(curTable) == null) {
                    Error.addErrorMessage(children.get(0).getLine(), "m");
                }
                break;
            case "return":
                for (Node item : children) {
                    if (item.getType() == 1) {
                        if (node.getContext().equals("<Exp>")) {
                            Exp(item);
                        }
                    }
                }
                break;
            case "printf":
                Node strNode = children.get(2);
                int expCnt = 0, posNote = 0;
                for (Node item : children) {
                    if (item.getContext().equals("<Exp>")) {
                        expCnt++;
                    }
                }
                int len = strNode.getContext().length();
                for (int i = 0; i < len - 1; i++) {
                    if (strNode.getContext().charAt(i) == '%' && strNode.getContext().charAt(i + 1) == 'd') {
                        posNote++;
                    }
                }
                if (expCnt != posNote) {
                    Error.addErrorMessage(children.get(0).getLine(), "l");
                }
                for (Node item : children) {
                    if (item.getContext().equals("<Exp>")) {
                        Exp(item);
                    }
                }
                break;
        }
    }

    public Symbol LVal(Node node) {
        ArrayList<Node> children = node.getChildren();
        Node identNode = children.get(0);
        Symbol originSymbol = SearchUp(identNode, curTable);
        if (originSymbol == null) {
            Error.addErrorMessage(identNode.getLine(), "c");
        }
        if (originSymbol instanceof VarSymbol) {
            int dimension = ((VarSymbol) (originSymbol)).getDimension();
            for (Node item : children) {
                if (item.getContext().equals("[")) {
                    dimension--;
                } else if (item.getContext().equals("<Exp>")) {
                    Exp(item);
                }
            }
            if (curTable.equals(headTable)) {
                return new VarSymbol(identNode.getToken(), false, dimension, true, children.get(0).getLine());
            }
            return new VarSymbol(identNode.getToken(), false, dimension, false, children.get(0).getLine());
        }
        return originSymbol;
    }

    public ArrayList<Symbol> Cond(Node node) {
        return LOrExp(node.getChildren().get(0));
    }

    public ArrayList<Symbol> LOrExp(Node node) {
        ArrayList<Node> children = node.getChildren();
        ArrayList<Symbol> symbols = new ArrayList<>();
        for (Node item : children) {
            if (item.getType() == 1 && item.getContext().equals("<LAndExp>")) {
                symbols.addAll(LAndExp(item));
            }
        }
        return symbols;
    }

    public ArrayList<Symbol> LAndExp(Node node) {
        ArrayList<Node> children = node.getChildren();
        ArrayList<Symbol> symbols = new ArrayList<>();
        for (Node item : children) {
            if (item.getType() == 1 && item.getContext().equals("<EqExp>")) {
                symbols.addAll(EqExp(item));
            }
        }
        return symbols;
    }

    public ArrayList<Symbol> EqExp(Node node) {
        ArrayList<Node> children = node.getChildren();
        ArrayList<Symbol> symbols = new ArrayList<>();
        for (Node item : children) {
            if (item.getType() == 1 && item.getContext().equals("<RelExp>")) {
                symbols.addAll(RelExp(item));
            }
        }
        return symbols;
    }

    public ArrayList<Symbol> RelExp(Node node) {
        ArrayList<Node> children = node.getChildren();
        ArrayList<Symbol> symbols = new ArrayList<>();
        for (Node item : children) {
            if (item.getType() == 1 && item.getContext().equals("<AddExp>")) {
                symbols.addAll(AddExp(item));
            }
        }
        return symbols;
    }

    public Symbol SearchUp(Node node, SymbolTable table) {
        if (table == null) {
            return null;
        } else {
            Symbol res = table.findName(node.getContext());
            if (res != null) {
                return res;
            }
            return SearchUp(node, table.getParent());
        }
    }

    public SymbolTable SearchWhile(SymbolTable table) {
        if (table == null) {
            return null;
        } else {
            if (table.isWhile()) {
                return table;
            }
            return SearchWhile(table.getParent());
        }
    }

    public Node SearchReturn(Node node) {
        if (node.getType() == 0 && node.getContext().equals("return")) {
            return node.getParent();
        } else {
            for (Node item : node.getChildren()) {
                Node res = SearchReturn(item);
                if (res != null) {
                    return res;
                }
            }
        }
        return null;
    }

    public int SearchForFuncCall(String name, SymbolTable table, ArrayList<ArrayList<Symbol>> params) {
        if (table == null) {
            /* No function found return type 3 */
            return 3;
        } else {
            if (table.findName(name) != null) {
                /* Function params size not match return type 1 */
                /* Function params type not match return type 2 */
                int res = table.matchFunc(name, params);
                if (res != 3) {
                    return res;
                }
            }
            return SearchForFuncCall(name, table.getParent(), params);
        }
    }

    public void VoidReturn(Node stmt) {
        if (stmt == null) {
            return;
        }
        for (Node item : stmt.getChildren()) {
            if (item.getType() == 1 && item.getContext().equals("<Exp>")) {
                Error.addErrorMessage(stmt.getChildren().get(0).getLine(), "f");
                return;
            }
        }
    }

    public void IntReturn(Node funcDef) {
        Node block = funcDef.getChildren().get(funcDef.getChildren().size() - 1);
        Node blockItem = null;
        Token rBrace = Ident(block.getChildren().get(block.getChildren().size() - 1));
        for (Node item : block.getChildren()) {
            if (item.getContext().equals("<BlockItem>")) {
                blockItem = item;
            }
        }
        if (blockItem == null) {
            Error.addErrorMessage(rBrace.line, "g");
            return;
        }
        Node stmt = blockItem.getChildren().get(0);
        for (Node node : stmt.getChildren()) {
            if (node.getType() == 1 && node.getContext().equals("<Exp>")) {
                return;
            }
        }
        Error.addErrorMessage(rBrace.line, "g");
    }

    public void CreateTable(SymbolTable parent) {
        SymbolTable table = new SymbolTable(false, isWhile);
        if (isWhile) {
            isWhile = false;
        }
        symbolTables.add(table);
        curTable.addChild(table);
        table.addParent(parent);
        curTable = table;
    }

    public void BackTable() {
        curTable = curTable.getParent();
    }

    public SymbolTable getHeadTable() {
        return headTable;
    }

    public void printAllTables() {
        System.out.println("The number of table is : " + symbolTables.size());
        headTable.printSymbols();
    }

    public void printAllNode(ArrayList<Node> nodes) {
        for (Node node : nodes) {
            System.out.println(node.getContext());
        }
    }
}
