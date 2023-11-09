package Crux;

import Crux.Lexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Scala {
    static boolean hadError = false;
    public static void main(String[] args) throws IOException {
        if(args.length > 1){
            System.out.println("Usage : scala [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        }
        else {
            runPrompt();
        }
    }
    private static void runFile(String path) throws IOException{
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes,Charset.defaultCharset()));
        if(hadError) System.exit(64);
    }
    private static void runPrompt() throws IOException{
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        for(;;){
            System.out.println("> ");
            String line = reader.readLine();
            if(line == null) break;
            run(line);
            hadError = false;
        }
    }
    private static void run(String source){
        Lexer scanner = new Lexer(source);
        List<Token> tokens = scanner.scanTokens();

        for(Token token : tokens){
            System.out.println(token);
        }
      //  List<Token> tokens = scanner.scanTokens();
//Parser parser = new Parser(tokens);
//Expr expression = parser.parse();
// Stop if there was a syntax error.
//if (hadError) return;
//System.out.println(new AstPrinter().print(expression));
//}
    }
    static void error(int line, String message){
        report(line,"",message);
    }
    private static void report(int line, String where, String message){
        System.err.println("[line "+line+"] Error"+where+": "+message);
        hadError = true;
    }
    static void error(Token token, String message) {
if (token.type == TokenType.EOF) {
report(token.line, " at end", message);
} else {
report(token.line, " at '" + token.lexeme + "'", message);
}
}
}
