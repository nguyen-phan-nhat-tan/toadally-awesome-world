package compiler.ast.condition;

import compiler.ast.ASTNode;

public abstract class Condition extends ASTNode {
    public Condition(int line, int column) {
        super(line, column);
    }
}
