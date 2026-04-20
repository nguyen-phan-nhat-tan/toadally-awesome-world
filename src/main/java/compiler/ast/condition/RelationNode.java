package compiler.ast.condition;

import compiler.ast.expression.Expression;

import compiler.ast.ASTVisitor;

public class RelationNode extends Condition {
    private final Expression left;
    private final String operator;
    private final Expression right;

    public RelationNode(Expression left, String operator, Expression right, int line, int column) {
        super(line, column);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public Expression getLeft() {
        return left;
    }
    public String getOperator() {
        return operator;
    }
    public Expression getRight() {
        return right;
    }
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
