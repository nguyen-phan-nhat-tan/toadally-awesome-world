package compiler.ast.expression;

import compiler.ast.ASTNode;

public abstract class Expression extends ASTNode {
    public Expression(int line, int column) {
        super(line, column);
    }
}
