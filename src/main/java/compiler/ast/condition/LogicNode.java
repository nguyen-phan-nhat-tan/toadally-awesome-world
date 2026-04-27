package compiler.ast.condition;

import compiler.ast.ASTNode;
import compiler.ast.ASTVisitor;
import java.util.List;

/**
 * Represents a logical combination of conditions (AND/OR chains).
 * 
 * This node type allows building compound boolean expressions by combining multiple
 * conditions with logical operators. AND and OR have different precedence: AND binds
 * tighter than OR (following standard boolean algebra conventions).
 * 
 * <b>Examples:</b>
 * <ul>
 *   <li>health &lt; 50 and energy &gt;= 10</li>
 *   <li>nearby[0] = 1 or nearby[1] = 1 or nearby[2] = 1</li>
 *   <li>energy &gt;= 20 and (nearby[0] = 2 or nearby[1] = 2)</li>
 * </ul>
 * 
 * LogicNodes form a binary tree of conditions, with RelationNode leaves representing
 * the atomic comparisons.
 * 
 * @see RelationNode
 * @see Condition
 */
public class LogicNode extends Condition {
    /** Left-hand side condition of the logical operation. */
    private final Condition left;
    
    /** The logical operator ("and" or "or"). */
    private final String operator;
    
    /** Right-hand side condition of the logical operation. */
    private final Condition right;

    /**
     * Creates a logical condition node.
     * 
     * @param left the left-hand side condition
     * @param operator the logical operator ("and" or "or")
     * @param right the right-hand side condition
     * @param line source line where the logical operation appears
     * @param column source column where the logical operation appears
     */
    public LogicNode(Condition left, String operator, Condition right, int line, int column) {
        super(line, column, left, right); //AI-Generated
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    /**
     * Returns the left-hand side condition.
     * 
     * @return the left condition
     */
    public Condition getLeft() {
        return left;
    }
    
    /**
     * Returns the logical operator.
     * 
     * @return operator string: "and" or "or"
     */
    public String getOperator() {
        return operator;
    }
    
    /**
     * Returns the right-hand side condition.
     * 
     * @return the right condition
     */
    public Condition getRight() {
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
