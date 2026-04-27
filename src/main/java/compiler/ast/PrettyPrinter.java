package compiler.ast;

import compiler.ast.command.*;
import compiler.ast.expression.*;
import compiler.ast.condition.*;

/**
 * Formats an AST back into source-like text.
 *
 * This visitor emits a readable representation suitable for debugging,
 * snapshots, and round-trip validation of parser output.
 */
public class PrettyPrinter implements ASTVisitor<String> {

    /** Creates a pretty-printer visitor. */
    public PrettyPrinter() {
    }

    @Override
    public String visit(Program program) {
        StringBuilder sb = new StringBuilder();
        for (Rule rule : program.getRules()) {
            sb.append(rule.accept(this)).append("\n");
        }
        return sb.toString().trim();
    }

    @Override
    public String visit(Rule rule) {
        String conditionStr = rule.getCondition().accept(this);
        String commandStr = rule.getCommand().accept(this);
        return conditionStr + " --> " + commandStr + ";";
    }

    @Override
    public String visit(CommandList commandList) {
        StringBuilder sb = new StringBuilder();
        // Assuming getUpdates() returns List<UpdateNode>
        for (UpdateNode update : commandList.getUpdates()) {
            sb.append(update.accept(this)).append(" ");
        }
        sb.append(commandList.getTerminalAction().accept(this));
        return sb.toString().trim();
    }

    @Override
    public String visit(UpdateNode update) {
        // mem[expr] := expr
        String target = update.getTargetMemory().accept(this);
        String value = update.getValue().accept(this);
        return "mem[" + target + "] := " + value;
    }

    @Override
    public String visit(ActionNode action) {
        String baseAction = action.getActionType().toString().toLowerCase();
        if (action.hasArgument()) {
            return baseAction + "[" + action.getArgument().accept(this) + "]";
        }
        return baseAction;
    }

    @Override
    public String visit(BinaryExpr expr) {
        String left = expr.getLeft().accept(this);
        String right = expr.getRight().accept(this);
        String op = expr.getOperator();
        
        return "(" + left + " " + op + " " + right + ")";
    }

    @Override
    public String visit(NumberNode number) {
        return String.valueOf(number.getValue());
    }

    @Override
    public String visit(MemoryNode memory) {
        return "mem[" + memory.getIndex().accept(this) + "]";
    }

    @Override
    public String visit(SensorNode sensor) {
        String baseSensor = sensor.getSensorType().toString().toLowerCase();
        
        if (sensor.hasArgument()) {
            return baseSensor + "[" + sensor.getArgument().accept(this) + "]";
        }
        return baseSensor;
    }

    @Override
    public String visit(RelationNode relation) {
        // expr rel expr
        String left = relation.getLeft().accept(this);
        String right = relation.getRight().accept(this);
        String op = relation.getOperator().toString(); 
        
        return left + " " + op + " " + right;
    }

    @Override
    public String visit(LogicNode logic) {
        String left = logic.getLeft().accept(this);
        String right = logic.getRight().accept(this);
        String op = logic.getOperator().toString().toLowerCase(); 
        
        return "{" + left + " " + op + " " + right + "}";
    }
}