package compiler.ast;

import java.util.List;

/**
 * Top-level aggregate node that gives later phases one stable handoff point for the
 * entire parsed source.
 */
public class Program extends ASTNode {
    private final List<Rule> rules;

    /**
     * Keeps rule order intact because evaluation semantics depend on source ordering.
     *
     * @param rules ordered list of program rules
     * @param line source line for the program start
     * @param column source column for the program start
     */
    public Program(List<Rule> rules, int line, int column) {
        super(line, column);
        this.rules = rules;
    }

    /**
     * @return ordered rules for deterministic visitor behavior
     */
    public List<Rule> getRules() {
        return rules;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
