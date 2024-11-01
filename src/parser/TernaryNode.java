package parser;

/**
 * this class represents a ternary expression
 *
 * @author Jake Camadine
 */
public class TernaryNode extends Node {
    private Node booleanExpression;
    private Node trueCase;
    private Node falseCase;

    public TernaryNode(Node booleanExpression, Node trueCase, Node falseCase) {
        this.booleanExpression = booleanExpression;
        this.trueCase = trueCase;
        this.falseCase = falseCase;
    }

    /**
     * @return the ternary operation's conditional
     */
    public Node getBooleanExpression() {
        return booleanExpression;
    }

    /**
     * @return the ternary's true case
     */
     public Node getTrueCase() {
        return trueCase;
     }

    /**
     * @return the ternary's false case
     */
    public Node getFalseCase() {
        return falseCase;
     }

    @Override
    public String toString() {
        return booleanExpression + " ? " + "(" + trueCase + ")" + " : " + "(" + falseCase + ")";
    }
}
