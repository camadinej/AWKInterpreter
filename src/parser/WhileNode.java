package parser;

/**
 * this class models a while loop in the
 * AWK programming language
 *
 * @author Jake Camadine
 */
public class WhileNode extends StatementNode{
    //condition
    private Node condition;
    //statements
    private BlockNode loopBlock;

    public WhileNode(Node condition, BlockNode loopBlock) {
        this.condition = condition;
        this.loopBlock = loopBlock;
    }

    /**
     * @return the while loop's conditional statement
     */
    public Node getCondition() {
        return condition;
    }

    /**
     * @return the loop block's statements
     */
    public BlockNode getLoopBlock() {
        return loopBlock;
    }

    @Override
    public String toString() {
        return "while" + "(" + condition + ")" + " {" + loopBlock + "}";
    }
}
