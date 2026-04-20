package compiler.ast.command;

import compiler.ast.expression.Expression;

import compiler.ast.ASTVisitor;
import compiler.lexer.TokenType;

public class ActionNode extends Command {
    private final TokenType actionType;
    private final Expression argument;

    public ActionNode(TokenType actionType, int line, int column, Expression argument) {
        super(line, column);
        this.actionType = actionType;
        this.argument = argument;
    }
    public TokenType getActionType() {
        return actionType;
    }

    public Expression getArgument() {
        return argument;
    }

    public boolean hasArgument() {
        return argument != null;
    }

   @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    } 
}