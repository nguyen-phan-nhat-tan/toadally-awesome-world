package simulation;

import ast.*;
import lexer.Sugar;
import lexer.TokenType;

import java.util.Objects;
import ast.PrettyPrinter;
import java.util.Random;

/**
 * Interprets a parsed AST program for one critter.
 * The interpreter operates on the critter's owned memory array rather than maintaining its own.
 */
public final class ProgramCritterInterpreter implements CritterInterpreter {
    private static final int MAX_PASSES = 999;

    private final Program program;
    private final Random random;
    private int[] cachedMemory; // Cache for testing/debugging access
    private final PrettyPrinter pretty = new PrettyPrinter();

    /**
     * Creates an interpreter with a default random source.
     *
     * @param program parsed program to execute
     */
    public ProgramCritterInterpreter(Program program) {
        this(program, new Random());
    }

    /**
     * Creates an interpreter with an explicit random source.
     *
     * @param program parsed program to execute
     * @param random randomness source for random sensor/action behavior
     */
    public ProgramCritterInterpreter(Program program, Random random) {
        this.program = Objects.requireNonNull(program, "program");
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public Action interpret(Critter critter, World world) {
        Objects.requireNonNull(critter, "critter");
        Objects.requireNonNull(world, "world");

        critter.syncMemoryArray();
        int[] memory = critter.getMemory();
        this.cachedMemory = memory; // Cache for testing/debugging

        for (int pass = 1; pass <= MAX_PASSES; pass++) {
            memory[5] = pass;
            for (Rule rule : program.getRules()) {
                if (!evaluateCondition(rule.getCondition(), critter, world, memory)) {
                    continue;
                }

                Action action = executeCommand(rule.getCommand(), critter, world, memory);
                if (action != null) {
                    // record last fired rule text on critter for inspection
                    try {
                        critter.setLastRule(pretty.format(rule));
                    } catch (Exception ex) {
                        critter.setLastRule("<unavailable>");
                    }
                    return action;
                }
                // If action is null (memory-only update), continue scanning the next rule in this pass
            }
            // Inner loop completed without a physical action; try next pass
        }

        return new Action(ActionType.WAIT);
    }

    /**
     * Reads a value from the critter's memory array (for testing/debugging).
     *
     * @param index memory index
     * @return stored value, or zero for out-of-range indices
     */
    public int getMemoryValue(int index) {
        if (cachedMemory == null) {
            return 0;
        }
        if (index < 0 || index >= cachedMemory.length) {
            return 0;
        }
        return cachedMemory[index];
    }

    /**
     * Returns the number of rules in the backing program.
     *
     * @return rule count
     */
    public int getRuleCount() {
        return program.getRules().size();
    }

    /**
     * Returns the backing program (immutable reference).
     *
     * @return program AST
     */
    public ast.Program getProgram() {
        return program;
    }

    @Override
    public CritterInterpreter offspringCopy() {
        return new ProgramCritterInterpreter(program);
    }

    private Action executeCommand(Command command, Critter critter, World world, int[] memory) {
        if (command instanceof ActionNode actionNode) {
            return toAction(actionNode, critter, world, memory);
        }
        if (command instanceof CommandList commandList) {
            for (UpdateNode update : commandList.getUpdates()) {
                executeUpdate(update, critter, world, memory);
            }
            return executeCommand(commandList.getTerminalAction(), critter, world, memory);
        }
        if (command instanceof UpdateNode updateNode) {
            executeUpdate(updateNode, critter, world, memory);
            return null;
        }
        return null;
    }

    private void executeUpdate(UpdateNode updateNode, Critter critter, World world, int[] memory) {
        int targetIndex = evaluateExpression(updateNode.getTargetMemory(), critter, world, memory);
        int value = evaluateExpression(updateNode.getValue(), critter, world, memory);
        if (targetIndex < 0 || targetIndex >= memory.length) {
            return;
        }
        if (!Sugar.isAssignableSugar(targetIndex)) {
            return;
        }
        critter.writeMemory(targetIndex, value);
    }

    private Action toAction(ActionNode actionNode, Critter critter, World world, int[] memory) {
        TokenType type = actionNode.getActionType();
        return switch (type) {
            case WAIT -> new Action(ActionType.WAIT);
            case FORWARD -> new Action(ActionType.FORWARD);
            case BACKWARD -> new Action(ActionType.BACKWARD);
            case LEFT -> new Action(ActionType.LEFT);
            case RIGHT -> new Action(ActionType.RIGHT);
            case EAT -> new Action(ActionType.EAT);
            case ATTACK -> new Action(ActionType.ATTACK);
            case GROW -> new Action(ActionType.GROW);
            case BUD -> new Action(ActionType.BUD);
            case SERVE -> new Action(ActionType.SERVE, evaluateExpression(actionNode.getArgument(), critter, world, memory));
            default -> new Action(ActionType.WAIT);
        };
    }

    private boolean evaluateCondition(Condition condition, Critter critter, World world, int[] memory) {
        if (condition instanceof RelationNode relationNode) {
            int left = evaluateExpression(relationNode.getLeft(), critter, world, memory);
            int right = evaluateExpression(relationNode.getRight(), critter, world, memory);
            return switch (relationNode.getOperator()) {
                case "<" -> left < right;
                case ">" -> left > right;
                case "<=" -> left <= right;
                case ">=" -> left >= right;
                case "=" -> left == right;
                case "!=" -> left != right;
                default -> false;
            };
        }
        if (condition instanceof LogicNode logicNode) {
            boolean left = evaluateCondition(logicNode.getLeft(), critter, world, memory);
            boolean right = evaluateCondition(logicNode.getRight(), critter, world, memory);
            return switch (logicNode.getOperator()) {
                case "and" -> left && right;
                case "or" -> left || right;
                default -> false;
            };
        }
        return false;
    }

    private int evaluateExpression(Expression expression, Critter critter, World world, int[] memory) {
        if (expression instanceof NumberNode numberNode) {
            return numberNode.getValue();
        }
        if (expression instanceof BinaryExpr binaryExpr) {
            int left = evaluateExpression(binaryExpr.getLeft(), critter, world, memory);
            int right = evaluateExpression(binaryExpr.getRight(), critter, world, memory);
            return switch (binaryExpr.getOperator()) {
                case "+" -> left + right;
                case "-" -> left - right;
                case "*" -> left * right;
                case "/" -> right == 0 ? 0 : Math.floorDiv(left, right);
                case "mod" -> right == 0 ? 0 : Math.floorMod(left, right);
                default -> 0;
            };
        }
        if (expression instanceof MemoryNode memoryNode) {
            int index = evaluateExpression(memoryNode.getIndex(), critter, world, memory);
            return readMemory(index, memory);
        }
        if (expression instanceof SensorNode sensorNode) {
            return evaluateSensor(sensorNode, critter, world, memory);
        }
        return 0;
    }

    private int evaluateSensor(SensorNode sensorNode, Critter critter, World world, int[] memory) {
        return switch (sensorNode.getSensorType()) {
            case RANDOM -> {
                int bound = Math.max(1, evaluateExpression(sensorNode.getArgument(), critter, world, memory));
                yield random.nextInt(bound);
            }
            case SMELL -> smellNearestFood(world, critter);
            case NEARBY -> {
                int relativeDir = evaluateExpression(sensorNode.getArgument(), critter, world, memory);
                yield senseNearby(world, critter, relativeDir);
            }
            case AHEAD -> {
                int distance = evaluateExpression(sensorNode.getArgument(), critter, world, memory);
                yield senseAhead(world, critter, distance);
            }
            default -> 0;
        };
    }

    /**
     * Implements nearby[dir] sensor with appearance encoding.
     * dir is relative to the observing critter.
     * Absolute direction = (observer.direction + relative_dir) % 6
     */
    private int senseNearby(World world, Critter critter, int relativeDir) {
        int absoluteDir = Math.floorMod(critter.getDirection() + relativeDir, 6);
        HexCoordinate targetCoord = new HexCoordinate(critter.getX(), critter.getY())
            .step(HexDirection.fromIndex(absoluteDir));
        return senseCoordinateWithAppearance(world, critter, targetCoord);
    }

    /**
     * Implements ahead[dist] sensor with appearance encoding.
     * Negative distances are treated as 0 (self).
     * Distance 0 returns self appearance, distance > 0 looks ahead.
     */
    private int senseAhead(World world, Critter critter, int distance) {
        // Treat negative as 0
        if (distance < 0) {
            distance = 0;
        }
        
        HexCoordinate coordinate = new HexCoordinate(critter.getX(), critter.getY());
        HexDirection direction = HexDirection.fromIndex(critter.getDirection());
        
        // Step forward distance times
        for (int i = 0; i < distance; i++) {
            coordinate = coordinate.step(direction);
        }
        
        return senseCoordinateWithAppearance(world, critter, coordinate);
    }

    /**
     * Sense a coordinate and return proper encoding: appearance for critters, (-food-1) for food.
     * Uses appearance formula relative to the observing critter.
     */
    private int senseCoordinateWithAppearance(World world, Critter critter, HexCoordinate coordinate) {
        HexState hex = world.getHex(coordinate.x(), coordinate.y());
        
        if (hex.isRock()) {
            return -1; // Rock encoding
        }
        
        if (hex.hasCritter()) {
            Critter observedCritter = world.getCritterAt(coordinate.x(), coordinate.y());
            if (observedCritter != null) {
                return observedCritter.calculateAppearance(critter.getDirection());
            }
        }
        
        if (hex.hasFood()) {
            int foodAmount = hex.getFoodAmount();
            return -(foodAmount) - 1;
        }
        
        return 0;
    }

    /**
     * Implements smell sensor using BFS to find nearest food within MAX_SMELL_DISTANCE.
     * Returns relative direction to nearest food, or 0 if none found.
     */
    private static class SmellState {
        final HexCoordinate hex;
        final int dir;

        SmellState(HexCoordinate hex, int dir) {
            this.hex = hex;
            this.dir = dir;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SmellState)) return false;
            SmellState that = (SmellState) o;
            return dir == that.dir && hex.x() == that.hex.x() && hex.y() == that.hex.y();
        }

        @Override
        public int hashCode() {
            return Objects.hash(hex.x(), hex.y(), dir);
        }
    }

    /**
     * Implements smell sensor using Dijkstra to find cheapest food route.
     * Returns (energyCost / critterSize) * 1000 + initialDir, or -1 if none found.
     */
    private int smellNearestFood(World world, Critter critter) {
        HexCoordinate startHex = new HexCoordinate(critter.getX(), critter.getY());
        int startDir = critter.getDirection();
        int size = Math.max(1, critter.getSize());

        AdjustablePriorityQueue<SmellState> queue = new BinaryHeap<>();
        java.util.Map<SmellState, Integer> costs = new java.util.HashMap<>();
        java.util.Map<SmellState, Integer> bestInitialDir = new java.util.HashMap<>();
        java.util.Set<SmellState> visited = new java.util.HashSet<>();

        SmellState startState = new SmellState(startHex, startDir);
        queue.insert(startState, 0);
        costs.put(startState, 0);
        bestInitialDir.put(startState, 0);

        while (queue.size() > 0) {
            SmellState current = queue.peekMin();
            int currentCost = queue.getPriority(current);
            queue.extractMin();

            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);

            int initialDir = bestInitialDir.get(current);

            // Check if facing food
            HexCoordinate forwardHex = current.hex.step(HexDirection.fromIndex(current.dir));
            HexState forwardState = world.getHex(forwardHex.x(), forwardHex.y());
            if (forwardState.hasFood() && forwardState.getFoodAmount() > 0) {
                if (HexMath.distance(startHex.x(), startHex.y(), forwardHex.x(), forwardHex.y()) <= Constants.MAX_SMELL_DISTANCE) {
                    return (currentCost / size) * 1000 + initialDir;
                }
            }

            // Generate neighbors
            // 1. Turn left
            int leftDir = (current.dir + 5) % 6;
            SmellState leftState = new SmellState(current.hex, leftDir);
            int leftCost = currentCost + size;
            int leftInitial = current.hex.equals(startHex) ? (leftDir - startDir + 6) % 6 : initialDir;
            relaxSmellState(queue, visited, costs, bestInitialDir, leftState, leftCost, leftInitial);

            // 2. Turn right
            int rightDir = (current.dir + 1) % 6;
            SmellState rightState = new SmellState(current.hex, rightDir);
            int rightCost = currentCost + size;
            int rightInitial = current.hex.equals(startHex) ? (rightDir - startDir + 6) % 6 : initialDir;
            relaxSmellState(queue, visited, costs, bestInitialDir, rightState, rightCost, rightInitial);

            // 3. Move forward
            if (!forwardState.isRock()) {
                SmellState forwardStepState = new SmellState(forwardHex, current.dir);
                int moveCost = currentCost + Constants.MOVE_COST * size;
                int forwardInitial = current.hex.equals(startHex) ? (current.dir - startDir + 6) % 6 : initialDir;

                if (HexMath.distance(startHex.x(), startHex.y(), forwardHex.x(), forwardHex.y()) <= Constants.MAX_SMELL_DISTANCE + 5) {
                    relaxSmellState(queue, visited, costs, bestInitialDir, forwardStepState, moveCost, forwardInitial);
                }
            }
        }

        return -1; // No food found
    }

    private void relaxSmellState(AdjustablePriorityQueue<SmellState> queue,
                                 java.util.Set<SmellState> visited,
                                 java.util.Map<SmellState, Integer> costs,
                                 java.util.Map<SmellState, Integer> bestInitialDir,
                                 SmellState state, int newCost, int initialDir) {
        if (visited.contains(state)) return;

        Integer oldCost = costs.get(state);
        if (oldCost == null) {
            costs.put(state, newCost);
            bestInitialDir.put(state, initialDir);
            queue.insert(state, newCost);
        } else if (newCost < oldCost) {
            costs.put(state, newCost);
            bestInitialDir.put(state, initialDir);
            queue.updatePriority(state, newCost);
        }
    }

    /**
     * Approximate hex direction from delta coordinates.
     * This is a simplified approximation for the smell sensor.
     */
    private HexDirection approximateDirection(int dx, int dy) {
        // Simplified direction calculation for doubled coordinates
        // Return the closest of the 6 directions
        if (dx > 0 && dy >= 0) return HexDirection.fromIndex(0); // East
        if (dx > 0 && dy < 0) return HexDirection.fromIndex(1);  // Southeast
        if (dx <= 0 && dy < 0) return HexDirection.fromIndex(2); // Southwest
        if (dx < 0 && dy <= 0) return HexDirection.fromIndex(3); // West
        if (dx < 0 && dy > 0) return HexDirection.fromIndex(4);  // Northwest
        return HexDirection.fromIndex(5); // Northeast
    }

    private int readMemory(int index, int[] memory) {
        if (index < 0 || index >= memory.length) {
            return 0;
        }
        return memory[index];
    }
}
