package compiler.lexer;

/**
 * Immutable token object so parser logic can safely pass references around without
 * accidental mutation of lexical context.
 */
public class Token {
    private final TokenType type;
    private final String lexeme;
    private final int line;
    private final int column;
    private final int value;

    /**
     * Convenience overload for non-numeric tokens to keep call sites concise while
     * preserving a single canonical constructor.
     *
     * @param type token category
     * @param lexeme token text from source
     * @param line source line (1-based)
     * @param column source column (1-based)
     */
    public Token(TokenType type, String lexeme, int line, int column) {
        this(type, lexeme, line, column, 0);
    }

    /**
     * Carries both textual and interpreted numeric form so parser/AST construction can
     * avoid reparsing number lexemes.
     *
     * @param type token category
     * @param lexeme token text from source
     * @param line source line (1-based)
     * @param column source column (1-based)
     * @param value numeric value for number-like tokens
     */
    public Token(TokenType type, String lexeme, int line, int column, int value) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
        this.value = value;
    }

    /**
     * @return token category used by parser dispatch
     */
    public TokenType getType() {
        return type;
    }

    /**
     * @return original text, useful for preserving user-facing formatting and messages
     */
    public String getLexeme() {
        return lexeme;
    }

    /**
     * @return line metadata for precise diagnostics
     */
    public int getLine() {
        return line;
    }

    /**
     * @return column metadata for precise diagnostics
     */
    public int getColumn() {
        return column;
    }

    /**
     * @return pre-parsed numeric payload to avoid repeated string-to-int conversion
     */
    public int getValue() {
        return value;
    }
}
