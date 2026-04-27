package compiler.ast.command;

import compiler.ast.ASTNode;

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
 * @see compiler.ast.Rule
 */
public abstract class Command extends ASTNode implements compiler.ast.marker.Cmd {
    /**
     * Initializes a Command node at the given source location.
     * 
     * @param line source line number (1-based)
     * @param column source column number (1-based)
     * @param children direct child nodes in source order
     */
    protected Command(int line, int column, ASTNode... children) {
        super(line, column, children);
    }
}
