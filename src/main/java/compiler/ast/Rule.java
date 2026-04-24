package compiler.ast;

import compiler.ast.command.Command;
import compiler.ast.condition.Condition;

/**
 * Couples a guard with its effect so execution logic can reason about one decision unit
 * at a time.
 */
public class Rule extends ASTNode {
    private final Condition condition;
    private final Command command;

    /**
     * Stores condition and command together to preserve source-level intent through later
     * transformations and analyses.
     *
     * @param condition rule guard condition
     * @param command command executed when the condition is true
     * @param line source line where the rule starts
     * @param column source column where the rule starts
     */
    public Rule(Condition condition, Command command, int line, int column) {
        super(line, column);
        this.condition = condition;
        this.command = command;
    }

    /**
     * @return guard used to decide whether this rule should fire
     */
    public Condition getCondition() {
        return condition;
    }

    /**
     * @return command payload executed when the guard evaluates to true
     */
    public Command getCommand() {
        return command;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
