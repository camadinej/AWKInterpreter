package parser;

import java.util.Optional;

/**
 * this class models conditional statements in the AWK
 * programming language
 *
 * @author Jake Camadine
 */
public class IfNode extends StatementNode{
    private Optional<Node> condition;

    private BlockNode blockStatements;

    private Optional<StatementNode> next;

    public IfNode(Optional<Node> condition, BlockNode blockStatements) {
        this.condition = condition;
        this.blockStatements = blockStatements;
        next = Optional.empty();
    }

    /**
     * @return the boolean expression of the conditional
     */
    public Optional<Node> getCondition() {
        if(condition.isPresent()) {
            return condition;
        }
        return Optional.empty();
    }

    /**
     * @return the next if or else statement in a chain of conditionals
     */
    public Optional<StatementNode> getNext() {
        return next;
    }

    public BlockNode getBlockStatements() {
        return blockStatements;
    }

    /**
     * initialize the next conditional statement in the chain
     *
     * @param next - the next conditional in a chain of conditionals
     */
    public void setNext(Optional<StatementNode> next) {
        this.next = next;
    }

    /**
     * @return true if next is not empty
     */
    public boolean hasNext() {
        if(next.isPresent()) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        if(condition.isEmpty()) {
            return "\nelse {" + blockStatements + "\n}";
        }
        return "if(" + condition.get() + ")" + "{\n" + blockStatements + "\n}";
    }
}
