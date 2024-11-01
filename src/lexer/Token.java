package lexer;
/**
 * This class is meant to tokenize a language
 * based on whether they are words, numbers, or different kinds of spaces between them
 *
 * @author Jake Camadine
 */
public class Token {
    public  enum TokenType {
        WORD, NUMBER, SEPARATOR,
        WHILE, IF, DO, FOR, BREAK, CONTINUE, ELSE, RETURN, BEGIN, END, PRINT, PRINTF, NEXT, IN, DELETE, GETLINE, EXIT, NEXTFILE, FUNCTION, STRINGLITERAL,
        GREATERTHANOREQUALTO, INCREMENT, DECREMENT, LESSTHANOREQUALTO, EQUALS, NOTEQUALTO, RAISEASSIGN, MODASSIGN, MULTASSIGN, DIVASSIGN, SUMASSIGN, SUBTRACTASSIGN, DOESNOTMATCH,
        AND, APPEND, OR,
        LEFTBRACE, RIGHTBRACE, LEFTBRACKET, RIGHTBRACKET, LEFTPARENTHESIS, RIGHTPARANTHESIS, FIELD, MATCH, ASSIGN, LESSTHAN, GREATERTHAN, NOT, PLUS, EXP, MINUS, QUESTIONMARK, COLON,
        MULTIPLY, FORWARDSLASH, MOD, VERTICALBAR, COMMA,
        REGEXP
    };
    private TokenType token;
    private String tokenValue;

    private int lineNumber;
    private int tokenStartPosition;

    public Token(int lineNum, int startPosition, TokenType token) {
        lineNumber = lineNum;
        tokenStartPosition = startPosition;
        this.token = token;
    }
    public Token(int lineNum, int startPosition, TokenType token, String value) {
        this(lineNum, startPosition, token);
        tokenValue = value;
    }

    /**
     * @param value - a String equal to any of TokenType's values
     * @return a constant of TokenType
     */
    public static TokenType getTokenConstant(String value) {
        return TokenType.valueOf(value.toUpperCase());
    }

    /**
     * @return the value associated with the respective token
     */
    public String getTokenValue() {
        return tokenValue;
    }

    /**
     * @return the Token's String representation
     */
    @Override
    public String toString() {
        if(tokenValue == null) {
            return token.toString();
        }
        return token.toString() + "(" + tokenValue + ")";
    }

    /**
     * @return the enum constant of this Token
     */
    public TokenType getType(){
        return token;
    }


}
