package parser;

import java.util.Optional;

/**
 * this class models a for loop in the AWK programming language
 *
 * @author Jake Camadine
 */
public class ForNode extends StatementNode {

    private Optional<Node> initializer;
    private Optional<Node> conditional;
    private Optional<Node> postIterationOperation;

    private BlockNode loopBlock;

    public ForNode(Optional<Node> initializer, Optional<Node> conditional, Optional<Node> postIterationOperation, BlockNode loopBlock) {
        this.initializer = initializer;
        this.conditional = conditional;
        this.postIterationOperation = postIterationOperation;
        this.loopBlock = loopBlock;
    }

    /**
     * @return the initializer statement in the for loop declaration
     */
    public Optional<Node> getInitializer() {
        return initializer;
    }

    /**
     * @return the conditional following the initializer
     */
    public Optional<Node> getConditional() {
        return conditional;
    }

    /**
     * @return the operation that occurs after each iteration of the loop
     */
    public Optional<Node> getPostIterationOperation() {
        return postIterationOperation;
    }

    /**
     * @return the loop's block
     */
    public BlockNode getLoopBlock() {
        return loopBlock;
    }

    @Override
    public String toString() {

        return "for(" + initializer.get() + " ;" + conditional.get() + "; " + postIterationOperation.get() +
                ")" + "{" + loopBlock + "}";
    }
}
