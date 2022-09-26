package Tool;

import ClassFile.CategoryCode;
import ClassFile.Node;
import ClassFile.Token;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Parser {
    private final ArrayList<Token> tokens;
    private final ArrayList<Node> nodes = new ArrayList<>();
    private Token curToken;
    private int tokenPtr;

    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        curToken = tokens.get(0);
        tokenPtr = 0;
    }

    public void exceptionOccurred() {

    }

    public void getToken() {
        Node tempNode = new Node(tokens.get(tokenPtr));
        nodes.add(tempNode);
        if (tokenPtr < tokens.size() - 1) {
            tokenPtr++;
            this.curToken = tokens.get(tokenPtr);
        }
    }

    public void addNode(String context) {
        Node tempNode = new Node(context);
        nodes.add(tempNode);
//        try {
//            Thread.sleep(1);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println(context + curToken.context + tokens.get(tokenPtr - 1).context + " " + tokenPtr);
    }

    public void parse() {
        CompUnit();
    }

    public void CompUnit() {
        while (curToken.type.equals(CategoryCode.CONST) || (curToken.type.equals(CategoryCode.INT) &&
                tokens.get(tokenPtr + 1).type.equals(CategoryCode.IDENT) && (
                tokens.get(tokenPtr + 2).type.equals(CategoryCode.LBRACK) ||
                        tokens.get(tokenPtr + 2).type.equals(CategoryCode.ASSIGN) ||
                        tokens.get(tokenPtr + 2).type.equals(CategoryCode.COMMA) ||
                        tokens.get(tokenPtr + 2).type.equals(CategoryCode.SEMICN)))) {
            Decl();
        }
        while ((curToken.type.equals(CategoryCode.VOID) || curToken.type.equals(CategoryCode.INT)) &&
                tokens.get(tokenPtr + 1).type.equals(CategoryCode.IDENT) &&
                tokens.get(tokenPtr + 2).type.equals(CategoryCode.LPARENT)) {
            FuncDef();
        }
        if (curToken.type.equals(CategoryCode.INT) && tokens.get(tokenPtr + 1).type.equals(CategoryCode.MAIN)) {
            MainFuncDef();
        } else {
            exceptionOccurred();
        }
        addNode("<CompUnit>");
    }

    public void Decl() {
        if (curToken.type.equals(CategoryCode.CONST)) {
            ConstDecl();
        } else if (curToken.type.equals(CategoryCode.INT)) {
            VarDecl();
        } else {
            exceptionOccurred();
        }
    }

    public void FuncDef() {
        FuncType();
        if (curToken.type.equals(CategoryCode.IDENT)) {
            getToken();
            if (curToken.type.equals(CategoryCode.LPARENT)) {
                getToken();
                if (curToken.type.equals(CategoryCode.RPARENT)) {
                    getToken();
                    Block();
                } else {
                    FuncFParams();
                    if (curToken.type.equals(CategoryCode.RPARENT)) {
                        getToken();
                        Block();
                    } else {
                        exceptionOccurred();
                    }
                }
            } else {
                exceptionOccurred();
            }
        } else {
            exceptionOccurred();
        }
        addNode("<FuncDef>");
    }

    public void MainFuncDef() {
        if (curToken.type.equals(CategoryCode.INT)) {
            getToken();
            if (curToken.type.equals(CategoryCode.MAIN)) {
                getToken();
                if (curToken.type.equals(CategoryCode.LPARENT)) {
                    getToken();
                    if (curToken.type.equals(CategoryCode.RPARENT)) {
                        getToken();
                        Block();
                    } else {
                        exceptionOccurred();
                    }
                } else {
                    exceptionOccurred();
                }
            } else {
                exceptionOccurred();
            }
        } else {
            exceptionOccurred();
        }
        addNode("<MainFuncDef>");
    }

    public void ConstDecl() {
        if (curToken.type.equals(CategoryCode.CONST)) {
            getToken();
            BType();
            ConstDef();
            while (curToken.type.equals(CategoryCode.COMMA)) {
                getToken();
                ConstDef();
            }
            if (curToken.type.equals(CategoryCode.SEMICN)) {
                getToken();
            } else {
                exceptionOccurred();
            }
        } else {
            exceptionOccurred();
        }
        addNode("<ConstDecl>");
    }

    public void VarDecl() {
        BType();
        VarDef();
        while (curToken.type.equals(CategoryCode.COMMA)) {
            getToken();
            VarDef();
        }
        if (curToken.type.equals(CategoryCode.SEMICN)) {
            getToken();
        } else {
            exceptionOccurred();
        }
        addNode("<VarDecl>");
    }

    public void FuncType() {
        if (curToken.type.equals(CategoryCode.INT) || curToken.type.equals(CategoryCode.VOID)) {
            getToken();
        } else {
            exceptionOccurred();
        }
        addNode("<FuncType>");
    }

    public void FuncFParams() {
        FuncFParam();
        while (curToken.type.equals(CategoryCode.COMMA)) {
            getToken();
            FuncFParam();
        }
        addNode("<FuncFParams>");
    }

    public void Block() {
        if (curToken.type.equals(CategoryCode.LBRACE)) {
            getToken();
            if (curToken.type.equals(CategoryCode.RBRACE)) {
                getToken();
            } else {
                while (!curToken.type.equals(CategoryCode.RBRACE)) {
                    BlockItem();
                }
                getToken();
            }
        } else {
            exceptionOccurred();
        }
        addNode("<Block>");
    }

    public void BType() {
        if (curToken.type.equals(CategoryCode.INT)) {
            getToken();
        } else {
            exceptionOccurred();
        }
    }

    public void ConstDef() {
        if (curToken.type.equals(CategoryCode.IDENT)) {
            getToken();
            while (curToken.type.equals(CategoryCode.LBRACK)) {
                getToken();
                ConstExp();
                if (curToken.type.equals(CategoryCode.RBRACK)) {
                    getToken();
                } else {
                    exceptionOccurred();
                }
            }
            if (curToken.type.equals(CategoryCode.ASSIGN)) {
                getToken();
                ConstInitVal();
            } else {
                exceptionOccurred();
            }
        } else {
            exceptionOccurred();
        }
        addNode("<ConstDef>");
    }

    public void VarDef() {
        if (curToken.type.equals(CategoryCode.IDENT)) {
            getToken();
            while (curToken.type.equals(CategoryCode.LBRACK)) {
                getToken();
                ConstExp();
                if (curToken.type.equals(CategoryCode.RBRACK)) {
                    getToken();
                } else {
                    exceptionOccurred();
                }
            }
            if (curToken.type.equals(CategoryCode.ASSIGN)) {
                getToken();
                InitVal();
            }
        } else {
            exceptionOccurred();
        }
        addNode("<VarDef>");
    }

    public void FuncFParam() {
        BType();
        if (curToken.type.equals(CategoryCode.IDENT)) {
            getToken();
            if (curToken.type.equals(CategoryCode.LBRACK)) {
                getToken();
                if (curToken.type.equals(CategoryCode.RBRACK)) {
                    getToken();
                    while (curToken.type.equals(CategoryCode.LBRACK)) {
                        getToken();
                        ConstExp();
                        if (curToken.type.equals(CategoryCode.RBRACK)) {
                            getToken();
                        } else {
                            exceptionOccurred();
                        }
                    }
                } else {
                    exceptionOccurred();
                }
            }
        } else {
            exceptionOccurred();
        }
        addNode("<FuncFParam>");
    }

    public void BlockItem() {
        if (curToken.type.equals(CategoryCode.CONST) || curToken.type.equals(CategoryCode.INT)) {
            Decl();
        } else if (curToken.type.equals(CategoryCode.IDENT) || curToken.type.equals(CategoryCode.LPARENT) ||
                curToken.type.equals(CategoryCode.INTCONST) || curToken.type.equals(CategoryCode.PLUS) ||
                curToken.type.equals(CategoryCode.MINU) || curToken.type.equals(CategoryCode.NOT) ||
                curToken.type.equals(CategoryCode.SEMICN) || curToken.type.equals(CategoryCode.LBRACE) ||
                curToken.type.equals(CategoryCode.IF) || curToken.type.equals(CategoryCode.WHILE) ||
                curToken.type.equals(CategoryCode.BREAK) || curToken.type.equals(CategoryCode.CONTINUE) ||
                curToken.type.equals(CategoryCode.RETURN) || curToken.type.equals(CategoryCode.PRINTF)) {
            Stmt();
        }
    }

    public void ConstExp() {
        AddExp();
        addNode("<ConstExp>");
    }

    public void ConstInitVal() {
        if (curToken.type.equals(CategoryCode.LBRACE)) {
            getToken();
            if (curToken.type.equals(CategoryCode.LBRACE) || curToken.type.equals(CategoryCode.IDENT) ||
                    curToken.type.equals(CategoryCode.LPARENT) || curToken.type.equals(CategoryCode.PLUS) ||
                    curToken.type.equals(CategoryCode.MINU) || curToken.type.equals(CategoryCode.NOT) ||
                    curToken.type.equals(CategoryCode.INTCONST)) {
                ConstInitVal();
                while (curToken.type.equals(CategoryCode.COMMA)) {
                    getToken();
                    ConstInitVal();
                }
                if (curToken.type.equals(CategoryCode.RBRACE)) {
                    getToken();
                }
            } else if (curToken.type.equals(CategoryCode.RBRACE)) {
                getToken();
            } else {
                exceptionOccurred();
            }
        } else {
            ConstExp();
        }
        addNode("<ConstInitVal>");
    }

    public void InitVal() {
        if (curToken.type.equals(CategoryCode.LBRACE)) {
            getToken();
            if (curToken.type.equals(CategoryCode.RBRACE)) {
                getToken();
            } else if (curToken.type.equals(CategoryCode.LPARENT) || curToken.type.equals(CategoryCode.IDENT) ||
                    curToken.type.equals(CategoryCode.INTCONST) || curToken.type.equals(CategoryCode.PLUS) ||
                    curToken.type.equals(CategoryCode.MINU) || curToken.type.equals(CategoryCode.NOT) ||
                    curToken.type.equals(CategoryCode.LBRACE)) {
                InitVal();
                while (curToken.type.equals(CategoryCode.COMMA)) {
                    getToken();
                    InitVal();
                }
                if (curToken.type.equals(CategoryCode.RBRACE)) {
                    getToken();
                } else {
                    exceptionOccurred();
                }
            }
        } else if (curToken.type.equals(CategoryCode.IDENT) || curToken.type.equals(CategoryCode.LPARENT) ||
                curToken.type.equals(CategoryCode.PLUS) || curToken.type.equals(CategoryCode.MINU) ||
                curToken.type.equals(CategoryCode.NOT) || curToken.type.equals(CategoryCode.INTCONST)) {
            Exp();
        }
        addNode("<InitVal>");
    }

    public void Stmt() {
        if (curToken.type.equals(CategoryCode.PRINTF)) {
            getToken();
            if (curToken.type.equals(CategoryCode.LPARENT)) {
                getToken();
                FormatString();
                while (curToken.type.equals(CategoryCode.COMMA)) {
                    getToken();
                    Exp();
                }
                if (curToken.type.equals(CategoryCode.RPARENT)) {
                    getToken();
                    if (curToken.type.equals(CategoryCode.SEMICN)) {
                        getToken();
                    } else {
                        exceptionOccurred();
                    }
                } else {
                    exceptionOccurred();
                }
            } else {
                exceptionOccurred();
            }
        } else if (curToken.type.equals(CategoryCode.RETURN)) {
            getToken();
            if (curToken.type.equals(CategoryCode.SEMICN)) {
                getToken();
            } else if (curToken.type.equals(CategoryCode.LPARENT) || curToken.type.equals(CategoryCode.IDENT) ||
                    curToken.type.equals(CategoryCode.INTCONST) || curToken.type.equals(CategoryCode.PLUS) ||
                    curToken.type.equals(CategoryCode.MINU) || curToken.type.equals(CategoryCode.NOT)) {
                Exp();
                if (curToken.type.equals(CategoryCode.SEMICN)) {
                    getToken();
                } else {
                    exceptionOccurred();
                }
            }
        } else if (curToken.type.equals(CategoryCode.BREAK) || curToken.type.equals(CategoryCode.CONTINUE)) {
            getToken();
            if (curToken.type.equals(CategoryCode.SEMICN)) {
                getToken();
            } else {
                exceptionOccurred();
            }
        } else if (curToken.type.equals(CategoryCode.WHILE)) {
            getToken();
            if (curToken.type.equals(CategoryCode.LPARENT)) {
                getToken();
                Cond();
                if (curToken.type.equals(CategoryCode.RPARENT)) {
                    getToken();
                    Stmt();
                } else {
                    exceptionOccurred();
                }
            } else {
                exceptionOccurred();
            }
        } else if (curToken.type.equals(CategoryCode.IF)) {
            getToken();
            if (curToken.type.equals(CategoryCode.LPARENT)) {
                getToken();
                Cond();
                if (curToken.type.equals(CategoryCode.RPARENT)) {
                    getToken();
                    Stmt();
                } else {
                    exceptionOccurred();
                }
                if (curToken.type.equals(CategoryCode.ELSE)) {
                    getToken();
                    Stmt();
                }
            } else {
                exceptionOccurred();
            }
        } else if (curToken.type.equals(CategoryCode.LBRACE)) {
            Block();
        } else {
            int i = 0, flag = 0;
            while (tokenPtr + i < tokens.size()) {
                if (tokens.get(tokenPtr + i).type.equals(CategoryCode.ASSIGN)) {
                    flag = 1;
                    break;
                } else if (tokens.get(tokenPtr + i).type.equals(CategoryCode.SEMICN)) {
                    flag = 2;
                    break;
                }
                i++;
            }
            if (flag == 1) {
                LVal();
                if (curToken.type.equals(CategoryCode.ASSIGN)) {
                    getToken();
                    if (curToken.type.equals(CategoryCode.GETINT)) {
                        getToken();
                        if (curToken.type.equals(CategoryCode.LPARENT)) {
                            getToken();
                            if (curToken.type.equals(CategoryCode.RPARENT)) {
                                getToken();
                                if (curToken.type.equals(CategoryCode.SEMICN)) {
                                    getToken();
                                } else {
                                    exceptionOccurred();
                                }
                            } else {
                                exceptionOccurred();
                            }
                        } else {
                            exceptionOccurred();
                        }
                    } else {
                        Exp();
                        if (curToken.type.equals(CategoryCode.SEMICN)) {
                            getToken();
                        } else {
                            exceptionOccurred();
                        }
                    }
                } else {
                    exceptionOccurred();
                }
            } else if (flag == 2) {
                if (curToken.type.equals(CategoryCode.LPARENT) || curToken.type.equals(CategoryCode.IDENT) ||
                        curToken.type.equals(CategoryCode.INTCONST) || curToken.type.equals(CategoryCode.PLUS) ||
                        curToken.type.equals(CategoryCode.MINU) || curToken.type.equals(CategoryCode.NOT)) {
                    Exp();
                }
                if (curToken.type.equals(CategoryCode.SEMICN)) {
                    getToken();
                } else {
                    exceptionOccurred();
                }
            } else {
                exceptionOccurred();
            }
        }
        addNode("<Stmt>");
    }

    public void AddExp() {
        MulExp();
        addNode("<AddExp>");
        while (curToken.type.equals(CategoryCode.PLUS) || curToken.type.equals(CategoryCode.MINU)) {
            getToken();
            MulExp();
            addNode("<AddExp>");
        }
    }

    public void Exp() {
        AddExp();
        addNode("<Exp>");
    }

    public void Cond() {
        LOrExp();
        addNode("<Cond>");
    }

    public void LVal() {
        if (curToken.type.equals(CategoryCode.IDENT)) {
            getToken();
            while (curToken.type.equals(CategoryCode.LBRACK)) {
                getToken();
                Exp();
                if (curToken.type.equals(CategoryCode.RBRACK)) {
                    getToken();
                } else {
                    exceptionOccurred();
                }
            }
        }
        addNode("<LVal>");
    }

    public void LOrExp() {
        LAndExp();
        addNode("<LOrExp>");
        while (curToken.type.equals(CategoryCode.OR)) {
            getToken();
            LAndExp();
            addNode("<LOrExp>");
        }
    }

    public void FormatString() {
        if (curToken.type.equals(CategoryCode.FORMATSTRING)) {
            getToken();
        } else {
            exceptionOccurred();
        }
    }

    public void MulExp() {
        UnaryExp();
        addNode("<MulExp>");
        while (curToken.type.equals(CategoryCode.MULT) || curToken.type.equals(CategoryCode.DIV) ||
                curToken.type.equals(CategoryCode.MOD)) {
            getToken();
            UnaryExp();
            addNode("<MulExp>");
        }
    }

    public void LAndExp() {
        EqExp();
        addNode("<LAndExp>");
        while (curToken.type.equals(CategoryCode.AND)) {
            getToken();
            EqExp();
            addNode("<LAndExp>");
        }
    }

    public void UnaryExp() {
        if (curToken.type.equals(CategoryCode.PLUS) || curToken.type.equals(CategoryCode.MINU) ||
                curToken.type.equals(CategoryCode.NOT)) {
            UnaryOp();
            UnaryExp();
        } else if (curToken.type.equals(CategoryCode.IDENT) &&
                tokens.get(tokenPtr + 1).type.equals(CategoryCode.LPARENT)) {
            getToken();
            if (curToken.type.equals(CategoryCode.LPARENT)) {
                getToken();
                if (curToken.type.equals(CategoryCode.RPARENT)) {
                    getToken();
                } else if (curToken.type.equals(CategoryCode.IDENT) || curToken.type.equals(CategoryCode.LPARENT) ||
                        curToken.type.equals(CategoryCode.INTCONST) || curToken.type.equals(CategoryCode.PLUS) ||
                        curToken.type.equals(CategoryCode.MINU) || curToken.type.equals(CategoryCode.NOT)) {
                    FuncRParams();
                    if (curToken.type.equals(CategoryCode.RPARENT)) {
                        getToken();
                    } else {
                        exceptionOccurred();
                    }
                }
            } else {
                exceptionOccurred();
            }
        } else if (curToken.type.equals(CategoryCode.IDENT) || curToken.type.equals(CategoryCode.INTCONST) ||
                curToken.type.equals(CategoryCode.LPARENT)) {
            PrimaryExp();
        } else {
            exceptionOccurred();
        }
        addNode("<UnaryExp>");
    }

    public void EqExp() {
        RelExp();
        addNode("<EqExp>");
        while (curToken.type.equals(CategoryCode.EQL) || curToken.type.equals(CategoryCode.NEQ)) {
            getToken();
            RelExp();
            addNode("<EqExp>");
        }
    }

    public void PrimaryExp() {
        if (curToken.type.equals(CategoryCode.LPARENT)) {
            getToken();
            Exp();
            if (curToken.type.equals(CategoryCode.RPARENT)) {
                getToken();
            } else {
                exceptionOccurred();
            }
        } else if (curToken.type.equals(CategoryCode.IDENT)) {
            LVal();
        } else if (curToken.type.equals(CategoryCode.INTCONST)) {
            Number();
        } else {
            exceptionOccurred();
        }
        addNode("<PrimaryExp>");
    }

    public void UnaryOp() {
        if (curToken.type.equals(CategoryCode.PLUS) || curToken.type.equals(CategoryCode.MINU) ||
                curToken.type.equals(CategoryCode.NOT)) {
            getToken();
        } else {
            exceptionOccurred();
        }
        addNode("<UnaryOp>");
    }

    public void FuncRParams() {
        Exp();
        while (curToken.type.equals(CategoryCode.COMMA)) {
            getToken();
            Exp();
        }
        addNode("<FuncRParams>");
    }

    public void RelExp() {
        AddExp();
        addNode("<RelExp>");
        while (curToken.type.equals(CategoryCode.LSS) || curToken.type.equals(CategoryCode.LEQ) ||
                curToken.type.equals(CategoryCode.GRE) || curToken.type.equals(CategoryCode.GEQ)) {
            getToken();
            AddExp();
            addNode("<RelExp>");
        }
    }

    public void Number() {
        if (curToken.type.equals(CategoryCode.INTCONST)) {
            getToken();
        } else {
            exceptionOccurred();
        }
        addNode("<Number>");
    }

    public void outputToFile() {
        String path = "output.txt";
        File file = new File(path);
        System.out.println(nodes.size());
        try (FileWriter writer = new FileWriter(file)) {
            for (Node item : nodes) {
                if (item.getType() == 0) {
                    writer.write(item.getCode() + " " + item.getContext() + "\n");
                } else {
                    writer.write(item.getContext() + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
