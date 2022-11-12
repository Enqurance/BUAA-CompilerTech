package ClassFile;

import java.util.ArrayList;

public class Node {
    private final Token token;
    private final int type;
    private final String context;
    private Node parent;
    private final ArrayList<Node> children = new ArrayList<>();
    private final int line;

    /* type == 0 ==> isEnd*/
    /* type == 1 ==> isNotEnd*/
    public Node(Token token) {
        this.token = token;
        this.type = 0;
        this.context = token.context;
        line = token.line;
    }

    public Node(String context, int line) {
        this.token = null;
        this.line = line;
        this.type = 1;
        this.context = context;
    }

    public String getContext() {
        return context;
    }

    public int getType() {
        return type;
    }

    public String getCode() {
        if (token != null) {
            return token.type;
        }
        return null;
    }

    public void addChild(Node node) {
        node.addParent(this);
        children.add(node);
    }

    public void addParent(Node node) {
        parent = node;
    }

    public void printAll(ArrayList<String> total) {
        for (Node item : children) {
            item.printAll(total);
        }
        if (type == 0) {
            total.add(getCode() + " " + getContext() + "\n");
        } else {
            if (!context.equals("<BlockItem>") && !context.equals("<BType>") && !context.equals("<Decl>")) {
                total.add(getContext() + "\n");
            }
        }
    }

    public void printChildren() {
        System.out.println("A Node's Children:");
        for (Node node : children) {
            System.out.print(node.getContext() + " ");
        }
        System.out.println();
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public Node getParent() {
        return parent;
    }

    public Token getToken() {
        return token;
    }

    public int getLine() {
        return line;
    }
}
