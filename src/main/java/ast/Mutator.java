package ast;

import lexer.TokenType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Applies random structural mutations to AST programs.
 *
 * This mutator is primarily used for fuzzing, robustness checks, and mutation-style
 * experiments that require syntactically valid but varied tree structures.
 */
public final class Mutator {
    private static final int MAX_ATTEMPTS = 20;
    private static final int MAX_MUTATION_ATTEMPTS = 50;

    private static final List<String> BINARY_OPS = List.of("+", "-", "*", "/", "mod");
    private static final List<String> REL_OPS = List.of("<", "<=", ">", ">=", "=", "!=");
    private static final List<String> LOGIC_OPS = List.of("and", "or");
    private static final List<TokenType> ACTIONS = List.of(
        TokenType.WAIT,
        TokenType.FORWARD,
        TokenType.BACKWARD,
        TokenType.LEFT,
        TokenType.RIGHT,
        TokenType.EAT,
        TokenType.ATTACK,
        TokenType.GROW,
        TokenType.BUD,
        TokenType.SERVE
    );
    private static final List<TokenType> SENSORS = List.of(
        TokenType.NEARBY,
        TokenType.AHEAD,
        TokenType.RANDOM,
        TokenType.SMELL
    );

    private enum MutationOperation {
        REMOVE,
        SWAP,
        TRANSFORM,
        INSERT,
        REPLACE,
        DUPLICATE
    }

    private final Program root;
    private final ASTNode target;
    private final Random random;
    private final MutationOperation operation;

    private Mutator(Program root, ASTNode target, Random random, MutationOperation operation) {
        this.root = root;
        this.target = target;
        this.random = random;
        this.operation = operation;
    }

    /**
     * Creates a new mutator with the given random source.
     * This constructor is used for instance-based mutation calls.
     *
     * @param random randomness source for mutation choices
     */
    public Mutator(Random random) {
        this.root = null;
        this.target = null;
        this.random = random;
        this.operation = null;
    }

    /**
     * Applies one or more random mutations to the input program.
     * Wraps the static mutate method for instance-based mutation.
     *
     * @param program program to mutate
     * @return mutated program
     */
    public Program mutate(Program program) {
        return mutate(program, random);
    }

    /**
     * Applies random structural mutations to the input program, forcing at least one
     * visible change in the rendered source when requested.
     *
     * @param program program to mutate
     * @param forceMutation true to retry until the pretty-printed program changes
     * @return mutated program
     */
    public Program mutate(Program program, boolean forceMutation) {
        return mutate(program, random, forceMutation);
    }

    /**
     * Applies zero or more random mutations to the input program.
     *
     * @param root program to mutate
     * @param random randomness source controlling target and operation selection
     * @return mutated program (or original when no mutation is chosen)
     */
    public static Program mutate(Program root, Random random) {
        return mutate(root, random, false);
    }

    /**
     * Applies zero or more random mutations to the input program.
     *
     * @param root program to mutate
     * @param random randomness source controlling target and operation selection
     * @param forceMutation true to keep retrying until the rendered program changes
     * @return mutated program (or original when no mutation is chosen)
     */
    public static Program mutate(Program root, Random random, boolean forceMutation) {
        Program current = root;
        if (!forceMutation && random.nextInt(4) != 0) {
            return current;
        }

        String originalText = forceMutation ? new PrettyPrinter().format(root) : null;
        int attempts = 0;

        do {
            ASTNode target = selectRandomNode(current, random);
            MutationOperation operation = MutationOperation.values()[random.nextInt(MutationOperation.values().length)];
            Mutator mutator = new Mutator(current, target, random, operation);
            ASTNode mutated = mutator.rewrite(current);
            if (mutated instanceof Program mutatedProgram) {
                current = mutatedProgram;
            }

            if (forceMutation && !new PrettyPrinter().format(current).equals(originalText)) {
                return current;
            }

            attempts++;
        } while (forceMutation ? attempts < MAX_MUTATION_ATTEMPTS : random.nextInt(4) == 0);

        return current;
    }

    /**
     * Selects a random AST node by subtree-size-weighted index.
     *
     * @param root root node to sample from
     * @param random randomness source
     * @return selected node within the subtree rooted at root
     */
    public static ASTNode selectRandomNode(ASTNode root, Random random) {
        int index = random.nextInt(root.subtreeSize);
        return selectByIndex(root, index);
    }

    
    private static ASTNode selectByIndex(ASTNode node, int index) {
        if (index == 0) {
            return node;
        }

        int remaining = index - 1;
        for (ASTNode child : node.getChildren()) {
            if (child == null) {
                continue;
            }
            if (remaining < child.subtreeSize) {
                return selectByIndex(child, remaining);
            }
            remaining -= child.subtreeSize;
        }

        return node;
    }

    private ASTNode mutateTarget(ASTNode node) {
        return switch (operation) {
            case REMOVE -> removeNode(node);
            case SWAP -> swapNode(node);
            case TRANSFORM -> transformNode(node);
            case INSERT -> insertNode(node);
            case REPLACE -> replaceNode(node);
            case DUPLICATE -> duplicateNode(node);
        };
    }

    private ASTNode removeNode(ASTNode node) {
        if (node instanceof Rule || node instanceof UpdateNode) {
            return null;
        }
        return transformNode(node);
    }

    private ASTNode swapNode(ASTNode node) {
        if (node instanceof Program program && program.getRules().size() > 1) {
            List<Rule> swapped = new ArrayList<>(program.getRules());
            int i = random.nextInt(swapped.size());
            int j = random.nextInt(swapped.size());
            while (j == i) {
                j = random.nextInt(swapped.size());
            }
            Collections.swap(swapped, i, j);
            return new Program(swapped, program.getLine(), program.getColumn());
        }
        if (node instanceof CommandList commandList && commandList.getUpdates().size() > 1) {
            List<UpdateNode> swapped = new ArrayList<>(commandList.getUpdates());
            int i = random.nextInt(swapped.size());
            int j = random.nextInt(swapped.size());
            while (j == i) {
                j = random.nextInt(swapped.size());
            }
            Collections.swap(swapped, i, j);
            return new CommandList(swapped, commandList.getTerminalAction(), commandList.getLine(), commandList.getColumn());
        }
        if (node instanceof BinaryExpr expr) {
            return new BinaryExpr(expr.getRight(), expr.getOperator(), expr.getLeft(), expr.getLine(), expr.getColumn());
        }
        if (node instanceof RelationNode relation) {
            return new RelationNode(relation.getRight(), relation.getOperator(), relation.getLeft(), relation.getLine(), relation.getColumn());
        }
        if (node instanceof LogicNode logic) {
            return new LogicNode(logic.getRight(), logic.getOperator(), logic.getLeft(), logic.getLine(), logic.getColumn());
        }
        if (node instanceof UpdateNode update) {
            return new UpdateNode(update.getValue(), update.getTargetMemory(), update.getLine(), update.getColumn());
        }
        return transformNode(node);
    }

    private ASTNode transformNode(ASTNode node) {
        if (node instanceof Program program) {
            List<Rule> rules = new ArrayList<>(program.getRules()); 
            if (rules.isEmpty()) {
                rules.add(randomRule(program.getLine(), program.getColumn()));
            } else {
                int index = random.nextInt(rules.size());
                rules.set(index, mutateRule(rules.get(index)));
            }
            return new Program(rules, program.getLine(), program.getColumn());
        }
        if (node instanceof Rule rule) {
            return mutateRule(rule);
        }
        if (node instanceof BinaryExpr expr) {
            return new BinaryExpr(expr.getLeft(), randomFrom(BINARY_OPS), expr.getRight(), expr.getLine(), expr.getColumn());
        }
        if (node instanceof NumberNode number) {
            int divisor = random.nextInt();
            if (divisor == 0) {
                divisor = 1;
            }
            int delta = Integer.MAX_VALUE / divisor;
            long adjusted = (long) number.getValue() + delta;
            int clamped = (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, adjusted));
            return new NumberNode(clamped, number.getLine(), number.getColumn());
        }
        if (node instanceof MemoryNode memory) {
            return new MemoryNode(memory.getIndex(), memory.getLine(), memory.getColumn());
        }
        if (node instanceof SensorNode sensor) {
            TokenType sensorType = randomFrom(SENSORS);
            Expression argument = sensorType == TokenType.SMELL ? null : sensor.getArgument();
            if (sensorType != TokenType.SMELL && argument == null) {
                argument = new NumberNode(Math.abs(random.nextInt(8)), sensor.getLine(), sensor.getColumn());
            }
            return new SensorNode(sensorType, argument, sensor.getLine(), sensor.getColumn());
        }
        if (node instanceof RelationNode relation) {
            return new RelationNode(relation.getLeft(), randomFrom(REL_OPS), relation.getRight(), relation.getLine(), relation.getColumn());
        }
        if (node instanceof LogicNode logic) {
            return new LogicNode(logic.getLeft(), randomFrom(LOGIC_OPS), logic.getRight(), logic.getLine(), logic.getColumn());
        }
        if (node instanceof ActionNode action) {
            TokenType actionType = randomFrom(ACTIONS);
            Expression argument = actionType == TokenType.SERVE ? action.getArgument() : null;
            if (actionType == TokenType.SERVE && argument == null) {
                argument = new NumberNode(Math.abs(random.nextInt(10)) + 1, action.getLine(), action.getColumn());
            }
            return new ActionNode(actionType, argument, action.getLine(), action.getColumn());
        }
        if (node instanceof UpdateNode update) {
            return new UpdateNode(update.getTargetMemory(), update.getValue(), update.getLine(), update.getColumn());
        }
        if (node instanceof CommandList commandList) {
            return new CommandList(new ArrayList<>(commandList.getUpdates()), commandList.getTerminalAction(), commandList.getLine(), commandList.getColumn());
        }
        return node;
    }

    private Rule mutateRule(Rule rule) {
        if (random.nextBoolean()) {
            Condition condition = (Condition) transformNode(rule.getCondition());
            if (condition == rule.getCondition()) {
                condition = randomCondition(rule.getLine(), rule.getColumn());
            }
            return new Rule(condition, rule.getCommand(), rule.getLine(), rule.getColumn());
        }

        Command command = (Command) transformNode(rule.getCommand());
        if (command == rule.getCommand()) {
            command = randomCommand(rule.getLine(), rule.getColumn());
        }
        return new Rule(rule.getCondition(), command, rule.getLine(), rule.getColumn());
    }

    private ASTNode insertNode(ASTNode node) {
        if (node instanceof Expression expr) {
            Expression fill = randomExpression(expr.getLine(), expr.getColumn());
            return new BinaryExpr(expr, randomFrom(BINARY_OPS), fill, expr.getLine(), expr.getColumn());
        }
        if (node instanceof Condition condition) {
            Condition fill = randomCondition(condition.getLine(), condition.getColumn());
            return new LogicNode(condition, randomFrom(LOGIC_OPS), fill, condition.getLine(), condition.getColumn());
        }
        if (node instanceof Command command) {
            List<UpdateNode> updates = List.of(randomUpdate(command.getLine(), command.getColumn()));
            return new CommandList(updates, command, command.getLine(), command.getColumn());
        }
        if (node instanceof Program program) {
            List<Rule> rules = new ArrayList<>(program.getRules());
            int index = rules.isEmpty() ? 0 : random.nextInt(rules.size() + 1);
            rules.add(index, randomRule(program.getLine(), program.getColumn()));
            return new Program(rules, program.getLine(), program.getColumn());
        }
        return transformNode(node);
    }

    private ASTNode replaceNode(ASTNode node) {
        ASTNode replacement = sampleSameKind(node);
        if (replacement == null) {
            return transformNode(node);
        }
        return replacement;
    }

    private ASTNode duplicateNode(ASTNode node) {
        ASTNode candidate = sampleSameKind(node);
        if (candidate == null) {
            return transformNode(node);
        }

        if (node instanceof Program program && candidate instanceof Rule duplicatedRule) {
            List<Rule> rules = new ArrayList<>(program.getRules());
            int index = rules.isEmpty() ? 0 : random.nextInt(rules.size() + 1);
            rules.add(index, duplicatedRule);
            return new Program(rules, program.getLine(), program.getColumn());
        }
        if (node instanceof Expression expr && candidate instanceof Expression duplicateExpr) {
            return new BinaryExpr(expr, randomFrom(BINARY_OPS), duplicateExpr, expr.getLine(), expr.getColumn());
        }
        if (node instanceof Condition condition && candidate instanceof Condition duplicateCondition) {
            return new LogicNode(condition, randomFrom(LOGIC_OPS), duplicateCondition, condition.getLine(), condition.getColumn());
        }
        if (node instanceof Command command) {
            List<UpdateNode> updates = List.of(randomUpdate(command.getLine(), command.getColumn()));
            return new CommandList(updates, command, command.getLine(), command.getColumn());
        }

        return transformNode(node);
    }

    private ASTNode sampleSameKind(ASTNode node) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            ASTNode candidate = selectRandomNode(root, random);
            if (candidate == node) {
                continue;
            }
            if (isSameKind(node, candidate)) {
                return deepCopy(candidate);
            }
        }
        return null;
    }

    private boolean isSameKind(ASTNode targetNode, ASTNode candidate) {
        if (targetNode instanceof ast.marker.Expr) {
            return candidate instanceof ast.marker.Expr;
        }
        if (targetNode instanceof ast.marker.Cond) {
            return candidate instanceof ast.marker.Cond;
        }
        if (targetNode instanceof ast.marker.Cmd) {
            return candidate instanceof ast.marker.Cmd;
        }
        if (targetNode instanceof ast.marker.Rule || targetNode instanceof Program) {
            return candidate instanceof ast.marker.Rule;
        }
        return targetNode.getClass().isInstance(candidate);
    }

    private ASTNode deepCopy(ASTNode node) {
        if (node instanceof Program program) {
            List<Rule> copiedRules = new ArrayList<>(program.getRules().size());
            for (Rule rule : program.getRules()) {
                copiedRules.add((Rule) deepCopy(rule));
            }
            return new Program(copiedRules, program.getLine(), program.getColumn());
        }
        if (node instanceof Rule rule) {
            return new Rule((Condition) deepCopy(rule.getCondition()), (Command) deepCopy(rule.getCommand()), rule.getLine(), rule.getColumn());
        }
        if (node instanceof BinaryExpr expr) {
            return new BinaryExpr((Expression) deepCopy(expr.getLeft()), expr.getOperator(), (Expression) deepCopy(expr.getRight()), expr.getLine(), expr.getColumn());
        }
        if (node instanceof NumberNode number) {
            return new NumberNode(number.getValue(), number.getLine(), number.getColumn());
        }
        if (node instanceof MemoryNode memory) {
            return new MemoryNode((Expression) deepCopy(memory.getIndex()), memory.getLine(), memory.getColumn());
        }
        if (node instanceof SensorNode sensor) {
            Expression argument = sensor.hasArgument() ? (Expression) deepCopy(sensor.getArgument()) : null;
            return new SensorNode(sensor.getSensorType(), argument, sensor.getLine(), sensor.getColumn());
        }
        if (node instanceof RelationNode relation) {
            return new RelationNode((Expression) deepCopy(relation.getLeft()), relation.getOperator(), (Expression) deepCopy(relation.getRight()), relation.getLine(), relation.getColumn());
        }
        if (node instanceof LogicNode logic) {
            return new LogicNode((Condition) deepCopy(logic.getLeft()), logic.getOperator(), (Condition) deepCopy(logic.getRight()), logic.getLine(), logic.getColumn());
        }
        if (node instanceof ActionNode action) {
            Expression argument = action.hasArgument() ? (Expression) deepCopy(action.getArgument()) : null;
            return new ActionNode(action.getActionType(), argument, action.getLine(), action.getColumn());
        }
        if (node instanceof UpdateNode update) {
            return new UpdateNode((Expression) deepCopy(update.getTargetMemory()), (Expression) deepCopy(update.getValue()), update.getLine(), update.getColumn());
        }
        if (node instanceof CommandList commandList) {
            List<UpdateNode> updates = new ArrayList<>(commandList.getUpdates().size());
            for (UpdateNode update : commandList.getUpdates()) {
                updates.add((UpdateNode) deepCopy(update));
            }
            return new CommandList(updates, (Command) deepCopy(commandList.getTerminalAction()), commandList.getLine(), commandList.getColumn());
        }
        throw new IllegalStateException("Unsupported node copy: " + node.getClass().getName());
    }

    private Rule randomRule(int line, int column) {
        return new Rule(randomCondition(line, column), randomCommand(line, column), line, column);
    }

    private Command randomCommand(int line, int column) {
        TokenType actionType = randomFrom(ACTIONS);
        Expression argument = actionType == TokenType.SERVE ? randomExpression(line, column) : null;
        return new ActionNode(actionType, argument, line, column);
    }

    private UpdateNode randomUpdate(int line, int column) {
        Expression index = new NumberNode(Math.abs(random.nextInt(6)), line, column);
        Expression target = new MemoryNode(index, line, column);
        return new UpdateNode(target, randomExpression(line, column), line, column);
    }

    private Condition randomCondition(int line, int column) {
        return new RelationNode(randomExpression(line, column), randomFrom(REL_OPS), randomExpression(line, column), line, column);
    }

    private Expression randomExpression(int line, int column) {
        int choice = random.nextInt(3);
        if (choice == 0) {
            return new NumberNode(Math.abs(random.nextInt(100)), line, column);
        }
        if (choice == 1) {
            return new MemoryNode(new NumberNode(Math.abs(random.nextInt(8)), line, column), line, column);
        }
        TokenType sensor = randomFrom(SENSORS);
        Expression argument = sensor == TokenType.SMELL ? null : new NumberNode(Math.abs(random.nextInt(6)), line, column);
        return new SensorNode(sensor, argument, line, column);
    }

    private <T> T randomFrom(List<T> values) {
        return values.get(random.nextInt(values.size()));
    }

    private ASTNode rewrite(ASTNode node) {
        return switch (node) {
            case Program program -> rewrite(program);
            case Rule rule -> rewrite(rule);
            case Command command -> rewrite(command);
            case Condition condition -> rewrite(condition);
            case Expression expression -> rewrite(expression);
            default -> throw new IllegalArgumentException("Unsupported AST node: " + node.getClass().getName());
        };
    }

    private ASTNode rewrite(Command command) {
        return switch (command) {
            case ActionNode action -> rewrite(action);
            case CommandList commandList -> rewrite(commandList);
            case UpdateNode update -> rewrite(update);
        };
    }

    private ASTNode rewrite(Condition condition) {
        return switch (condition) {
            case LogicNode logic -> rewrite(logic);
            case RelationNode relation -> rewrite(relation);
        };
    }

    private ASTNode rewrite(Expression expression) {
        return switch (expression) {
            case BinaryExpr expr -> rewrite(expr);
            case MemoryNode memory -> rewrite(memory);
            case NumberNode number -> rewrite(number);
            case SensorNode sensor -> rewrite(sensor);
        };
    }

    private ASTNode rewrite(Program program) {
        if (program == target) {
            ASTNode mutated = mutateTarget(program);
            if (mutated instanceof Program mutatedProgram && !mutatedProgram.getRules().isEmpty()) {
                return mutatedProgram;
            }
            return program;
        }

        List<Rule> rewritten = new ArrayList<>();
        boolean changed = false;
        for (Rule rule : program.getRules()) {
            ASTNode result = rewrite(rule);
            if (result == null) {
                changed = true;
                continue;
            }
            if (!(result instanceof Rule newRule)) {
                changed = true;
                continue;
            }
            rewritten.add(newRule);
            if (newRule != rule) {
                changed = true;
            }
        }

        if (rewritten.isEmpty()) {
            rewritten.add(program.getRules().isEmpty() ? randomRule(program.getLine(), program.getColumn()) : program.getRules().get(0));
            changed = true;
        }

        if (!changed) {
            return program;
        }
        return new Program(rewritten, program.getLine(), program.getColumn());
    }
    private ASTNode rewrite(Rule rule) {
        if (rule == target) {
            return mutateTarget(rule);
        }

        Condition condition = (Condition) rewrite(rule.getCondition());
        Command command = (Command) rewrite(rule.getCommand());
        if (condition == rule.getCondition() && command == rule.getCommand()) {
            return rule;
        }
        return new Rule(condition, command, rule.getLine(), rule.getColumn());
    }
    private ASTNode rewrite(ActionNode action) {
        if (action == target) {
            return mutateTarget(action);
        }

        Expression argument = action.hasArgument() ? (Expression) rewrite(action.getArgument()) : null;
        if (argument == action.getArgument()) {
            return action;
        }
        return new ActionNode(action.getActionType(), argument, action.getLine(), action.getColumn());
    }
    private ASTNode rewrite(UpdateNode update) {
        if (update == target) {
            return mutateTarget(update);
        }

        Expression targetMemory = (Expression) rewrite(update.getTargetMemory());
        Expression value = (Expression) rewrite(update.getValue());
        if (targetMemory == update.getTargetMemory() && value == update.getValue()) {
            return update;
        }
        return new UpdateNode(targetMemory, value, update.getLine(), update.getColumn());
    }
    private ASTNode rewrite(CommandList commandList) {
        if (commandList == target) {
            return mutateTarget(commandList);
        }

        List<UpdateNode> rewrittenUpdates = new ArrayList<>();
        boolean changed = false;
        for (UpdateNode update : commandList.getUpdates()) {
            ASTNode result = rewrite(update);
            if (result == null) {
                changed = true;
                continue;
            }
            if (!(result instanceof UpdateNode rewritten)) {
                changed = true;
                continue;
            }
            rewrittenUpdates.add(rewritten);
            if (rewritten != update) {
                changed = true;
            }
        }

        Command terminal = null;
        if (commandList.getTerminalAction() != null) {
            terminal = (Command) rewrite(commandList.getTerminalAction());
            if (terminal != commandList.getTerminalAction()) {
                changed = true;
            }
        } else {
            // If terminal action is null, use a default wait action
            terminal = new ActionNode(TokenType.WAIT, null, commandList.getLine(), commandList.getColumn());
            changed = true;
        }

        if (!changed) {
            return commandList;
        }
        return new CommandList(rewrittenUpdates, terminal, commandList.getLine(), commandList.getColumn());
    }
    private ASTNode rewrite(BinaryExpr expr) {
        if (expr == target) {
            return mutateTarget(expr);
        }

        Expression left = (Expression) rewrite(expr.getLeft());
        Expression right = (Expression) rewrite(expr.getRight());
        if (left == expr.getLeft() && right == expr.getRight()) {
            return expr;
        }
        return new BinaryExpr(left, expr.getOperator(), right, expr.getLine(), expr.getColumn());
    }
    private ASTNode rewrite(NumberNode number) {
        if (number == target) {
            return mutateTarget(number);
        }
        return number;
    }
    private ASTNode rewrite(MemoryNode memory) {
        if (memory == target) {
            return mutateTarget(memory);
        }

        Expression index = (Expression) rewrite(memory.getIndex());
        if (index == memory.getIndex()) {
            return memory;
        }
        return new MemoryNode(index, memory.getLine(), memory.getColumn());
    }
    private ASTNode rewrite(SensorNode sensor) {
        if (sensor == target) {
            return mutateTarget(sensor);
        }

        Expression argument = sensor.hasArgument() ? (Expression) rewrite(sensor.getArgument()) : null;
        if (argument == sensor.getArgument()) {
            return sensor;
        }
        return new SensorNode(sensor.getSensorType(), argument, sensor.getLine(), sensor.getColumn());
    }
    private ASTNode rewrite(RelationNode relation) {
        if (relation == target) {
            return mutateTarget(relation);
        }

        Expression left = (Expression) rewrite(relation.getLeft());
        Expression right = (Expression) rewrite(relation.getRight());
        if (left == relation.getLeft() && right == relation.getRight()) {
            return relation;
        }
        return new RelationNode(left, relation.getOperator(), right, relation.getLine(), relation.getColumn());
    }
    private ASTNode rewrite(LogicNode logic) {
        if (logic == target) {
            return mutateTarget(logic);
        }

        Condition left = (Condition) rewrite(logic.getLeft());
        Condition right = (Condition) rewrite(logic.getRight());
        if (left == logic.getLeft() && right == logic.getRight()) {
            return logic;
        }
        return new LogicNode(left, logic.getOperator(), right, logic.getLine(), logic.getColumn());
    }
}
