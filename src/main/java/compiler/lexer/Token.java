package compiler.lexer;

public class Token {
    private final TokenType type;
    private final String lexeme;
    private final int line;
    private final int column;
    private final int value;

    public Token(TokenType type, String lexeme, int line, int column) {
        this(type, lexeme, line, column, 0);
    }

    public Token(TokenType type, String lexeme, int line, int column, int value) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
        this.value = value;
    }

    public TokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public int getValue() {
        return value;
    }
}
