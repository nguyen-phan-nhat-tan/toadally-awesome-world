package compiler.lexer;

import compiler.error.SyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String source;
    private int currentIndex;
    private int line;
    private int column;
    private int start;
    private int tokenColumn;
    private final List<Token> tokens;

    public Lexer(String source) {
        this.source = source;
        this.currentIndex = 0;
        this.line = 1;
        this.column = 1;
        this.tokenColumn = 1;
        this.tokens = new ArrayList<>();
    }

    public List<Token> tokenize() {
        while (!isAtEnd()) {
           start = currentIndex;
           tokenColumn = column;
           scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", line, column));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case ' ':
            case '\r':
            case '\t':
            case '\n':
                return;
            case '[':
                addToken(TokenType.LBRACKET, "[");
                return;
            case ']':
                addToken(TokenType.RBRACKET, "]");
                return;
            case '(':
                addToken(TokenType.LPAREN, "(");
                return;
            case ')':
                addToken(TokenType.RPAREN, ")");
                return;
            case '{':
                addToken(TokenType.LBRACE, "{");
                return;
            case '}':
                addToken(TokenType.RBRACE, "}");
                return;
            case ';':
                addToken(TokenType.SEMICOLON, ";");
                return;
            case ':':
                if (match('=')){
                    addToken(TokenType.ASSIGN, ":=");
                } else {
                    throw new SyntaxException("Unexpected character: " + c, line, column);
                }
                return;
            case '+':
                addToken(TokenType.ADDOP, "+");
                return;
            case '*':
                addToken(TokenType.MULOP, "*");
                return;
            case '-':
                if (match('-') && match('>')) {
                    addToken(TokenType.ARROW, "-->");
                } else {
                    addToken(TokenType.ADDOP, "-");
                }
                return;
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                } else {
                    addToken(TokenType.MULOP, "/");
                }
                return;
            case '<':
                if (match('=')){
                    addToken(TokenType.REL, "<=");
                } else {
                    addToken(TokenType.REL, "<");
                }
                return;
            case '>':
                if (match('=')){
                    addToken(TokenType.REL, ">=");
                } else {
                    addToken(TokenType.REL, ">");
                }
                return;
            case '=':
                addToken(TokenType.REL, "=");
                return;
            case '!':
                if (match('=')){
                    addToken(TokenType.REL, "!=");
                } else {
                    throw new SyntaxException("Unexpected character: " + c, line, column);
                }
                return;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    throw new SyntaxException("Unexpected character: " + c, line, column);
                }
                return;
        }
    }

    private void number(){
        while (isDigit(peek())) {
            advance();
        }
        String lexeme = source.substring(start, currentIndex);
        int value;
        try {
            value = Integer.parseInt(lexeme);
        } catch (NumberFormatException e) {
            throw new SyntaxException("Invalid number: " + lexeme, line, column);
        }
        addToken(TokenType.NUMBER, lexeme, value);
    }

    private void identifier() {
        while (isAlpha(peek()) || isDigit(peek())) {
            advance();
        }
        String lexeme = source.substring(start, currentIndex);
        Integer sugarIndex = Sugar.getIndexSugar(lexeme);
        if (sugarIndex != null) {
            injectSugarToken(sugarIndex);
            return;
        }
        TokenType type = keyword(lexeme);
        addToken(type, lexeme);
    }

    private TokenType keyword(String lexeme){
        switch (lexeme.toLowerCase()) {
            case "and":
                return TokenType.AND;
            case "or":
                return TokenType.OR;
            case "mem":
                return TokenType.MEM;
            case "wait":
                return TokenType.WAIT;
            case "forward":
                return TokenType.FORWARD;
            case "backward":
                return TokenType.BACKWARD;
            case "left":
                return TokenType.LEFT;
            case "right":
                return TokenType.RIGHT;
            case "eat":
                return TokenType.EAT;
            case "attack":
                return TokenType.ATTACK;
            case "grow":
                return TokenType.GROW;
            case "bud":
                return TokenType.BUD;
            case "serve":
                return TokenType.SERVE;
            case "nearby":
                return TokenType.NEARBY;
            case "ahead":
                return TokenType.AHEAD;
            case "random":
                return TokenType.RANDOM;
            case "smell":
                return TokenType.SMELL;
            case "mod":
                return TokenType.MULOP;
            default:
                throw new SyntaxException("Unknown identifier: " + lexeme, line, column);
        }
    }
    
    private void injectSugarToken(int index) {
        addToken(TokenType.MEM, "mem");
        addToken(TokenType.LBRACKET, "[");
        addToken(TokenType.NUMBER, String.valueOf(index), index);
        addToken(TokenType.RBRACKET, "]");
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    // Overloading addToken 
    private void addToken(TokenType type, String lexeme) {
        addToken(type, lexeme, 0);
    }

    private void addToken(TokenType type, String lexeme, int value) {
        tokens.add(new Token(type, lexeme, line, tokenColumn, value));
    }

    private boolean match(char expected){
        if (isAtEnd()) {
            return false;
        }
        if (source.charAt(currentIndex) != expected) {
            return false;
        }
        advance();
        return true;
    }

    private char advance() {
        char c = source.charAt(currentIndex++);
        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return c;
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(currentIndex);
    }

    private boolean isAtEnd() {
        return currentIndex >= source.length();
    }
}
