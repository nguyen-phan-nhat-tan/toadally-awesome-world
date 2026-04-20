package compiler.ast.condition;

import compiler.ast.ASTVisitor;
public class LogicNode extends Condition {
    private final Condition left;
    private final String operator;
    private final Condition right;

    public LogicNode(Condition left, String operator, Condition right, int line, int column) {
        super(line, column);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public Condition getLeft() {
        return left;
    }
    public String getOperator() {
        return operator;
    }
    public Condition getRight() {
        return right;
    }
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
    
}
