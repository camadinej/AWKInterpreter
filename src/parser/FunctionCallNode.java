package parser;

import java.util.LinkedList;
import java.util.Optional;

/**
 * models a call to user-defined and built-in function calls
 * in the AWK programming language
 *
 * @author Jake Camadine
 */
public class FunctionCallNode extends StatementNode{
    private String functionName;

    private LinkedList<Node> parameters;

    public FunctionCallNode(String functionName, LinkedList<Node> parameters) {
        this.functionName = functionName;
        this.parameters = parameters;
    }

    /**
     * @return the name of the function being called
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * @return the parameters being passed into an AWK function call
     */
    public LinkedList<Node> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        if(parameters.isEmpty()){
            return functionName + "(" + ")";
        }
        return functionName + "(" + parameters + ")";
    }
}
