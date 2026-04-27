package compiler.ast.expression;

import compiler.ast.ASTVisitor;
import compiler.ast.ASTNode;
import java.util.List;

/**
 * Represents a numeric literal in the AST.
 * 
 * Numbers are leaf nodes (no children) in the expression tree and represent
 * constant integer values. They appear in arithmetic expressions, memory operations,
 * and condition comparisons.
 * 
 * <b>Examples:</b>
 * <ul>
 *   <li>42</li>
 *   <li>0</li>
 *   <li>100</li>
 * </ul>
 * 
 * @see BinaryExpr
 * @see Expression
 */
public class NumberNode extends Expression {
    /** The numeric value represented by this node. */
    private final int value;

    /**
     * Creates a numeric literal node.
     * 
     * @param value the integer value
     * @param line source line where the number appears
     * @param column source column where the number appears
     */
    public NumberNode(int value, int line, int column) {
        super(line, column); //AI-Generated
        this.value = value;
    }

    /**
     * Returns the numeric value.
     * 
     * @return the integer value of this node
     */
    public int getValue() {
        return value;
    }

    @Override
    public List<ASTNode> getChildren() {
        return List.of(); //AI-Generated
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
