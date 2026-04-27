package compiler.error;

/**
 * Thrown when the input violates lexical or syntactic grammar rules.
 * 
 * Syntax exceptions are unchecked (extend RuntimeException) because they represent fatal
 * compilation errors that should typically stop processing. Each exception captures the exact
 * location in source where the error was detected, enabling precise error reporting.
 * 
 * @see compiler.lexer.Lexer
 * @see compiler.parser.Parser
 */
public class SyntaxException extends RuntimeException {
    /** Source line number (1-based) where the error was detected. */
    private final int line;
    /** Source column number (1-based) where the error was detected. */
    private final int column;

    /**
     * Creates a syntax error with the given message and source location.
     * 
     * The message is formatted with line and column information so the error message
     * is self-contained and useful for IDE integration and developer output.
     *
     * @param message human-readable description of the error
     * @param line source line number (1-based) where the error occurred
     * @param column source column number (1-based) where the error occurred
     */
    public SyntaxException(String message, int line, int column) {
        super(String.format("Syntax Error at line %d, col %d: %s", line, column, message));
        this.line = line;
        this.column = column;
    }

    /**
     * Returns the source line where the error occurred.
     * 
     * @return line number (1-based)
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the source column where the error occurred.
     * 
     * @return column number (1-based)
     */
    public int getColumn() {
        return column;
    }
}
