package compiler.ast;

import compiler.ast.command.*;
import compiler.ast.expression.*;
import compiler.ast.condition.*;
import java.util.List;

/**
 * Pretty-prints the abstract syntax tree as an ASCII tree diagram.
 * 
 * This visitor visualizes the tree structure using ASCII art, showing how nodes are nested
 * and their relationships. Useful for debugging and understanding parsed code structure.
 * 
 * Example output:
 * <pre>
 * Program
 * ├── Rule
 * │   ├── RelationNode [&lt;]
 * │   │   ├── MemoryNode
 * │   │   │   └── NumberNode [0]
 * │   │   └── NumberNode [50]
 * │   └── ActionNode [forward]
 * └── Rule
 *     ...
 * </pre>
 * 
 * @see ASTVisitor
 * @see PrettyPrinter
 */
public class AsciiTreePrinter implements ASTVisitor<String> {
    /** Creates an ASCII tree printer visitor. */
    public AsciiTreePrinter() {
    }

    private String indentChild(String childText, boolean isLast) {
        if (childText == null || childText.isEmpty()) return "";
        
        String[] lines = childText.split("\n");
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < lines.length; i++) {
            if (i == 0) {
                sb.append(isLast ? "└── " : "├── ").append(lines[i]);
            } else {
                sb.append(isLast ? "    " : "│   ").append(lines[i]);
            }
            if (i < lines.length - 1) sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public String visit(Program program) {
        StringBuilder sb = new StringBuilder("Program");
        List<Rule> rules = program.getRules();
        
        for (int i = 0; i < rules.size(); i++) {
            boolean isLast = (i == rules.size() - 1);
            sb.append("\n").append(indentChild(rules.get(i).accept(this), isLast));
        }
        return sb.toString();
    }

    @Override
    public String visit(Rule rule) {
        return "Rule\n" +
               indentChild(rule.getCondition().accept(this), false) + "\n" +
               indentChild(rule.getCommand().accept(this), true);
    }

    @Override
    public String visit(CommandList commandList) {
        StringBuilder sb = new StringBuilder("CommandList");
        List<UpdateNode> updates = commandList.getUpdates();
        
        for (UpdateNode update : updates) {
            sb.append("\n").append(indentChild(update.accept(this), false));
        }
        sb.append("\n").append(indentChild(commandList.getTerminalAction().accept(this), true));
        return sb.toString();
    }

    @Override
    public String visit(UpdateNode update) {
        return "UpdateNode\n" +
               indentChild(update.getTargetMemory().accept(this), false) + "\n" +
               indentChild(update.getValue().accept(this), true);
    }

    @Override
    public String visit(ActionNode action) {
        if (action.hasArgument()) {
            return "ActionNode [" + action.getActionType() + "]\n" +
                   indentChild(action.getArgument().accept(this), true);
        }
        return "ActionNode [" + action.getActionType() + "]";
    }

    @Override
    public String visit(BinaryExpr expr) {
        return "BinaryExpr [" + expr.getOperator() + "]\n" +
               indentChild(expr.getLeft().accept(this), false) + "\n" +
               indentChild(expr.getRight().accept(this), true);
    }

    @Override
    public String visit(NumberNode number) {
        return "NumberNode [" + number.getValue() + "]";
    }

    @Override
    public String visit(MemoryNode memory) {
        return "MemoryNode\n" +
               indentChild(memory.getIndex().accept(this), true);
    }

    @Override
    public String visit(SensorNode sensor) {
        if (sensor.hasArgument()) {
            return "SensorNode [" + sensor.getSensorType() + "]\n" +
                   indentChild(sensor.getArgument().accept(this), true);
        }
        return "SensorNode [" + sensor.getSensorType() + "]";
    }

    @Override
    public String visit(RelationNode relation) {
        return "RelationNode [" + relation.getOperator() + "]\n" +
               indentChild(relation.getLeft().accept(this), false) + "\n" +
               indentChild(relation.getRight().accept(this), true);
    }

    @Override
    public String visit(LogicNode logic) {
        return "LogicNode [" + logic.getOperator() + "]\n" +
               indentChild(logic.getLeft().accept(this), false) + "\n" +
               indentChild(logic.getRight().accept(this), true);
    }
}