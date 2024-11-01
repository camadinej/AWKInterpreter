package parser;

import java.util.Optional;

/**
 * this class models a variable reference
 * in the AWK programming language
 *
 * @author Jake Camadine
 */
public class VariableReferenceNode extends Node{
    private String variableName;
    private Optional<Node> indexExpression;

    public VariableReferenceNode(String variableName) {
        this.variableName = variableName;
        indexExpression = Optional.empty();
    }
    public VariableReferenceNode(String variableName, Node indexExpression) {
        this(variableName);
        this.indexExpression = Optional.of(indexExpression);
    }

    /**
     * @return the name of AWK variable reference
     */
    public String getVariableName() {
        return variableName;
    }

    /**
     * @return the expression in the brackets of an AWK array element reference
     */
    public Optional<Node> getIndexExpression() {
        return indexExpression;
    }


    @Override
    public String toString() {
        if(indexExpression.isPresent()) {
            return "ref_" + variableName + "[" + indexExpression.get() + "]";
        }
        return "ref_" + variableName;
    }
}
