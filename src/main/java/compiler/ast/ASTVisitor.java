package compiler.ast;

import compiler.ast.command.*;
import compiler.ast.expression.*;
import compiler.ast.condition.*;

public interface ASTVisitor<T> {
    T visit(Program program);
    T visit(Rule rule);
    
    T visit(ActionNode action);
    T visit(UpdateNode update);
    T visit(CommandList commandList);

    T visit(BinaryExpr expr);
    T visit(NumberNode number);
    T visit(MemoryNode memory);
    T visit(SensorNode sensor);

    T visit(RelationNode relation);
    T visit(LogicNode logic);
}
