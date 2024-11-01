package parser;

import lexer.Token;

import java.util.LinkedList;
import java.util.Optional;

/**
 * This class serves as a recursive decent parser for the AWK language.
 *
 * @author Jake Camadine
 */
public class Parser {
    private TokenHandler tokenHandler;

    public Parser(LinkedList<Token> tokens) { tokenHandler = new TokenHandler(tokens); }

    /**
     * constructs a symbol tree based on the AWK programming language.
     *
     * @return the root of the symbol tree
     */
    public ProgramNode parse() {
        ProgramNode parsedProgram = new ProgramNode();

        while(tokenHandler.moreTokens()) {
            if(!parseFunction(parsedProgram) && !parseAction(parsedProgram)) {
                throw new UnsupportedOperationException("unexpected token");
            }
        }
        return parsedProgram;
    }

    /**
     * checks BEGIN blocks, END blocks, and pattern-action statements for syntactic correctness.
     * Populates the proper nodes with the proper data.
     *
     * @param programNode - the root of the symbol tree
     * @return true if a BEGIN block, END block, or pattern-action statement was parsed successfully
     */
    public boolean parseAction(ProgramNode programNode) {
        Optional<Token> begin = tokenHandler.matchAndRemove(Token.TokenType.BEGIN);
        Optional<BlockNode> blockNode;

        if(begin.isPresent()) {
            blockNode = parseBlock();
            if(blockNode.isPresent()) {
                programNode.getBeginNodes().add(blockNode.get());
                return true;
            }
            else {
                throw new UnsupportedOperationException("left brace expected after BEGIN token");
            }
        }
        begin = tokenHandler.matchAndRemove(Token.TokenType.END);
        if(begin.isPresent()) {
            blockNode = parseBlock();
            if(blockNode.isPresent()) {
                programNode.getEndNodes().add(blockNode.get());
                return true;
            }
            else {
                throw new UnsupportedOperationException("left brace expected after END token");
            }
        }
        blockNode = parseBlock();
        if(blockNode.isPresent()) {
            programNode.getBlockNodes().add(blockNode.get());
            return true;
        }
        return false;
    }

    /**
     * checks a function declaration for syntactic correctness. Populates the proper nodes in the symbol tree
     * with the proper data.
     *
     * @param programNode - the root of the symbol tree
     * @return true if a function has successfully been parsed
     */
    public boolean parseFunction(ProgramNode programNode) {
        Optional<Token> tempToken = tokenHandler.matchAndRemove(Token.TokenType.FUNCTION);
        FunctionDefinitionNode functionDefinition;
        String functionName;
        LinkedList<Token> parameters = new LinkedList<>();
        Optional<BlockNode> functionBlock;

        if(tempToken.isPresent()) {
            tempToken = tokenHandler.matchAndRemove(Token.TokenType.WORD);
            if(tempToken.isPresent()) {
                functionName = tempToken.get().getTokenValue();
            }
            else {
                throw new UnsupportedOperationException("function name expected");
            }
            tempToken = tokenHandler.matchAndRemove(Token.TokenType.LEFTPARENTHESIS);
            if(tempToken.isEmpty()) {
                throw new UnsupportedOperationException("left parenthesis expected");
            }
            while(tempToken.isPresent()) {
                acceptSeparators();
                tempToken = tokenHandler.matchAndRemove(Token.TokenType.WORD);
                if(!tempToken.isEmpty()) {
                    parameters.add(tempToken.get());
                }
                else {
                    break;
                }
                tempToken = tokenHandler.matchAndRemove(Token.TokenType.COMMA);
                if(tempToken.isPresent()) {
                    acceptSeparators();
                }
            }
            tempToken = tokenHandler.matchAndRemove(Token.TokenType.RIGHTPARANTHESIS);
            if(tempToken.isEmpty()) {
                throw new UnsupportedOperationException("unexpected token");
            }
            functionDefinition = new FunctionDefinitionNode(functionName, parameters);
            functionBlock = parseBlock();
            if(functionBlock.isEmpty()) {
                throw new UnsupportedOperationException("function definition with no block");
            }
            functionDefinition.getStatements().addAll(functionBlock.get().getStatements());
            programNode.getFunctionNodes().add(functionDefinition);
            return true;
        }
        return false;
    }

    /**
     * checks a block for syntactic correctness.
     *
     * @return a BlockNode based on whether a condition is present and whether it is a single or multiline block
     */
    public Optional<BlockNode> parseBlock() {
        BlockNode retBlockNode;
        Optional<StatementNode> statement;
        Optional<Node> condition;

        acceptSeparators();
        condition = parseOperation();
        if(tokenHandler.matchAndRemove(Token.TokenType.LEFTBRACE).isPresent()) {
            if(condition.isEmpty()) {
                retBlockNode = new BlockNode();
            }
            else {
                retBlockNode = new BlockNode(condition.get());
            }
            acceptSeparators();
            do {
                statement = parseStatement();
                if(statement.isPresent()) {
                    retBlockNode.getStatements().add(statement.get());
                }
                acceptSeparators();
            } while(statement.isPresent());
            if(tokenHandler.matchAndRemove(Token.TokenType.RIGHTBRACE).isPresent()) {
                acceptSeparators();
                return Optional.of(retBlockNode);
            }
            else {
                throw new UnsupportedOperationException("closing brace expected");
            }
        }
        else if(condition.isPresent()) {
            acceptSeparators();
            if(condition.get() instanceof StatementNode) {
                retBlockNode = new BlockNode();
                retBlockNode.getStatements().add((StatementNode) condition.get());
                return Optional.of(retBlockNode);
            }
        }
        else {
            statement = parseStatement();
            if(statement.isPresent()) {
                retBlockNode = new BlockNode();
                retBlockNode.getStatements().add(statement.get());
                return Optional.of(retBlockNode);
            }
        }
        return Optional.empty();

    }

    /**
     * parses statements written in the AWK programming language.
     *
     * @return the first statement that successfully parses. Returns empty if none do.
     */
    private Optional<StatementNode> parseStatement() {
        Optional<StatementNode> statement;
        Optional<Node> operation;

        statement = parseContinue();
        if(statement.isPresent()) {
            return statement;
        }
        statement = parseBreak();
        if(statement.isPresent()) {
            return statement;
        }
        statement = parseIf();
        if(statement.isPresent()) {
            return statement;
        }
        statement = parseFor();
        if(statement.isPresent()) {
            return statement;
        }
        statement  = parseDelete();
        if(statement.isPresent()) {
            return statement;
        }
        statement = parseWhile();
        if(statement.isPresent()) {
            return statement;
        }
        statement = parseDoWhile();
        if(statement.isPresent()) {
            return statement;
        }
        statement = parseReturn();
        if(statement.isPresent()) {
            return statement;
        }
        operation = parseOperation();
        if(operation.isPresent()) {
            if(operation.get() instanceof StatementNode) {
                return Optional.of((StatementNode)operation.get());
            }
        }
        return Optional.empty();
    }

    /**
     * checks a continue statement for syntactic correctness and
     * creates the appropriate statement node.
     *
     * @return a continue node based on the present tokens
     */
    private Optional<StatementNode> parseContinue() {

        if(tokenHandler.matchAndRemove(Token.TokenType.CONTINUE).isPresent()) {
            return Optional.of(new ContinueNode());
        }
        return Optional.empty();
    }

    /**
     * checks a break statement for syntactic correctness and
     * creates the appropriate statement node.
     *
     * @return a break node based on the present tokens
     */
    private Optional<StatementNode> parseBreak() {
        if(tokenHandler.matchAndRemove(Token.TokenType.BREAK).isPresent()) {
            return Optional.of(new BreakNode());
        }
        return Optional.empty();
    }

    /**
     * checks an if statement for syntactic correctness and
     * creates the appropriate statement node. Creates a linked list
     * of chained conditional statements when appropriate.
     *
     * @return an IfNode based on the present tokens
     */
    private Optional<StatementNode> parseIf() {
        Optional<Node> condition;
        Optional<BlockNode> blockStatements;
        IfNode retNode;

        if(tokenHandler.matchAndRemove(Token.TokenType.IF).isPresent()) {
            if (tokenHandler.matchAndRemove(Token.TokenType.LEFTPARENTHESIS).isPresent()) {
                condition = parseOperation();
                if (condition.isEmpty()) {
                    throw new UnsupportedOperationException("condition expected inside if parenthesis");
                }
                if (tokenHandler.matchAndRemove(Token.TokenType.RIGHTPARANTHESIS).isEmpty()) {
                    throw new UnsupportedOperationException("right parenthesis expected");
                }
                blockStatements = parseBlock();
                if(blockStatements.isEmpty()) {
                    throw new UnsupportedOperationException("single or multiline block expected");
                }
                retNode = new IfNode(condition, blockStatements.get());
                if(tokenHandler.matchAndRemove(Token.TokenType.ELSE).isPresent()) {
                    if(tokenHandler.peek(0).isPresent()) {
                        if (tokenHandler.peek(0).get().getType() == Token.TokenType.IF) {
                            retNode.setNext(parseIf());
                            return Optional.of(retNode);
                        }
                    }
                    blockStatements = parseBlock();
                    retNode.setNext(Optional.of(new IfNode(Optional.empty(), blockStatements.get())));
                    return Optional.of(retNode);
                }
                return Optional.of(retNode);
            } else {
                throw new UnsupportedOperationException("left parenthesis expected after if");
            }
        }
        return Optional.empty();
    }

    /**
     * checks for loops for syntactic correctness and creates the
     * appropriate statement node based on whether a membership operation is present.
     *
     * @return a ForInNode or ForNode based on the present tokens
     */
    private Optional<StatementNode> parseFor() {
        Boolean inTokenFound = false;
        Optional<Node> membershipOperation;
        Optional<Node> initializer;
        Optional<Node> conditional;
        Optional<Node> postIterationOperation;
        Optional<BlockNode> loopBlock;

        if(tokenHandler.matchAndRemove(Token.TokenType.FOR).isPresent()) {
            if(tokenHandler.matchAndRemove(Token.TokenType.LEFTPARENTHESIS).isPresent()) {
                for(int i = 0; tokenHandler.peek(i).isPresent(); i++) {
                    if (tokenHandler.peek(i).get().getType() == Token.TokenType.IN) {
                        inTokenFound = true;
                        break;
                    }
                    if (tokenHandler.peek(i).get().getType() == Token.TokenType.RIGHTPARANTHESIS) {
                        break;
                    }
                }
                if(inTokenFound) {
                    membershipOperation = parseOperation();
                    if(tokenHandler.matchAndRemove(Token.TokenType.RIGHTPARANTHESIS).isEmpty()) {
                        throw new UnsupportedOperationException("right parenthesis expected");
                    }
                    loopBlock = parseBlock();
                    if(loopBlock.isEmpty()) {
                        throw new UnsupportedOperationException("single or multiline block expected");
                    }
                    return Optional.of(new ForInNode((OperationNode)membershipOperation.get(), loopBlock.get()));
                }
                else {
                    initializer = parseOperation();
                    acceptSeparators();
                    conditional = parseOperation();
                    acceptSeparators();
                    postIterationOperation = parseOperation();
                    if(tokenHandler.matchAndRemove(Token.TokenType.RIGHTPARANTHESIS).isEmpty()) {
                        throw new UnsupportedOperationException("right parenthesis expected");
                    }
                    loopBlock = parseBlock();
                    if(loopBlock.isEmpty()) {
                        throw new UnsupportedOperationException("single or multiline block expected");
                    }
                    return Optional.of(new ForNode(initializer, conditional, postIterationOperation, loopBlock.get()));
                }
            }
            else {
                throw new UnsupportedOperationException("left parenthesis expected after for");
            }
        }
        return Optional.empty();
    }

    /**
     * checks a delete statement for syntactic correctness
     * and creates the appropriate statement node.
     *
     * @return a delete node based on the present tokens
     */
    private Optional<StatementNode> parseDelete() {
        Optional<Node> reference;
        if(tokenHandler.matchAndRemove(Token.TokenType.DELETE).isPresent()) {
            reference = parseOperation();
            //throw exception if reference is empty?
            if(reference.isEmpty()) {
                throw new UnsupportedOperationException("reference expected after delete");
            }
            return Optional.of(new DeleteNode(reference.get()));
        }
        return Optional.empty();
    }

    /**
     * checks while loops for syntactic correctness and
     * creates the appropriate statement node.
     *
     * @return a while node based on the present tokens
     */
    private Optional<StatementNode> parseWhile() {
        Optional<Node> condition;
        Optional<BlockNode> loopBlock;

        if(tokenHandler.matchAndRemove(Token.TokenType.WHILE).isPresent()) {
            if(tokenHandler.matchAndRemove(Token.TokenType.LEFTPARENTHESIS).isPresent()){
                condition = parseOperation();
                if(condition.isEmpty()) {
                    throw new UnsupportedOperationException("condition expected inside parenthesis");
                }
                if(tokenHandler.matchAndRemove(Token.TokenType.RIGHTPARANTHESIS).isEmpty()) {
                    throw new UnsupportedOperationException("right parenthesis expected");
                }
                loopBlock = parseBlock();
                if(loopBlock.isPresent()) {
                    return Optional.of(new WhileNode(condition.get(), loopBlock.get()));
                }
                else {
                    throw new UnsupportedOperationException("missing loop block");
                }
            }
            else {
                throw new UnsupportedOperationException("left parenthesis expected");
            }
        }
        return Optional.empty();
    }

    /**
     * checks do-while loops for syntactic correctness
     * and creates the appropriate statement node.
     *
     * @return a DoWhileNode based on the present tokens
     */
    private Optional<StatementNode> parseDoWhile() {
        Optional<Node> condition;
        Optional<BlockNode> loopBlock;

        if(tokenHandler.matchAndRemove(Token.TokenType.DO).isPresent()) {
            loopBlock = parseBlock();
            if(loopBlock.isEmpty()) {
                throw new UnsupportedOperationException("single or multi-line block expected");
            }
            if(tokenHandler.matchAndRemove(Token.TokenType.WHILE).isPresent()) {
                if(tokenHandler.matchAndRemove(Token.TokenType.LEFTPARENTHESIS).isPresent()) {
                    condition = parseOperation();
                    if(condition.isEmpty()) {
                        throw new UnsupportedOperationException("expression expected inside parenthesis");
                    }
                    if(tokenHandler.matchAndRemove(Token.TokenType.RIGHTPARANTHESIS).isEmpty()) {
                        throw new UnsupportedOperationException("right parenthesis expected");
                    }
                    return Optional.of(new DoWhileNode(condition.get(), loopBlock.get()));
                }
            }
            else {
                throw new UnsupportedOperationException("while expected");
            }

        }
        return Optional.empty();
    }

    /**
     * checks return statements for syntactic correctness
     * and creates and appropriate statement node.
     *
     * @return a ReturnNode based on the present tokens
     */
    private Optional<StatementNode> parseReturn() {
        Optional<Node> parameter;

        if(tokenHandler.matchAndRemove(Token.TokenType.RETURN).isPresent()) {
            parameter = parseOperation();
            if(parameter.isPresent()) {
                return Optional.of(new ReturnNode(parameter));
            }
            return Optional.of(new ReturnNode());
        }
        return Optional.empty();
    }

    /**
     * checks a function call for syntactic correctness
     * and creates and appropriate statement node.
     *
     * @return a FunctionCallNode based on the present tokens
     */
    private Optional<StatementNode> parseFunctionCall() {
        LinkedList<Node> parameterList = new LinkedList<>();
        Optional<Node> parameter;
        String functionName;
        boolean leftParenIsPresent = false;

        if(tokenHandler.matchAndRemove(Token.TokenType.GETLINE).isPresent()) {
            parameter = parseBottomLevel();
            if(parameter.isPresent()) {
                parameterList.add(parameter.get());
            }
            return Optional.of(new FunctionCallNode("getline", parameterList));
        }
        if(tokenHandler.matchAndRemove(Token.TokenType.PRINT).isPresent()) {
            if(tokenHandler.matchAndRemove(Token.TokenType.LEFTPARENTHESIS).isPresent()) {
                leftParenIsPresent = true;
            }
            do{
                parameter = parseOperation();
                if(parameter.isPresent()) {
                    parameterList.add(parameter.get());
                }
            }while (tokenHandler.matchAndRemove(Token.TokenType.COMMA).isPresent());
            if(leftParenIsPresent) {
                if(tokenHandler.matchAndRemove(Token.TokenType.RIGHTPARANTHESIS).isEmpty()) {
                    throw new UnsupportedOperationException("closing parenthesis expected");
                }
            }
            return Optional.of(new FunctionCallNode("print", parameterList));
        }
        if(tokenHandler.matchAndRemove(Token.TokenType.PRINTF).isPresent()) {
            if(tokenHandler.matchAndRemove(Token.TokenType.LEFTPARENTHESIS).isPresent()) {
                leftParenIsPresent = true;
            }
            do{
                parameter = parseOperation();
                if(parameter.isPresent()) {
                    parameterList.add(parameter.get());
                }
            }while (tokenHandler.matchAndRemove(Token.TokenType.COMMA).isPresent());
            if(leftParenIsPresent) {
                if(tokenHandler.matchAndRemove(Token.TokenType.RIGHTPARANTHESIS).isEmpty()) {
                    throw new UnsupportedOperationException("closing parenthesis expected");
                }
            }
            return Optional.of(new FunctionCallNode("printf", parameterList));
        }
        if(tokenHandler.matchAndRemove(Token.TokenType.EXIT).isPresent()) {
            parameter = parseOperation();
            if(parameter.isPresent()) {
                parameterList.add(parameter.get());
            }
            return Optional.of(new FunctionCallNode("exit", parameterList));
        }
        if(tokenHandler.matchAndRemove(Token.TokenType.NEXTFILE).isPresent()) {
            return Optional.of(new FunctionCallNode("nextfile", parameterList));
        }
        if(tokenHandler.matchAndRemove(Token.TokenType.NEXT).isPresent()) {
            return Optional.of(new FunctionCallNode("next", parameterList));
        }
        if(isFunctionCall()) {
            functionName = tokenHandler.matchAndRemove(Token.TokenType.WORD).get().getTokenValue();
            tokenHandler.matchAndRemove(Token.TokenType.LEFTPARENTHESIS);
            while(tokenHandler.matchAndRemove(Token.TokenType.RIGHTPARANTHESIS).isEmpty()) {
                acceptSeparators();
                parameter = parseOperation();
                if(parameter.isPresent()) {
                    parameterList.add(parameter.get());
                }
                tokenHandler.matchAndRemove(Token.TokenType.COMMA);
            }
            return Optional.of(new FunctionCallNode(functionName, parameterList));
        }
        return Optional.empty();
    }

    /**
     * checks operations for syntactic correctness.
     *
     * @return a subtree of operations based on the AWK expression
     */
    public Optional<Node> parseOperation() {
        Optional<Node> left = parseTernary();
        Optional<Node> right;

        if(tokenHandler.matchAndRemove(Token.TokenType.ASSIGN).isPresent()) {
            right = parseOperation();
            if(left.isEmpty() || right.isEmpty()) {
                throw new UnsupportedOperationException("expression expected before and after assignment operator");
            }
            return Optional.of(new AssignmentNode(left.get(), right.get()));
        }
        if(tokenHandler.matchAndRemove(Token.TokenType.SUMASSIGN).isPresent()) {
            right = parseOperation();
            if(left.isEmpty() || right.isEmpty()) {
                throw new UnsupportedOperationException("expression expected before and after assignment operator");
            }
            return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, OperationNode.OperationType.ADD)));
        }
        if(tokenHandler.matchAndRemove(Token.TokenType.SUBTRACTASSIGN).isPresent()) {
            right = parseOperation();
            if(left.isEmpty() || right.isEmpty()) {
                throw new UnsupportedOperationException("expression expected before and after assignment operator");
            }
            return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, OperationNode.OperationType.SUBTRACT)));
        }
        if(tokenHandler.matchAndRemove(Token.TokenType.MULTASSIGN).isPresent()) {
            right = parseOperation();
            if(left.isEmpty() || right.isEmpty()) {
                throw new UnsupportedOperationException("expression expected before and after assignment operator");
            }
            return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, OperationNode.OperationType.MULTIPLY)));
        }
        if(tokenHandler.matchAndRemove(Token.TokenType.DIVASSIGN).isPresent()) {
            right = parseOperation();
            if(left.isEmpty() || right.isEmpty()) {
                throw new UnsupportedOperationException("expression expected before and after assignment operator");
            }
            return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, OperationNode.OperationType.DIVIDE)));
        }
        if(tokenHandler.matchAndRemove(Token.TokenType.MODASSIGN).isPresent()) {
            right = parseOperation();
            if(left.isEmpty() || right.isEmpty()) {
                throw new UnsupportedOperationException("expression expected before and after assignment operator");
            }
            return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, OperationNode.OperationType.MODULO)));
        }
        if(tokenHandler.matchAndRemove(Token.TokenType.RAISEASSIGN).isPresent()) {
            right = parseOperation();
            if(left.isEmpty() || right.isEmpty()) {
                throw new UnsupportedOperationException("expression expected before and after assignment operator");
            }
            return Optional.of(new AssignmentNode(left.get(), new OperationNode(left.get(), right, OperationNode.OperationType.EXP)));
        }
        return left;

    }

    /**
     * Checks ternary expressions for syntactic correctness.
     *
     * @return a ternary node if the operator is present. Returns the result of other parsing methods otherwise.
     */
    private Optional<Node> parseTernary() {
        Optional<Node> booleanExpression = parseLogicalOr();
        Optional<Node> trueCase;
        Optional<Node> falseCase;

        if(tokenHandler.matchAndRemove(Token.TokenType.QUESTIONMARK).isPresent()) {
            trueCase = parseOperation();
            if(trueCase.isEmpty() || booleanExpression.isEmpty()) {
                throw new UnsupportedOperationException("expression expected before and after ternary operator");
            }
            if(tokenHandler.matchAndRemove(Token.TokenType.COLON).isPresent()) {
                falseCase = parseOperation();
                if(falseCase.isEmpty()) {
                    throw new UnsupportedOperationException("expression expected before and after colon");
                }
            }
            else {
                throw new UnsupportedOperationException("colon expected after true case in ternary expression");
            }
            return Optional.of(new TernaryNode(booleanExpression.get(), trueCase.get(), falseCase.get()));
        }
        return booleanExpression;
    }

    /**
     * checks logical or statements for syntactic correctness.
     *
     * @return an operation node based of this operation if the operator is present. Returns the result of the next parsing method otherwise..
     */
    private Optional<Node> parseLogicalOr() {
        Optional<Node> left = parseLogicalAnd();
        Optional<Node> right;
        Optional<Token> operation = tokenHandler.matchAndRemove(Token.TokenType.OR);

        do {
            if(operation.isPresent()) {
                right = parseLogicalAnd();
                if(right.isEmpty() || left.isEmpty()) {
                    throw new UnsupportedOperationException("expression expected after conditional");
                }
                left = Optional.of(new OperationNode(left.get(),right, OperationNode.OperationType.OR));
                operation = tokenHandler.matchAndRemove(Token.TokenType.OR);
            }
            else {
                return left;
            }
        } while(true);
    }

    /**
     * checks logical and statements for syntactic correctness.
     *
     * @return an operation node based of this operation if the operator is present. Returns the result of the next parsing method otherwise.
     */
    private Optional<Node> parseLogicalAnd() {
        Optional<Node> left = parseArrayMembership();
        Optional<Node> right;
        Optional<Token> operation = tokenHandler.matchAndRemove(Token.TokenType.AND);

        do {
            if(operation.isPresent()) {
                right = parseArrayMembership();
                if(right.isEmpty() || left.isEmpty()) {
                    throw new UnsupportedOperationException("expression expected after conditional");
                }
                left = Optional.of(new OperationNode(left.get(),right, OperationNode.OperationType.AND));
                operation = tokenHandler.matchAndRemove(Token.TokenType.AND);
            }
            else {
                return left;
            }
        } while(true);
    }

    /**
     * checks array membership statements for syntactic correctness.
     *
     * @return an operation node based of this operation if the operator is present. Returns the result of the next parsing method otherwise.
     */
    private Optional<Node> parseArrayMembership() {
        Optional<Node> left = parseMatch();
        Optional<Node> right;
        Optional<Token> tempToken;

        do {
            if(tokenHandler.matchAndRemove(Token.TokenType.IN).isPresent()) {
                tempToken = tokenHandler.matchAndRemove(Token.TokenType.WORD);
                if(tempToken.isEmpty() || left.isEmpty()) {
                    throw new UnsupportedOperationException("expression expected before and after membership operator");
                }
                right = Optional.of(new VariableReferenceNode(tempToken.get().getTokenValue()));
                left = Optional.of(new OperationNode(left.get(), right, OperationNode.OperationType.IN));
            }
            else {
                return left;
            }

        } while(true);
    }

    /**
     * checks match and non-match for syntactic correctness.
     *
     * @return an operation node based of this operation if the operator is present. Returns the result of the next parsing method otherwise.
     */
    private Optional<Node> parseMatch() {
        Optional<Node> left = parseComparison();
        Optional<Node> right;

        if(tokenHandler.matchAndRemove(Token.TokenType.MATCH).isPresent()) {
            right = parseComparison();
            if(right.isEmpty() || left.isEmpty()) {
                throw new UnsupportedOperationException("expression expected before and after match");
            }
            else if(!(right.get() instanceof PatternNode)) {
                throw new UnsupportedOperationException("pattern expected after match");
            }
            return Optional.of(new OperationNode(left.get(), right, OperationNode.OperationType.MATCH));
        }
        else if(tokenHandler.matchAndRemove(Token.TokenType.DOESNOTMATCH).isPresent()) {
            right = parseComparison();
            if(right.isEmpty() || left.isEmpty()) {
                throw new UnsupportedOperationException("expression expected before and after not-match");
            }
            else if(!(right.get() instanceof PatternNode)) {
                throw new UnsupportedOperationException("pattern expected after not-match");
            }
            return Optional.of(new OperationNode(left.get(), right, OperationNode.OperationType.NOTMATCH));
        }
        return left;
    }

    /**
     * checks boolean comparisons for syntactic correctness.
     *
     * @return an operation node based of this operation if the operator is present. Returns the result of the next parsing method otherwise.
     */
    private Optional<Node> parseComparison() {
        Optional<Node> left = parseStringConcatenation();
        Optional<Node> right;
        Optional<Token> operation;

        operation = tokenHandler.matchAndRemove(Token.TokenType.LESSTHAN);
        if(operation.isEmpty()) {
            operation = tokenHandler.matchAndRemove(Token.TokenType.LESSTHANOREQUALTO);
        }
        else {
            right = parseStringConcatenation();
            if(left.isEmpty() || right.isEmpty()) {
                throw new UnsupportedOperationException("expression expected before and after conditional operator");
            }
            return Optional.of(new OperationNode(left.get(), right, OperationNode.OperationType.LESSTHAN));
        }
        if(operation.isEmpty()) {
            operation = tokenHandler.matchAndRemove(Token.TokenType.NOTEQUALTO);
        }
        else {
            right = parseStringConcatenation();
            if(left.isEmpty() || right.isEmpty()) {
                throw new UnsupportedOperationException("expression expected before and after conditional operator");
            }
            return Optional.of(new OperationNode(left.get(), right, OperationNode.OperationType.LESSOREQUAL));
        }
        if(operation.isEmpty()) {
            operation = tokenHandler.matchAndRemove(Token.TokenType.EQUALS);
        }
        else {
            right = parseStringConcatenation();
            if(left.isEmpty() || right.isEmpty()) {
                throw new UnsupportedOperationException("expression expected before and after conditional operator");
            }
            return Optional.of(new OperationNode(left.get(), right, OperationNode.OperationType.NOTEQUALTO));
        }
        if(operation.isEmpty()) {
            operation = tokenHandler.matchAndRemove(Token.TokenType.GREATERTHAN);
        }
        else {
            right = parseStringConcatenation();
            if(left.isEmpty() || right.isEmpty()) {
                throw new UnsupportedOperationException("expression expected before and after conditional operator");
            }
            return Optional.of(new OperationNode(left.get(), right, OperationNode.OperationType.EQUALTO));
        }
        if(operation.isEmpty()) {
            operation =  tokenHandler.matchAndRemove(Token.TokenType.GREATERTHANOREQUALTO);
        }
        else {
            right = parseStringConcatenation();
            if(left.isEmpty() || right.isEmpty()) {
                throw new UnsupportedOperationException("expression expected before and after conditional operator");
            }
            return Optional.of(new OperationNode(left.get(), right, OperationNode.OperationType.GREATERTHAN));
        }
        if(operation.isEmpty()) {
            return left;
        }
        else {
            right = parseStringConcatenation();
            if(left.isEmpty() || right.isEmpty()) {
                throw new UnsupportedOperationException("expression expected before and after conditional operator");
            }
            return Optional.of(new OperationNode(left.get(), right, OperationNode.OperationType.GREATEROREQUAL));
        }
    }

    /**
     * checks syntactic correctness in String concatenation.
     *
     * @return an operation node based of this operation if the operator is present. Returns the result of another parsing method otherwise.
     */
    private Optional<Node> parseStringConcatenation() {
        Optional<Node> left = parseTerm();
        Optional<Node> right;

        do{
            right = parseTerm();
            if(right.isPresent()) {
                left = Optional.of(new OperationNode(left.get(), right, OperationNode.OperationType.CONCATENATE));
            }
            else {
                return left;
            }
        } while(true);
    }

    /**
     * checks syntactic correctness in addition and subtraction.
     *
     * @return an operation node based of this operation if the operator is present. Returns the result of the next parsing method otherwise.
     */
    private Optional<Node> parseTerm() {
        Optional<Node> left  = parseFactor();
        Optional<Node> right;

        do {
            if(tokenHandler.matchAndRemove(Token.TokenType.PLUS).isPresent()) {
                right = parseFactor();
                if(left.isEmpty() || right.isEmpty()) {
                    throw new UnsupportedOperationException("terms expected before and after addition operator");
                }
                left = Optional.of(new OperationNode(left.get(), right, OperationNode.OperationType.ADD));
            }
            else if(tokenHandler.matchAndRemove(Token.TokenType.MINUS).isPresent()) {
                right = parseFactor();
                if(left.isEmpty() || right.isEmpty()) {
                    throw new UnsupportedOperationException("terms expected before and after subtraction operator");
                }
                left = Optional.of(new OperationNode(left.get(),right, OperationNode.OperationType.SUBTRACT));
            }
            else {
                return left;
            }
        } while(true);
    }

    /**
     * checks syntactic correctness in multiplication division and modulus.
     *
     * @return an operation node based of this operation if the operator is present. Returns the result of the next parsing method otherwise.
     */
    private Optional<Node> parseFactor() {
        Optional<Node> left = parseExponent();
        Optional<Node> right;

        do {
            if(tokenHandler.matchAndRemove(Token.TokenType.MULTIPLY).isPresent()) {
                right = parseExponent();
                if(left.isEmpty() || right.isEmpty()) {
                    throw new UnsupportedOperationException("factors expected before and after multiplication operator");
                }
                left = Optional.of(new OperationNode(left.get(), right, OperationNode.OperationType.MULTIPLY));
            }
            else if(tokenHandler.matchAndRemove(Token.TokenType.FORWARDSLASH).isPresent()) {
                right = parseExponent();
                if(left.isEmpty() || right.isEmpty()) {
                    throw new UnsupportedOperationException("factors expected before and after division operator");
                }
                left = Optional.of(new OperationNode(left.get(), right, OperationNode.OperationType.DIVIDE));
            }
            else if(tokenHandler.matchAndRemove(Token.TokenType.MOD).isPresent()) {
                right = parseExponent();
                if(left.isEmpty() || right.isEmpty()) {
                    throw new UnsupportedOperationException("expressions expected before and after modulus");
                }
                left = Optional.of(new OperationNode(left.get(), right, OperationNode.OperationType.MODULO));
            }
            else {
                return left;
            }
        } while(true);
    }

    /**
     * checks syntactic correctness in exponential expressions.
     *
     * @return an operation node based of this operation if the operator is present. Returns the result of the next parsing method otherwise.
     */
    private Optional<Node> parseExponent() {
        Optional<Node> left = parsePostIncrementAndDecrement();
        Optional<Node> right;

        if(tokenHandler.matchAndRemove(Token.TokenType.EXP).isPresent()) {
            right = parseExponent();
            if(left.isEmpty() || right.isEmpty()) {
                throw new UnsupportedOperationException("expression expected before and after exponential");
            }
            return Optional.of(new OperationNode(left.get(), right, OperationNode.OperationType.EXP));
        }
        return left;
    }

    /**
     * checks syntactic correctness in post incrementing and decrementing.
     *
     * @return an operation node of the proper operation if present. Return the next
     */
    private Optional<Node> parsePostIncrementAndDecrement() {
        Optional<Node> tempNode = parseBottomLevel();

        if(tempNode.isPresent()) {
            if(tokenHandler.matchAndRemove(Token.TokenType.INCREMENT).isPresent()) {
                return Optional.of(new AssignmentNode(tempNode.get(), new OperationNode(tempNode.get(), OperationNode.OperationType.POSTINCREMENT)));
            }
            if(tokenHandler.matchAndRemove(Token.TokenType.DECREMENT).isPresent()) {
                return Optional.of(new AssignmentNode(tempNode.get(), new OperationNode(tempNode.get(), OperationNode.OperationType.POSTDECREMENT)));
            }
        }
        return tempNode;
    }

    /**
     * checks basic operations for syntactical correctness.
     *
     * @return a corresponding node, if present.
     */
    private Optional<Node> parseBottomLevel() {
        Optional<Node> tempNode;
        Optional<StatementNode> functionCall;
        Optional<Token> tempToken = tokenHandler.matchAndRemove(Token.TokenType.STRINGLITERAL);

        if(tempToken.isPresent()) {
            return Optional.of(new ConstantNode(tempToken.get().getTokenValue()));
        }
        tempToken = tokenHandler.matchAndRemove(Token.TokenType.NUMBER);
        if(tempToken.isPresent()) {
            return Optional.of(new ConstantNode(tempToken.get().getTokenValue()));
        }
        tempToken = tokenHandler.matchAndRemove(Token.TokenType.REGEXP);
        if(tempToken.isPresent()) {
            return Optional.of(new PatternNode(tempToken.get().getTokenValue()));
        }
        if(tokenHandler.matchAndRemove(Token.TokenType.LEFTPARENTHESIS).isPresent()) {
            tempNode = parseOperation();
            if(tempNode.isPresent()) {
                if(tokenHandler.matchAndRemove(Token.TokenType.RIGHTPARANTHESIS).isPresent()) {
                    return tempNode;
                }
                throw new UnsupportedOperationException("closing parenthesis expected");
            }
        }
        if(tokenHandler.matchAndRemove(Token.TokenType.NOT).isPresent()) {
            tempNode = parseOperation();
            if(tempNode.isPresent()) {
                return Optional.of(new OperationNode(tempNode.get(), OperationNode.OperationType.NOT));
            }
            throw new UnsupportedOperationException("expression expected after '!'");
        }
        if(tokenHandler.matchAndRemove(Token.TokenType.MINUS).isPresent()) {
            tempNode = parseOperation();
            if(tempNode.isPresent()) {
                return Optional.of(new OperationNode(tempNode.get(), OperationNode.OperationType.UNEG));
            }
            throw new UnsupportedOperationException("expression expected after '-' operator");
        }
        if(tokenHandler.matchAndRemove(Token.TokenType.PLUS).isPresent()) {
            tempNode = parseOperation();
            if(tempNode.isPresent()) {
                return Optional.of(new OperationNode(tempNode.get(), OperationNode.OperationType.UPLUS));
            }
            throw new UnsupportedOperationException("expression expected after '+' operator");
        }
        if(tokenHandler.matchAndRemove(Token.TokenType.INCREMENT).isPresent()) {
            tempNode = parseLValue();
            if(tempNode.isPresent()) {
                return Optional.of(new AssignmentNode(tempNode.get(), new OperationNode(tempNode.get(), OperationNode.OperationType.PREINCREMENT)));
            }
            throw new UnsupportedOperationException("reference expected");
        }
        if(tokenHandler.matchAndRemove(Token.TokenType.DECREMENT).isPresent()) {
            tempNode = parseLValue();
            if(tempNode.isPresent()) {
                return Optional.of(new AssignmentNode(tempNode.get(), new OperationNode(tempNode.get(), OperationNode.OperationType.PREDECREMENT)));
            }
            throw new UnsupportedOperationException("reference expected");
        }
        functionCall = parseFunctionCall();
        if(functionCall.isPresent()) {
            return Optional.of(functionCall.get());
        }
        return parseLValue();
    }

    /**
     * checks syntactical correctness in fields, variable names, and array references.
     *
     * @return a corresponding node, if present.
     */
    private Optional<Node> parseLValue() {
        Optional<Node> tempNode;
        Optional<Token> tempToken;

        if(tokenHandler.matchAndRemove(Token.TokenType.FIELD).isPresent()) {
            tempNode = parseBottomLevel();
            if(tempNode.isPresent()) {
                return Optional.of(new OperationNode(tempNode.get(), OperationNode.OperationType.FIELDREF));
            }
            throw new UnsupportedOperationException("field reference expected");
        }
        tempToken = tokenHandler.matchAndRemove(Token.TokenType.WORD);
        if(tempToken.isPresent()) {
            if(tokenHandler.matchAndRemove(Token.TokenType.LEFTBRACKET).isPresent()) {
                tempNode = parseOperation();
                if(tempNode.isPresent()) {
                    if(tokenHandler.matchAndRemove(Token.TokenType.RIGHTBRACKET).isPresent()) {
                        return Optional.of(new VariableReferenceNode(tempToken.get().getTokenValue(), tempNode.get()));
                    }
                    throw new UnsupportedOperationException("closing bracket expected");
                }
            }
            return Optional.of(new VariableReferenceNode(tempToken.get().getTokenValue()));
        }
        return Optional.empty();
    }

    /**
     * matches and removes expected separators.
     *
     * @return true if separators have been removed
     */
    public boolean acceptSeparators() {
        Optional<Token> tempToken;
        boolean separatorFound = false;

        do {
            tempToken = tokenHandler.matchAndRemove(Token.TokenType.SEPARATOR);
            if(!tempToken.equals(Optional.empty())) {
                separatorFound = true;
            }
        } while(tempToken.isPresent() && tokenHandler.moreTokens());

        return separatorFound;
    }

    /**
     * looks ahead through the list of tokens to determine if there is
     * a function call present.
     *
     * @return true if a group of tokens is of the form of a function call in
     * in the AWK programming language
     */
    public boolean isFunctionCall() {
        if(tokenHandler.peek(0).isPresent()) {
            if(tokenHandler.peek(0).get().getType() == Token.TokenType.WORD) {
                if(tokenHandler.peek(1).isPresent()) {
                    if(tokenHandler.peek(1).get().getType() == Token.TokenType.LEFTPARENTHESIS){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
