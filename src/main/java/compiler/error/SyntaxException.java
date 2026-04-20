package compiler.error;

public class SyntaxException extends RuntimeException {
    private final int line;
    private final int column;

    public SyntaxException(String message, int line, int column) {
        super(String.format("Syntax Error at line %d, col %d: %s", line, column, message));
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
