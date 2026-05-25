package ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lexer.TokenType;
import java.util.Objects;

/**
 * Base class for all command AST nodes.
 * 
 * Commands represent actions that the critter can execute. A command is the "effect" part
 * of a rule; when a rule's condition is true, its command executes.
 * 
 * Command types include:
 * <ul>
 *   <li>{@link ActionNode}: single action keywords (wait, forward, eat, etc.)</li>
 *   <li>{@link UpdateNode}: memory assignments (mem[i] := value)</li>
 *   <li>{@link CommandList}: sequences of updates followed by a terminal action</li>
 * </ul>
 * 
 * @see ActionNode
 * @see UpdateNode
 * @see CommandList
* @see ast.Rule
 */
public sealed abstract class Command extends ASTNode implements ast.marker.Cmd
    permits ActionNode, CommandList, UpdateNode {

    protected Command(int line, int column, ASTNode... children){
        super(line, column, children);
    }

    protected Command(int line, int column, List<? extends ASTNode> children) {
        super(line, column, children);
    }

}

/**
 * Represents an action command in the AST.
 * 
 * An action is a single behavioral operation that the critter executes:
 * <ul>
 *   <li><b>Movement:</b> forward, backward, left, right (change position/orientation)</li>
 *   <li><b>Interaction:</b> eat, attack, grow, bud, serve (affect state or environment)</li>
 *   <li><b>Control:</b> wait (pause)</li>
 * </ul>
 * 
 * Most actions have no arguments. The SERVE action is special: it takes a memory slot
 * argument indicating what to serve to allies.
 * 
 * @see Command
 * @see CommandList
 */
final class ActionNode extends Command {
    /** The type of action to perform (wait, forward, eat, etc.). */
    private final TokenType actionType;

    /** Optional argument (for SERVE action; null for most others). */
    private final Expression argument;

    private final List<ASTNode> childrenView;
    /**
     * Creates an action node.
     * 
     * @param actionType the action keyword (WAIT, FORWARD, BACKWARD, LEFT, RIGHT, EAT, ATTACK, GROW, BUD, or SERVE)
     * @param line source line where the action appears
     * @param column source column where the action appears
     * @param argument optional argument (non-null only for SERVE; the memory slot to serve)
     */
    public ActionNode(TokenType actionType, Expression argument, int line, int column){
        super(line, column, argument);
        this.actionType = Objects.requireNonNull(actionType, "Action type cannot be null");
        this.argument = argument;

        this.childrenView = ASTNodeUtils.toChildList(this.argument);
    }

    /**
     * Returns the action type.
     * 
     * @return the TokenType corresponding to the action keyword
     */
    public TokenType getActionType(){
        return actionType;
    }

    /**
     * Returns the optional argument (for SERVE action only).
     * 
     * @return the argument expression, or null if this action has no argument
     */
    public Expression getArgument() {
        return argument;
    }

    /**
     * Checks whether this action has an argument.
     * 
     * Only SERVE actions have arguments; all others return false.
     * 
     * @return true if an argument is present; false otherwise
     */
    public boolean hasArgument() {
        return argument != null;
    }

    @Override
    public List<ASTNode> getChildren() {
        return childrenView;
    }
}

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
final class CommandList extends Command {

    /** Ordered list of memory updates to execute before the terminal action. */
    private final List<UpdateNode> updates;

    /** The terminal action to execute (ActionNode or another command). */
    private final Command terminalAction;

    private final List<ASTNode> childrenView;

    /**
     * Creates a command list with optional memory updates followed by a terminal action.
     * 
     * @param updates list of memory updates to execute (may be empty)
     * @param terminalAction the action to execute last (ActionNode or other command)
     * @param line source line where the command begins
     * @param column source column where the command begins
     */
    public CommandList(List<UpdateNode> updates, Command terminalAction, int line, int column){
        super(line, column, collectChildren(updates, terminalAction));
        
        this.terminalAction = Objects.requireNonNull(terminalAction, "Terminal action cannot be null");
        this.updates = updates == null ? List.of() : List.copyOf(updates);

        this.childrenView = Collections.unmodifiableList(collectChildren(updates, terminalAction));
    }

    private static List<ASTNode> collectChildren(List<UpdateNode> updates, Command terminalAction){
        List<ASTNode> children = new ArrayList<>(updates);
        if (updates != null){
            children.addAll(updates);
        }
        if (terminalAction != null){
            children.add(terminalAction);
        }
        return children;
    }
    /**
     * Returns the memory updates in execution order.
     * 
     * @return list of UpdateNode objects (may be empty)
     */
    public List<UpdateNode> getUpdates(){
        return updates;
    }

    /**
     * Returns the terminal action executed after all updates.
     * 
     * @return the terminal Command (ActionNode or other)
     */
    public Command getTerminalAction(){
        return terminalAction;
    }

    @Override
    public List<ASTNode> getChildren(){
        return childrenView;
    }
}

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
final class UpdateNode extends Command {
    /** Expression evaluating to the memory slot index to update. */
    private final Expression targetMemory;

    /** Expression evaluating to the new value for the target slot. */
    private final Expression value;

    private final List<ASTNode> childrenView;

    /**
     * Creates a memory update node.
     * 
     * @param targetMemory expression that evaluates to the target slot index
     * @param value expression that evaluates to the new value
     * @param line source line where the update appears
     * @param column source column where the update appears
     */
    public UpdateNode(Expression targetMemory, Expression value, int line, int column){
        super(line, column, targetMemory, value);

        this.targetMemory = Objects.requireNonNull(targetMemory, "Target memory expression cannot be null");
        this.value = Objects.requireNonNull(value, "Value expression cannot be null");

        this.childrenView = List.of(this.targetMemory, this.value);
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
        return childrenView;
    }
}