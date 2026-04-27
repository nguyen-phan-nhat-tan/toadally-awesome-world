package compiler.ast.condition;

import compiler.ast.expression.Expression;

import compiler.ast.ASTNode;
import compiler.ast.ASTVisitor;
import java.util.List;

/**
 * Represents an atomic relational condition in the AST.
 * 
 * A relation compares two expressions using a relational operator, producing a boolean result.
 * Relational operators supported:
 * <ul>
 *   <li>< (less than)</li>
 *   <li>> (greater than)</li>
 *   <li><= (less than or equal)</li>
 *   <li>>= (greater than or equal)</li>
 *   <li>= (equal)</li>
 *   <li>!= (not equal)</li>
 * </ul>
 * 
 * <b>Examples:</b>
 * <ul>
 *   <li>health < 50</li>
 *   <li>energy >= 10</li>
 *   <li>nearby[0] = 2</li>
 * </ul>
 * 
 * Relations are the base case of condition evaluation. Compound conditions (AND/OR chains)
 * are built from relations using {@link LogicNode}.
 * 
 * @see LogicNode
 * @see Condition
 */
public class RelationNode extends Condition {
    /** Left-hand side expression of the relation. */
    private final Expression left;
    
    /** The relational operator (<, >, <=, >=, =, !=). */
    private final String operator;
    
    /** Right-hand side expression of the relation. */
    private final Expression right;

    /**
     * Creates a relational condition node.
     * 
     * @param left the left-hand side expression
     * @param operator the relational operator as a string (<, >, <=, >=, =, !=)
     * @param right the right-hand side expression
     * @param line source line where the relation appears
     * @param column source column where the relation appears
     */
    public RelationNode(Expression left, String operator, Expression right, int line, int column) {
        super(line, column, left, right); //AI-Generated
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    /**
     * Returns the left-hand side expression.
     * 
     * @return the left expression
     */
    public Expression getLeft() {
        return left;
    }
    
    /**
     * Returns the relational operator.
     * 
     * @return operator string: <, >, <=, >=, =, or !=
     */
    public String getOperator() {
        return operator;
    }
    
    /**
     * Returns the right-hand side expression.
     * 
     * @return the right expression
     */
    public Expression getRight() {
        return right;
    }

    @Override
    public List<ASTNode> getChildren() {
        return List.of(left, right); //AI-Generated
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
