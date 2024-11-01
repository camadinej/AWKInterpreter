package parser;

/**
 * this class models an assignment operation in the
 * AWK programming language.
 *
 * @author Jake Camadine
 */
public class AssignmentNode extends StatementNode{
    private Node target;
    private Node assignment;

    public AssignmentNode(Node target, Node assignment) {
        this.target = target;
        this.assignment = assignment;
    }

    /**
     * @return the left hand side of an assignment statement
     */
    public Node getTarget() {
        return target;
    }

    /**
     * @return the right hand side of an assignment statement
     */
    public Node getAssignment() {
        return assignment;
    }

    @Override
    public String toString() {
        return "(" + target + " = " + assignment + ")";
    }
}
