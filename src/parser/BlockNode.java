package parser;

import java.util.LinkedList;
import java.util.Optional;

/**
 * this class represents a block of executable statements in the AWK programming language
 *
 * @author Jake Camadine
 */
public class BlockNode extends Node{
    private LinkedList<StatementNode> statements;

    private Optional<Node> condition;

    public BlockNode() {
        statements = new LinkedList<>();
        condition = Optional.empty();
    }
    public BlockNode(Node condition) {
        this();
        this.condition = Optional.ofNullable(condition);
    }

    /**
     * @return the statements in the block
     */
    public LinkedList<StatementNode> getStatements() {
        return statements;
    }

    /**
     * @return the block's condition
     */
    public Optional<Node> getCondition() {
        return condition;
    }

    /**
     * @return the block in a String format based on AWK syntax
     */
    @Override
    public String toString() {
        StringBuilder blockString = new StringBuilder();
        IfNode ifNode;

        if(condition.isPresent()) {
            blockString.append(condition.get() + "{");
        }
        else if(condition.isEmpty() && statements.size() > 1) {
            blockString.append("{");
        }
        for(StatementNode statement : statements) {
            if(statement instanceof IfNode) {
                ifNode = (IfNode) statement;
                blockString.append("\n" + statement + "\n");
                while(ifNode.hasNext()) {
                    if(ifNode.hasNext()) {
                        if(((IfNode)(ifNode.getNext().get())).getCondition().isPresent()) {
                            blockString.append("else ");
                        }
                    }
                    ifNode = (IfNode) ifNode.getNext().get();
                    blockString.append( ifNode + "\n");
                }

            }
            else {
                blockString.append("\n" + statement + "\n");
            }
        }
        if(condition.isPresent() || statements.size() > 1) {
            blockString.append("}");
        }
        return blockString.toString();

    }
}
