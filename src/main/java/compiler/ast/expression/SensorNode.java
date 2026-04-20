package compiler.ast.expression;

import compiler.ast.ASTVisitor;
import compiler.lexer.TokenType;

public class SensorNode extends Expression {
    private final TokenType sensorType;
    private final Expression argument;

    public SensorNode(TokenType sensorType, Expression argument, int line, int column) {
        super(line, column);
        this.sensorType = sensorType;
        this.argument = argument;
    }

    public TokenType getSensorType() {
        return sensorType;
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
