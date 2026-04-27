package compiler.ast;

import compiler.ast.command.Command;
import compiler.ast.condition.Condition;
import java.util.List;

/**
 * Represents a single rule in the program.
 * 
 * A rule is a reactive unit: if its condition is true, its command executes.
 * Rules are the fundamental building block of critter behavior. The program is simply
 * a sequence of rules, typically evaluated in order until one matches.
 * 
 * The condition and command are stored together as a single logical unit, making it easy
 * for later phases (analysis, interpretation, code generation) to reason about rules as
 * complete decision units without decomposing them.
 * 
 * @see Condition
 * @see Command
 * @see Program
 */
public class Rule extends ASTNode implements compiler.ast.marker.Rule {
    /** The guard condition that determines whether this rule should fire. */
    private final Condition condition;
    
    /** The command to execute when the condition evaluates to true. */
    private final Command command;

    /**
     * Creates a new rule pairing a condition and command.
     * 
     * The condition and command are evaluated as a unit: the rule only fires if the
     * condition is true. Position information is preserved from the first token of the rule
     * for accurate error reporting.
     *
     * @param condition the guard condition (non-null)
     * @param command the command to execute (non-null)
     * @param line source line where the rule starts
     * @param column source column where the rule starts
     */
    public Rule(Condition condition, Command command, int line, int column) {
        super(line, column, condition, command);
        this.condition = condition;
        this.command = command;
    }

    /**
     * Returns the condition that must be true for this rule to fire.
     * 
     * @return the Condition node (never null)
     */
    public Condition getCondition() {
        return condition;
    }

    /**
     * Returns the command to execute if this rule's condition is true.
     * 
     * @return the Command node (never null)
     */
    public Command getCommand() {
        return command;
    }

    @Override
    public List<ASTNode> getChildren() {
        return List.of(condition, command);
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
