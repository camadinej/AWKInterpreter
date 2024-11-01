package lexer;
import lexer.StringHandler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * this class is a lexicographical analyzer that breaks a language down into respective tokens.
 *
 * @author Jake Camadine
 */
public class Lexer {
    private HashMap<String, Token> keyWordMap;
    private HashMap<String, Token> oneCharSymbolMap;
    private HashMap<String, Token> twoCharSymbolMap;

    private StringHandler stringHandler;

    private int lineNumber;
    private int linePosition;

    public Lexer(String awkFile) {
        keyWordMap = getKeyWordMap();
        twoCharSymbolMap = getTwoCharSymbolMap();
        oneCharSymbolMap = getOneCharSymbolMap();
        stringHandler = new StringHandler(awkFile);
        lineNumber = 1;
        linePosition = 1;
    }

    /**
     * @return - a hashmap from Strings of keywords to Tokens of
     * corresponding keywords
     */
    private HashMap<String, Token> getKeyWordMap() {
        HashMap<String, Token> tempMap = new HashMap<>(19);
        String keyWordList = "while,if,do," +
                "for,break,continue," +
                "else,return,BEGIN," +
                "END,print,printf," +
                "next,in,delete," +
                "getline,exit,nextfile," +
                "function";
        StringTokenizer tokenizer = new StringTokenizer(keyWordList, ",");

        while(tokenizer.hasMoreTokens()) {
            String keyWord = tokenizer.nextToken();
            tempMap.put(keyWord, new Token(0, 0, Token.getTokenConstant(keyWord)));
        }
        return tempMap;
    }

    /**
     * @return a hashmap of from Strings of two character symbols to corresponding Tokens
     */
    private HashMap<String, Token> getTwoCharSymbolMap() {
        HashMap<String, Token> tempMap = new HashMap<>(16);
        String twoSymbolList = ">=,++,--,<=,==,!=,^=,%=," +
                                "*=,/=,+=,-=,!~,&&,>>,||";
        String twoSymbolType = "GREATERTHANOREQUALTO,INCREMENT,DECREMENT," +
                                "LESSTHANOREQUALTO,EQUALS,NOTEQUALTO,RAISEASSIGN," +
                                "MODASSIGN,MULTASSIGN,DIVASSIGN,SUMASSIGN,SUBTRACTASSIGN,DOESNOTMATCH,AND,APPEND,OR";

        StringTokenizer tokenizer = new StringTokenizer(twoSymbolList, ",");
        StringTokenizer typeTokenizer = new StringTokenizer(twoSymbolType, ",");

        while(tokenizer.hasMoreTokens()) {
            String twoCharSymbol = tokenizer.nextToken();
            String twoCharSymbolType = typeTokenizer.nextToken();
            tempMap.put(twoCharSymbol, new Token(0, 0, Token.getTokenConstant(twoCharSymbolType)));
        }
        return tempMap;
    }

    /**
     * @return a hashmap from Strings of one character symbols to corresponding Tokens
     */
    private HashMap<String, Token> getOneCharSymbolMap() {
        HashMap<String, Token> tempMap = new HashMap<>(23);
        String oneSymbolList = "{,},[,],(,),$,~,=,<,>,!,+,^,-,?,:,*,/,%,|";
        String oneSymbolType = "LEFTBRACE,RIGHTBRACE,LEFTBRACKET," +
                "RIGHTBRACKET,LEFTPARENTHESIS,RIGHTPARANTHESIS," +
                "FIELD,MATCH,ASSIGN,LESSTHAN,GREATERTHAN," +
                "NOT,PLUS,EXP,MINUS,QUESTIONMARK,COLON," +
                "MULTIPLY,FORWARDSLASH,MOD,VERTICALBAR,";

        StringTokenizer tokenizer = new StringTokenizer(oneSymbolList, ",");
        StringTokenizer typeTokenizer = new StringTokenizer(oneSymbolType, ",");

        while(tokenizer.hasMoreTokens()) {
            String oneCharSymbol = tokenizer.nextToken();
            String oneCharSymbolType = typeTokenizer.nextToken();
            tempMap.put(oneCharSymbol, new Token(0,0, Token.getTokenConstant(oneCharSymbolType)));
        }
        tempMap.put(",", new Token(0, 0, Token.getTokenConstant("COMMA")));
        tempMap.put("\n", new Token(0,0, Token.getTokenConstant("SEPARATOR")));
        tempMap.put(";", new Token(0,0, Token.getTokenConstant("SEPARATOR")));
        return tempMap;
    }

    /**
     * @return the AWK file as a LinkedList of tokens
     */
    public LinkedList<Token> lex() {
        LinkedList<Token> tokenList = new LinkedList<>();
        Token tempToken;

        while(!stringHandler.isDone()) {
            if(stringHandler.peek(1) == ' ' || stringHandler.peek(1) == '\t') { //spaces and tabs
                stringHandler.swallow(1);
                linePosition++;
            }else if(isLetter(stringHandler.peek(1))) {
                tokenList.add(processWord());
            }else if (isDigit(stringHandler.peek(1)) || stringHandler.peek(1) == '.') {
                tokenList.add(processNumber());
            }else if(stringHandler.peek(1) == '"') {
                tokenList.add(processStringLiteral());
            }else if(stringHandler.peek(1) == '\n') {
                tokenList.add(new Token(lineNumber, ++linePosition, Token.getTokenConstant("SEPARATOR")));
                lineNumber++;
                linePosition = 0;
                stringHandler.swallow(1);
            }else if(stringHandler.peek(1) == '\r') {
                stringHandler.swallow(1);
            }else if(stringHandler.peek(1) == '#') {
                processComment();
            }else if(stringHandler.peek(1) == '`') {
                tokenList.add(processPattern());
            }else {
                tokenList.add(processSymbol());
            }
        }
        tokenList.add(new Token(lineNumber, linePosition, Token.getTokenConstant("SEPARATOR")));
        return tokenList;
    }

    /**
     * @return either a WORD or corresponding keyWord token based on the sequence of letters and underscores
     * encountered by the stringHandler
     */
    private Token processWord() {
        int wordStartPosition = ++linePosition;
        StringBuilder tokenValue = new StringBuilder();

        while(!stringHandler.isDone()) {
            if(isLetter(stringHandler.peek(1)) || isDigit(stringHandler.peek(1)) || stringHandler.peek(1) == '_') {
                tokenValue.append(stringHandler.getChar());
                linePosition++;
            }
            else {
                break;
            }
        }
        if(keyWordMap.get(tokenValue.toString()) != null) {
            return new Token(lineNumber, wordStartPosition, Token.getTokenConstant(keyWordMap.get(tokenValue.toString()).toString()));
        }
       else { return new Token(lineNumber, wordStartPosition, Token.getTokenConstant("WORD"), tokenValue.toString()); }
    }

    /**
     * @return a NUMBER token based on the sequence of numbers and decimals encountered by the stringHandler
     */
    private Token processNumber() {
        int numberStartPosition = ++linePosition;
        boolean decimalPresent = false;
        StringBuilder tokenValue = new StringBuilder();

        while(!stringHandler.isDone()) {
            if(!decimalPresent && stringHandler.peek(1) == '.'){
                tokenValue.append(stringHandler.getChar());
                linePosition++;
                decimalPresent = true;
            }
            if(stringHandler.isDone()) {
                break;
            }
            if (isDigit(stringHandler.peek(1))) {
                tokenValue.append(stringHandler.getChar());
                linePosition++;
            }
            else {
                break;
            }
        }
        return new Token(lineNumber, numberStartPosition, Token.getTokenConstant("NUMBER"), tokenValue.toString());
    }

    /**
     * @return a String Literal token containing all characters between the quote the method was called on and the final quote in the literal
     */
    private Token processStringLiteral() {
        int literalStartPosition = ++linePosition;
        StringBuilder literalValue = new StringBuilder();

        stringHandler.swallow(1);
        try{
            if (stringHandler.peek(1) == '"') {
                stringHandler.swallow(1);
                linePosition++;
                return new Token(lineNumber, literalStartPosition, Token.getTokenConstant("STRINGLITERAL"), "");
            }
        }catch (StringIndexOutOfBoundsException s) {
            throw new UnsupportedOperationException("unterminated literal");
        }
        try {
            while (stringHandler.peek(1) != '"' && stringHandler.peek(1) != '\"') {
                if (stringHandler.peek(1) == '\\') {
                    stringHandler.swallow(1);
                    linePosition++;
                    if (stringHandler.peek(1) == '"' || stringHandler.peek(1) == '\"') {
                        literalValue.append(stringHandler.getChar());
                        linePosition++;
                    }
                } else {
                    literalValue.append(stringHandler.getChar());
                    linePosition++;
                }
            }
        }catch(StringIndexOutOfBoundsException endQuoteNotFound) {
            throw new UnsupportedOperationException("unterminated literal");
        }
        stringHandler.swallow(1);
        linePosition++;
        return new Token(lineNumber, literalStartPosition, Token.getTokenConstant("STRINGLITERAL"), literalValue.toString());
    }

    /**
     * increments Lexer's stringHandler past commenting in an AWK program
     */
    private void processComment() {
        while(!stringHandler.isDone()) {
            if(stringHandler.peek(1) != '\n') {
                stringHandler.swallow(1);
            }
            else{
                lineNumber++;
                linePosition = 0;
                return;
            }
        }
    }

    /**
     * @return a regex token composed of the characters encountered by the stringHandler up until the next backtick is found.
     */
    private Token processPattern() {
       int patternStartPosition = ++linePosition;
       StringBuilder patternValue = new StringBuilder();
       stringHandler.swallow(1);

        if(stringHandler.peek(1) == '`') {
            stringHandler.swallow(1);
            linePosition++;
            return new Token(lineNumber, patternStartPosition, Token.getTokenConstant("REGEXP"), "");
        }
        try {
            while (stringHandler.peek(1) != '`') {
                if (stringHandler.peek(1) == '\\') {
                    stringHandler.swallow(1);
                    linePosition++;
                    if (stringHandler.peek(1) == '`') {
                        patternValue.append(stringHandler.getChar());
                        linePosition++;
                    }
                } else {
                    patternValue.append(stringHandler.getChar());
                    linePosition++;
                }
            }
        }catch (StringIndexOutOfBoundsException s) {
            throw new UnsupportedOperationException("unterminated regexp");
        }
        stringHandler.swallow(1);
        linePosition++;
        return new Token(lineNumber, patternStartPosition, Token.getTokenConstant("REGEXP"), patternValue.toString());
    }

    /**
     * @return a token corresponding to the symbols encountered by stringHandler and matching tokens in the symbol maps
     */
    private Token processSymbol() {
        int symbolStartPosition = ++linePosition;
        String tempString;

        try {
            if(twoCharSymbolMap.get(stringHandler.peekString(2)) != null) {
                tempString = stringHandler.peekString(2);
                linePosition += 2;
                stringHandler.swallow(2);
                return new Token(lineNumber, symbolStartPosition, Token.getTokenConstant(twoCharSymbolMap.get(tempString).toString()));
            }
        }catch(StringIndexOutOfBoundsException s) {
            if(oneCharSymbolMap.get(stringHandler.peekString(1)) != null) {
                tempString = stringHandler.peekString(1);
                linePosition++;
                stringHandler.swallow(1);
                return new Token(lineNumber, symbolStartPosition, Token.getTokenConstant(oneCharSymbolMap.get(tempString).toString()));
            }
        }

        if(oneCharSymbolMap.get(stringHandler.peekString(1)) != null) {
            tempString = stringHandler.peekString(1);
            linePosition++;
            stringHandler.swallow(1);
            return new Token(lineNumber, symbolStartPosition, Token.getTokenConstant(oneCharSymbolMap.get(tempString).toString()));
        }
        throw new UnsupportedOperationException("unrecognized symbol line: " + lineNumber + "position: " + linePosition);
    }

    /**
     * @param charToCheck - the character being type-checked.
     * @return true if the character is within the ASCII value range(inclusive)
     */
    public boolean isLetter(char charToCheck) {
        if(Character.toUpperCase(charToCheck) >= 65 && Character.toUpperCase(charToCheck) <= 90) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * @param charToCheck - the character being type-checked
     * @return true if the character is an integer
     */
    private boolean isDigit(char charToCheck) {
        try {
            Integer.parseInt(Character.toString(charToCheck));
            return true;
        }catch(NumberFormatException nfe) {
            return false;
        }
    }
}
