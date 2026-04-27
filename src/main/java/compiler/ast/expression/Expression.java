package compiler.ast.expression;

import compiler.ast.ASTNode;

/**
 * Base class for all expression AST nodes.
 * 
 * Expressions represent arithmetic and lookup operations that produce values (integers).
 * They appear in:
 * <ul>
 *   <li>Conditions: expressions on both sides of relational operators (e.g., health < 50)</li>
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
//AI-Generated
public abstract class Expression extends ASTNode implements compiler.ast.marker.Expr {
    /**
     * Initializes an Expression node at the given source location.
     * 
     * @param line source line number (1-based)
     * @param column source column number (1-based)
     * @param children direct child nodes in source order
     */
    protected Expression(int line, int column, ASTNode... children) {
        super(line, column, children);
    }
}
