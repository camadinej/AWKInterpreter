package parser;

import lexer.Token;
import java.util.LinkedList;

/**
 * This class represents a user defined function in the AWK programming language
 *
 * @author Jake Camadine
 */
public class FunctionDefinitionNode extends Node{
    private String functionName;

    private LinkedList<StatementNode> statements;
    private LinkedList<Token> parameters;

    public FunctionDefinitionNode(String functionName, LinkedList<Token> parameters) {
        this.functionName = functionName;
        this.parameters = new LinkedList<>(parameters);
        this.statements = new LinkedList<>();
    }

    /**
     * @return the statements in the function block
     */
    public LinkedList<StatementNode> getStatements() {
        return statements;
    }

    /**
     * @return the name of a user defined function
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * @return the list of parameters
     */
    public LinkedList<Token> getParameters() {
        return parameters;
    }
    /**
     * @return a String representation of the function based on AWK syntax
     */
    @Override
    public String toString() {
        StringBuilder functionString = new StringBuilder();

        functionString.append("function ");
        functionString.append(functionName);
        functionString.append("(" + parameters + ") ");
        functionString.append("{\n" + statements + "\n}\n");
        return functionString.toString();
    }
}
