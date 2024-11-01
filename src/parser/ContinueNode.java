package parser;

/**
 * this class represents a continue statement in the AWK
 * programming language
 *
 * @author Jake Camadine
 */
public class ContinueNode extends StatementNode{
    public ContinueNode() {

    }

    @Override
    public String toString() {
        return "\ncontinue";
    }
}
