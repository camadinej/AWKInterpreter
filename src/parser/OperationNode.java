package parser;

import java.util.Optional;

/**
 * this class models a node in an abstract symbol tree
 * that holds data associated with the possible operations in
 * the AWK programming language
 *
 * @author Jake Camadine
 */
public class OperationNode extends Node{

    public enum OperationType { EQUALTO, NOTEQUALTO, LESSTHAN, LESSOREQUAL, GREATERTHAN, GREATEROREQUAL, AND,
    OR, NOT, MATCH, NOTMATCH, FIELDREF, PREINCREMENT, POSTINCREMENT, PREDECREMENT, POSTDECREMENT, UPLUS, UNEG, IN, EXP, ADD,
    SUBTRACT, MULTIPLY, DIVIDE, MODULO, CONCATENATE
    }

    private OperationType operationType;

    private Node left;
    private Optional<Node> right;

    public OperationNode(Node left, OperationType type) {
        this.left = left;
        operationType = type;
        right = Optional.empty();
    }

    public OperationNode(Node left, Optional<Node> right, OperationType type) {
        this(left, type);
        this.right = right;
    }

    /**
     * @return the type of operation being carried out
     */
    public OperationType getOperationType() {
        return operationType;
    }

    /**
     * @return the left child of this node
     */
    public Node getLeft() {
        return left;
    }

    /**
     * @return the right child of this node
     */
    public Optional<Node> getRight() {
        return right;
    }

    @Override
    public String toString() {
        if(right.isPresent()) {
            return  "(" + left.toString() + ")" + " " + operationType + " " + "(" + right.get() + ")";
        }
        return operationType + " " + left.toString();
    }


}
