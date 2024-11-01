package parser;

/**
 * this class represents a break statement in the AWK programming language
 *
 * @author Jake Camadine
 */
public class BreakNode extends StatementNode{

    public BreakNode() {

    }

    @Override
    public String toString() {
        return "break";
    }
}
