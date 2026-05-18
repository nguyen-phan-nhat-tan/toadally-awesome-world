package ast;


/**
 * Formats an AST back into source-like text using sealed-type dispatch.
 */
public final class PrettyPrinter {

    /**
     * Formats any supported AST node.
     *
     * @param node AST node to format
     * @return source-like representation
     */
    public String format(ASTNode node) {
        return switch (node) {
            case Program program -> format(program);
            case Rule rule -> format(rule);
            case Command command -> format(command);
            case Condition condition -> format(condition);
            case Expression expression -> format(expression);
            default -> throw new IllegalArgumentException("Unsupported AST node: " + node.getClass().getName());
        };
    }

    public String format(Program program) {
        StringBuilder sb = new StringBuilder();
        for (Rule rule : program.getRules()) {
            sb.append(format(rule)).append("\n");
        }
        return sb.toString().trim();
    }

    public String format(Rule rule) {
        return format(rule.getCondition()) + " --> " + format(rule.getCommand()) + ";";
    }

    public String format(Command command) {
        return switch (command) {
            case CommandList commandList -> format(commandList);
            case UpdateNode update -> format(update);
            case ActionNode action -> format(action);
        };
    }

    public String format(CommandList commandList) {
        StringBuilder sb = new StringBuilder();
        for (UpdateNode update : commandList.getUpdates()) {
            sb.append(format(update)).append(" ");
        }
        sb.append(format(commandList.getTerminalAction()));
        return sb.toString().trim();
    }

    public String format(UpdateNode update) {
        return "mem[" + format(update.getTargetMemory()) + "] := " + format(update.getValue());
    }

    public String format(ActionNode action) {
        String baseAction = action.getActionType().toString().toLowerCase();
        if (action.hasArgument()) {
            return baseAction + "[" + format(action.getArgument()) + "]";
        }
        return baseAction;
    }

    public String format(Expression expression) {
        return switch (expression) {
            case BinaryExpr expr -> format(expr);
            case MemoryNode memory -> format(memory);
            case NumberNode number -> format(number);
            case SensorNode sensor -> format(sensor);
        };
    }

    public String format(BinaryExpr expr) {
        return "(" + format(expr.getLeft()) + " " + expr.getOperator() + " " + format(expr.getRight()) + ")";
    }

    public String format(NumberNode number) {
        return String.valueOf(number.getValue());
    }

    public String format(MemoryNode memory) {
        return "mem[" + format(memory.getIndex()) + "]";
    }

    public String format(SensorNode sensor) {
        String baseSensor = sensor.getSensorType().toString().toLowerCase();
        if (sensor.hasArgument()) {
            return baseSensor + "[" + format(sensor.getArgument()) + "]";
        }
        return baseSensor;
    }

    public String format(Condition condition) {
        return switch (condition) {
            case RelationNode relation -> format(relation);
            case LogicNode logic -> format(logic);
        };
    }

    public String format(RelationNode relation) {
        return format(relation.getLeft()) + " " + relation.getOperator() + " " + format(relation.getRight());
    }

    public String format(LogicNode logic) {
        return "{" + format(logic.getLeft()) + " " + logic.getOperator().toLowerCase() + " " + format(logic.getRight()) + "}";
    }
}
