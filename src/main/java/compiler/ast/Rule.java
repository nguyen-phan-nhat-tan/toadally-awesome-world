package compiler.ast;

import compiler.ast.command.Command;
import compiler.ast.condition.Condition;
public class Rule extends ASTNode {
    private final Condition condition;
    private final Command command;

    public Rule(Condition condition, Command command, int line, int column) {
        super(line, column);
        this.condition = condition;
        this.command = command;
    }

    public Condition getCondition() {
        return condition;
    }

    public Command getCommand() {
        return command;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
