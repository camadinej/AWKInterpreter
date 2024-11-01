package parser;

/**
 * this class models a for loop using a membership condition
 * in the AWK programming language
 *
 * @author Jake Camadine
 */
public class ForInNode extends StatementNode{
    private OperationNode in; //the operation using the in operator

    private BlockNode loopBlock;

    public ForInNode(OperationNode in, BlockNode loopBlock) {
        this.in = in;
        this.loopBlock = loopBlock;
    }

    /**
     * @return the membership operation of the loop
     */
    public OperationNode getInStatement() {
        return in;
    }

    /**
     * @return the loop block
     */
    public BlockNode getLoopBlock() {
        return loopBlock;
    }

    @Override
    public String toString() {
        return "for(" + in + ") {\n" + loopBlock + "\n}";
    }

}
