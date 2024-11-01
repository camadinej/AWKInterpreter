package lexer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

/**
 * This class is the entry point for passing AWK files to a Lexer
 *
 * @author Jake Camadine
 */
public class Main {
    public static void main(String[] args) throws IOException {
        Path awkFilePath = Paths.get(args[0]);

        String fileContent = new String(Files.readAllBytes(awkFilePath));
        Lexer lexer = new Lexer(fileContent);
        System.out.println(lexer.lex());
    }
}
