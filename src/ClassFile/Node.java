package ClassFile;

import java.util.ArrayList;

public class Node {
    private final Token token;
    private final int type;
    private final String context;
    private Node parent;
    private final ArrayList<Node> children = new ArrayList<>();

    /* type == 0 ==> isEnd*/
    /* type == 1 ==> isNotEnd*/
    public Node(Token token) {
        this.token = token;
        this.type = 0;
        this.context = token.context;
    }

    public Node(String context) {
        this.token = null;
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
            if (!context.equals("<BlockItem>") && !context.equals("<BType>") && !context.equals("<Decl>"))
                total.add(getContext() + "\n");
        }
    }
}
