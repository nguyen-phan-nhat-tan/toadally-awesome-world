package compiler.ast.expression;

import compiler.ast.ASTVisitor;
import compiler.ast.ASTNode;
import java.util.List;

/**
 * Represents a binary arithmetic expression in the AST.
 * 
 * Binary expressions combine two operands with an arithmetic operator to produce
 * a single value. Supported operators are:
 * <ul>
 *   <li>+ (addition)</li>
 *   <li>- (subtraction)</li>
 *   <li>* (multiplication)</li>
 *   <li>/ (division, integer truncation)</li>
 * </ul>
 * 
 * The parser ensures that operators form a proper precedence tree: multiplication and
 * division bind tighter than addition and subtraction. All operators are left-associative.
 * 
 * <b>Examples:</b>
 * <ul>
 *   <li>5 + 3 (addition)</li>
 *   <li>energy * 2 (multiplication with memory access)</li>
 *   <li>health - 5 + 10 (evaluates as (health - 5) + 10)</li>
 * </ul>
 * 
 * @see NumberNode
 * @see MemoryNode
 * @see SensorNode
 * @see Expression
 */
public class BinaryExpr extends Expression {
    /** Left-hand side operand. */
    private final Expression left;
    
    /** The arithmetic operator (+, -, *, /). */
    private final String operator;
    
    /** Right-hand side operand. */
    private final Expression right;

    /**
     * Creates a binary arithmetic expression node.
     * 
     * @param left the left operand expression
     * @param operator the operator (+, -, *, /)
     * @param right the right operand expression
     * @param line source line where the expression appears
     * @param column source column where the expression appears
     */
    public BinaryExpr(Expression left, String operator, Expression right, int line, int column) {
        super(line, column, left, right);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    /**
     * Returns the left operand.
     * 
     * @return the left expression
     */
    public Expression getLeft() {
        return left;
    }

    /**
     * Returns the operator.
     * 
     * @return the operator string: +, -, *, or /
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Returns the right operand.
     * 
     * @return the right expression
     */
    public Expression getRight() {
        return right;
    }

    @Override
    public List<ASTNode> getChildren() {
        return List.of(left, right);
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
