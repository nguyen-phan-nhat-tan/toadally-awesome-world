package compiler.ast;

import java.util.List;

public class Program extends ASTNode {
    private final List<Rule> rules;

    public Program(List<Rule> rules, int line, int column) {
        super(line, column);
        this.rules = rules;
    }

    public List<Rule> getRules() {
        return rules;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
