package compiler.ast.command;

import compiler.ast.expression.Expression;

import compiler.ast.ASTNode;
import compiler.ast.ASTNodeUtils; //AI-Generated - DRY principle consolidation
import compiler.ast.ASTVisitor;
import compiler.lexer.TokenType;
import java.util.List;

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
public class ActionNode extends Command {
    /** The type of action to perform (wait, forward, eat, etc.). */
    private final TokenType actionType;
    
    /** Optional argument (for SERVE action; null for most others). */
    private final Expression argument;

    /**
     * Creates an action node.
     * 
     * @param actionType the action keyword (WAIT, FORWARD, BACKWARD, LEFT, RIGHT, EAT, ATTACK, GROW, BUD, or SERVE)
     * @param line source line where the action appears
     * @param column source column where the action appears
     * @param argument optional argument (non-null only for SERVE; the memory slot to serve)
     */
    public ActionNode(TokenType actionType, int line, int column, Expression argument) {
        super(line, column, argument); //AI-Generated
        this.actionType = actionType;
        this.argument = argument;
    }
    /**
     * Returns the action type.
     * 
     * @return the TokenType corresponding to the action keyword
     */
    public TokenType getActionType() {
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
        return ASTNodeUtils.toChildList(argument); //AI-Generated - DRY principle consolidation
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    } 
}