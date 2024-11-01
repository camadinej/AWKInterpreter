package parser;

/**
 * this class models a do-while loop in the AWK programming language
 *
 * @author Jake Camadine
 */
public class DoWhileNode extends StatementNode{
    private Node condition;

    private BlockNode loopBlock;

    public DoWhileNode(Node condition, BlockNode loopBlock) {
        this.condition = condition;
        this.loopBlock = loopBlock;
    }

    /**
     * @return the loop condition
     */
    public Node getCondition() {
        return condition;
    }

    /**
     * @return the loop block
     */
    public BlockNode getLoopBlock() {
        return loopBlock;
    }

    @Override
    public String toString() {
        return "do" + loopBlock +
                "while(" + condition + ")";
    }
}
