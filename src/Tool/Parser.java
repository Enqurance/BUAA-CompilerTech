package Tool;

import ClassFile.CategoryCode;
import ClassFile.Error;
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
    private Node head;

    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        curToken = tokens.get(0);
        tokenPtr = 0;
    }

    public void exceptionOccurred() {

    }

    public Token error(String lack, int line) {
        if (lack.equals(CategoryCode.SEMICN)) {
            Error.addErrorMessage(line, "i");
        } else if (lack.equals(CategoryCode.RPARENT)) {
            Error.addErrorMessage(line, "j");
        } else if (lack.equals(CategoryCode.RBRACK)) {
            Error.addErrorMessage(line, "k");
        }
        return new Token(lack, line, 4);
    }

    public void getToken() {
        Node tempNode = new Node(tokens.get(tokenPtr));
        nodes.add(tempNode);
        if (tokenPtr < tokens.size() - 1) {
            tokenPtr++;
            this.curToken = tokens.get(tokenPtr);
        }
    }

    public Node addNode(String context, int line) {
        Node tempNode = new Node(context, line);
        nodes.add(tempNode);
        return tempNode;
    }

    public void connect(ArrayList<Node> children, Node parent) {
        for (Node item : children) {
            parent.addChild(item);
        }
    }

    public void parse() {
        Node compUnit = CompUnit();
        compUnit.addParent(null);
        this.head = compUnit;
    }

    public Node CompUnit() {
        ArrayList<Node> children = new ArrayList<>();
        while (curToken.type.equals(CategoryCode.CONST) || (curToken.type.equals(CategoryCode.INT) &&
                tokens.get(tokenPtr + 1).type.equals(CategoryCode.IDENT) && (
                tokens.get(tokenPtr + 2).type.equals(CategoryCode.LBRACK) ||
                        tokens.get(tokenPtr + 2).type.equals(CategoryCode.ASSIGN) ||
                        tokens.get(tokenPtr + 2).type.equals(CategoryCode.COMMA) ||
                        tokens.get(tokenPtr + 2).type.equals(CategoryCode.SEMICN)))) {
            Node decl = Decl();
            children.add(decl);
        }
        while ((curToken.type.equals(CategoryCode.VOID) || curToken.type.equals(CategoryCode.INT)) &&
                tokens.get(tokenPtr + 1).type.equals(CategoryCode.IDENT) &&
                tokens.get(tokenPtr + 2).type.equals(CategoryCode.LPARENT)) {
            Node funcDef = FuncDef();
            children.add(funcDef);
        }
        if (curToken.type.equals(CategoryCode.INT) && tokens.get(tokenPtr + 1).type.equals(CategoryCode.MAIN)) {
            Node mainFuncDef = MainFuncDef();
            children.add(mainFuncDef);
        } else {
            exceptionOccurred();
        }
        Node node = addNode("<CompUnit>", children.get(0).getLine());
        connect(children, node);
        return node;
    }

    public Node Decl() {
        ArrayList<Node> children = new ArrayList<>();
        if (curToken.type.equals(CategoryCode.CONST)) {
            Node constDecl = ConstDecl();
            children.add(constDecl);
        } else if (curToken.type.equals(CategoryCode.INT)) {
            Node varDecl = VarDecl();
            children.add(varDecl);
        } else {
            exceptionOccurred();
        }
        Node node = addNode("<Decl>", children.get(0).getLine());
        connect(children, node);
        return node;
    }

    public Node FuncDef() {
        ArrayList<Node> children = new ArrayList<>();
        Node funcType = FuncType();
        children.add(funcType);
        if (curToken.type.equals(CategoryCode.IDENT)) {
            children.add(new Node(curToken));
            getToken();
            if (curToken.type.equals(CategoryCode.LPARENT)) {
                children.add(new Node(curToken));
                getToken();
                if (curToken.type.equals(CategoryCode.RPARENT)) {
                    children.add(new Node(curToken));
                    getToken();
                    Node block = Block();
                    children.add(block);
                } else {
                    if (!curToken.type.equals(CategoryCode.LBRACE)) {
                        Node funcFParams = FuncFParams();
                        children.add(funcFParams);
                    }
                    if (curToken.type.equals(CategoryCode.RPARENT)) {
                        children.add(new Node(curToken));
                        getToken();
                        Node block = Block();
                        children.add(block);
                    } else {
                        exceptionOccurred();
                        children.add(new Node(error(CategoryCode.RPARENT, tokens.get(tokenPtr - 1).line)));
                        Node block = Block();
                        children.add(block);
                    }
                }
            } else {
                exceptionOccurred();
            }
        } else {
            exceptionOccurred();
        }
        Node node = addNode("<FuncDef>", children.get(0).getLine());
        connect(children, node);
        return node;
    }

    public Node MainFuncDef() {
        ArrayList<Node> children = new ArrayList<>();
        if (curToken.type.equals(CategoryCode.INT)) {
            children.add(new Node(curToken));
            getToken();
            if (curToken.type.equals(CategoryCode.MAIN)) {
                children.add(new Node(curToken));
                getToken();
                if (curToken.type.equals(CategoryCode.LPARENT)) {
                    children.add(new Node(curToken));
                    getToken();
                    if (curToken.type.equals(CategoryCode.RPARENT)) {
                        children.add(new Node(curToken));
                        getToken();
                        Node block = Block();
                        children.add(block);
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
        Node node = addNode("<MainFuncDef>", children.get(0).getLine());
        connect(children, node);
        return node;
    }

    public Node ConstDecl() {
        ArrayList<Node> children = new ArrayList<>();
        if (curToken.type.equals(CategoryCode.CONST)) {
            children.add(new Node(curToken));
            getToken();
            Node bType = BType();
            children.add(bType);
            Node constDef = ConstDef();
            children.add(constDef);
            while (curToken.type.equals(CategoryCode.COMMA)) {
                children.add(new Node(curToken));
                getToken();
                Node constDef1 = ConstDef();
                children.add(constDef1);
            }
            if (curToken.type.equals(CategoryCode.SEMICN)) {
                children.add(new Node(curToken));
                getToken();
            } else {
                exceptionOccurred();
                children.add(new Node(error(CategoryCode.SEMICN, tokens.get(tokenPtr - 1).line)));
            }
        } else {
            exceptionOccurred();
        }
        Node node = addNode("<ConstDecl>", children.get(0).getLine());
        connect(children, node);
        return node;
    }

    public Node VarDecl() {
        ArrayList<Node> children = new ArrayList<>();
        Node bType = BType();
        children.add(bType);
        Node varDef = VarDef();
        children.add(varDef);
        while (curToken.type.equals(CategoryCode.COMMA)) {
            children.add(new Node(curToken));
            getToken();
            Node varDef1 = VarDef();
            children.add(varDef1);
        }
        if (curToken.type.equals(CategoryCode.SEMICN)) {
            children.add(new Node(curToken));
            getToken();
        } else {
            exceptionOccurred();
            children.add(new Node(error(CategoryCode.SEMICN, tokens.get(tokenPtr - 1).line)));
        }
        Node node = addNode("<VarDecl>", children.get(0).getLine());
        connect(children, node);
        return node;
    }

    public Node FuncType() {
        ArrayList<Node> children = new ArrayList<>();
        if (curToken.type.equals(CategoryCode.INT) || curToken.type.equals(CategoryCode.VOID)) {
            children.add(new Node(curToken));
            getToken();
        } else {
            exceptionOccurred();
        }
        Node node = addNode("<FuncType>", children.get(0).getLine());
        connect(children, node);
        return node;
    }

    public Node FuncFParams() {
        ArrayList<Node> children = new ArrayList<>();
        Node funcFParam = FuncFParam();
        children.add(funcFParam);
        while (curToken.type.equals(CategoryCode.COMMA)) {
            children.add(new Node(curToken));
            getToken();
            Node funcFParam1 = FuncFParam();
            children.add(funcFParam1);
        }
        Node node = addNode("<FuncFParams>", children.get(0).getLine());
        connect(children, node);
        return node;
    }

    public Node Block() {
        ArrayList<Node> children = new ArrayList<>();
        if (curToken.type.equals(CategoryCode.LBRACE)) {
            children.add(new Node(curToken));
            getToken();
            if (curToken.type.equals(CategoryCode.RBRACE)) {
                children.add(new Node(curToken));
                getToken();
            } else {
                while (!curToken.type.equals(CategoryCode.RBRACE)) {
                    Node blockItem = BlockItem();
                    children.add(blockItem);
                }
                children.add(new Node(curToken));
                getToken();
            }
        } else {
            exceptionOccurred();
        }
        Node node = addNode("<Block>", children.get(0).getLine());
        connect(children, node);
        return node;
    }

    public Node BType() {
        ArrayList<Node> children = new ArrayList<>();
        if (curToken.type.equals(CategoryCode.INT)) {
            children.add(new Node(curToken));
            getToken();
        } else {
            exceptionOccurred();
        }
        Node node = addNode("<BType>", children.get(0).getLine());
        connect(children, node);
        return node;
    }

    public Node ConstDef() {
        ArrayList<Node> children = new ArrayList<>();
        if (curToken.type.equals(CategoryCode.IDENT)) {
            children.add(new Node(curToken));
            getToken();
            while (curToken.type.equals(CategoryCode.LBRACK)) {
                children.add(new Node(curToken));
                getToken();
                Node constExp = ConstExp();
                children.add(constExp);
                if (curToken.type.equals(CategoryCode.RBRACK)) {
                    children.add(new Node(curToken));
                    getToken();
                } else {
                    exceptionOccurred();
                    children.add(new Node(error(CategoryCode.RBRACK, tokens.get(tokenPtr - 1).line)));
                }
            }
            if (curToken.type.equals(CategoryCode.ASSIGN)) {
                children.add(new Node(curToken));
                getToken();
                Node node = ConstInitVal();
                children.add(node);
            } else {
                exceptionOccurred();
            }
        } else {
            exceptionOccurred();
        }
        Node node = addNode("<ConstDef>", children.get(0).getLine());
        connect(children, node);
        return node;
    }

    public Node VarDef() {
        ArrayList<Node> children = new ArrayList<>();
        if (curToken.type.equals(CategoryCode.IDENT)) {
            children.add(new Node(curToken));
            getToken();
            while (curToken.type.equals(CategoryCode.LBRACK)) {
                children.add(new Node(curToken));
                getToken();
                Node constExp = ConstExp();
                children.add(constExp);
                if (curToken.type.equals(CategoryCode.RBRACK)) {
                    children.add(new Node(curToken));
                    getToken();
                } else {
                    exceptionOccurred();
                    children.add(new Node(error(CategoryCode.RBRACK, tokens.get(tokenPtr - 1).line)));
                }
            }
            if (curToken.type.equals(CategoryCode.ASSIGN)) {
                children.add(new Node(curToken));
                getToken();
                Node initVal = InitVal();
                children.add(initVal);
            }
        } else {
            exceptionOccurred();
        }
        Node node = addNode("<VarDef>", children.get(0).getLine());
        connect(children, node);
        return node;
    }

    public Node FuncFParam() {
        ArrayList<Node> children = new ArrayList<>();
        Node bType = BType();
        children.add(bType);
        if (curToken.type.equals(CategoryCode.IDENT)) {
            children.add(new Node(curToken));
            getToken();
            if (curToken.type.equals(CategoryCode.LBRACK)) {
                children.add(new Node(curToken));
                getToken();
                if (curToken.type.equals(CategoryCode.RBRACK)) {
                    children.add(new Node(curToken));
                    getToken();
                    while (curToken.type.equals(CategoryCode.LBRACK)) {
                        children.add(new Node(curToken));
                        getToken();
                        Node constExp = ConstExp();
                        children.add(constExp);
                        if (curToken.type.equals(CategoryCode.RBRACK)) {
                            children.add(new Node(curToken));
                            getToken();
                        } else {
                            exceptionOccurred();
                            children.add(new Node(error(CategoryCode.RBRACK, tokens.get(tokenPtr - 1).line)));
                        }
                    }
                } else {
                    exceptionOccurred();
                    children.add(new Node(error(CategoryCode.RBRACK, tokens.get(tokenPtr - 1).line)));
                    while (curToken.type.equals(CategoryCode.LBRACK)) {
                        children.add(new Node(curToken));
                        getToken();
                        Node constExp = ConstExp();
                        children.add(constExp);
                        if (curToken.type.equals(CategoryCode.RBRACK)) {
                            children.add(new Node(curToken));
                            getToken();
                        } else {
                            exceptionOccurred();
                            children.add(new Node(error(CategoryCode.RBRACK, tokens.get(tokenPtr - 1).line)));
                        }
                    }
                }
            }
        } else {
            exceptionOccurred();
        }
        Node node = addNode("<FuncFParam>", children.get(0).getLine());
        connect(children, node);
        return node;
    }

    public Node BlockItem() {
        ArrayList<Node> children = new ArrayList<>();
        if (curToken.type.equals(CategoryCode.CONST) || curToken.type.equals(CategoryCode.INT)) {
            Node decl = Decl();
            children.add(decl);
        } else if (curToken.type.equals(CategoryCode.IDENT) || curToken.type.equals(CategoryCode.LPARENT) ||
                curToken.type.equals(CategoryCode.INTCONST) || curToken.type.equals(CategoryCode.PLUS) ||
                curToken.type.equals(CategoryCode.MINU) || curToken.type.equals(CategoryCode.NOT) ||
                curToken.type.equals(CategoryCode.SEMICN) || curToken.type.equals(CategoryCode.LBRACE) ||
                curToken.type.equals(CategoryCode.IF) || curToken.type.equals(CategoryCode.WHILE) ||
                curToken.type.equals(CategoryCode.BREAK) || curToken.type.equals(CategoryCode.CONTINUE) ||
                curToken.type.equals(CategoryCode.RETURN) || curToken.type.equals(CategoryCode.PRINTF)) {
            Node stmt = Stmt();
            children.add(stmt);
        }
        Node node = addNode("<BlockItem>", children.get(0).getLine());
        connect(children, node);
        return node;
    }

    public Node ConstExp() {
        ArrayList<Node> children = new ArrayList<>();
        Node addExp = AddExp();
        children.add(addExp);
        Node node = addNode("<ConstExp>", children.get(0).getLine());
        connect(children, node);
        return node;
    }

    public Node ConstInitVal() {
        ArrayList<Node> children = new ArrayList<>();
        if (curToken.type.equals(CategoryCode.LBRACE)) {
            children.add(new Node(curToken));
            getToken();
            if (curToken.type.equals(CategoryCode.LBRACE) || curToken.type.equals(CategoryCode.IDENT) ||
                    curToken.type.equals(CategoryCode.LPARENT) || curToken.type.equals(CategoryCode.PLUS) ||
                    curToken.type.equals(CategoryCode.MINU) || curToken.type.equals(CategoryCode.NOT) ||
                    curToken.type.equals(CategoryCode.INTCONST)) {
                Node constInitVal = ConstInitVal();
                children.add(constInitVal);
                while (curToken.type.equals(CategoryCode.COMMA)) {
                    children.add(new Node(curToken));
                    getToken();
                    Node constInitVal1 = ConstInitVal();
                    children.add(constInitVal1);
                }
                if (curToken.type.equals(CategoryCode.RBRACE)) {
                    children.add(new Node(curToken));
                    getToken();
                }
            } else if (curToken.type.equals(CategoryCode.RBRACE)) {
                children.add(new Node(curToken));
                getToken();
            } else {
                exceptionOccurred();
            }
        } else {
            Node constExp = ConstExp();
            children.add(constExp);
        }
        Node node = addNode("<ConstInitVal>", children.get(0).getLine());
        connect(children, node);
        return node;
    }

    public Node InitVal() {
        ArrayList<Node> children = new ArrayList<>();
        if (curToken.type.equals(CategoryCode.LBRACE)) {
            children.add(new Node(curToken));
            getToken();
            if (curToken.type.equals(CategoryCode.RBRACE)) {
                children.add(new Node(curToken));
                getToken();
            } else if (curToken.type.equals(CategoryCode.LPARENT) || curToken.type.equals(CategoryCode.IDENT) ||
                    curToken.type.equals(CategoryCode.INTCONST) || curToken.type.equals(CategoryCode.PLUS) ||
                    curToken.type.equals(CategoryCode.MINU) || curToken.type.equals(CategoryCode.NOT) ||
                    curToken.type.equals(CategoryCode.LBRACE)) {
                Node initVal = InitVal();
                children.add(initVal);
                while (curToken.type.equals(CategoryCode.COMMA)) {
                    children.add(new Node(curToken));
                    getToken();
                    Node initVal1 = InitVal();
                    children.add(initVal1);
                }
                if (curToken.type.equals(CategoryCode.RBRACE)) {
                    children.add(new Node(curToken));
                    getToken();
                } else {
                    exceptionOccurred();
                }
            }
        } else if (curToken.type.equals(CategoryCode.IDENT) || curToken.type.equals(CategoryCode.LPARENT) ||
                curToken.type.equals(CategoryCode.PLUS) || curToken.type.equals(CategoryCode.MINU) ||
                curToken.type.equals(CategoryCode.NOT) || curToken.type.equals(CategoryCode.INTCONST)) {
            Node exp = Exp();
            children.add(exp);
        }
        Node node = addNode("<InitVal>", children.get(0).getLine());
        connect(children, node);
        return node;
    }

    public Node Stmt() {
        ArrayList<Node> children = new ArrayList<>();
        if (curToken.type.equals(CategoryCode.PRINTF)) {    /* Printf */
            children.add(new Node(curToken));
            getToken();
            if (curToken.type.equals(CategoryCode.LPARENT)) {
                children.add(new Node(curToken));
                getToken();
                Node formatString = FormatString();
                children.add(formatString);
                while (curToken.type.equals(CategoryCode.COMMA)) {
                    children.add(new Node(curToken));
                    getToken();
                    Node exp = Exp();
                    children.add(exp);
                }
                if (curToken.type.equals(CategoryCode.RPARENT)) {
                    children.add(new Node(curToken));
                    getToken();
                    if (curToken.type.equals(CategoryCode.SEMICN)) {
                        children.add(new Node(curToken));
                        getToken();
                    } else {
                        exceptionOccurred();
                        children.add(new Node(error(CategoryCode.SEMICN, tokens.get(tokenPtr - 1).line)));
                    }
                } else {
                    exceptionOccurred();
                    children.add(new Node(error(CategoryCode.RPARENT, tokens.get(tokenPtr - 1).line)));
                    if (curToken.type.equals(CategoryCode.SEMICN)) {
                        children.add(new Node(curToken));
                        getToken();
                    } else {
                        exceptionOccurred();
                        children.add(new Node(error(CategoryCode.SEMICN, tokens.get(tokenPtr - 1).line)));
                    }
                }
            } else {
                exceptionOccurred();
            }
        } else if (curToken.type.equals(CategoryCode.RETURN)) { /* Return */
            children.add(new Node(curToken));
            getToken();
            if (curToken.type.equals(CategoryCode.SEMICN)) {
                children.add(new Node(curToken));
                getToken();
            } else if (curToken.type.equals(CategoryCode.LPARENT) || curToken.type.equals(CategoryCode.IDENT) ||
                    curToken.type.equals(CategoryCode.INTCONST) || curToken.type.equals(CategoryCode.PLUS) ||
                    curToken.type.equals(CategoryCode.MINU) || curToken.type.equals(CategoryCode.NOT)) {
                Node exp = Exp();
                children.add(exp);
                if (curToken.type.equals(CategoryCode.SEMICN)) {
                    children.add(new Node(curToken));
                    getToken();
                } else {
                    exceptionOccurred();
                    children.add(new Node(error(CategoryCode.SEMICN, tokens.get(tokenPtr - 1).line)));
                }
            }
        } else if (curToken.type.equals(CategoryCode.BREAK) || curToken.type.equals(CategoryCode.CONTINUE)) {   /* Continue and break */
            children.add(new Node(curToken));
            getToken();
            if (curToken.type.equals(CategoryCode.SEMICN)) {
                children.add(new Node(curToken));
                getToken();
            } else {
                exceptionOccurred();
                children.add(new Node(error(CategoryCode.SEMICN, tokens.get(tokenPtr - 1).line)));
            }
        } else if (curToken.type.equals(CategoryCode.WHILE)) {  /* While */
            children.add(new Node(curToken));
            getToken();
            if (curToken.type.equals(CategoryCode.LPARENT)) {
                children.add(new Node(curToken));
                getToken();
                Node cond = Cond();
                children.add(cond);
                if (curToken.type.equals(CategoryCode.RPARENT)) {
                    children.add(new Node(curToken));
                    getToken();
                    Node stmt = Stmt();
                    children.add(stmt);
                } else {
                    exceptionOccurred();
                    children.add(new Node(error(CategoryCode.RPARENT, tokens.get(tokenPtr - 1).line)));
                    Node stmt = Stmt();
                    children.add(stmt);
                }
            } else {
                exceptionOccurred();
            }
        } else if (curToken.type.equals(CategoryCode.IF)) {     /* If */
            children.add(new Node(curToken));
            getToken();
            if (curToken.type.equals(CategoryCode.LPARENT)) {
                children.add(new Node(curToken));
                getToken();
                Node cond = Cond();
                children.add(cond);
                if (curToken.type.equals(CategoryCode.RPARENT)) {
                    children.add(new Node(curToken));
                    getToken();
                    Node stmt = Stmt();
                    children.add(stmt);
                } else {
                    exceptionOccurred();
                    children.add(new Node(error(CategoryCode.RPARENT, tokens.get(tokenPtr - 1).line)));
                    Node stmt = Stmt();
                    children.add(stmt);
                }
                if (curToken.type.equals(CategoryCode.ELSE)) {
                    children.add(new Node(curToken));
                    getToken();
                    Node stmt = Stmt();
                    children.add(stmt);
                }
            } else {
                exceptionOccurred();
            }
        } else if (curToken.type.equals(CategoryCode.LBRACE)) {     /* Block */
            Node block = Block();
            children.add(block);
        } else {
            int  flag;
//            while (tokenPtr + i < tokens.size()) {
//                if (tokens.get(tokenPtr + i).type.equals(CategoryCode.ASSIGN)) {
//                    flag = 1;
//                    break;
//                } else if (tokens.get(tokenPtr + i).type.equals(CategoryCode.SEMICN)) {
//                    flag = 2;
//                    break;
//                }
//                i++;
//            }
            boolean res = TryLValAssign();
            if (res) {
                flag = 1;
            } else {
                flag = 2;
            }
            if (flag == 1) {
                Node lVal = LVal();
                children.add(lVal);
                if (curToken.type.equals(CategoryCode.ASSIGN)) {
                    children.add(new Node(curToken));
                    getToken();
                    if (curToken.type.equals(CategoryCode.GETINT)) {
                        children.add(new Node(curToken));
                        getToken();
                        if (curToken.type.equals(CategoryCode.LPARENT)) {
                            children.add(new Node(curToken));
                            getToken();
                            if (curToken.type.equals(CategoryCode.RPARENT)) {
                                children.add(new Node(curToken));
                                getToken();
                                if (curToken.type.equals(CategoryCode.SEMICN)) {
                                    children.add(new Node(curToken));
                                    getToken();
                                } else {
                                    exceptionOccurred();
                                    children.add(new Node(error(CategoryCode.SEMICN, tokens.get(tokenPtr - 1).line)));
                                }
                            } else {
                                exceptionOccurred();
                                children.add(new Node(error(CategoryCode.RPARENT, tokens.get(tokenPtr - 1).line)));
                                if (curToken.type.equals(CategoryCode.SEMICN)) {
                                    children.add(new Node(curToken));
                                    getToken();
                                } else {
                                    exceptionOccurred();
                                    children.add(new Node(error(CategoryCode.SEMICN, tokens.get(tokenPtr - 1).line)));
                                }
                            }
                        } else {
                            exceptionOccurred();
                        }
                    } else {
                        Node exp = Exp();
                        children.add(exp);
                        if (curToken.type.equals(CategoryCode.SEMICN)) {
                            children.add(new Node(curToken));
                            getToken();
                        } else {
                            exceptionOccurred();
                            children.add(new Node(error(CategoryCode.SEMICN, tokens.get(tokenPtr - 1).line)));
                        }
                    }
                } else {
                    exceptionOccurred();
                }
            } else if (flag == 2) {
                if (curToken.type.equals(CategoryCode.LPARENT) || curToken.type.equals(CategoryCode.IDENT) ||
                        curToken.type.equals(CategoryCode.INTCONST) || curToken.type.equals(CategoryCode.PLUS) ||
                        curToken.type.equals(CategoryCode.MINU) || curToken.type.equals(CategoryCode.NOT)) {
                    Node exp = Exp();
                    children.add(exp);
                }
                if (curToken.type.equals(CategoryCode.SEMICN)) {
                    children.add(new Node(curToken));
                    getToken();
                } else {
                    exceptionOccurred();
                    children.add(new Node(error(CategoryCode.SEMICN, tokens.get(tokenPtr - 1).line)));
                }
            } else {
                exceptionOccurred();
            }
        }
        Node node = addNode("<Stmt>", children.get(0).getLine());
        connect(children, node);
        return node;
    }

    public Node AddExp() {
        ArrayList<Node> children = new ArrayList<>();
        Node mulExp = MulExp();
        children.add(mulExp);
        Node node = addNode("<AddExp>", children.get(0).getLine());
        while (curToken.type.equals(CategoryCode.PLUS) || curToken.type.equals(CategoryCode.MINU)) {
            Node tNode = new Node(curToken);
            getToken();
            Node mulExp1 = MulExp();
            Node addExp = addNode("<AddExp>", children.get(0).getLine());
            children.add(addExp);
            children.add(tNode);
            children.add(mulExp1);
        }
        connect(children, node);
        return node;
    }

    public Node Exp() {
        Node addExp = AddExp();
        Node node = addNode("<Exp>", addExp.getLine());
        node.addChild(addExp);
        return node;
    }

    public Node Cond() {
        Node lOrExp = LOrExp();
        Node node = addNode("<Cond>", lOrExp.getLine());
        node.addChild(lOrExp);
        return node;
    }

    public Node LVal() {
        ArrayList<Node> children = new ArrayList<>();
        if (curToken.type.equals(CategoryCode.IDENT)) {
            children.add(new Node(curToken));
            getToken();
            while (curToken.type.equals(CategoryCode.LBRACK)) {
                children.add(new Node(curToken));
                getToken();
                Node exp = Exp();
                children.add(exp);
                if (curToken.type.equals(CategoryCode.RBRACK)) {
                    children.add(new Node(curToken));
                    getToken();
                } else {
                    exceptionOccurred();
                    children.add(new Node(error(CategoryCode.RBRACK, tokens.get(tokenPtr - 1).line)));
                }
            }
        }
        Node node = addNode("<LVal>", children.get(0).getLine());
        connect(children, node);
        return node;
    }

    public Node LOrExp() {
        ArrayList<Node> children = new ArrayList<>();
        Node lAndExp = LAndExp();
        children.add(lAndExp);
        Node node = addNode("<LOrExp>", children.get(0).getLine());
        while (curToken.type.equals(CategoryCode.OR)) {
            Node tNode = new Node(curToken);
            getToken();
            Node lAndExp1 = LAndExp();
            Node addExp = addNode("<LOrExp>", children.get(0).getLine());
            children.add(addExp);
            children.add(tNode);
            children.add(lAndExp1);
        }
        connect(children, node);
        return node;
    }

    public Node FormatString() {
        Node formatString = null;
        if (curToken.type.equals(CategoryCode.FORMATSTRING)) {
            formatString = new Node(curToken);
            judgeFormatString(formatString);
            getToken();
        } else {
            exceptionOccurred();
        }
        return formatString;
    }

    public Node MulExp() {
        ArrayList<Node> children = new ArrayList<>();
        Node unaryExp = UnaryExp();
        children.add(unaryExp);
        Node node = addNode("<MulExp>", children.get(0).getLine());
        while (curToken.type.equals(CategoryCode.MULT) || curToken.type.equals(CategoryCode.DIV) ||
                curToken.type.equals(CategoryCode.MOD)) {
            Node tNode = new Node(curToken);
            getToken();
            Node unaryExp1 = UnaryExp();
            Node mulExp = addNode("<MulExp>", children.get(0).getLine());
            children.add(mulExp);
            children.add(tNode);
            children.add(unaryExp1);
        }
        connect(children, node);
        return node;
    }

    public Node LAndExp() {
        ArrayList<Node> children = new ArrayList<>();
        Node eqExp = EqExp();
        children.add(eqExp);
        Node node = addNode("<LAndExp>", children.get(0).getLine());
        while (curToken.type.equals(CategoryCode.AND)) {
            Node tNode = new Node(curToken);
            getToken();
            Node eqExp1 = EqExp();
            Node lAndExp = addNode("<LAndExp>", children.get(0).getLine());
            children.add(lAndExp);
            children.add(tNode);
            children.add(eqExp1);
        }
        connect(children, node);
        return node;
    }

    public Node UnaryExp() {
        ArrayList<Node> children = new ArrayList<>();
        if (curToken.type.equals(CategoryCode.PLUS) || curToken.type.equals(CategoryCode.MINU) ||
                curToken.type.equals(CategoryCode.NOT)) {
            Node node = UnaryOp();
            children.add(node);
            Node unaryExp = UnaryExp();
            children.add(unaryExp);
        } else if (curToken.type.equals(CategoryCode.IDENT) &&
                tokens.get(tokenPtr + 1).type.equals(CategoryCode.LPARENT)) {
            children.add(new Node(curToken));
            getToken();
            if (curToken.type.equals(CategoryCode.LPARENT)) {
                children.add(new Node(curToken));
                getToken();
                if (curToken.type.equals(CategoryCode.RPARENT)) {
                    children.add(new Node(curToken));
                    getToken();
                } else if (curToken.type.equals(CategoryCode.IDENT) || curToken.type.equals(CategoryCode.LPARENT) ||
                        curToken.type.equals(CategoryCode.INTCONST) || curToken.type.equals(CategoryCode.PLUS) ||
                        curToken.type.equals(CategoryCode.MINU) || curToken.type.equals(CategoryCode.NOT)) {
                    Node funcRParams = FuncRParams();
                    children.add(funcRParams);
                    if (curToken.type.equals(CategoryCode.RPARENT)) {
                        children.add(new Node(curToken));
                        getToken();
                    } else {
                        exceptionOccurred();
                        children.add(new Node(error(CategoryCode.RPARENT, tokens.get(tokenPtr - 1).line)));
                    }
                } else {
                    exceptionOccurred();
                    children.add(new Node(error(CategoryCode.RPARENT, tokens.get(tokenPtr - 1).line)));
                }
            } else {
                exceptionOccurred();
            }
        } else if (curToken.type.equals(CategoryCode.IDENT) || curToken.type.equals(CategoryCode.INTCONST) ||
                curToken.type.equals(CategoryCode.LPARENT)) {
            Node primaryExp = PrimaryExp();
            children.add(primaryExp);
        } else {
            exceptionOccurred();
        }
        Node node = addNode("<UnaryExp>", children.get(0).getLine());
        connect(children, node);
        return node;
    }

    public Node EqExp() {
        ArrayList<Node> children = new ArrayList<>();
        Node relExp = RelExp();
        children.add(relExp);
        Node node = addNode("<EqExp>", children.get(0).getLine());
        while (curToken.type.equals(CategoryCode.EQL) || curToken.type.equals(CategoryCode.NEQ)) {
            Node tNode = new Node(curToken);
            getToken();
            Node relExp1 = RelExp();
            Node eqExp = addNode("<EqExp>", children.get(0).getLine());
            children.add(eqExp);
            children.add(tNode);
            children.add(relExp1);
        }
        connect(children, node);
        return node;
    }

    public Node PrimaryExp() {
        ArrayList<Node> children = new ArrayList<>();
        if (curToken.type.equals(CategoryCode.LPARENT)) {
            children.add(new Node(curToken));
            getToken();
            Node exp = Exp();
            children.add(exp);
            if (curToken.type.equals(CategoryCode.RPARENT)) {
                children.add(new Node(curToken));
                getToken();
            } else {
                exceptionOccurred();
            }
        } else if (curToken.type.equals(CategoryCode.IDENT)) {
            Node lVal = LVal();
            children.add(lVal);
        } else if (curToken.type.equals(CategoryCode.INTCONST)) {
            Node number = Number();
            children.add(number);
        } else {
            exceptionOccurred();
        }
        Node node = addNode("<PrimaryExp>", children.get(0).getLine());
        connect(children, node);
        return node;
    }

    public Node UnaryOp() {
        ArrayList<Node> children = new ArrayList<>();
        if (curToken.type.equals(CategoryCode.PLUS) || curToken.type.equals(CategoryCode.MINU) ||
                curToken.type.equals(CategoryCode.NOT)) {
            children.add(new Node(curToken));
            getToken();
        } else {
            exceptionOccurred();
        }
        Node node = addNode("<UnaryOp>", children.get(0).getLine());
        connect(children, node);
        return node;
    }

    public Node FuncRParams() {
        ArrayList<Node> children = new ArrayList<>();
        Node exp = Exp();
        children.add(exp);
        while (curToken.type.equals(CategoryCode.COMMA)) {
            children.add(new Node(curToken));
            getToken();
            Node exp1 = Exp();
            children.add(exp1);
        }
        Node node = addNode("<FuncRParams>", children.get(0).getLine());
        connect(children, node);
        return node;
    }

    public Node RelExp() {
        ArrayList<Node> children = new ArrayList<>();
        Node addExp = AddExp();
        children.add(addExp);
        Node node = addNode("<RelExp>", children.get(0).getLine());
        while (curToken.type.equals(CategoryCode.LSS) || curToken.type.equals(CategoryCode.LEQ) ||
                curToken.type.equals(CategoryCode.GRE) || curToken.type.equals(CategoryCode.GEQ)) {
            Node tNode = new Node(curToken);
            getToken();
            Node addExp1 = AddExp();
            Node relExp = addNode("<RelExp>", children.get(0).getLine());
            children.add(relExp);
            children.add(tNode);
            children.add(addExp1);
        }
        connect(children, node);
        return node;
    }

    public Node Number() {
        ArrayList<Node> children = new ArrayList<>();
        if (curToken.type.equals(CategoryCode.INTCONST)) {
            children.add(new Node(curToken));
            getToken();
        } else {
            exceptionOccurred();
        }
        Node node = addNode("<Number>", children.get(0).getLine());
        connect(children, node);
        return node;
    }

    public Node getHead() {
        return head;
    }

    public void judgeFormatString(Node format) {
        int len = format.getContext().length();
        for (int i = 1; i < len - 1; i++) {
            char c = format.getContext().charAt(i);
            if (!((c == 32) || (c == 33) || (c >= 40 && c <= 126 && c != 92) ||
                    (c == 92 && format.getContext().charAt(i + 1) == 'n') ||
                    (c == 37 && format.getContext().charAt(i + 1) == 'd'))) {
                Error.addErrorMessage(format.getLine(), "a");
                break;
            }
        }
    }

    public boolean TryLValAssign() {
        int offset = 0, stack = 0;
        if (tokens.get(tokenPtr + offset).type.equals(CategoryCode.IDENT)) {
            offset++;
        }
        if (tokens.get(tokenPtr + offset).type.equals(CategoryCode.LBRACK)) {
            offset++;
            stack++;
            while (stack != 0) {
                if (tokens.get(tokenPtr + offset).type.equals(CategoryCode.LBRACK)) {
                    stack++;
                } else if (tokens.get(tokenPtr + offset).type.equals(CategoryCode.RBRACK)) {
                    stack--;
                }
                offset++;
            }
        }
        if (tokens.get(tokenPtr + offset).type.equals(CategoryCode.LBRACK)) {
            offset++;
            stack++;
            while (stack != 0) {
                if (tokens.get(tokenPtr + offset).type.equals(CategoryCode.LBRACK)) {
                    stack++;
                } else if (tokens.get(tokenPtr + offset).type.equals(CategoryCode.RBRACK)) {
                    stack--;
                }
                offset++;
            }
        }
        return tokens.get(tokenPtr + offset).type.equals(CategoryCode.ASSIGN);
//            while (tokens.get(tokenPtr + offset).type.equals(CategoryCode.LBRACK)) {
//                offset++;
//                Node exp = Exp();
//                children.add(exp);
//                if (curToken.type.equals(CategoryCode.RBRACK)) {
//                    children.add(new Node(curToken));
//                    getToken();
//                } else {
//                    exceptionOccurred();
//                    children.add(new Node(error(CategoryCode.RBRACK, tokens.get(tokenPtr - 1).line)));
//                }
//            }
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

    public void outputTreeToFile() {
        ArrayList<String> total = new ArrayList<>();
        head.printAll(total);
        String path = "output.txt";
        File file = new File(path);
        System.out.println(nodes.size());
        try (FileWriter writer = new FileWriter(file)) {
            for (String str : total) {
                writer.write(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
