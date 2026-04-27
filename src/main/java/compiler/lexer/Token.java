package compiler.lexer;

/**
 * Represents a single lexical token extracted from source code.
 * 
 * Tokens are immutable and carry four essential pieces of information:
 * <ul>
 *   <li><b>Type:</b> the grammatical category (keyword, operator, literal, etc.)</li>
 *   <li><b>Lexeme:</b> the exact text as it appeared in source</li>
 *   <li><b>Line &amp; Column:</b> source position for error reporting</li>
 *   <li><b>Value:</b> pre-parsed numeric form (for number tokens only)</li>
 * </ul>
 * 
 * The parser receives an ordered list of tokens and makes decisions based on type and lookahead,
 * without needing to re-scan source text. Position information is preserved for diagnostics.
 * 
 * @see TokenType
 * @see Lexer#tokenize()
 */
public class Token {
    private final TokenType type;
    private final String lexeme;
    private final int line;
    private final int column;
    private final int value;

    /**
     * Convenience overload for non-numeric tokens (keywords, operators, identifiers).
     * 
     * Initializes the numeric value to 0 since these token types do not represent numbers.
     *
     * @param type token category (non-null)
     * @param lexeme token text as it appeared in source (non-null)
     * @param line source line number (1-based)
     * @param column source column number (1-based)
     */
    public Token(TokenType type, String lexeme, int line, int column) {
        this(type, lexeme, line, column, 0);
    }

    /**
     * Full constructor carrying both textual and numeric forms.
     * 
     * For number tokens, the value is pre-parsed during lexing to avoid re-parsing in the parser.
     * For non-numeric tokens, value is typically 0.
     *
     * @param type token category (non-null)
     * @param lexeme token text from source (non-null)
     * @param line source line number (1-based)
     * @param column source column number (1-based)
     * @param value pre-parsed numeric value (0 for non-numeric tokens)
     */
    public Token(TokenType type, String lexeme, int line, int column, int value) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
        this.value = value;
    }

    /**
     * Returns the token type (category).
     * 
     * The parser uses this to make dispatch decisions about how to handle the token.
     * 
     * @return the token type (never null)
     */
    public TokenType getType() {
        return type;
    }

    /**
     * Returns the original source lexeme text.
     *
     * @return original text, useful for preserving user-facing formatting and messages
     */
    public String getLexeme() {
        return lexeme;
    }

    /**
     * Returns the source line number.
     *
     * @return line metadata for precise diagnostics
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the source column number.
     *
     * @return column metadata for precise diagnostics
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns the numeric payload associated with this token.
     *
     * @return pre-parsed numeric payload to avoid repeated string-to-int conversion
     */
    public int getValue() {
        return value;
    }
}
