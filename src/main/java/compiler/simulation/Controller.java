package compiler.simulation;

import compiler.ast.MutatorVisitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Sequential simulation driver.
 */
public class Controller {
    /** Energy spent each turn for base movement/rotation actions. */
    public static final int TURN_ENERGY_COST = 1;
    private static final int SOLAR_FLUX = 1;
    private static final int INITIAL_ENERGY = 250;
    private static final int CORPSE_FOOD_VALUE = 1;
    private static final double MUTATION_PROBABILITY = 0.25;

    private final World world;
    private final List<Critter> turnOrder;
    private final Random random;

    /**
     * Creates a controller with an explicit initial turn order.
     *
     * @param world simulation world to mutate
     * @param turnOrder initial critter scheduling order
     */
    public Controller(World world, List<Critter> turnOrder) {
        this.world = Objects.requireNonNull(world, "world");
        this.turnOrder = new ArrayList<>(Objects.requireNonNull(turnOrder, "turnOrder"));
        this.random = new Random();
    }

    /**
     * Creates a controller with an empty turn order.
     *
     * @param world simulation world to mutate
     */
    public Controller(World world) {
        this(world, List.of());
    }

    /**
     * Returns the controlled world.
     *
     * @return world instance
     */
    public World getWorld() {
        return world;
    }

    /**
     * Returns an immutable view of the current turn order.
     *
     * @return ordered critter list
     */
    public List<Critter> getTurnOrder() {
        return Collections.unmodifiableList(turnOrder);
    }

    /**
     * Adds a critter to the world and schedules it for future turns.
     *
     * @param critter critter to add
     * @param x target x coordinate
     * @param y target y coordinate
     */
    public void addCritter(Critter critter, int x, int y) {
        Objects.requireNonNull(critter, "critter");
        world.placeCritter(critter, x, y);
        turnOrder.add(critter);
    }

    /**
     * Advances the simulation by one controller step.
     *
     * Each living critter gets one interpretation/action cycle. Dead critters are
     * converted to food and removed from scheduling.
     */
    public void step() {
        List<Critter> newborns = new ArrayList<>();
        for (java.util.Iterator<Critter> iterator = turnOrder.iterator(); iterator.hasNext();) {
            Critter critter = iterator.next();
            if (critter.isDead()) {
                world.killCritter(critter, CORPSE_FOOD_VALUE);
                iterator.remove();
                continue;
            }
            Action action = critter.getInterpreterVisitor().interpret(critter, world);
            Critter newborn = applyAction(critter, action);
            if (newborn != null) {
                newborns.add(newborn);
            }
            if (critter.isDead()) {
                world.killCritter(critter, CORPSE_FOOD_VALUE);
                iterator.remove();
            }
        }
        turnOrder.addAll(newborns);
    }

    private Critter applyAction(Critter critter, Action action) {
        Objects.requireNonNull(action, "action");
        switch (action.getType()) {
            case WAIT -> critter.adjustEnergy(critter.getSize() * SOLAR_FLUX);
            case LEFT -> {
                if (!spendEnergy(critter, TURN_ENERGY_COST)) {
                    return null;
                }
                critter.setDirection(critter.getDirection() + 5);
            }
            case RIGHT -> {
                if (!spendEnergy(critter, TURN_ENERGY_COST)) {
                    return null;
                }
                critter.setDirection(critter.getDirection() + 1);
            }
            case FORWARD -> {
                if (!spendEnergy(critter, TURN_ENERGY_COST)) {
                    return null;
                }
                world.moveCritter(critter, HexDirection.fromIndex(critter.getDirection()));
            }
            case BACKWARD -> {
                if (!spendEnergy(critter, TURN_ENERGY_COST)) {
                    return null;
                }
                world.moveCritter(critter, HexDirection.fromIndex(critter.getDirection()).opposite());
            }
            case EAT -> {
                if (!spendEnergy(critter, TURN_ENERGY_COST)) {
                    return null;
                }
                HexCoordinate ahead = neighborAhead(critter);
                int consumed = world.takeFood(ahead.x(), ahead.y(), Integer.MAX_VALUE);
                if (consumed > 0) {
                    critter.adjustEnergy(consumed);
                }
            }
            case SERVE -> {
                if (!spendEnergy(critter, TURN_ENERGY_COST)) {
                    return null;
                }
                int requested = action.hasArgument() ? Math.max(0, action.getArgument()) : 0;
                int served = Math.min(requested, critter.getEnergy());
                if (served <= 0) {
                    return null;
                }
                critter.adjustEnergy(-served);
                HexCoordinate ahead = neighborAhead(critter);
                world.addFood(ahead.x(), ahead.y(), served);
            }
            case ATTACK -> {
                if (!spendEnergy(critter, TURN_ENERGY_COST)) {
                    return null;
                }
                HexCoordinate ahead = neighborAhead(critter);
                Critter target = world.getCritterAt(ahead.x(), ahead.y());
                if (target != null && target.isAlive()) {
                    world.killCritter(target, CORPSE_FOOD_VALUE);
                }
            }
            case GROW -> {
                if (!spendEnergy(critter, TURN_ENERGY_COST)) {
                    return null;
                }
                critter.grow();
            }
            case BUD -> {
                if (!spendEnergy(critter, TURN_ENERGY_COST)) {
                    return null;
                }
                HexCoordinate behind = neighborBehind(critter);
                if (!world.isValidCoordinate(behind.x(), behind.y())) {
                    return null;
                }
                if (!world.getHex(behind.x(), behind.y()).isEmpty()) {
                    return null;
                }
                Critter offspring = critter.createOffspring(INITIAL_ENERGY);
                applyMutations(offspring);
                world.placeCritter(offspring, behind.x(), behind.y());
                return offspring;
            }
        }
        return null;
    }

    private boolean spendEnergy(Critter critter, int amount) {
        critter.adjustEnergy(-amount);
        return critter.isAlive();
    }

    private HexCoordinate neighborAhead(Critter critter) {
        return world.neighbor(critter.getX(), critter.getY(), HexDirection.fromIndex(critter.getDirection()));
    }

    private HexCoordinate neighborBehind(Critter critter) {
        return world.neighbor(
            critter.getX(),
            critter.getY(),
            HexDirection.fromIndex(critter.getDirection()).opposite()
        );
    }

    /**
     * Applies mutations to an offspring critter.
     * With probability MUTATION_PROBABILITY, triggers at least one mutation.
     * If a mutation occurs, repeats with MUTATION_PROBABILITY for each additional.
     * Chooses between attribute mutation (50%) and AST mutation (50%).
     *
     * @param offspring critter to mutate
     */
    private void applyMutations(Critter offspring) {
        if (random.nextDouble() >= MUTATION_PROBABILITY) {
            return;
        }
        
        do {
            if (random.nextBoolean()) {
                offspring.mutateAttribute(random);
            } else {
                if (offspring.getInterpreterVisitor() instanceof ProgramCritterInterpreter) {
                    ProgramCritterInterpreter interp = (ProgramCritterInterpreter) offspring.getInterpreterVisitor();
                    compiler.ast.Program mutated = new MutatorVisitor(random).mutate(interp.getProgram());
                    if (mutated != null && mutated != interp.getProgram()) {
                        offspring.updateInterpreter(new ProgramCritterInterpreter(mutated, random));
                    }
                }
            }
            
            if (random.nextDouble() >= MUTATION_PROBABILITY) {
                break;
            }
        } while (true);
    }
}