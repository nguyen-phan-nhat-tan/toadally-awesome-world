package compiler.ast;

/**
 * Centralizes source location in the base type so diagnostics and tooling can rely on
 * every node carrying position data, regardless of concrete subtype.
 */
public abstract class ASTNode {
    protected final int line;
    protected final int column;

    /**
     * Stores location once at creation time to preserve the original parse context for
     * later error reporting and debug output.
     *
     * @param line source line (1-based)
     * @param column source column (1-based)
     */
    public ASTNode(int line, int column) {
        this.line = line;
        this.column = column;
    }
    
    /**
     * Exposed so downstream passes can report precise diagnostics without re-threading
     * positional metadata through every API.
     *
     * @return source line where the node starts
     */
    public int getLine() {
        return line;
    }

    /**
     * Exposed for the same reason as line information: consistent, high-fidelity error
     * messages across all compiler stages.
     *
     * @return source column where the node starts
     */
    public int getColumn() {
        return column;
    }

    /**
     * Maintains operation extensibility (printing, analysis, transforms) without forcing
     * behavior into the node classes themselves.
     *
     * @param visitor visitor implementation
     * @param <T> return type produced by the visitor
     * @return visitor result
     */
    public abstract <T> T accept(ASTVisitor<T> visitor);    

}
