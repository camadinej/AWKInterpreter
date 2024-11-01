package interpreter;

import lexer.Token;
import parser.*;

import java.util.regex.Matcher;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class represents and interpreter for the AWK programming language
 *
 * @author Jake Camadine
 */
public class Interpreter {
    /**
     * This class is models the input file control loop in an AWK program
     */
     public class LineManager {
        private List<String> inputText;

        public LineManager(List<String> inputText) {
            this.inputText =  inputText;
            globalVariables.put("FNR", new InterpreterDataType("0"));
            globalVariables.put("$0", new InterpreterDataType(""));
        }

        /**
         * takes the current line, splits it by the field separator, and assigns the array elements to the values of the n field references
         *
         * @return true if there is a line to split
         */
        public boolean splitAndAssign() {
            if(inputText.isEmpty()) { return false; }
            var currentLine = inputText.remove(0);
            var fieldReferenceLiterals = currentLine.split(globalVariables.get("FS").getData());

            globalVariables.put("$0", new InterpreterDataType(currentLine));
            for(int i = 0; i < fieldReferenceLiterals.length; i++) {
                globalVariables.put("$" + (i + 1), new InterpreterDataType(fieldReferenceLiterals[i]));
            }
            globalVariables.get("NF").setData(String.valueOf(fieldReferenceLiterals.length));
            globalVariables.get("NR").setData(String.valueOf((Integer.parseInt(globalVariables.get("NR").getData()) + 1)));
            globalVariables.get("FNR").setData(String.valueOf((Integer.parseInt(globalVariables.get("FNR").getData()) + 1)));
            return true;
        }

    }

    private ProgramNode program;
    private LineManager lineManager;

    private HashMap<String, InterpreterDataType> globalVariables;
    private HashMap<String, FunctionDefinitionNode> functions;

    public Interpreter(ProgramNode programNode, Path inputFile) throws IOException {
        program = programNode;
        globalVariables = new HashMap<>();
        functions = new HashMap<>();
        if(inputFile == null){
            lineManager = new LineManager(new LinkedList<>());
        }
        else {
            lineManager = new LineManager(Files.readAllLines(inputFile));
        }
        var paramList = new LinkedList<Token>();
        paramList.add(new Token(0, 0, Token.TokenType.WORD, "expressions"));
        functions.put("print", new BuiltInFunctionDefinitionNode("print", paramList, true, (params) -> {
            StringBuilder expression =  new StringBuilder();
            if(params.isEmpty()) {
                expression.append(globalVariables.get("$0").getData());
            }
            else {
                for (int i = 0; i < ((InterpreterArrayDataType) params.get("expressions")).getArrayData().size(); i++) {
                    expression.append(((InterpreterArrayDataType) params.get("expressions")).getArrayData().get(String.valueOf(i)).getData());
                }
            }
            System.out.println(expression);
            return null;
        }));
        paramList.addFirst(new Token(0, 0, Token.TokenType.WORD, "formatSpecifier"));
        functions.put("printf", new BuiltInFunctionDefinitionNode("printf", paramList, true, (params) -> {
            String[] expression =  new String[((InterpreterArrayDataType)params.get("expressions")).getArrayData().size()];
            for(int i = 0; i < ((InterpreterArrayDataType)params.get("expressions")).getArrayData().size(); i++) {
                expression[i] = (((InterpreterArrayDataType)params.get("expressions")).getArrayData().get(String.valueOf(i)).getData());
            }
            System.out.printf(params.get("formatSpecifier").getData(), expression);
            return null;
        }));
        functions.put("sprintf", new BuiltInFunctionDefinitionNode("sprintf", paramList, true, (params) -> {
            String[] expression =  new String[((InterpreterArrayDataType)params.get("expressions")).getArrayData().size()];
            for(int i = 0; i < ((InterpreterArrayDataType)params.get("expressions")).getArrayData().size(); i++) {
                expression[i] = (((InterpreterArrayDataType)params.get("expressions")).getArrayData().get(String.valueOf(i)).getData());
            }
            return String.format(params.get("formatSpecifier").getData(), expression);
        }));
        functions.put("next", new BuiltInFunctionDefinitionNode("next", new LinkedList<>(), false, (params) -> {
            lineManager.splitAndAssign();
            return null;
        }));
        paramList.clear();
        paramList.add(new Token(0, 0, Token.TokenType.WORD, "reference"));
        functions.put("getline", new BuiltInFunctionDefinitionNode("getline", paramList, false, (params) -> {
            lineManager.splitAndAssign();
            if(params.size() > 0){
                params.get("reference").setData(globalVariables.get("$0").getData());
            }
            return globalVariables.get("$0").getData();
        }));
        paramList.clear();
        paramList.add(new Token(0, 0, Token.TokenType.WORD, "regexp"));
        paramList.add(new Token(0, 0, Token.TokenType.WORD, "replacement"));
        paramList.add(new Token(0, 0, Token.TokenType.WORD, "target"));
        functions.put("gsub", new BuiltInFunctionDefinitionNode("gsub", paramList, false, (params) -> {
            Pattern stringPattern = Pattern.compile(params.get("regexp").getData());
            Matcher patternMatcher;
            int numberOfMatches = 0;
            if(params.size() == 2) {
                patternMatcher = stringPattern.matcher(globalVariables.get("$0").getData());
                while(patternMatcher.find()) {
                    numberOfMatches++;
                }
                globalVariables.get("$0").setData(globalVariables.get("$0").getData().replaceAll(params.get("regexp").getData(), params.get("replacement").getData())); //we need to actually store the result
                return String.valueOf(numberOfMatches);
            }
            patternMatcher = stringPattern.matcher(params.get("target").getData());
            while(patternMatcher.find()) {
                numberOfMatches++;
            }
            var tempString = params.get("target").getData().replaceAll(params.get("regexp").getData(), params.get("replacement").getData());
            params.get("target").setData(tempString);
            return String.valueOf(numberOfMatches);
        }));
        paramList.removeLast();
        ((BuiltInFunctionDefinitionNode)functions.get("gsub")).getAltSignatures().add(new LinkedList<>(paramList));
        paramList.add(new Token(0, 0, Token.TokenType.WORD, "target"));
        functions.put("sub", new BuiltInFunctionDefinitionNode("sub", paramList, false, (params) -> {
            Pattern stringPattern = Pattern.compile(params.get("regexp").getData());
            Matcher patternMatcher;
            int numberOfMatches = 0;
            if(params.size() == 2) {
                patternMatcher = stringPattern.matcher(globalVariables.get("$0").getData());
                if(patternMatcher.find()) {
                    numberOfMatches++;
                }
                globalVariables.get("$0").setData(globalVariables.get("$0").getData().replaceFirst(params.get("regexp").getData(), params.get("replacement").getData()));
                return String.valueOf(numberOfMatches);
            }
            patternMatcher = stringPattern.matcher(params.get("target").getData());
            if(patternMatcher.find()) {
                numberOfMatches++;
            }
            var tempString = params.get("target").getData().replaceFirst(params.get("regexp").getData(), params.get("replacement").getData());
            params.get("target").setData(tempString);
            return String.valueOf(numberOfMatches);
        }));
        paramList.removeLast();
        ((BuiltInFunctionDefinitionNode) functions.get("sub")).getAltSignatures().add(new LinkedList<>(paramList));
        paramList.clear();
        paramList.add(new Token(0, 0, Token.TokenType.WORD, "testString"));
        paramList.add(new Token(0, 0, Token.TokenType.WORD, "regexp"));
        functions.put("match", new BuiltInFunctionDefinitionNode("match", paramList, false, (params) -> {
            Pattern stringPattern = Pattern.compile(params.get("regexp").getData());
            Matcher patternMatcher = stringPattern.matcher(params.get("testString").getData());

            if(patternMatcher.find()) {
                globalVariables.put("RSTART", new InterpreterDataType(String.valueOf(patternMatcher.start() + 1)));
                globalVariables.put("RLENGTH", new InterpreterDataType(String.valueOf(patternMatcher.end() + 1 - patternMatcher.start() + 1)));
                return globalVariables.get("RSTART").getData();
            }
            else {
                globalVariables.put("RSTART", new InterpreterDataType(String.valueOf(0)));
                globalVariables.put("RLENGTH", new InterpreterDataType(String.valueOf(-1)));
                return globalVariables.get("RSTART").getData();
            }
        }));
        paramList.clear();
        paramList.add(new Token(0, 0, Token.TokenType.WORD, "testString"));
        paramList.add(new Token(0, 0, Token.TokenType.WORD, "regexp"));
        functions.put("index", new BuiltInFunctionDefinitionNode("index", paramList, false, (params) -> {
            Pattern stringPattern = Pattern.compile(params.get("regexp").getData());
            Matcher patternMatcher = stringPattern.matcher(params.get("testString").getData());

            if(patternMatcher.find()) {
                return String.valueOf(patternMatcher.start()+1);
            }
            return String.valueOf(0);
        }));
        paramList.clear();
        paramList.add(new Token(0, 0, Token.TokenType.WORD, "testString"));
        functions.put("length", new BuiltInFunctionDefinitionNode("length", paramList, false, (params) -> String.valueOf(params.get("testString").getData().length())));
        functions.put("tolower", new BuiltInFunctionDefinitionNode("tolower", paramList, false, (params) -> params.get("testString").getData().toLowerCase()));
        functions.put("toupper", new BuiltInFunctionDefinitionNode("toupper", paramList, false, (params) -> params.get("testString").getData().toUpperCase()));
        paramList.clear();
        paramList.add(new Token(0, 0, Token.TokenType.WORD, "stringToSplit"));
        paramList.add(new Token(0, 0, Token.TokenType.WORD, "targetArray"));
        paramList.add(new Token(0, 0, Token.TokenType.WORD, "fieldSeparator"));
        functions.put("split", new BuiltInFunctionDefinitionNode("split", paramList, false, (params) -> {
            String[] targetArray;
            if(params.size() == 2) {
                targetArray = params.get("stringToSplit").getData().split(globalVariables.get("FS").getData());
                for(int i = 0; i < targetArray.length; i++) {
                    ((InterpreterArrayDataType)params.get("targetArray")).getArrayData().put(String.valueOf(i), new InterpreterDataType(targetArray[i]));
                }
                return String.valueOf(targetArray.length);
            }
            targetArray = params.get("stringToSplit").getData().split(params.get("fieldSeparator").getData());
            for(int i = 0; i < targetArray.length; i++) {
                ((InterpreterArrayDataType)params.get("targetArray")).getArrayData().put(String.valueOf(i), new InterpreterDataType(targetArray[i]));
            }
            return String.valueOf(targetArray.length);
        }));
        paramList.removeLast();
        ((BuiltInFunctionDefinitionNode)functions.get("split")).getAltSignatures().add(new LinkedList<>(paramList));
        paramList.clear();
        paramList.add(new Token(0, 0, Token.TokenType.WORD, "targetString"));
        paramList.add(new Token(0, 0, Token.TokenType.WORD, "subStringStart"));
        paramList.add(new Token(0, 0, Token.TokenType.WORD, "subStringLength"));
        functions.put("substr", new BuiltInFunctionDefinitionNode("substr", paramList, false, (params) -> {
            if(params.size() == 3) {
                return params.get("targetString").getData().substring(Integer.parseInt(params.get("subStringStart").getData()) - 1, Integer.parseInt(params.get("subStringStart").getData())-1 + Integer.parseInt(params.get("subStringLength").getData()));
            }
            return params.get("targetString").getData().substring(Integer.parseInt(params.get("subStringStart").getData()) - 1);
        }));
        paramList.removeLast();
        ((BuiltInFunctionDefinitionNode)functions.get("substr")).getAltSignatures().add(new LinkedList<>(paramList));

        if(inputFile != null) {
            globalVariables.put("FILENAME", new InterpreterDataType(inputFile.getFileName().toString()));
        }
        else {
            globalVariables.put("FILENAME", new InterpreterDataType(null));
        }
        globalVariables.put("FS", new InterpreterDataType(" "));
        globalVariables.put("NF", new InterpreterDataType("0"));
        globalVariables.put("NR", new InterpreterDataType("0"));
        globalVariables.put("OFMT", new InterpreterDataType("%.6g"));
        globalVariables.put("OFS", new InterpreterDataType(" "));
        globalVariables.put("ORS", new InterpreterDataType("\n"));

        for(FunctionDefinitionNode function : programNode.getFunctionNodes()) {
            functions.put(function.getFunctionName(), function);
        }
    }

    public LineManager getLineManager() {
        return lineManager;
    }

    public HashMap<String, InterpreterDataType> getGlobalVariables() {
        return globalVariables;
    }

    public HashMap<String, FunctionDefinitionNode> getFunctions() {
        return functions;
    }

    /**
     * walks through the program node and executes the program
     */
    public void interpretProgram() {
        for(BlockNode beginBlock: program.getBeginNodes()) {
            interpretBlock(beginBlock);
        }
        lineManager.splitAndAssign();
         do {
            for(BlockNode block: program.getBlockNodes()) {
                interpretBlock(block);
            }
        } while(lineManager.splitAndAssign());
        for(BlockNode endBlock: program.getEndNodes()) {
            interpretBlock(endBlock);
        }

    }

    /**
     * interprets the blocks of a program
     *
     * @param block : a BEGIN, END, or general BLOCK
     */
    private void interpretBlock(BlockNode block) {
        var condition = "0";
        if(block.getCondition().isPresent()) {
            condition = getIDT(block.getCondition().get(), null).getData();
        }
        if(condition.compareTo("1") == 0 || block.getCondition().isEmpty()) {
            for(StatementNode statement: block.getStatements()) {
                processStatement(statement, null);
            }
        }
    }

    /**
     * interprets statements
     *
     * @param statement : the statement being evaluated
     * @param localVariables : the possible local variables of a function
     * @return the ReturnType based on AWK flow control
     */
    private ReturnType processStatement(StatementNode statement, HashMap<String, InterpreterDataType> localVariables) {
        if(statement instanceof BreakNode) {
            return new ReturnType(ReturnType.FlowControlStatement.BREAK);
        }
        else if(statement instanceof ContinueNode) {
            return new ReturnType(ReturnType.FlowControlStatement.CONTINUE);
        }
        else if(statement instanceof DeleteNode) {
            return evaluateDeleteStatement((DeleteNode) statement, localVariables);
        }
        else if(statement instanceof DoWhileNode) {
            return evaluateDoWhile((DoWhileNode) statement, localVariables);
        }
        else if(statement instanceof ForNode) {
            return evaluateForLoop((ForNode) statement, localVariables);
        }
        else if(statement instanceof ForInNode) {
            return evaluateForInLoop((ForInNode) statement, localVariables);
        }
        else if(statement instanceof IfNode) {
            return evaluateIfStatements((IfNode) statement, localVariables);
        }
        else if(statement instanceof ReturnNode) {
            if(((ReturnNode) statement).getParameter().isPresent()) {
                return new ReturnType(ReturnType.FlowControlStatement.RETURN, getIDT(((ReturnNode) statement).getParameter().get(), localVariables).getData());
            }
            return new ReturnType(ReturnType.FlowControlStatement.RETURN);
        }
        else if(statement instanceof WhileNode) {
            while(getIDT(((WhileNode) statement).getCondition(), localVariables).getData().compareTo("1") == 0) {
                var retVal = interpretListOfStatements(((WhileNode) statement).getLoopBlock().getStatements(), localVariables);
                if(retVal.getFlowControlType() == ReturnType.FlowControlStatement.BREAK) {
                    break;
                }
                else if(retVal.getFlowControlType() == ReturnType.FlowControlStatement.RETURN) {
                    return retVal;
                }
            }
            return new ReturnType(ReturnType.FlowControlStatement.NONE);
        }
        else if(!(statement instanceof AssignmentNode) && !(statement instanceof FunctionCallNode)) {
                throw new RuntimeException();
        }
        getIDT(statement, localVariables);
        return new ReturnType(ReturnType.FlowControlStatement.NONE);
    }

    /**
     * iteratively applies process statement to a block
     *
     * @param statements a list of block statements
     * @param localVariables the possible local variables of a function
     * @return a ReturnType of flow control type NONE unless another type of flow control statement is encountered first
     */
    private ReturnType interpretListOfStatements(LinkedList<StatementNode> statements, HashMap<String, InterpreterDataType> localVariables) {
        ReturnType retVal;
        for(StatementNode statement: statements) {
            retVal = processStatement(statement, localVariables);
            if(retVal.getFlowControlType() != ReturnType.FlowControlStatement.NONE) {
                return retVal;
            }
        }
        return new ReturnType(ReturnType.FlowControlStatement.NONE);
    }

    /**
     * evaluates basic AWK operations
     *
     * @param node : the operation that is currently being interpreted
     * @param localVariables : the possible local variables of a function
     * @return a new InterpreterDataType with the result of the operation
     */
    public InterpreterDataType getIDT(Node node, HashMap<String, InterpreterDataType> localVariables) {
        InterpreterDataType left;
        InterpreterDataType right;

        if(node instanceof AssignmentNode) {
            if(!(((AssignmentNode) node).getTarget() instanceof VariableReferenceNode) && !(((AssignmentNode) node).getTarget() instanceof OperationNode)) {
                throw new RuntimeException("invalid assignment target");
            }
            else if(((AssignmentNode) node).getTarget() instanceof OperationNode) {
                if(((OperationNode) ((AssignmentNode) node).getTarget()).getOperationType() != OperationNode.OperationType.FIELDREF){
                    throw new RuntimeException("invalid assignment target");
                }
            }
            right = getIDT(((AssignmentNode) node).getAssignment(), localVariables);
            left = getIDT(((AssignmentNode) node).getTarget(), localVariables);
            left.setData(right.getData());
            return left;
        }
        if(node instanceof ConstantNode) {
            return new InterpreterDataType(node.toString());
        }
        if(node instanceof FunctionCallNode) {
            return new InterpreterDataType(runFunctionCall((FunctionCallNode) node, localVariables));
        }
        if(node instanceof PatternNode) {
            throw new RuntimeException("illegal pattern placement");
        }
        if(node instanceof TernaryNode) {
            var conditional = getIDT(((TernaryNode) node).getBooleanExpression(), localVariables);
            conditional = getBooleanResult(conditional);
            if(conditional.getData().compareTo("1") == 0) {
                return getIDT(((TernaryNode) node).getTrueCase(), localVariables);
            }
            else {
                return getIDT(((TernaryNode) node).getFalseCase(), localVariables);
            }
        }
        if(node instanceof VariableReferenceNode) {
            return evaluateVariableReference((VariableReferenceNode) node, localVariables);
        }
        else {
            return evaluateOperation((OperationNode) node, localVariables);
        }
    }

    /**
     * interprets a function call
     *
     * @param node : a function call node
     * @param localVariables : the possible local variables of a function
     * @return "" for now
     */
    private String runFunctionCall(FunctionCallNode node, HashMap<String, InterpreterDataType> localVariables) {
        var map = new HashMap<String, InterpreterDataType>();
        var function = functions.get(node.getFunctionName());
        LinkedList<Token> parameters = null;
        if(function == null) {
            throw new RuntimeException("function not defined");
        }
        if(function.getParameters().size() != node.getParameters().size()) {
            if(!(function instanceof BuiltInFunctionDefinitionNode)) {
                throw new RuntimeException("function called with more parameters than defined");
            }
            else if(!((BuiltInFunctionDefinitionNode) function).isVariadic()) {
                for(LinkedList<Token> signature : ((BuiltInFunctionDefinitionNode) function).getAltSignatures()) {
                    if(signature.size() == node.getParameters().size()) {
                        parameters = signature;
                        break;
                    }
                }
                if(parameters == null) {
                    throw new RuntimeException("function called with more parameters than defined");
                }
            }
            else {
                parameters = function.getParameters();
            }
        }
        else {
            parameters = function.getParameters();
        }
        if(function instanceof BuiltInFunctionDefinitionNode) {
            if(((BuiltInFunctionDefinitionNode) function).isVariadic()) {
                for(int i = 0; i < parameters.size(); i++) {
                    if(i == parameters.size()-1) {
                        map.put(parameters.get(i).getTokenValue(), new InterpreterArrayDataType());
                        for(int j = i; j < node.getParameters().size(); j++) {
                            ((InterpreterArrayDataType)map.get(parameters.get(i).getTokenValue())).getArrayData().put(String.valueOf(j-i), getIDT(node.getParameters().get(j), localVariables));
                        }
                        break;
                    }
                    map.put(parameters.get(i).getTokenValue(), getIDT(node.getParameters().get(i), localVariables));
                }
            }
            else {
                var i = 0;
                for(Token parameterName: parameters) {
                    map.put(parameterName.getTokenValue(), getIDT(node.getParameters().get(i++), localVariables));
                }
            }
            return ((BuiltInFunctionDefinitionNode) function).execute(map);
        }
        else {
            var i = 0;
            for(Token parameterName: parameters) {
                map.put(parameterName.getTokenValue(), getIDT(node.getParameters().get(i++), localVariables));
            }
            return interpretListOfStatements(function.getStatements(), map).getReturnValue();
        }
    }

    /**
     * interprets n-length chains of if statements
     *
     * @param statement : the statement currently being evaluated
     * @param localVariables : the possible local variables of a function
     * @return the ReturnType determined from the block statements
     */
    private ReturnType evaluateIfStatements(IfNode statement, HashMap<String, InterpreterDataType> localVariables) {
        while(statement.hasNext()) {
            if(statement.getCondition().isEmpty()) {
                var retVal = interpretListOfStatements(statement.getBlockStatements().getStatements(), localVariables);
                return retVal;
            }
            else if(getIDT(statement.getCondition().get(), localVariables).getData().compareTo("1") == 0) {
                var retVal = interpretListOfStatements(statement.getBlockStatements().getStatements(), localVariables);
                return retVal;
            }
            statement = (IfNode) statement.getNext().get();
        }
        if(statement.getCondition().isEmpty()) {
            var retVal = interpretListOfStatements(statement.getBlockStatements().getStatements(), localVariables);
            return retVal;

        }
        else if(getIDT(statement.getCondition().get(), localVariables).getData().compareTo("1") == 0) {
            var retVal = interpretListOfStatements(statement.getBlockStatements().getStatements(), localVariables);
            if(retVal.getFlowControlType() != ReturnType.FlowControlStatement.NONE) {
                return retVal;
            }
        }
        return new ReturnType(ReturnType.FlowControlStatement.NONE);
    }

    /**
     * interprets a for-in loop
     *
     * @param statement : the statement currently being evaluated
     * @param localVariables : the possible local variables of a function
     * @return the corresponding ReturnType based on the AWK language
     */
    private ReturnType evaluateForInLoop(ForInNode statement, HashMap<String, InterpreterDataType> localVariables) {
        var reference = getIDT(statement.getInStatement().getRight().get(), localVariables);
        if(!(reference instanceof InterpreterArrayDataType)) {
            throw new RuntimeException("attempt to use scalar reference as array");
        }
        globalVariables.put(((VariableReferenceNode) statement.getInStatement().getLeft()).getVariableName(), new InterpreterDataType());
        for(String key: ((InterpreterArrayDataType) reference).getArrayData().keySet()) {
            globalVariables.get(((VariableReferenceNode) statement.getInStatement().getLeft()).getVariableName()).setData(key);
            var retVal = interpretListOfStatements(statement.getLoopBlock().getStatements(), localVariables);
            if(retVal.getFlowControlType() == ReturnType.FlowControlStatement.BREAK) {
                break;
            }
            else if(retVal.getFlowControlType() == ReturnType.FlowControlStatement.RETURN) {
                return retVal;
            }
        }
        return new ReturnType(ReturnType.FlowControlStatement.NONE);
    }

    /**
     * interprets a for loop
     *
     * @param statement : the statement currently being evaluated
     * @param localVariables : the possible local variables of a function
     * @return the corresponding ReturnType based on the AWK language
     */
    private ReturnType evaluateForLoop(ForNode statement, HashMap<String, InterpreterDataType> localVariables) {
        if(statement.getInitializer().isPresent()) {
            if(!(statement.getInitializer().get() instanceof StatementNode)) {
                throw new RuntimeException("invalid statement");
            }
            processStatement((StatementNode) statement.getInitializer().get(), localVariables);
        }
        if(statement.getConditional().isEmpty()) {
            while(true) {
                var retVal = interpretListOfStatements(statement.getLoopBlock().getStatements(), localVariables);
                if(retVal.getFlowControlType() == ReturnType.FlowControlStatement.BREAK) {
                    break;
                }
                else if(retVal.getFlowControlType() == ReturnType.FlowControlStatement.RETURN) {
                    return retVal;
                }
                if(statement.getPostIterationOperation().isPresent()) {
                    getIDT(statement.getPostIterationOperation().get(), localVariables);
                }
            }
            return new ReturnType(ReturnType.FlowControlStatement.NONE);
        }
        else {
            while(getIDT(statement.getConditional().get(), localVariables).getData().compareTo("1") == 0) {
                var retVal = interpretListOfStatements(statement.getLoopBlock().getStatements(), localVariables);
                if(retVal.getFlowControlType() == ReturnType.FlowControlStatement.BREAK) {
                    break;
                }
                else if(retVal.getFlowControlType() == ReturnType.FlowControlStatement.RETURN) {
                    return retVal;
                }
                if(statement.getPostIterationOperation().isPresent()) {
                    getIDT(statement.getPostIterationOperation().get(), localVariables);
                }
            }
            return new ReturnType(ReturnType.FlowControlStatement.NONE);
        }
    }

    /**
     * interprets a do-while loop
     *
     * @param statement : the statement currently being evaluated
     * @param localVariables : the possible local variables of a function
     * @return the corresponding ReturnType based on the AWK language
     */
    private ReturnType evaluateDoWhile(DoWhileNode statement, HashMap<String, InterpreterDataType> localVariables) {
        do {
            var retVal = interpretListOfStatements(statement.getLoopBlock().getStatements(), localVariables);
            if(retVal.getFlowControlType() == ReturnType.FlowControlStatement.BREAK) {
                break;
            }
            else if(retVal.getFlowControlType() == ReturnType.FlowControlStatement.RETURN) {
                return retVal;
            }

        }while(getIDT(statement.getCondition(), localVariables).getData().compareTo("1") == 0);
        return new ReturnType(ReturnType.FlowControlStatement.NONE);
    }

    /**
     * interprets a delete statement
     *
     * @param statement : the statement currently being evaluates
     * @param localVariables : the possible local variables of a function
     * @return a ReturnType of NORMAL FlowControlStatement type
     */
    private ReturnType evaluateDeleteStatement(DeleteNode statement, HashMap<String, InterpreterDataType> localVariables) {
        if(!(statement.getReference() instanceof VariableReferenceNode)) {
            throw new RuntimeException("reference expected but instead found: " + statement.getReference().toString());
        }

        if(localVariables != null) {
            if(localVariables.containsKey(((VariableReferenceNode) statement.getReference()).getVariableName())) {
                if(!(localVariables.get(((VariableReferenceNode) statement.getReference()).getVariableName()) instanceof InterpreterArrayDataType)) {
                    throw new RuntimeException("attempt to use scalar reference as an array");
                }
                if(((VariableReferenceNode) statement.getReference()).getIndexExpression().isPresent()) {
                    var index = getIDT(((VariableReferenceNode) statement.getReference()).getIndexExpression().get(), localVariables);
                    ((InterpreterArrayDataType) localVariables.get(((VariableReferenceNode) statement.getReference()).getVariableName())).getArrayData().remove(index.getData());
                    return new ReturnType(ReturnType.FlowControlStatement.NONE);
                }
                localVariables.remove(((VariableReferenceNode) statement.getReference()).getVariableName());
                return new ReturnType(ReturnType.FlowControlStatement.NONE);
            }
        }
        else if(globalVariables.containsKey(((VariableReferenceNode) statement.getReference()).getVariableName())) {
            if(!(globalVariables.get(((VariableReferenceNode) statement.getReference()).getVariableName()) instanceof InterpreterArrayDataType)) {
                throw new RuntimeException("attempt to use scalar reference as an array");
            }
            if(((VariableReferenceNode) statement.getReference()).getIndexExpression().isPresent()) {
                var index = getIDT(((VariableReferenceNode) statement.getReference()).getIndexExpression().get(), null);
                ((InterpreterArrayDataType) globalVariables.get(((VariableReferenceNode) statement.getReference()).getVariableName())).getArrayData().remove(index.getData());
                return new ReturnType(ReturnType.FlowControlStatement.NONE);
            }
            globalVariables.remove(((VariableReferenceNode) statement.getReference()).getVariableName());
            return new ReturnType(ReturnType.FlowControlStatement.NONE);
        }
        return new ReturnType(ReturnType.FlowControlStatement.NONE);
    }

    /**
     * interprets operations
     *
     * @param node : the operation that is currently being interpreted
     * @param localVariables : the possible local variables of a function
     * @return a new InterpreterDataType with the result of the operation
     */
    private InterpreterDataType evaluateOperation(OperationNode node, HashMap<String, InterpreterDataType> localVariables) {
        if (isComparisonOperation(node)) {
            return evaluateComparison(node, localVariables);
        } else if (isBooleanOperation(node)) {
            return evaluateBoolean(node, localVariables);
        } else if (node.getOperationType() == OperationNode.OperationType.MATCH || node.getOperationType() == OperationNode.OperationType.NOTMATCH) {
            return evaluateMatch(node, localVariables);
        } else if (node.getOperationType() == OperationNode.OperationType.FIELDREF) {
            return evaluateFieldReference(node, localVariables);
        } else if (node.getOperationType() == OperationNode.OperationType.PREDECREMENT || node.getOperationType() == OperationNode.OperationType.PREINCREMENT
                || node.getOperationType() == OperationNode.OperationType.POSTDECREMENT || node.getOperationType() == OperationNode.OperationType.POSTINCREMENT) {
            return evaluateIncrementAndDecrement(node, localVariables);
        } else if (node.getOperationType() == OperationNode.OperationType.UPLUS || node.getOperationType() == OperationNode.OperationType.UNEG) {
            return evaluateNegation(node, localVariables);
        } else if (node.getOperationType() == OperationNode.OperationType.IN) {
            return evaluateArrayMembership(node, localVariables);
        } else if (isMathOperation(node)) {
            return evaluateMathOperation(node, localVariables);
        } else {
            return evaluateConcatenation(node, localVariables);
        }
    }

    /**
     * interprets String concatenation
     *
     * @param node : the operation that is currently being interpreted
     * @param localVariables : the possible local variables of a function
     * @return a new InterpreterDataType with the result of the operation
     */
    private InterpreterDataType evaluateConcatenation(OperationNode node, HashMap<String, InterpreterDataType> localVariables) {
        var left = getIDT(node.getLeft(), localVariables);
        InterpreterDataType right;
        if(node.getRight().isPresent()) {
            right = getIDT(node.getRight().get(), localVariables);
            return new InterpreterDataType(left.getData() + right.getData());
        }
        return new InterpreterDataType(left.getData());
    }

    /**
     * interprets arithmetic operations
     *
     * @param node : the operation that is currently being interpreted
     * @param localVariables : the possible local variables of a function
     * @return a new InterpreterDataType with the result of the operation
     */
    private InterpreterDataType evaluateMathOperation(OperationNode node, HashMap<String, InterpreterDataType> localVariables) {
        var left = getIDT(node.getLeft(), localVariables);
        var right = getIDT(node.getRight().get(), localVariables);

        if(left.getData().compareTo("") == 0 && right.getData().compareTo("") == 0){
            left.setData("0");
            right.setData("0");
        }
        else if(left.getData().compareTo("") == 0) {
            if(isInt(right.getData())) {
                left.setData("0");
            }
            else if(isFloat(right.getData())) {
                left.setData("0.0");
            }
        }
        else if(right.getData().compareTo("") == 0) {
            if(isInt(left.getData())) {
                right.setData("0");
            }
            else if(isFloat(left.getData())) {
                right.setData("0.0");
            }
        }
        if(isInt(left.getData()) && isInt(right.getData())) {
            var leftAsInt = Integer.parseInt(left.getData());
            var rightAsInt = Integer.parseInt(right.getData());

            switch(node.getOperationType()) {
                case EXP:
                    return new InterpreterDataType(String.valueOf((int)Math.pow(leftAsInt, rightAsInt)));
                case ADD:
                    return new InterpreterDataType(String.valueOf(leftAsInt + rightAsInt));
                case SUBTRACT:
                    return new InterpreterDataType(String.valueOf(leftAsInt - rightAsInt));
                case MULTIPLY:
                    return new InterpreterDataType(String.valueOf(leftAsInt * rightAsInt));
                case DIVIDE:
                    return new InterpreterDataType(String.valueOf(leftAsInt / rightAsInt));
                default:
                    return new InterpreterDataType(String.valueOf(leftAsInt % rightAsInt));
            }
        }
        if(!isFloat(left.getData()) || !isFloat(right.getData())) {
            throw new NumberFormatException("illegal arithmetic operation");
        }
        var leftAsFloat = Float.parseFloat(left.getData());
        var rightAsFloat = Float.parseFloat(right.getData());

        switch(node.getOperationType()) {
            case EXP:
                return new InterpreterDataType(String.valueOf(Math.pow(leftAsFloat, rightAsFloat)));
            case ADD:
                return new InterpreterDataType(String.valueOf(leftAsFloat + rightAsFloat));
            case SUBTRACT:
                return new InterpreterDataType(String.valueOf(leftAsFloat - rightAsFloat));
            case MULTIPLY:
                return new InterpreterDataType(String.valueOf(leftAsFloat * rightAsFloat));
            case DIVIDE:
                return new InterpreterDataType(String.valueOf(leftAsFloat / rightAsFloat));
            default:
                return new InterpreterDataType(String.valueOf(leftAsFloat % rightAsFloat));
        }
    }

    /**
     * interprets array membership operations
     *
     * @param node : the operation that is currently being interpreted
     * @param localVariables : the possible local variables of a function
     * @return a new InterpreterDataType with the result of the operation
     */
    private InterpreterDataType evaluateArrayMembership(OperationNode node, HashMap<String, InterpreterDataType> localVariables) {
        var left = getIDT(node.getLeft(), localVariables);
        var right = getIDT(node.getRight().get(), localVariables);
        if(!(right instanceof InterpreterArrayDataType)) {
            throw new RuntimeException("illegal membership operation." + node.getRight().get() + "is not an array");
        }
        if(((InterpreterArrayDataType) right).getArrayData().containsKey(left.getData())) {
            return new InterpreterDataType("1");
        }
        return new InterpreterDataType("0");
    }

    /**
     * interprets unary negations and positive operations
     *
     * @param node : the operation that is currently being interpreted
     * @param localVariables : the possible local variables of a function
     * @return
     */
    private InterpreterDataType evaluateNegation(OperationNode node, HashMap<String, InterpreterDataType> localVariables) {
        var left  = getIDT(node.getLeft(), localVariables);

        if(left.getData().compareTo("") == 0) {
            left.setData("0");
        }
        if(isInt(left.getData())) {
            var leftAsInt = Integer.parseInt(left.getData());
            if(node.getOperationType() == OperationNode.OperationType.UNEG) {
                return new InterpreterDataType(String.valueOf(leftAsInt * -1));
            }
            return new InterpreterDataType(String.valueOf(leftAsInt));

        }
        if(!isFloat(left.getData())) {
            throw new NumberFormatException("cannot cast " + node.getLeft() + " to Float");
        }
        var leftAsFloat = Float.parseFloat(left.getData());
        if(node.getOperationType() == OperationNode.OperationType.UNEG) {
            return new InterpreterDataType(String.valueOf(leftAsFloat * -1));
        }
        return new InterpreterDataType(String.valueOf(leftAsFloat));
    }

    /**
     * evaluates the basic increment and decrement operations
     *
     * @param node : the operation that is currently being interpreted
     * @param localVariables : the possible local variables of a function
     * @return a new InterpreterDataType with the result of the operation
     */
    private InterpreterDataType evaluateIncrementAndDecrement(OperationNode node, HashMap<String, InterpreterDataType> localVariables) {
        var left = getIDT(node.getLeft(), localVariables);

        if(left.getData().compareTo("") == 0) {
            left.setData("0");
        }
        if(isInt(left.getData())) {
            var leftAsInt = Integer.parseInt(left.getData());
            if (node.getOperationType() == OperationNode.OperationType.PREDECREMENT) {
                left.setData(String.valueOf(leftAsInt - 1));
                return left;
            } else if (node.getOperationType() == OperationNode.OperationType.PREINCREMENT) {
                left.setData(String.valueOf(leftAsInt + 1));
                return left;
            } else if (node.getOperationType() == OperationNode.OperationType.POSTDECREMENT) {
                left.setData(String.valueOf(leftAsInt - 1));
                return left;
            } else {
                left.setData(String.valueOf(leftAsInt + 1));
                return left;
            }

        }
        
        if (!isFloat(left.getData())) {
            throw new NumberFormatException("cannot cast " + node.getLeft() + " to Float");
        }
        var leftAsFloat = Float.parseFloat(left.getData());

        if (node.getOperationType() == OperationNode.OperationType.PREDECREMENT) {
            left.setData(String.valueOf(leftAsFloat - 1));
            return left;
        } else if (node.getOperationType() == OperationNode.OperationType.PREINCREMENT) {
            left.setData(String.valueOf(leftAsFloat + 1));
            return left;
        } else if (node.getOperationType() == OperationNode.OperationType.POSTDECREMENT) {
            var retVal = new InterpreterDataType(left.getData());
            left.setData(String.valueOf(leftAsFloat - 1));
            return retVal;
        } else {
            var retVal = new InterpreterDataType(left.getData());
            left.setData(String.valueOf(leftAsFloat + 1));
            return retVal;
        }
    }

    /**
     * interprets a field reference
     *
     * @param node : the operation that is currently being interpreted
     * @param localVariables : the possible local variables of a function
     * @return
     */
    private InterpreterDataType evaluateFieldReference(OperationNode node, HashMap<String, InterpreterDataType> localVariables) {
        var left = getIDT(node.getLeft(), localVariables);
        if(globalVariables.containsKey("$" + left.getData())) {
            return globalVariables.get("$" + left.getData());
        }
        globalVariables.put("$" + left.getData(), new InterpreterDataType(""));
        return globalVariables.get("$" + left.getData());
    }

    /**
     * interprets a basic pattern matching operation
     *
     * @param node : the operation that is currently being interpreted
     * @param localVariables : the possible local variables of a function
     * @return
     */
    private InterpreterDataType evaluateMatch(OperationNode node, HashMap<String, InterpreterDataType> localVariables) {
        var pattern = Pattern.compile(((PatternNode)node.getRight().get()).getRegexPattern());
        var matcher = pattern.matcher(getIDT(node.getLeft(), localVariables).getData());
        if(node.getOperationType() == OperationNode.OperationType.MATCH) {
            if(matcher.find()) {
                return new InterpreterDataType("1");
            }
            return new InterpreterDataType("0");
        }
        if(matcher.find()) {
            return new InterpreterDataType("0");
        }
        return new InterpreterDataType("1");
    }

    /**
     * interprets a basic logical operation
     *
     * @param node : the operation that is currently being interpreted
     * @param localVariables : the possible local variables of a function
     * @return a new InterpreterDataType with the result of the operation
     */
    private InterpreterDataType evaluateBoolean(OperationNode node, HashMap<String, InterpreterDataType> localVariables) {
        if(node.getOperationType() == OperationNode.OperationType.AND) {
            var left = getIDT(node.getLeft(), localVariables);
            var right = getIDT(node.getRight().get(), localVariables);
            left = getBooleanResult(left);
            right = getBooleanResult(right);
            if(left.getData().compareTo("1") == 0 && right.getData().compareTo("1") == 0) {
                return new InterpreterDataType("1");
            }
            return new InterpreterDataType("0");
        }
        if(node.getOperationType() == OperationNode.OperationType.OR) {
            var left = getIDT(node.getLeft(), localVariables);
            var right = getIDT(node.getRight().get(), localVariables);
            left = getBooleanResult(left);
            right = getBooleanResult(right);
            if(left.getData().compareTo("1") == 0 || right.getData().compareTo("1") == 0) {
                return new InterpreterDataType("1");
            }
            return new InterpreterDataType("0");
        }
        var left = getIDT(node.getLeft(), localVariables);
        left = getBooleanResult(left);
        if(left.getData().compareTo("1") == 0) {
            return new InterpreterDataType("0");
        }
        return new InterpreterDataType("1");
    }

    /**
     * interprets a basic comparison statement
     *
     * @param node : the operation that is currently being interpreted
     * @param localVariables  : the possible local variables of a function
     * @return a new InterpreterDataType with the result of the comparison
     */
    private InterpreterDataType evaluateComparison(OperationNode node, HashMap<String, InterpreterDataType> localVariables) {
        var left = getIDT(node.getLeft(), localVariables);
        var right = getIDT(node.getRight().get(), localVariables);
        float leftAsFloat;
        float rightAsFloat;

        if(isFloat(left.getData()) && isFloat(right.getData())){
            leftAsFloat = Float.parseFloat(left.getData());
            rightAsFloat = Float.parseFloat(right.getData());
            switch (node.getOperationType()) {
                case EQUALTO:
                    if(leftAsFloat == rightAsFloat){
                        return new InterpreterDataType("1");
                    }
                    return new InterpreterDataType("0");
                case NOTEQUALTO:
                    if(leftAsFloat != rightAsFloat) {
                        return new InterpreterDataType("1");
                    }
                    return new InterpreterDataType("0");
                case LESSTHAN:
                    if(leftAsFloat < rightAsFloat) {
                        return new InterpreterDataType("1");
                    }
                    return new InterpreterDataType("0");
                case LESSOREQUAL:
                    if(leftAsFloat <= rightAsFloat) {
                        return new InterpreterDataType("1");
                    }
                    return new InterpreterDataType("0");
                case GREATERTHAN:
                    if(leftAsFloat > rightAsFloat) {
                        return new InterpreterDataType("1");
                    }
                    return new InterpreterDataType("0");

                default:
                    if(leftAsFloat >= rightAsFloat) {
                        return new InterpreterDataType("1");
                    }
                    return new InterpreterDataType("0");
            }
        }
        switch (node.getOperationType()) {
            case EQUALTO:
                if(left.getData().compareTo(right.getData()) == 0) {
                    return new InterpreterDataType("1");
                }
                return new InterpreterDataType("0");
            case NOTEQUALTO:
                if(left.getData().compareTo(right.getData()) != 0) {
                    return new InterpreterDataType("1");
                }
                return new InterpreterDataType("0");
            case LESSTHAN:
                if(left.getData().compareTo(right.getData()) < 0) {
                    return new InterpreterDataType("1");
                }
                return new InterpreterDataType("0");
            case LESSOREQUAL:
                if(left.getData().compareTo(right.getData()) <= 0) {
                    return new InterpreterDataType("1");
                }
                return new InterpreterDataType("0");
            case GREATERTHAN:
                if(left.getData().compareTo(right.getData()) > 0) {
                    return new InterpreterDataType("1");
                }
                return new InterpreterDataType("0");
            default:
                if(left.getData().compareTo(right.getData()) >= 0) {
                    return new InterpreterDataType("1");
                }
                return new InterpreterDataType("0");
        }
    }

    /**
     * interprets a variable reference
     *
     * @param node : the operation that is currently being interpreted
     * @param localVariables : the possible local variables of a function
     * @return a new InterpreterDataType with the result of the operation
     */
    private InterpreterDataType evaluateVariableReference(VariableReferenceNode node, HashMap<String, InterpreterDataType> localVariables) {
        InterpreterDataType indexExpression;
        if(node.getIndexExpression().isPresent()) {
            indexExpression = getIDT(node.getIndexExpression().get(), localVariables);
            if(localVariables != null) {
                if (localVariables.containsKey(node.getVariableName())) {
                    if (!(localVariables.get(node.getVariableName()) instanceof InterpreterArrayDataType)) {
                        throw new RuntimeException("reference to element in " + node.getVariableName() + " but " + node.getVariableName() + " is not an array");
                    }
                    if (!((InterpreterArrayDataType) localVariables.get(node.getVariableName())).getArrayData().containsKey(indexExpression.getData())) {
                        ((InterpreterArrayDataType) localVariables.get(node.getVariableName())).getArrayData().put(indexExpression.getData(), new InterpreterDataType());
                    }
                    return ((InterpreterArrayDataType) localVariables.get(node.getVariableName())).getArrayData().get(indexExpression.getData());
                }
            }
            else if(globalVariables.containsKey(node.getVariableName())) {
                if(!(globalVariables.get(node.getVariableName()) instanceof InterpreterArrayDataType)) {
                    throw new RuntimeException("reference to element in " + node.getVariableName() + " but " + node.getVariableName() + " is not an array");
                }
                if (!((InterpreterArrayDataType) globalVariables.get(node.getVariableName())).getArrayData().containsKey(indexExpression.getData())) {
                    ((InterpreterArrayDataType) globalVariables.get(node.getVariableName())).getArrayData().put(indexExpression.getData(), new InterpreterDataType());
                }
                return ((InterpreterArrayDataType) globalVariables.get(node.getVariableName())).getArrayData().get(indexExpression.getData());
            }
            else {
                globalVariables.put(node.getVariableName(), new InterpreterArrayDataType());
                ((InterpreterArrayDataType)globalVariables.get(node.getVariableName())).getArrayData().put(indexExpression.getData(), new InterpreterDataType());
                return ((InterpreterArrayDataType) globalVariables.get(node.getVariableName())).getArrayData().get(indexExpression.getData());
            }
        }
        if(localVariables != null) {
            if (localVariables.containsKey(node.getVariableName())) {
                return localVariables.get(node.getVariableName());
            }
        }
        if(globalVariables.containsKey(node.getVariableName())) {
            return globalVariables.get(node.getVariableName());
        }
        globalVariables.put(node.getVariableName(), new InterpreterDataType());
        return globalVariables.get(node.getVariableName());
    }

    /**
     * this method simplifies evaluating boolean operations by
     * mapping all non-zero floats to true and everything else to false based
     * on the AWK programming language.
     *
     * @param idt : an InterpreterDataType
     * @return an InterpreterDataType with a boolean value
     */
    private InterpreterDataType getBooleanResult(InterpreterDataType idt) {
        if(isFloat(idt.getData())) {
            var dataAsFloat = Float.parseFloat(idt.getData());
            if(dataAsFloat != 0) {
                return new InterpreterDataType("1");
            }
        }
        return new InterpreterDataType("0");
    }

    /**
     * @param node : the operation that is currently being interpreted
     * @return true if the operator is a comparison operator
     */
    private boolean isComparisonOperation(OperationNode node) {
        if(node.getOperationType() == OperationNode.OperationType.EQUALTO || node.getOperationType() == OperationNode.OperationType.NOTEQUALTO
                || node.getOperationType() == OperationNode.OperationType.LESSTHAN || node.getOperationType() == OperationNode.OperationType.LESSOREQUAL
                || node.getOperationType() == OperationNode.OperationType.GREATERTHAN || node.getOperationType() == OperationNode.OperationType.GREATEROREQUAL) {
            return true;
        }
        return false;
    }

    /**
     * @param node : the operation that is currently being interpreted
     * @return true if the operation belongs to the logical operations
     */
    private boolean isBooleanOperation(OperationNode node) {
        if(node.getOperationType() == OperationNode.OperationType.AND || node.getOperationType() == OperationNode.OperationType.OR
                || node.getOperationType() == OperationNode.OperationType.NOT) {
            return true;
        }
        return false;
    }

    /**
     * @param node : the operation that is currently being interpreted
     * @return true if the operation belongs to the arithmetic operations
     */
    private boolean isMathOperation(OperationNode node) {
        if(node.getOperationType() == OperationNode.OperationType.EXP || node.getOperationType() == OperationNode.OperationType.ADD
                || node.getOperationType() == OperationNode.OperationType.SUBTRACT || node.getOperationType() == OperationNode.OperationType.MULTIPLY
                || node.getOperationType() == OperationNode.OperationType.DIVIDE || node.getOperationType() == OperationNode.OperationType.MODULO) {
            return true;
        }
        return false;
    }

    /**
     * @param data : a String to parse
     * @return true if the String parses as a float
     */
    private boolean isFloat(String data) {
        try{
            Float.parseFloat(data);
        }
        catch (NumberFormatException numberFormatException) {
            return false;
        }
        return true;
    }

    /**
     * @param data : a String to parse
     * @return  true if the String parses as an integer
     */
    private boolean isInt(String data) {
        try{
            Integer.parseInt(data);
        }
        catch (NumberFormatException numberFormatException) {
            return false;
        }
        return true;
    }
}
