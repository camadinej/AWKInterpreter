package interpreter;

import lexer.Token;
import parser.FunctionDefinitionNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Function;

/**
 * This class models a built-in AWK function
 *
 * @author Jake Camadine
 */
public class BuiltInFunctionDefinitionNode extends FunctionDefinitionNode {
    private boolean isVariadic;

    private Function<HashMap<String, InterpreterDataType>, String> execute;

    private LinkedList<LinkedList<Token>> altSignatures;

    public BuiltInFunctionDefinitionNode(String functionName, LinkedList<Token> parameters, boolean isVariadic, Function<HashMap<String, InterpreterDataType>, String> execute) {
        super(functionName, parameters);
        this.execute = execute;
        this.isVariadic = isVariadic;
        altSignatures = new LinkedList<>();
    }

    /**
     * runs the function assigned to execute
     *
     * @param parameterMap a map of interpreter data types corresponding to this functions parameters
     * @return
     */
    public String execute(HashMap<String, InterpreterDataType> parameterMap) {
        return execute.apply(parameterMap);
    }

    /**
     * @return the list of possible overloaded signatures
     */
    public LinkedList<LinkedList<Token>> getAltSignatures() {
        return altSignatures;
    }

    /**
     * @return true if and only if this function is variadic
     */
    public boolean isVariadic() {
        return isVariadic;
    }
}
