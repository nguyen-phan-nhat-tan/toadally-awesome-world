package compiler.ast.command;

import compiler.ast.expression.Expression;
import compiler.ast.ASTNode;
import compiler.ast.ASTVisitor;
import java.util.List;

/**
 * Represents a memory update command in the AST.
 * 
 * A memory update modifies the critter's persistent state: mem[index] := value.
 * Updates can be queued before a terminal action in a command sequence, allowing
 * the critter to modify multiple memory slots in a single rule execution.
 * 
 * @see UpdateNode
 * @see CommandList
 */
public class UpdateNode extends Command {
    /** Expression evaluating to the memory slot index to update. */
    private final Expression targetMemory;
    
    /** Expression evaluating to the new value for the target slot. */
    private final Expression value;

    /**
     * Creates a memory update node.
     * 
     * @param targetMemory expression that evaluates to the target slot index
     * @param value expression that evaluates to the new value
     * @param line source line where the update appears
     * @param column source column where the update appears
     */
    public UpdateNode(Expression targetMemory, Expression value, int line, int column) {
        super(line, column, targetMemory, value);
        this.targetMemory = targetMemory;
        this.value = value;
    }

    /**
     * Returns the target memory slot index expression.
     * 
     * @return the index expression (typically a NumberNode or MemoryNode)
     */
    public Expression getTargetMemory() {
        return targetMemory;
    }

    /**
     * Returns the value expression to be stored.
     * 
     * @return the value expression to assign to the target slot
     */
    public Expression getValue() {
        return value;
    }

    @Override
    public List<ASTNode> getChildren() {
        return List.of(targetMemory, value);
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
    
}
