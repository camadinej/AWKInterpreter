package parser;

import java.util.Optional;

/**
 * This class represents a return statement in the AWK
 * programming language.
 *
 * @author Jake Camadine
 */
public class ReturnNode extends StatementNode {
    private Optional<Node> parameter;

    public ReturnNode() {
        parameter = Optional.empty();
    }
    public ReturnNode(Optional<Node> parameter) {
        this.parameter = parameter;
    }

    /**
     * @return the return statement's parameter
     */
    public Optional<Node> getParameter() {
        return parameter;
    }

    @Override
    public String toString() {
        if(parameter.isPresent()) {
            return "return " + parameter;
        }
        return "return";
    }
}
