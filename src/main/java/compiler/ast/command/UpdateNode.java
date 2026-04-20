package compiler.ast.command;

import compiler.ast.expression.Expression;
import compiler.ast.ASTVisitor;
public class UpdateNode extends Command {
    private final Expression targetMemory;
    private final Expression value;

    public UpdateNode(Expression targetMemory, Expression value, int line, int column) {
        super(line, column);
        this.targetMemory = targetMemory;
        this.value = value;
    }

    public Expression getTargetMemory() {
        return targetMemory;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
    
}
