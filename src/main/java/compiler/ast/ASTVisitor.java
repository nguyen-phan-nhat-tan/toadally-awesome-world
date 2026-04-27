package compiler.ast;

/**
 * Interface for visiting AST nodes and performing operations on them.
 * 
 * The Visitor design pattern allows us to implement new operations (printing, analysis,
 * interpretation, code generation) without modifying the AST node classes. Each visit method
 * corresponds to a specific AST node type and can perform type-specific logic.
 * 
 * Implementations of this interface are called by {@link ASTNode#accept(ASTVisitor)} to
 * perform some operation on the AST. Multiple visitors can coexist:
 * <ul>
 *   <li>AsciiTreePrinter: prints the tree structure</li>
 *   <li>PrettyPrinter: formats the source code</li>
 *   <li>MutatorVisitor: applies transformations</li>
 *   <li>Custom visitors: analysis, code generation, interpretation</li>
 * </ul>
 * 
 * @param <T> the return type produced by each visit method
 * @see ASTNode#accept(ASTVisitor)
 * @see Program
 * @see Rule
 */
public interface ASTVisitor<T> {
    /**
     * Visits a complete program node (top-level AST root).
     * 
     * @param program the program containing all rules
     * @return visitor-defined result for a program
     */
    T visit(Program program);

    /**
     * Visits an individual rule node (condition + command pair).
     * 
     * Provides rule-level granularity for policies or analyses that need to reason about
     * rules as distinct units rather than just their constituent parts.
     * 
     * @param rule the rule to visit
     * @return visitor-defined result for a rule
     */
    T visit(Rule rule);
    
    // ========== Command Nodes ==========
    
    /** Visits an action command (e.g., wait, forward, eat). */
    T visit(compiler.ast.command.ActionNode action);
    
    /** Visits a memory update command (mem[i] := value). */
    T visit(compiler.ast.command.UpdateNode update);
    
    /** Visits a command list (sequence of updates followed by an action). */
    T visit(compiler.ast.command.CommandList commandList);

    // ========== Expression Nodes ==========
    
    /** Visits a binary arithmetic expression (e.g., a + b, c * d). */
    T visit(compiler.ast.expression.BinaryExpr expr);
    
    /** Visits a numeric literal. */
    T visit(compiler.ast.expression.NumberNode number);
    
    /** Visits a memory access expression (mem[index]). */
    T visit(compiler.ast.expression.MemoryNode memory);
    
    /** Visits a sensor query expression (e.g., nearby[dir], smell). */
    T visit(compiler.ast.expression.SensorNode sensor);

    // ========== Condition Nodes ==========
    
    /** Visits a relational condition (e.g., x < 5, energy >= 10). */
    T visit(compiler.ast.condition.RelationNode relation);
    
    /** Visits a logical condition (and/or combinations of relations). */
    T visit(compiler.ast.condition.LogicNode logic);
}
