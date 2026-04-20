package compiler.ast.expression;

import compiler.ast.ASTVisitor;

public class MemoryNode extends Expression {
    private final Expression index;

    public MemoryNode(Expression index, int line, int column) {
        super(line, column);
        this.index = index;
    }

    public Expression getIndex() {
        return index;
    }
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
