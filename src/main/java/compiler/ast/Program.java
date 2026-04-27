package compiler.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete program at the top level of the AST.
 * 
 * A program is simply an ordered collection of rules. This node serves as a stable
 * handoff point between compiler phases: the parser produces a Program, which is then
 * passed to analysis, transformation, and interpretation stages.
 * 
 * The order of rules matters for semantics: rules are typically evaluated in order,
 * and the first matching rule fires. By preserving source order, we maintain predictable
 * and debuggable behavior.
 * 
 * @see Rule
 * @see compiler.parser.Parser#parseProgram()
 */
public class Program extends ASTNode {
    /** Ordered list of all rules in the program. */
    private final List<Rule> rules;

    /**
     * Keeps rule order intact because evaluation semantics depend on source ordering.
     *
     * @param rules ordered list of program rules
     * @param line source line for the program start
     * @param column source column for the program start
     */
    public Program(List<Rule> rules, int line, int column) {
        super(line, column, rules); //AI-Generated
        this.rules = rules;
    }

    /**
     * Returns the ordered list of rules in this program.
     * 
     * Callers should treat this as immutable; modifying the returned list will not
     * affect the Program state but may cause issues in visitors and analyses.
     * 
     * @return the list of rules in source order (never null, may be empty)
     */
    public List<Rule> getRules() {
        return rules;
    }

    @Override
    public List<ASTNode> getChildren() {
        return new ArrayList<>(rules); //AI-Generated
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
