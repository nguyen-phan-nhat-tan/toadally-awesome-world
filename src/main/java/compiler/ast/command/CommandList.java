package compiler.ast.command;

import compiler.ast.ASTNode;
import compiler.ast.ASTVisitor;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a command sequence: zero or more memory updates followed by a terminal action.
 * 
 * This node type allows critters to modify multiple memory slots before executing an action,
 * all within a single rule execution. The updates are executed in order, followed by the
 * terminal action.
 * 
 * <b>Examples:</b>
 * <ul>
 *   <li>mem[0] := 5; forward; (one update, then action)</li>
 *   <li>mem[1] := mem[0] + 1; mem[2] := mem[1] * 2; eat; (two updates, then action)</li>
 *   <li>forward; (zero updates, just action)</li>
 * </ul>
 * 
 * @see UpdateNode
 * @see ActionNode
 * @see Command
 */
public class CommandList extends Command {
    /** Ordered list of memory updates to execute before the terminal action. */
    private final List<UpdateNode> updates;
    
    /** The terminal action to execute (ActionNode or another command). */
    private final Command terminalAction;

    /**
     * Creates a command list with optional memory updates followed by a terminal action.
     * 
     * @param updates list of memory updates to execute (may be empty)
     * @param terminalAction the action to execute last (ActionNode or other command)
     * @param line source line where the command begins
     * @param column source column where the command begins
     */
    public CommandList(List<UpdateNode> updates, Command terminalAction, int line, int column) {
        super(line, column, collectChildren(updates, terminalAction).toArray(new ASTNode[0]));
        this.updates = updates;
        this.terminalAction = terminalAction;
    }

    private static List<ASTNode> collectChildren(List<UpdateNode> updates, Command terminalAction) {
        List<ASTNode> children = new ArrayList<>(updates);
        children.add(terminalAction);
        return children;
    }

    /**
     * Returns the memory updates in execution order.
     * 
     * @return list of UpdateNode objects (may be empty)
     */
    public List<UpdateNode> getUpdates() {
        return updates;
    }

    /**
     * Returns the terminal action executed after all updates.
     * 
     * @return the terminal Command (ActionNode or other)
     */
    public Command getTerminalAction() {
        return terminalAction;
    }

    @Override
    public List<ASTNode> getChildren() {
        return collectChildren(updates, terminalAction);
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
