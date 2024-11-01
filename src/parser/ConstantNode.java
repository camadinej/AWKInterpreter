package parser;

/**
 * this class models a node in the syntax tree
 * containing constant values such as numerical constants
 * and String literals
 *
 * @author Jake Camadine
 */
public class ConstantNode extends Node{
    private String constantVal;

    public ConstantNode(String constantVal) {
        this.constantVal = constantVal;
    }

    @Override
    public String toString() {
        return constantVal;
    }
}
