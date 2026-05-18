package ast;


/**
 * Base class for all condition AST nodes.
 * 
 * Conditions represent boolean expressions that determine whether a rule should fire.
 * They are the "guard" part of a rule; a rule only executes if its condition evaluates to true.
 * 
 * Condition types include:
 * <ul>
 *   <li>{@link RelationNode}: atomic comparison (expr relop expr)</li>
 *   <li>{@link LogicNode}: logical combinations (and/or chains of conditions)</li>
 * </ul>
 * 
 * @see RelationNode
 * @see LogicNode
 * @see ast.Rule
 */
public sealed abstract class Condition extends ASTNode implements ast.marker.Cond
        permits LogicNode, RelationNode {
    /**
     * Initializes a Condition node at the given source location.
     * 
     * @param line source line number (1-based)
     * @param column source column number (1-based)
     * @param children direct child nodes in source order
     */
    protected Condition(int line, int column, ASTNode... children) {
        super(line, column, children);
    }
}
