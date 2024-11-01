package parser;

/**
 * this class represents a regular expression in the
 * AWK programming language
 *
 * @author Jake Camadine
 */
public class PatternNode extends Node{
    private String regexPattern;

    public PatternNode(String regexPattern) {
        this.regexPattern = regexPattern;
    }

    /**
     * @return the pattern without toString's formatting
     */
    public String getRegexPattern() {
        return regexPattern;
    }

    @Override
    public String toString() {
        return "regex: " + regexPattern;
    }
}
