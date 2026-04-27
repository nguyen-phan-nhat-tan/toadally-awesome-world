package compiler.ast.expression;

import compiler.ast.ASTVisitor;
import compiler.ast.ASTNode;
import compiler.ast.ASTNodeUtils; //AI-Generated - DRY principle consolidation
import compiler.lexer.TokenType;
import java.util.List;

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
 *   <li>random[100] < 50 (50% chance condition)</li>
 *   <li>smell (check for odors)</li>
 * </ul>
 * 
 * @see NumberNode
 * @see MemoryNode
 * @see BinaryExpr
 * @see Expression
 */
public class SensorNode extends Expression {
    /** The type of sensor (NEARBY, AHEAD, RANDOM, SMELL). */
    private final TokenType sensorType;
    
    /** Optional argument expression (non-null for NEARBY, AHEAD, RANDOM; null for SMELL). */
    private final Expression argument;

    /**
     * Creates a sensor query node.
     * 
     * @param sensorType the sensor keyword (NEARBY, AHEAD, RANDOM, or SMELL)
     * @param argument the index/max argument (non-null for most sensors, null for SMELL)
     * @param line source line where the sensor appears
     * @param column source column where the sensor appears
     */
    public SensorNode(TokenType sensorType, Expression argument, int line, int column) {
        super(line, column, argument); //AI-Generated
        this.sensorType = sensorType;
        this.argument = argument;
    }

    /**
     * Returns the sensor type.
     * 
     * @return the TokenType (NEARBY, AHEAD, RANDOM, or SMELL)
     */
    public TokenType getSensorType() {
        return sensorType;
    }

    /**
     * Returns the optional argument expression.
     * 
     * @return the argument expression, or null if this sensor has no argument (SMELL)
     */
    public Expression getArgument() {
        return argument;
    }

    /**
     * Checks whether this sensor query has an argument.
     * 
     * Only SMELL sensors have no argument; all others return true.
     * 
     * @return true if an argument is present; false for SMELL sensor
     */
    public boolean hasArgument() {
        return argument != null;
    }

    @Override
    public List<ASTNode> getChildren() {
        return ASTNodeUtils.toChildList(argument); //AI-Generated - DRY principle consolidation
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
