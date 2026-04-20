package compiler.ast.expression;

import compiler.ast.ASTVisitor;
public class NumberNode extends Expression {
    private final int value;

    public NumberNode(int value, int line, int column) {
        super(line, column);
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
