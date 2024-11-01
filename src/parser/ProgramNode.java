package parser;

import java.util.LinkedList;

/**
 * this class represents a complete AWK program
 *
 * @author Jake Camadine
 */
public class ProgramNode extends Node{

    private LinkedList<BlockNode> beginNodes;
    private LinkedList<BlockNode> endNodes;
    private LinkedList<BlockNode> blockNodes;
    private LinkedList<FunctionDefinitionNode> functionNodes;

    public ProgramNode() {
        beginNodes = new LinkedList<BlockNode>();
        endNodes = new LinkedList<BlockNode>();
        blockNodes = new LinkedList<BlockNode>();
        functionNodes = new LinkedList<FunctionDefinitionNode>();
    }

    /**
     * @return the program's functions
     */
    public LinkedList<FunctionDefinitionNode> getFunctionNodes() { return functionNodes; }

    /**
     * @return the program's BEGIN statements
     */
    public LinkedList<BlockNode> getBeginNodes() { return beginNodes; }

    /**
     * @return the program's END statements
     */
    public LinkedList<BlockNode> getEndNodes() { return endNodes; }

    /**
     * @return the program's pattern-action statements
     */
    public LinkedList<BlockNode> getBlockNodes() { return blockNodes; }

    /**
     * @return the program in a String representation based on AWK syntax
     */
    @Override
    public String toString() {
        StringBuilder programString = new StringBuilder();

        for(int i = 0; i < beginNodes.size(); i++) {
            programString.append("BEGIN{" + beginNodes.get(i).toString() + "}\n");
        }

        for(int i = 0; i < blockNodes.size(); i++) {
            programString.append(blockNodes.get(i).toString() + "\n");
        }

        for(int i = 0; i < functionNodes.size(); i++) {
            programString.append(functionNodes.get(i).toString() + "\n");
        }

        for(int i = 0; i < endNodes.size(); i++) {
            programString.append("END{" + endNodes.get(i).toString() + "}\n");
        }

        return programString.toString();
    }
}
