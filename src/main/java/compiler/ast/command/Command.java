package compiler.ast.command;

import compiler.ast.ASTNode;

public abstract class Command extends ASTNode {
    public Command(int line, int column) {
        super(line, column);
    }
}
