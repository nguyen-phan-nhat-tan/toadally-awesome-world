package compiler.ast;

import compiler.ast.command.ActionNode;
import compiler.ast.command.Command;
import compiler.ast.command.CommandList;
import compiler.ast.command.UpdateNode;
import compiler.ast.condition.Condition;
import compiler.ast.condition.LogicNode;
import compiler.ast.condition.RelationNode;
import compiler.ast.expression.BinaryExpr;
import compiler.ast.expression.Expression;
import compiler.ast.expression.MemoryNode;
import compiler.ast.expression.NumberNode;
import compiler.ast.expression.SensorNode;
import compiler.lexer.TokenType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

//AI-Generated
public final class MutatorVisitor implements ASTVisitor<ASTNode> {
    private static final int MAX_ATTEMPTS = 20;

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

    private MutatorVisitor(Program root, ASTNode target, Random random, MutationOperation operation) {
        this.root = root;
        this.target = target;
        this.random = random;
        this.operation = operation;
    }

    //AI-Generated
    public static Program mutate(Program root, Random random) {
        Program current = root;
        if (random.nextInt(4) != 0) {
            return current;
        }

        do {
            ASTNode target = selectRandomNode(current, random);
            MutationOperation operation = MutationOperation.values()[random.nextInt(MutationOperation.values().length)];
            MutatorVisitor mutator = new MutatorVisitor(current, target, random, operation);
            ASTNode mutated = current.accept(mutator);
            if (mutated instanceof Program mutatedProgram) {
                current = mutatedProgram;
            }
        } while (random.nextInt(4) == 0);

        return current;
    }

    //AI-Generated
    public static ASTNode selectRandomNode(ASTNode root, Random random) {
        int index = random.nextInt(root.subtreeSize);
        return selectByIndex(root, index);
    }

    //AI-Generated
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
            List<Rule> rules = new ArrayList<>(program.getRules()); //AI-Generated
            if (rules.isEmpty()) {
                rules.add(randomRule(program.getLine(), program.getColumn()));
            } else {
                int index = random.nextInt(rules.size());
                rules.set(index, (Rule) transformNode(rules.get(index)));
            }
            return new Program(rules, program.getLine(), program.getColumn());
        }
        if (node instanceof Rule rule) {
            return new Rule(rule.getCondition(), rule.getCommand(), rule.getLine(), rule.getColumn());
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
            return new ActionNode(actionType, action.getLine(), action.getColumn(), argument);
        }
        if (node instanceof UpdateNode update) {
            return new UpdateNode(update.getTargetMemory(), update.getValue(), update.getLine(), update.getColumn());
        }
        if (node instanceof CommandList commandList) {
            return new CommandList(new ArrayList<>(commandList.getUpdates()), commandList.getTerminalAction(), commandList.getLine(), commandList.getColumn());
        }
        return node;
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
        if (targetNode instanceof compiler.ast.marker.Expr) {
            return candidate instanceof compiler.ast.marker.Expr;
        }
        if (targetNode instanceof compiler.ast.marker.Cond) {
            return candidate instanceof compiler.ast.marker.Cond;
        }
        if (targetNode instanceof compiler.ast.marker.Cmd) {
            return candidate instanceof compiler.ast.marker.Cmd;
        }
        if (targetNode instanceof compiler.ast.marker.Rule || targetNode instanceof Program) {
            return candidate instanceof compiler.ast.marker.Rule;
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
            return new ActionNode(action.getActionType(), action.getLine(), action.getColumn(), argument);
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
        return new ActionNode(actionType, line, column, argument);
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

    @Override
    public ASTNode visit(Program program) {
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
            ASTNode result = rule.accept(this);
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

    @Override
    public ASTNode visit(Rule rule) {
        if (rule == target) {
            return mutateTarget(rule);
        }

        Condition condition = (Condition) rule.getCondition().accept(this);
        Command command = (Command) rule.getCommand().accept(this);
        if (condition == rule.getCondition() && command == rule.getCommand()) {
            return rule;
        }
        return new Rule(condition, command, rule.getLine(), rule.getColumn());
    }

    @Override
    public ASTNode visit(ActionNode action) {
        if (action == target) {
            return mutateTarget(action);
        }

        Expression argument = action.hasArgument() ? (Expression) action.getArgument().accept(this) : null;
        if (argument == action.getArgument()) {
            return action;
        }
        return new ActionNode(action.getActionType(), action.getLine(), action.getColumn(), argument);
    }

    @Override
    public ASTNode visit(UpdateNode update) {
        if (update == target) {
            return mutateTarget(update);
        }

        Expression targetMemory = (Expression) update.getTargetMemory().accept(this);
        Expression value = (Expression) update.getValue().accept(this);
        if (targetMemory == update.getTargetMemory() && value == update.getValue()) {
            return update;
        }
        return new UpdateNode(targetMemory, value, update.getLine(), update.getColumn());
    }

    @Override
    public ASTNode visit(CommandList commandList) {
        if (commandList == target) {
            return mutateTarget(commandList);
        }

        List<UpdateNode> rewrittenUpdates = new ArrayList<>();
        boolean changed = false;
        for (UpdateNode update : commandList.getUpdates()) {
            ASTNode result = update.accept(this);
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

        Command terminal = (Command) commandList.getTerminalAction().accept(this);
        if (terminal != commandList.getTerminalAction()) {
            changed = true;
        }

        if (!changed) {
            return commandList;
        }
        return new CommandList(rewrittenUpdates, terminal, commandList.getLine(), commandList.getColumn());
    }

    @Override
    public ASTNode visit(BinaryExpr expr) {
        if (expr == target) {
            return mutateTarget(expr);
        }

        Expression left = (Expression) expr.getLeft().accept(this);
        Expression right = (Expression) expr.getRight().accept(this);
        if (left == expr.getLeft() && right == expr.getRight()) {
            return expr;
        }
        return new BinaryExpr(left, expr.getOperator(), right, expr.getLine(), expr.getColumn());
    }

    @Override
    public ASTNode visit(NumberNode number) {
        if (number == target) {
            return mutateTarget(number);
        }
        return number;
    }

    @Override
    public ASTNode visit(MemoryNode memory) {
        if (memory == target) {
            return mutateTarget(memory);
        }

        Expression index = (Expression) memory.getIndex().accept(this);
        if (index == memory.getIndex()) {
            return memory;
        }
        return new MemoryNode(index, memory.getLine(), memory.getColumn());
    }

    @Override
    public ASTNode visit(SensorNode sensor) {
        if (sensor == target) {
            return mutateTarget(sensor);
        }

        Expression argument = sensor.hasArgument() ? (Expression) sensor.getArgument().accept(this) : null;
        if (argument == sensor.getArgument()) {
            return sensor;
        }
        return new SensorNode(sensor.getSensorType(), argument, sensor.getLine(), sensor.getColumn());
    }

    @Override
    public ASTNode visit(RelationNode relation) {
        if (relation == target) {
            return mutateTarget(relation);
        }

        Expression left = (Expression) relation.getLeft().accept(this);
        Expression right = (Expression) relation.getRight().accept(this);
        if (left == relation.getLeft() && right == relation.getRight()) {
            return relation;
        }
        return new RelationNode(left, relation.getOperator(), right, relation.getLine(), relation.getColumn());
    }

    @Override
    public ASTNode visit(LogicNode logic) {
        if (logic == target) {
            return mutateTarget(logic);
        }

        Condition left = (Condition) logic.getLeft().accept(this);
        Condition right = (Condition) logic.getRight().accept(this);
        if (left == logic.getLeft() && right == logic.getRight()) {
            return logic;
        }
        return new LogicNode(left, logic.getOperator(), right, logic.getLine(), logic.getColumn());
    }
}
