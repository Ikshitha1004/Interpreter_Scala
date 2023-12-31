package Crux;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Optional;
import static Crux.Tokentype.*;
// You should remove this line because the package name should match the folder structure.
// package Interpreter_Scala.Crux;
class Parser {
    private static class ParseError extends RuntimeException {};
    private final List<Token> tokens;
    ArrayList<String> valValues=new ArrayList<>();
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Expr Expression() {
        //return equality();
        return assignment();
    }

    private Stmt declaration() {
        try {
            if (match(Tokentype.VAR)) return varDeclaration();
            if (match(Tokentype.VAL)) return valDeclaration();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt statement() {
        if (match(Tokentype.FOR)) return forStatement();
        if (match(Tokentype.IF)) return ifStatement();
        if (match(Tokentype.PRINT)) return printStatement();
        if (match(Tokentype.WHILE)) return whileStatement();
        if (match(Tokentype.LEFT_BRACE)) return new Stmt.Block(block());
        return expressionStatement();
    }

    private Stmt generator(Token nameToken) {

        consume(Tokentype.GENERATOR, "Expect <-");
        Expr initializer = new Expr.Literal(Integer.parseInt(consume(INT, "Expect initial value").lexeme));
//        System.out.println(Integer.parseInt(consume(INT, "Expect final value").lexeme));
        return new Stmt.Var(nameToken, initializer);

    }
//    for (i <- 1 to 5) {
//        println(i)
//    }
//
//
//    Desugared while loop:
//    scala
//    var i = 1
//while (i <= 5) {
//        println(i)
//        i += 1
//    }


    private Stmt forStatement() {
        consume(Tokentype.LEFT_PAREN, "Expect '(' after 'for'.");

        Token nameToken = consume(Tokentype.IDENTIFIER, "Expect variable name.");
        Stmt initializer = generator(nameToken);
//        if (match(Tokentype.SEMICOLON)) {
//        } else if (match(VAR)) {
//            initializer = varDeclaration();
//        } else {
//            initializer = expressionStatement();
//        }
        consume(Tokentype.TO, "Expect to");
        Expr initr = new Expr.Variable(nameToken);
        Token plus = advance();
        Token operator = advance();
        Expr finalr =new Expr.Literal(Integer.parseInt(consume(INT, "Expect final value").lexeme));
        Expr condition = new Expr.Binary(initr, operator, finalr);//maybe wrong as expression operator expression
        int a=1;
        if(match(Tokentype.BY))
        {
            a=Integer.parseInt(consume(INT,"Expect number for increment").lexeme);
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");

        Expr value = new Expr.Binary(initr, plus, new Expr.Literal(a));
        //System.out.println(value.);

        Expr increment = new Expr.Assign(nameToken, value);
//        if (!check(RIGHT_PAREN)) {
//           increment = Expression();
//        }
//        consume(RIGHT_PAREN, "Expect ')' after for clauses.");
        Stmt body = statement();

        body = new Stmt.Block(
                Arrays.asList(
                        body,
                        new Stmt.Expression(increment)));
        body = new Stmt.While(condition, body);
        body = new Stmt.Block(Arrays.asList(initializer, body));
        return body;
//    }
    }

    private Stmt ifStatement() {
        consume(Tokentype.LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = Expression();
        consume(Tokentype.RIGHT_PAREN, "Expect ')' after if condition.");
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(Tokentype.ELSE)) {
            elseBranch = statement();
        }
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement() {
        consume(Tokentype.LEFT_PAREN, "Expect '(' after 'println'");
        Expr value = Expression();
        consume(Tokentype.RIGHT_PAREN, "Expect ')' after expression");
        consume(Tokentype.SEMICOLON, "Expect ';' after print statement.");
        return new Stmt.Print(value);
    }

    //delimiter as nextline shld be fig out
    private Stmt varDeclaration() {
        Token nameToken = consume(Tokentype.IDENTIFIER, "Expect variable name.");
//        Optional<String> varType = Optional.empty();
        Expr initializer = null;

        if (match(Tokentype.COLON)) {
            // Assuming Tokentype.COLON is used for specifying the type
            if (match(Tokentype.INT, Tokentype.DOUBLE, Tokentype.FLOAT, Tokentype.STRING)) {

            } else {
                throw error(peek(), "Expect datatype");
            }


            // Assuming Tokentype.COLON is used for specifying the type
//            Token datatype=getcurrent();
//            switch (datatype.type){
//                case INT:
//                    varType = Optional.of(consume(Tokentype.INT, "Expect variable type.").lexeme);break;
//                case DOUBLE:
//                    varType = Optional.of(consume(Tokentype.DOUBLE, "Expect variable type.").lexeme);break;
//                case FLOAT:
//                    varType = Optional.of(consume(Tokentype.FLOAT, "Expect variable type.").lexeme);break;
//                case STRING:
//                    varType = Optional.of(consume(Tokentype.STRING, "Expect variable type.").lexeme);break;
//            }
            if (match(Tokentype.EQUAL)) {
                initializer = Expression();
            } else {
                throw error(peek(), "Expect '=' expression");
            }
            //varType = Optional.of(consume(Tokentype.INT, "Expect variable type.").lexeme);
        }
        else if (match(Tokentype.EQUAL)) {
            initializer = Expression();
        }
        else {
//            consume(Tokentype.SEMICOLON, "Expected but ';' found..");
            throw error(peek(), "Expect '=' expression");
//
        }


        consume(Tokentype.SEMICOLON, "Expect ';' after variable declaration.");

        // Return an instance of Stmt directly
        return new Stmt.Var(nameToken, initializer);//removed vartype here.check again the method
    }



    private Stmt valDeclaration() {
        Token nameToken = consume(Tokentype.IDENTIFIER, "Expect variable name.");
        Optional<String> varType = Optional.empty();
        Expr initializer = null;
           valValues.add(nameToken.lexeme);


        if (match(Tokentype.COLON)) {
            // Assuming Tokentype.COLON is used for specifying the type
            if (match(Tokentype.INT, Tokentype.DOUBLE, Tokentype.FLOAT, Tokentype.STRING)) {

            }
            else {
                throw error(peek(), "Expect datatype");
            }




            // Assuming Tokentype.COLON is used for specifying the type
//            Token datatype=getcurrent();
//            switch (datatype.type){
//                case INT:
//                    varType = Optional.of(consume(Tokentype.INT, "Expect variable type.").lexeme);break;
//                case DOUBLE:
//                    varType = Optional.of(consume(Tokentype.DOUBLE, "Expect variable type.").lexeme);break;
//                case FLOAT:
//                    varType = Optional.of(consume(Tokentype.FLOAT, "Expect variable type.").lexeme);break;
//                case STRING:
//                    varType = Optional.of(consume(Tokentype.STRING, "Expect variable type.").lexeme);break;
//            }
            if (match(Tokentype.EQUAL)) {
                initializer = Expression();
            }
            else {
                throw error(peek(), "Expect '=' expression");
            }
        }
            //varType = Optional.of(consume(Tokentype.INT, "Expect variable type.").lexeme);

        else if (match(Tokentype.EQUAL)) {
            initializer = Expression();
        }
        else {
//            consume(Tokentype.SEMICOLON, "Expected but ';' found..");
            throw error(peek(), "Expect '=' expression");
//
        }


        consume(Tokentype.SEMICOLON, "Expect ';' after variable declaration.");

        // Return an instance of Stmt directly
        return new Stmt.Var(nameToken, initializer);//removed vartype here.check again the method
    }



    private Stmt whileStatement() {
        consume(Tokentype.LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = Expression();
        consume(Tokentype.RIGHT_PAREN, "Expect ')' after condition.");
        Stmt body = statement();
        return new Stmt.While(condition, body);
    }
    private Stmt expressionStatement() {
        Expr expr =   Expression();
        consume(Tokentype.SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }
    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        while (!check(Tokentype.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(Tokentype.RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }
    private Expr assignment() {
        Expr expr = or();
        if (match(Tokentype.EQUAL)) {
            Token equals = previous();
            Expr value = assignment();
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                  for(String t:valValues){
                    if(t.equals(name.lexeme)){
                     throw  error(name, " reassignment to val not possible");
                    }
                }
                return new Expr.Assign(name, value);
            }
            error(equals, "Invalid assignment target.");
        }
            return expr;
        }
    private Expr or() {
        Expr expr = and();
        while (match(Tokentype.OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }
    private Expr and() {
        Expr expr = equality();
        while (match(Tokentype.AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    //    Expr parse() {
//        try {
//            return Expression();
//        } catch (ParseError error) {
//            return null;
//        }
//    }
//    List<Stmt> parse() {
//        List<Stmt> statements = new ArrayList<>();
//        while (!isAtEnd()) {
//            statements.add(statement());
//        }
//        return statements;
//    }
    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }
    private Expr equality() {
        Expr expressions = comparison();
        while (match(Tokentype.BANG_EQUAL, Tokentype.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expressions = new Expr.Binary(expressions, operator, right);
        }
        return expressions;
    }

    private boolean match(Tokentype... types) {
        for (Tokentype type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }
    private Token consume(Tokentype type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private boolean check(Tokentype type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private boolean isAtEnd() {
        return peek().type == Tokentype.EOF;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }
   private Token getcurrent(){
       if (!isAtEnd()) return tokens.get(current);
       return null;
   }
    private Token previous() {
        return  tokens.get(current - 1);
    }
    //  private ParseError error(Token token, String message) {
//Lox.error(token, message);
//return new ParseError();
//}
    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == Tokentype.NEXTLINE) return;

            switch (peek().type) {

                case IF:
                case ELSE:
                case FOR:
                case CLASS:
                case WHILE:
                case RETURN:
                case VAR:
                case PRINT:
                case AND:
                case OR:
                case TRAIT:
                case VAL:
                case CASE:
                    return;
            }
            advance();
        }
    }
//}

    private Expr comparison() {
        Expr expressions = term();
        while (match(Tokentype.GREATER, Tokentype.GREATER_EQUAL, Tokentype.LESS, Tokentype.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = factor();
            expressions = new Expr.Binary(expressions, operator, right);
        }
        return expressions;
    }

    private Expr term() {
        Expr expressions = factor();
        while (match(Tokentype.MINUS, Tokentype.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expressions = new Expr.Binary(expressions, operator, right);
        }
        return expressions;
    }

    private Expr factor() {
        Expr expressions = unary();
        while (match(Tokentype.SLASH, Tokentype.STAR)) {
            Token operator = previous();
            Expr right = unary();
            expressions = new Expr.Binary(expressions, operator, right);
        }
        return expressions;
    }

    private Expr unary() {
        if (match(Tokentype.BANG, Tokentype.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    private Expr primary() {

        if (match(Tokentype.FALSE)) return new Expr.Literal(false);
        if (match(Tokentype.TRUE)) return new Expr.Literal(true);
        if (match(Tokentype.NIL)) return new Expr.Literal(null);
        if (match(Tokentype.DOUBLE, Tokentype.STRING,Tokentype.INT)) {
            return new Expr.Literal(previous().literal);}
        if (match(Tokentype.IDENTIFIER)) {
//            System.out.println("Entered");
            return new Expr.Variable(previous());
        }


        if (match(Tokentype.LEFT_PAREN)) {
            Expr expressions = Expression();
            consume(RIGHT_PAREN, "Expect ')' after expression");
            return new Expr.Grouping(expressions);
        }
        throw error(peek(), "Expect expression");
    }
//    private Token consume(Tokentype type, String message) {
//        if (check(type)) return advance();
//        throw error(peek(), message);
//    }


    private ParseError error(Token token, String message) {
        Scala.error(token, message); // Assuming 'lox' is a valid reference.
        return new ParseError();
    }}


