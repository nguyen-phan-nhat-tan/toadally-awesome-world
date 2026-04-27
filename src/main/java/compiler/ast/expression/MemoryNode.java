package compiler.ast.expression;

import compiler.ast.ASTVisitor;
import compiler.ast.ASTNode;
import java.util.List;

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
public class MemoryNode extends Expression {
    /** Expression that evaluates to the index of the memory slot to read. */
    private final Expression index;

    /**
     * Creates a memory access node.
     * 
     * @param index the expression that evaluates to the slot index
     * @param line source line where the memory access appears
     * @param column source column where the memory access appears
     */
    public MemoryNode(Expression index, int line, int column) {
        super(line, column, index); //AI-Generated
        this.index = index;
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
    public List<ASTNode> getChildren() {
        return List.of(index); //AI-Generated
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
