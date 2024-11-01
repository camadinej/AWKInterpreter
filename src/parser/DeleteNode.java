package parser;

/**
 * this class represents a delete statement in the AWK programming language
 */
public class DeleteNode extends StatementNode{
    private Node reference;

    public DeleteNode(Node reference) {
        this.reference = reference;
    }

    /**
     * @return the reference to be deleted
     */
    public Node getReference() {
        return reference;
    }

    @Override
    public String toString() {
        return "delete " + reference;
    }
}
