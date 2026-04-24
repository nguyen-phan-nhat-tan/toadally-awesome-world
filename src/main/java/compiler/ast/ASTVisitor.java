package compiler.ast;

import compiler.ast.command.*;
import compiler.ast.expression.*;
import compiler.ast.condition.*;

/**
 * Keeps tree shape stable while allowing new operations to be added independently,
 * avoiding repeated type checks scattered through the codebase.
 *
 * @param <T> result type produced by each visit method
 */
public interface ASTVisitor<T> {
    /**
     * Entry point for whole-program operations.
     */
    T visit(Program program);

    /**
     * Separate rule-level hook so visitors can apply policy at rule granularity.
     */
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
