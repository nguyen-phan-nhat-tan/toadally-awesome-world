package compiler.ast;

import java.util.List;

/**
 * Base class for all abstract syntax tree nodes.
 * 
 * Every AST node carries source position information (line and column) so that error
 * diagnostics, debug output, and IDE tooling can pinpoint issues in the source code.
 * Additionally, each node tracks the total size of its subtree for metrics and traversal.
 * 
 * All AST nodes are immutable after construction; their children and attributes cannot change.
 * This design enables safe sharing of AST references across compiler phases.
 * 
 * <b>Visitor Pattern:</b>
 * AST traversal is implemented using the visitor pattern via the {@link #accept(ASTVisitor)} method.
 * This decouples tree structure from operations, allowing new analyses and transformations
 * without modifying node classes.
 * 
 * @see ASTVisitor
 * @see Program
 * @see Rule
 */
public abstract class ASTNode {
    /** Source line number (1-based) where this node begins in the source code. */
    protected final int line;
    
    /** Source column number (1-based) where this node begins in the source code. */
    protected final int column;
    
    /** Total number of nodes in this subtree (including this node). Used for metrics and bounds-checking. */
    public final int subtreeSize;
    
    /**
     * Initializes an AST node at the given source location.
     * 
     * This constructor is called by concrete AST node subclasses. Position information is
     * recorded at creation time so it remains available for error reporting throughout compilation,
     * even if source text is no longer accessible.
     *
     * @param line source line number (1-based, where line 1 is the first line)
     * @param column source column number (1-based, where column 1 is the first character)
     * @param children direct child nodes in source order (may be null or empty)
     */
    protected ASTNode(int line, int column, ASTNode... children) {
        this.line = line;
        this.column = column;
        this.subtreeSize = 1 + sumChildren(children);
    }

    /**
     * Alternative constructor accepting children as a List.
     * 
     * Useful for parsing phases that accumulate children in a collection before
     * creating the parent node.
     *
     * @param line source line number (1-based)
     * @param column source column number (1-based)
     * @param children direct child nodes as a list (may be null or empty)
     */

    protected ASTNode(int line, int column, List<? extends ASTNode> children) {
        this.line = line;
        this.column = column;
        this.subtreeSize = 1 + sumChildren(children);
    }


    private static int sumChildren(ASTNode... children) {
        int sum = 0;
        if (children == null) {
            return 0;
        }
        for (ASTNode child : children) {
            if (child != null) {
                sum += child.subtreeSize;
            }
        }
        return sum;
    }


    private static int sumChildren(List<? extends ASTNode> children) {
        int sum = 0;
        if (children == null) {
            return 0;
        }
        for (ASTNode child : children) {
            if (child != null) {
                sum += child.subtreeSize;
            }
        }
        return sum;
    }
    
    /**
     * Returns the source line number where this node begins.
     * 
     * Used by error reporting, debugging, and IDE features to show the user
     * exactly where in the source file a problem occurred.
     * 
     * @return the line number (1-based)
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the source column number where this node begins.
     * 
     * Used by error reporting, debugging, and IDE features to pinpoint the exact
     * character position in the source line where an issue starts.
     * 
     * @return the column number (1-based)
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns the immediate children of this node in source order.
     * 
     * This method is used by tree traversal algorithms (printing, analysis, transforms)
     * to navigate the AST structure. The order of children mirrors their appearance in source code.
     * 
     * @return list of direct child nodes (may be empty, never null)
     */
    public abstract List<ASTNode> getChildren();

    /**
     * Accepts a visitor to perform an operation on this node.
     * 
     * This implements the visitor design pattern, allowing new operations to be added
     * without modifying the AST node classes. The visitor pattern enables clean separation
     * between tree structure and algorithms (printing, analysis, code generation, etc.).
     *
     * @param visitor the visitor to apply (non-null)
     * @param <T> the return type produced by the visitor
     * @return the result produced by the visitor's visit method for this node type
     */
    public abstract <T> T accept(ASTVisitor<T> visitor);    

}
