package simulation;

import ast.Mutator;
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
    private static final int MANA_DROP_ATTEMPTS = 100;
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
        this(world, turnOrder, new Random());
    }

    /**
     * Creates a controller with an explicit initial turn order and random source.
     *
     * @param world simulation world to mutate
     * @param turnOrder initial critter scheduling order
     * @param random randomness source
     */
    public Controller(World world, List<Critter> turnOrder, Random random) {
        this.world = Objects.requireNonNull(world, "world");
        this.turnOrder = new ArrayList<>(Objects.requireNonNull(turnOrder, "turnOrder"));
        this.random = Objects.requireNonNull(random, "random");
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
                world.killCritter(critter, Constants.FOOD_PER_SIZE * critter.getSize());
                iterator.remove();
                continue;
            }
            Action action = critter.getCritterInterpreter().interpret(critter, world);
            Critter newborn = applyAction(critter, action);
            if (newborn != null) {
                newborns.add(newborn);
            }
            if (critter.isDead()) {
                world.killCritter(critter, Constants.FOOD_PER_SIZE * critter.getSize());
                iterator.remove();
            }
        }
        maybeDropManna();
        turnOrder.addAll(newborns);
    }

    private Critter applyAction(Critter critter, Action action) {
        Objects.requireNonNull(action, "action");
        int size = critter.getSize();
        int complexity = critter.getComplexity();

        switch (action.getType()) {
            case WAIT -> critter.adjustEnergy(size * Constants.SOLAR_FLUX);
            case LEFT -> {
                if (!spendEnergy(critter, size)) {
                    return null;
                }
                critter.setDirection(critter.getDirection() + 5);
            }
            case RIGHT -> {
                if (!spendEnergy(critter, size)) {
                    return null;
                }
                critter.setDirection(critter.getDirection() + 1);
            }
            case FORWARD -> {
                if (!spendEnergy(critter, size * Constants.MOVE_COST)) {
                    return null;
                }
                world.moveCritter(critter, HexDirection.fromIndex(critter.getDirection()));
            }
            case BACKWARD -> {
                if (!spendEnergy(critter, size * Constants.MOVE_COST)) {
                    return null;
                }
                world.moveCritter(critter, HexDirection.fromIndex(critter.getDirection()).opposite());
            }
            case EAT -> {
                if (!spendEnergy(critter, size)) {
                    return null;
                }
                HexCoordinate ahead = neighborAhead(critter);
                int maxAbsorbable = Math.max(0, Constants.ENERGY_PER_SIZE * size - critter.getEnergy());
                int consumed = world.takeFood(ahead.x(), ahead.y(), maxAbsorbable);
                if (consumed > 0) {
                    critter.adjustEnergy(consumed);
                }
            }
            case SERVE -> {
                int baseCost = size;
                if (!spendEnergy(critter, baseCost)) {
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
                if (!spendEnergy(critter, size * Constants.ATTACK_COST)) {
                    return null;
                }
                HexCoordinate ahead = neighborAhead(critter);
                Critter target = world.getCritterAt(ahead.x(), ahead.y());
                if (target != null && target.isAlive()) {
                    double x = Constants.DAMAGE_INC * (size * critter.getOffense() - target.getSize() * target.getDefense());
                    double px = 1.0 / (1.0 + Math.exp(-x));
                    int damage = (int) Math.round(Constants.BASE_DAMAGE * size * px);
                    target.adjustEnergy(-damage);
                }
            }
            case GROW -> {
                if (!spendEnergy(critter, size * complexity * Constants.GROW_COST)) {
                    return null;
                }
                critter.grow();
            }
            case BUD -> {
                if (!spendEnergy(critter, Constants.BUD_COST * complexity)) {
                    return null;
                }
                HexCoordinate behind = neighborBehind(critter);
                if (!world.isValidCoordinate(behind.x(), behind.y())) {
                    return null;
                }
                // Check for rock or critter; food is acceptable and will be destroyed
                HexState targetHex = world.getHex(behind.x(), behind.y());
                if (targetHex.isRock() || targetHex.hasCritter()) {
                    return null;
                }
                // Destroy any food on the target hex (budding succeeds regardless)
                world.takeFood(behind.x(), behind.y(), Integer.MAX_VALUE);
                Critter offspring = critter.createOffspring(Constants.INITIAL_ENERGY);
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

    private void maybeDropManna() {
        int numHexes = world.getWidth() * world.getHeight() / 2;
        // Use a proportional expected value so small worlds can still receive occasional manna.
        double expected = (numHexes / 1000.0) * Constants.MANNA_COUNT;
        int drops = (int) Math.floor(expected);
        double frac = expected - drops;
        if (random.nextDouble() < frac) {
            drops++;
        }

        for (int i = 0; i < drops; i++) {
            for (int attempt = 0; attempt < MANA_DROP_ATTEMPTS; attempt++) {
                int x = random.nextInt(world.getWidth());
                int y = random.nextInt(world.getHeight());
                if (!world.isValidCoordinate(x, y)) {
                    continue;
                }
                if (!world.getHex(x, y).isEmpty()) {
                    continue;
                }
                if (world.addFood(x, y, Constants.MANNA_AMOUNT)) {
                    break;
                }
            }
        }
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
        double roll = random.nextDouble();
        // System.out.println("[MUTATE] Bud event! Mutation roll: " + roll + " vs threshold " + MUTATION_PROBABILITY);
        if (roll >= MUTATION_PROBABILITY) {
            // System.out.println("[MUTATE] No mutation triggered (roll failed)");
            return;
        }
        
        // System.out.println("[MUTATE] Mutation triggered! Beginning mutation loop...");
        int mutationCount = 0;
        do {
            mutationCount++;
            // System.out.println("[MUTATE] Mutation #" + mutationCount);
            
            if (random.nextBoolean()) {
                // System.out.println("[MUTATE] -> Attribute mutation");
                offspring.mutateAttribute(random);
            } else {
                // System.out.println("[MUTATE] -> AST mutation");
                if (offspring.getCritterInterpreter() instanceof ProgramCritterInterpreter) {
                    ProgramCritterInterpreter interp = (ProgramCritterInterpreter) offspring.getCritterInterpreter();
                    int beforeRules = interp.getProgram().getRules().size();
                    // System.out.println("[MUTATE] Before: " + beforeRules + " rules");
                    
                    try {
                        ast.Program mutated = new Mutator(random).mutate(interp.getProgram());
                        int afterRules = mutated != null ? mutated.getRules().size() : -1;
                        // System.out.println("[MUTATE] After: " + afterRules + " rules, Object changed? " + (mutated != interp.getProgram()));
                        
                        if (mutated != null && mutated != interp.getProgram()) {
                            offspring.updateInterpreter(new ProgramCritterInterpreter(mutated, random));
                            // System.out.println("[MUTATE] Offspring interpreter updated! (Rules: " + beforeRules + " -> " + afterRules + ")");
                        } else {
                            // System.out.println("[MUTATE] Mutation returned same object (no change)");
                        }
                    } catch (Exception e) {
                        // System.out.println("[MUTATE] CRITICAL: Mutator crashed!");
                        e.printStackTrace();
                    }
                }
            }
            
            double continueRoll = random.nextDouble();
            if (continueRoll >= MUTATION_PROBABILITY) {
                // System.out.println("[MUTATE] Mutation loop ending (continue roll: " + continueRoll + " >= " + MUTATION_PROBABILITY + ")");
                break;
            }
            // System.out.println("[MUTATE] Continuing loop (continue roll: " + continueRoll + " < " + MUTATION_PROBABILITY + ")");
        } while (true);
        // System.out.println("[MUTATE] Total mutations applied: " + mutationCount);
    }
}
