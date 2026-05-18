package ast;

import java.util.List;

/**
 * Prints an AST as an ASCII tree using sealed-type dispatch.
 */
public final class AsciiTreePrinter {

    /**
     * Prints any supported AST node.
     *
     * @param node AST node to render
     * @return ASCII tree representation
     */
    public String print(ASTNode node) {
        return switch (node) {
            case Program program -> print(program);
            case Rule rule -> print(rule);
            case Command command -> print(command);
            case Condition condition -> print(condition);
            case Expression expression -> print(expression);
            default -> throw new IllegalArgumentException("Unsupported AST node: " + node.getClass().getName());
        };
    }

    private String indentChild(String childText, boolean isLast) {
        if (childText == null || childText.isEmpty()) {
            return "";
        }

        String[] lines = childText.split("\n");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            if (i == 0) {
                sb.append(isLast ? "`-- " : "|-- ").append(lines[i]);
            } else {
                sb.append(isLast ? "    " : "|   ").append(lines[i]);
            }
            if (i < lines.length - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public String print(Program program) {
        StringBuilder sb = new StringBuilder("Program");
        List<Rule> rules = program.getRules();

        for (int i = 0; i < rules.size(); i++) {
            boolean isLast = (i == rules.size() - 1);
            sb.append("\n").append(indentChild(print(rules.get(i)), isLast));
        }
        return sb.toString();
    }

    public String print(Rule rule) {
        return "Rule\n"
            + indentChild(print(rule.getCondition()), false) + "\n"
            + indentChild(print(rule.getCommand()), true);
    }

    public String print(Command command) {
        return switch (command) {
            case CommandList commandList -> print(commandList);
            case UpdateNode update -> print(update);
            case ActionNode action -> print(action);
        };
    }

    public String print(CommandList commandList) {
        StringBuilder sb = new StringBuilder("CommandList");
        List<UpdateNode> updates = commandList.getUpdates();

        for (UpdateNode update : updates) {
            sb.append("\n").append(indentChild(print(update), false));
        }
        sb.append("\n").append(indentChild(print(commandList.getTerminalAction()), true));
        return sb.toString();
    }

    public String print(UpdateNode update) {
        return "UpdateNode\n"
            + indentChild(print(update.getTargetMemory()), false) + "\n"
            + indentChild(print(update.getValue()), true);
    }

    public String print(ActionNode action) {
        if (action.hasArgument()) {
            return "ActionNode [" + action.getActionType() + "]\n"
                + indentChild(print(action.getArgument()), true);
        }
        return "ActionNode [" + action.getActionType() + "]";
    }

    public String print(Expression expression) {
        return switch (expression) {
            case BinaryExpr expr -> print(expr);
            case MemoryNode memory -> print(memory);
            case NumberNode number -> print(number);
            case SensorNode sensor -> print(sensor);
        };
    }

    public String print(BinaryExpr expr) {
        return "BinaryExpr [" + expr.getOperator() + "]\n"
            + indentChild(print(expr.getLeft()), false) + "\n"
            + indentChild(print(expr.getRight()), true);
    }

    public String print(NumberNode number) {
        return "NumberNode [" + number.getValue() + "]";
    }

    public String print(MemoryNode memory) {
        return "MemoryNode\n" + indentChild(print(memory.getIndex()), true);
    }

    public String print(SensorNode sensor) {
        if (sensor.hasArgument()) {
            return "SensorNode [" + sensor.getSensorType() + "]\n"
                + indentChild(print(sensor.getArgument()), true);
        }
        return "SensorNode [" + sensor.getSensorType() + "]";
    }

    public String print(Condition condition) {
        return switch (condition) {
            case RelationNode relation -> print(relation);
            case LogicNode logic -> print(logic);
        };
    }

    public String print(RelationNode relation) {
        return "RelationNode [" + relation.getOperator() + "]\n"
            + indentChild(print(relation.getLeft()), false) + "\n"
            + indentChild(print(relation.getRight()), true);
    }

    public String print(LogicNode logic) {
        return "LogicNode [" + logic.getOperator() + "]\n"
            + indentChild(print(logic.getLeft()), false) + "\n"
            + indentChild(print(logic.getRight()), true);
    }
}
