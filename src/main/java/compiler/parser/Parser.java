package compiler.parser;

import java.util.ArrayList;
import java.util.List;

import compiler.ast.*;
import compiler.ast.command.*;
import compiler.ast.condition.*;
import compiler.ast.expression.*;
import compiler.lexer.*;
import compiler.error.SyntaxException;

public final class Parser {
    private final List<Token> tokens;
    private int currentIndex;

    public Parser(String source) {
        this.tokens = new Lexer(source).tokenize();
        this.currentIndex = 0;
    }

    public Program parseProgram() {
        List<Rule> parsedRules = new ArrayList<>();
    
        while (!isAtEnd() && peek().getType() != TokenType.EOF) {
            parsedRules.add(parseRule());
        }
        return new Program(parsedRules, 0, 0);
    }

    private Rule parseRule() {
        Token firstToken = peek();
        Condition condition = parseCondition();
        consume(TokenType.ARROW, "Expected '-->' after condition");
        Command command = parseCommand();
        consume(TokenType.SEMICOLON, "Expected ';' after command");
        return new Rule(condition, command, firstToken.getLine(), firstToken.getColumn());
    }

    private Condition parseCondition() {
        Condition condition = parseConjunction();

        while (match(TokenType.OR)) {
            Token operator = previous();
            Condition right = parseConjunction();
            condition = new LogicNode(condition, operator.getLexeme(), right, operator.getLine(), operator.getColumn());
        }

        return condition;
    }

    private Condition parseConjunction() {
        Condition condition = parseRelation();

        while (match(TokenType.AND)) {
            Token operator = previous();
            Condition right = parseRelation();
            condition = new LogicNode(condition, operator.getLexeme(), right, operator.getLine(), operator.getColumn());
        }

        return condition;
    }

    private Condition parseRelation() {
        Token startToken = peek();

        if (match(TokenType.LBRACE)) {
            Condition condition = parseCondition();
            consume(TokenType.RBRACE, "Expected '}' after condition");
            return condition;
        }

        Expression left = parseExpression();

        if (isRelOp(peek().getType())) {
            Token operator = advance();
            Expression right = parseExpression();
            return new RelationNode(left, operator.getLexeme(), right, operator.getLine(), operator.getColumn());
        }

        throw new SyntaxException("Expected a relation operator or a parenthesized condition", startToken.getLine(), startToken.getColumn());
    }

    private Command parseCommand() {
        Token startToken = peek();
        List<UpdateNode> updates = new ArrayList<>();
        
        while (check(TokenType.MEM)){
            updates.add(parseUpdate());
        }

        Command terminalAction;
        if (isActionKeyword(peek().getType())) {
            terminalAction = parseAction();
        } else if (!updates.isEmpty()) {
            terminalAction = updates.remove(updates.size() - 1);
        } else {
            throw new SyntaxException("Expected an action command", peek().getLine(), peek().getColumn());
        }
        return new CommandList(updates, terminalAction, startToken.getLine(), startToken.getColumn());
    }

    private UpdateNode parseUpdate() {
        Token start = consume(TokenType.MEM, "Expected 'MEM' for an update command");
        consume(TokenType.LBRACKET, "Expected '[' after 'MEM'");
        Expression index = parseExpression();
        consume(TokenType.RBRACKET, "Expected ']' after index expression");

        consume(TokenType.ASSIGN, "Expected ':=' in update command");
        Expression value = parseExpression();

        return new UpdateNode(index, value, start.getLine(), start.getColumn());
    }

    private Expression parseExpression() {
        Expression expr = parseTerm();
        while (match(TokenType.ADDOP)) {
                Token operator = previous();
                Expression right = parseTerm();
                expr = new BinaryExpr(expr, operator.getType(), right, operator.getLine(), operator.getColumn());
         }
        return expr;
    }

    private Expression parseTerm() {
        Expression expr = parseFactor();

        while (match(TokenType.MULOP)) {
                Token operator = previous();
                Expression right = parseFactor();
                expr = new BinaryExpr(expr, operator.getType(), right, operator.getLine(), operator.getColumn());
         }
        return expr;
    }

    private Expression parseFactor() {
        Token token = peek();
        if (match(TokenType.NUMBER)){
            return new NumberNode(token.getValue(), token.getLine(), token.getColumn());
        }

        if (match(TokenType.MEM)){
            consume(TokenType.LBRACKET, "Expected '[' after 'MEM'");
            Expression index = parseExpression();
            consume(TokenType.RBRACKET, "Expected ']' after index expression");
            return new MemoryNode(index, token.getLine(), token.getColumn());   
        }
        
        if (match(TokenType.LPAREN)){
            Expression expr = parseExpression();
            consume(TokenType.RPAREN, "Expected ')' after expression");
            return expr;
        }

        if (check(TokenType.ADDOP) && peek().getLexeme().equals("-")) {
            Token op = advance();
            Expression right = parseFactor();

            Expression zero = new NumberNode(0, op.getLine(), op.getColumn());
        
            return new BinaryExpr(zero, op.getType(), right, op.getLine(), op.getColumn());
        }

        if (isSensorKeyword(token.getType())) {
            return parseSensor();
        }

        throw new SyntaxException("Expected a number, memory access, or parenthesized expression", token.getLine(), token.getColumn());
    }

    private Command parseAction() {
        Token actionToken = advance();
        TokenType type = actionToken.getType();

        if (type != TokenType.SERVE){
            return new ActionNode(type, actionToken.getLine(), actionToken.getColumn(), null);
        }

        consume(TokenType.LBRACKET, "Expected '[' after SERVE");
        Expression argument = parseExpression();
        consume(TokenType.RBRACKET, "Expected ']' after SERVE argument");

        return new ActionNode(type, actionToken.getLine(), actionToken.getColumn(), argument);
    }

    private Expression parseSensor() {
        Token sensorToken = advance();
        TokenType type = sensorToken.getType();

        if (type == TokenType.SMELL){
            return new SensorNode(type, null, sensorToken.getLine(), sensorToken.getColumn());
        }

        consume(TokenType.LBRACKET, "Expected '[' after " + sensorToken.getLexeme() + " sensor");
        Expression argument = parseExpression();
        consume(TokenType.RBRACKET, "Expected ']' after " + sensorToken.getLexeme() + " sensor argument");

        return new SensorNode(type, argument, sensorToken.getLine(), sensorToken.getColumn());
    }







    // Helper methods for parsing
    private boolean isRelOp(TokenType type) {
        switch (type) {
            case REL:
                return true;
            default:
                return false;
        }
    }
    private boolean isActionKeyword(TokenType type) {
        switch (type) {
            case WAIT:
            case FORWARD:
            case BACKWARD:
            case LEFT:
            case RIGHT:
            case GROW:
            case EAT:
            case ATTACK:
            case SERVE:
            case BUD:
                return true;
            default:
                return false;
        }
    }

    private boolean isSensorKeyword(TokenType type) {
        switch (type) {
            case NEARBY:
            case AHEAD:
            case RANDOM:
            case SMELL:
                return true;
            default:
                return false;
        }
    }

    private Token peek() {
        return tokens.get(currentIndex);
    }

    private Token previous() {
        return tokens.get(currentIndex - 1);
    }

    private boolean isAtEnd() {
        return peek().getType() == TokenType.EOF || currentIndex >= tokens.size();
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token advance() {
        if (!isAtEnd()) currentIndex++;
        return previous();
    }

    private Token consume(TokenType type, String errorMessage) {
        if (check(type)) return advance();
        throw new SyntaxException(errorMessage, peek().getLine(), peek().getColumn());
    }
}
