package ast;

import java.util.List;

/**
 * Base class for all abstract syntax tree nodes.
 *  
 * * Class Invariants:
 * - Nodes are strictly immutable.
 * - getLine() >= 1 and getColumn() >= 1.
 * - subtreeSize >= 1, representing this node plus the sum of all descendants' subtree sizes.
 * - The order of children in getChildren() perfectly mirrors their appearance in the source code.
 * 
 * @see Program
 * @see Rule
 */
public sealed abstract class ASTNode 
    permits Program, Rule, Expression, Condition, Command {
    /** Source line number (1-based) where this node begins in the source code. */
    protected final int line;
    
    /** Source column number (1-based) where this node begins in the source code. */
    protected final int column;
    
    /** Total number of nodes in this subtree (including this node). Used for metrics and bounds-checking. */
    public final int subtreeSize;

/**
     * Initializes an AST node at the given source location using a variadic array of children.
     * 
     * Position information is recorded at creation time to ensure accurate 
     * error reporting during interpretation or mutation, even if the original source 
     * text is discarded.
     *
     * @param line     The source line number (1-based)
     * @param column   The source column number (1-based)
     * @param children Direct child nodes in source order (null elements are ignored in size calculations)
     */
    protected ASTNode(int line, int column, ASTNode... children) {
        this.line = line;
        this.column = column;
        this.subtreeSize = 1 + sumChildren(children);
    }

    /**
     * Alternative constructor accepting children as a List.
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

    /**
     * Computes the sum of the subtree sizes for an array of child nodes.
     * 
     * @param children An array of ASTNodes
     * @return The combined total of all children's subtree sizes
     */
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

    /**
     * Computes the sum of the subtree sizes for a list of child nodes.
     * 
     * @param children A List of ASTNodes
     * @return The combined total of all children's subtree sizes
     */
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
     * @return the line number (1-based)
     */
    public int getLine(){
        return line;
    }

    /**
     * Returns the source column number where this node begins.
     * 
     * @return the column number (1-based)
     */
    public int getColumn(){
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
}

