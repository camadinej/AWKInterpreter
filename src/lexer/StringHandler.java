package lexer;
/**
 * This class is meant for accessing and tracking the position
 * in an AWK file
 *
 * @author Jake Camadine
 */
public class StringHandler {

    private String awkFile;

    private int index;


    public StringHandler(String data) {
        awkFile = data;
        index = -1;
    }

    /**
     * for checking the character i spaces ahead in the sequence while maintaining current position.
     *
     * @param i - the number of spaces ahead of the current index to check in the sequence
     * @return the character i spaces from the current position
     */
    public char peek(int i) {
        if((index + i) < awkFile.length()) {
            return awkFile.charAt(index + i);
        }
        else {
            throw new StringIndexOutOfBoundsException("Past end of file");
        }
    }

    /**
     * for checking the string composed of the next i characters.
     *
     * @param i - the length of the String following the current character
     * @return the String i characters ahead of the current character
     */
    public String peekString(int i) {
        int tempIndex = index;

        StringBuilder accumulator =  new StringBuilder();

        if(index + i < awkFile.length()) {
            while (tempIndex < (index + i)) {
                tempIndex++;
                accumulator.append(awkFile.charAt(tempIndex));
            }
            return accumulator.toString();
        }
        else {
            throw new StringIndexOutOfBoundsException("Past end of file");
        }
    }

    /**
     * returns the next character and advances the index.
     *
     * @return the next character in the awk file
     */
    public char getChar() {
        if (index + 1 < awkFile.length()) {
            return awkFile.charAt(++index);
        }
        else {
            throw new StringIndexOutOfBoundsException("Past end of file");
        }
    }

    /**
     * increments the index i spaces.
     *
     * @param i - the number of spaces to increment
     */
    public void swallow(int i) {
        if(index + 1 < awkFile.length()) {
            index += i;
        }
        else {
            throw new StringIndexOutOfBoundsException("Past end of file");
        }
    }

    /**
     * @return true if the index is greater than or equal to the length of the file
     */
    public boolean isDone() {
        if(index >= (awkFile.length()-1)) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * @return the remaining text in the file
     */
    public String remainder() {
        return awkFile.substring(index, awkFile.length());
    }


}
