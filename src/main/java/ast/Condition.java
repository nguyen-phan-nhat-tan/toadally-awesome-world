package ast;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Base class for all condition AST nodes.
 * <p>
 * Conditions represent boolean expressions that determine whether a rule should fire.
 * They are the "guard" part of a rule; a rule only executes if its condition evaluates to true.
 * </p>
 * <p>
 * Condition types include:
 * </p>
 * <ul>
 *   <li>{@link RelationNode}: atomic comparison (expr relop expr)</li>
 *   <li>{@link LogicNode}: logical combinations (and/or chains of conditions)</li>
 * </ul>
 * 
 * @see RelationNode
 * @see LogicNode
 * @see ast.Rule
 */
public sealed abstract class Condition extends ASTNode implements ast.marker.Cond
    permits LogicNode, RelationNode {
    /**
     * Initializes a Condition node at the given source location.
     * 
     * @param line source line number (1-based)
     * @param column source column number (1-based)
     * @param children direct child nodes in source order
     */
    protected Condition(int line, int column, ASTNode... children){
        super(line, column, children);
    }
}


/**
 * Represents a logical combination of conditions (AND/OR chains).
 * <p>
 * This node type allows building compound boolean expressions by combining multiple
 * conditions with logical operators. {@code AND} and {@code OR} have different precedence: 
 * {@code AND} binds tighter than {@code OR} (following standard boolean algebra conventions).
 * </p>
 * <p>
 * <b>Examples:</b>
 * </p>
 * <ul>
 *   <li>{@code health < 50 and energy >= 10}</li>
 *   <li>{@code nearby[0] = 1 or nearby[1] = 1 or nearby[2] = 1}</li>
 *   <li>{@code energy >= 20 and (nearby[0] = 2 or nearby[1] = 2)}</li>
 * </ul>
 * <p>
 * {@code LogicNode}s form a binary tree of conditions, with {@link RelationNode} leaves 
 * representing the atomic comparisons.
 * </p>
 * 
 * @see RelationNode
 * @see Condition
 */
final class LogicNode extends Condition {
    private static final Set<String> VALID_OPERATORS = Set.of("and", "or");

    /** Left-hand side condition of the logical operation. */
    private final Condition left;
    
    /** The logical operator ("and" or "or"). */
    private final String operator;
    
    /** Right-hand side condition of the logical operation. */
    private final Condition right;

    private final List<ASTNode> childrenView;
    
    /**
     * Creates a logical condition node.
     * * @param left the left-hand side condition (must not be null)
     * @param operator the logical operator ("and" or "or")
     * @param right the right-hand side condition (must not be null)
     * @param line source line where the logical operation appears
     * @param column source column where the logical operation appears
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if the operator is invalid
     */
    public LogicNode(Condition left, String operator, Condition right, int line, int column){
        super(line, column, left, right);
        this.left = Objects.requireNonNull(left, "Left condition cannot be null");
        this.right = Objects.requireNonNull(right, "Right condition cannot be null");
        this.operator = Objects.requireNonNull(operator, "Operator cannot be null");

        if (!VALID_OPERATORS.contains(this.operator)){
            throw new IllegalArgumentException("Invalid logic operator: " + operator);
        }

        this.childrenView = List.of(this.left, this.right);
    }

    /**
     * Returns the left-hand side condition.
     * 
     * @return the left condition
     */
    public Condition getLeft(){
        return left;
    }

    /**
     * Returns the logical operator.
     * 
     * @return operator string: "and" or "or"
     */
    public String getOperator(){
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
        return childrenView;
    }
}

/**
 * Represents an atomic relational condition in the AST.
 * <p>
 * A relation compares two expressions using a relational operator, producing a boolean result.
 * Relational operators supported:
 * </p>
 * <ul>
 *   <li>{@code <} (less than)</li>
 *   <li>{@code >} (greater than)</li>
 *   <li>{@code <=} (less than or equal)</li>
 *   <li>{@code >=} (greater than or equal)</li>
 *   <li>{@code =} (equal)</li>
 *   <li>{@code !=} (not equal)</li>
 * </ul>
 * <p>
 * <b>Examples:</b>
 * </p>
 * <ul>
 *   <li>{@code health < 50}</li>
 *   <li>{@code energy >= 10}</li>
 *   <li>{@code nearby[0] = 2}</li>
 * </ul>
 * <p>
 * Relations are the base case of condition evaluation. Compound conditions (AND/OR chains)
 * are built from relations using {@link LogicNode}.
 * </p>
 * 
 * @see LogicNode
 * @see Condition
 */
final class RelationNode extends Condition {
    private static final Set<String> VALID_OPERATORS = Set.of("<", "<=", "=", ">=", ">", "!=");

    /** Left-hand side expression of the relation. */
    private final Expression left;
    
    /** The relational operator (&lt;, &gt;, &lt;=, &gt;=, =, !=). */
    private final String operator;
    
    /** Right-hand side expression of the relation. */
    private final Expression right;

    private final List<ASTNode> childrenView;

    /**
     * Creates a relational condition node.
     * 
     * @param left the left-hand side expression
     * @param operator the relational operator as a string (&lt;, &gt;, &lt;=, &gt;=, =, !=)
     * @param right the right-hand side expression
     * @param line source line where the relation appears
     * @param column source column where the relation appears
     */
    public RelationNode(Expression left, String operator, Expression right, int line, int column) {
        super(line, column, left, right);

        this.left = Objects.requireNonNull(left, "Left expression cannot be null");
        this.right = Objects.requireNonNull(right, "Right expression cannot be null");
        this.operator = Objects.requireNonNull(operator, "Operator cannot be null");

        if (!VALID_OPERATORS.contains(this.operator)) {
            throw new IllegalArgumentException("Invalid relation operator: " + operator);
        }

        this.childrenView = List.of(this.left, this.right);
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
     * @return operator string: &lt;, &gt;, &lt;=, &gt;=, =, or !=
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
        return childrenView;
    }
}