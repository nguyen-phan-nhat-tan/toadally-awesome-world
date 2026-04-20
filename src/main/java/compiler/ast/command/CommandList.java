package compiler.ast.command;

import compiler.ast.ASTVisitor;
import java.util.List;
public class CommandList extends Command {
    private final List<UpdateNode> updates;
    private final Command terminalAction;

    public CommandList(List<UpdateNode> updates, Command terminalAction, int line, int column) {
        super(line, column);
        this.updates = updates;
        this.terminalAction = terminalAction;
    }

    public List<UpdateNode> getUpdates() {
        return updates;
    }

    public Command getTerminalAction() {
        return terminalAction;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
