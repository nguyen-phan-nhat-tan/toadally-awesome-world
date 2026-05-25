package ast;

import java.util.List;
import lexer.TokenType;
import java.util.Objects;
import java.util.Set;
/**
 * Base class for all expression AST nodes.
 * 
 * Expressions represent arithmetic and lookup operations that produce values (integers).
 * They appear in:
 * <ul>
 *   <li>Conditions: expressions on both sides of relational operators (e.g., health &lt; 50)</li>
 *   <li>Commands: memory indices and values for updates (e.g., mem[0] := energy + 10)</li>
 *   <li>Sensors: arguments to sensor queries (e.g., nearby[direction])</li>
 * </ul>
 * 
 * Expression types include:
 * <ul>
 *   <li>{@link NumberNode}: numeric literals</li>
 *   <li>{@link BinaryExpr}: arithmetic operations (+, -, *, /)</li>
 *   <li>{@link MemoryNode}: memory access (mem[index])</li>
 *   <li>{@link SensorNode}: sensor queries (nearby, ahead, random, smell)</li>
 * </ul>
 * 
 * @see NumberNode
 * @see BinaryExpr
 * @see MemoryNode
 * @see SensorNode
 */  
public sealed abstract class Expression extends ASTNode implements ast.marker.Expr
    permits BinaryExpr, MemoryNode, NumberNode, SensorNode {
    /**
     * Initializes an Expression node at the given source location.
     * 
     * @param line source line number (1-based)
     * @param column source column number (1-based)
     * @param children direct child nodes in source order
     */
    protected Expression(int line, int column, ASTNode... children){
        super(line, column, children);
    }
}

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
final class BinaryExpr extends Expression {
    private static final Set<String> VALID_OPERATORS = Set.of("+", "-", "*", "/", "mod");
    /** Left-hand side operand. */
    private final Expression left;
    /** The arithmetic operator (+, -, *, /, mod). */
    private final String operator;
    /** Right-hand side operand. */
    private final Expression right;

    private final List<ASTNode> childrenView;

    /**
     * Creates a binary arithmetic expression node.
     * 
     * @param left the left operand expression
     * @param operator the operator (+, -, *, /)
     * @param right the right operand expression
     * @param line source line where the expression appears
     * @param column source column where the expression appears
     */
    public BinaryExpr(Expression left, String operator, Expression right, int line, int column){
        super(line, column, left, right);

        this.left = Objects.requireNonNull(left, "Left expression cannot be null");
        this.right = Objects.requireNonNull(right, "Right expression cannot be null");
        this.operator = Objects.requireNonNull(operator, "Operator cannot be null");

        if (!VALID_OPERATORS.contains(this.operator)){
            throw new IllegalArgumentException("Invalid arithmetic operator: " + operator);
        }
        this.childrenView = List.of(left, right);
    }

    /**
     * Returns the left operand.
     * 
     * @return the left expression
     */
    public Expression getLeft(){
        return left;
    }

    /**
     * Returns the operator.
     * 
     * @return the operator string: +, -, *, or /
     */
    public String getOperator(){
        return operator;
    }

    /**
     * Returns the right operand.
     * 
     * @return the right expression
     */
    public Expression getRight(){
        return right;
    }

    @Override
    public List<ASTNode> getChildren(){
        return childrenView;
    }
}

/**
 * Represents a memory access expression in the AST.
 * 
 * Memory nodes retrieve values from the critter's persistent memory, indexed by
 * the index expression. This is used in:
 * <ul>
 *   <li>Condition expressions: comparing memory values (mem[0] &lt; 50)</li>
 *   <li>Arithmetic expressions: using memory in calculations (mem[1] * 2)</li>
 *   <li>Memory updates: as targets (mem[0] := 10) or sources (mem[0] := mem[1])</li>
 * </ul>
 * 
 * The index can be any expression (number, arithmetic, another memory access, sensor, etc.).
 * 
 * <b>Examples:</b>
 * <ul>
 *   <li>mem[0] (read slot 0)</li>
 *   <li>mem[energy] (read slot determined by energy value)</li>
 *   <li>mem[nearby[0]] (read slot determined by nearby sensor)</li>
 * </ul>
 * 
 * @see NumberNode
 * @see BinaryExpr
 * @see SensorNode
 * @see Expression
 */
final class MemoryNode extends Expression {
    /** Expression that evaluates to the index of the memory slot to read. */
    private final Expression index;

    private final List<ASTNode> childrenView;
    /**
     * Creates a memory access node.
     * 
     * @param index the expression that evaluates to the slot index
     * @param line source line where the memory access appears
     * @param column source column where the memory access appears
     */
    public MemoryNode(Expression index, int line, int column){
        super(line, column, index);
        this.index = Objects.requireNonNull(index, "Index cannot be null");

        this.childrenView = List.of(index);
    }

    /**
     * Returns the index expression.
     * 
     * @return the expression that determines which memory slot to access
     */
    public Expression getIndex() {
        return index;
    }

    @Override
    public List<ASTNode> getChildren(){
        return childrenView;
    }
}

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
final class NumberNode extends Expression {
    /** The numeric value represented by this node. */
    private final int value;

    private final List<ASTNode> childrenView;
    /**
     * Creates a numeric literal node.
     * 
     * @param value the integer value
     * @param line source line where the number appears
     * @param column source column where the number appears
     */
    public NumberNode(int value, int line, int column){
        super(line, column);
        this.value = value;

        this.childrenView = List.of();
    }

    /**
     * Returns the numeric value.
     * 
     * @return the integer value of this node
     */
    public int getValue(){
        return value;
    }

    @Override
    public List<ASTNode> getChildren() {
        return childrenView;
    }
}

/**
 * Represents a sensor query expression in the AST.
 * 
 * Sensor nodes query the critter's state or environment to obtain values for use
 * in conditions and expressions. Supported sensors are:
 * <ul>
 *   <li><b>nearby[direction]:</b> check what's nearby in a direction (0-5 for hexagon)</li>
 *   <li><b>ahead[distance]:</b> check what's ahead at a given distance</li>
 *   <li><b>random[max]:</b> generate random integer in [0, max)</li>
 *   <li><b>smell:</b> detect odors (no argument)</li>
 * </ul>
 * 
 * Most sensors require an argument (index). The SMELL sensor is special: it has no argument.
 * 
 * <b>Examples:</b>
 * <ul>
 *   <li>nearby[0] = 1 (check if there's food nearby)</li>
 *   <li>ahead[2] != 0 (check if there's something 2 steps ahead)</li>
 *   <li>random[100] &lt; 50 (50% chance condition)</li>
 *   <li>smell (check for odors)</li>
 * </ul>
 * 
 * @see NumberNode
 * @see MemoryNode
 * @see BinaryExpr
 * @see Expression
 */
final class SensorNode extends Expression {
    /** The type of sensor (NEARBY, AHEAD, RANDOM, SMELL). */
    private final TokenType sensorType;

    /** Optional argument expression (non-null for NEARBY, AHEAD, RANDOM; null for SMELL). */
    private final Expression argument;

    private final List<ASTNode> childrenView;

    /**
     * Creates a sensor query node.
     * 
     * @param sensorType the sensor keyword (NEARBY, AHEAD, RANDOM, or SMELL)
     * @param argument the index/max argument (non-null for most sensors, null for SMELL)
     * @param line source line where the sensor appears
     * @param column source column where the sensor appears
     */
    public SensorNode(TokenType sensorType, Expression argument, int line, int column){
        super(line, column, argument);
        this.sensorType = Objects.requireNonNull(sensorType, "Sensor type cannot be null");
        this.argument = argument;

        this.childrenView = ASTNodeUtils.toChildList(argument);
    }
    /**
     * Returns the sensor type.
     * 
     * @return the TokenType (NEARBY, AHEAD, RANDOM, or SMELL)
     */
    public TokenType getSensorType(){
        return sensorType;
    }

    /**
     * Returns the optional argument expression.
     * 
     * @return the argument expression, or null if this sensor has no argument (SMELL)
     */
    public Expression getArgument(){
        return argument;
    }

    /**
     * Checks whether this sensor query has an argument.
     * 
     * Only SMELL sensors have no argument; all others return true.
     * 
     * @return true if an argument is present; false for SMELL sensor
     */
    public boolean hasArgument(){
        return argument != null;
    }

    @Override
    public List<ASTNode> getChildren() {
        return childrenView;
    }
}