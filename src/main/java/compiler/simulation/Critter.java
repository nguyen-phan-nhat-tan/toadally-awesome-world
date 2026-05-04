package compiler.simulation;

import compiler.ast.Program;
import java.util.Objects;

/**
 * Mutable critter state tracked by the world and controller.
 * This class owns the critter's memory array, which scales with memorySize.
 */
public class Critter {
    private static final int DEFAULT_SIZE = 1;
    private static final int MIN_MEMORY_SIZE = 7;
    private static final int MIN_OFFENSE = 1;
    private static final int MIN_DEFENSE = 1;
    private static final int MIN_POSTURE = 0;
    private static final int MAX_POSTURE = 99;

    private InterpreterVisitor interpreterVisitor;
    private volatile int x;
    private volatile int y;
    private volatile int direction;
    private volatile int energy;
    private volatile int size;
    private volatile int memorySize;
    private volatile int offense;
    private volatile int defense;
    private volatile int posture;
    private volatile int[] memory;
    private volatile boolean alive = true;

    /**
     * Creates a critter with explicit position and interpreter.
     *
     * @param x starting x coordinate
     * @param y starting y coordinate
     * @param direction initial facing direction index
     * @param energy initial energy amount
     * @param interpreterVisitor behavior provider
     */
    public Critter(int x, int y, int direction, int energy, InterpreterVisitor interpreterVisitor) {
        this(x, y, direction, energy, DEFAULT_SIZE, interpreterVisitor);
    }

    /**
     * Creates a critter with explicit position, size, and interpreter.
     *
     * @param x starting x coordinate
     * @param y starting y coordinate
     * @param direction initial facing direction index
     * @param energy initial energy amount
     * @param size initial critter size (minimum 1)
     * @param interpreterVisitor behavior provider
     */
    public Critter(int x, int y, int direction, int energy, int size, InterpreterVisitor interpreterVisitor) {
        this.x = x;
        this.y = y;
        this.direction = Math.floorMod(direction, 6);
        this.energy = energy;
        this.size = Math.max(1, size);
        this.memorySize = MIN_MEMORY_SIZE;
        this.offense = MIN_OFFENSE;
        this.defense = MIN_DEFENSE;
        this.posture = MIN_POSTURE;
        this.interpreterVisitor = Objects.requireNonNull(interpreterVisitor, "interpreterVisitor");
        
        // Initialize memory array with at least MIN_MEMORY_SIZE slots
        this.memory = new int[Math.max(MIN_MEMORY_SIZE, this.memorySize)];
        syncMemoryArray();
    }

    /**
     * Creates a critter at the origin with a provided interpreter.
     *
     * @param energy initial energy amount
     * @param direction initial facing direction index
     * @param interpreterVisitor behavior provider
     */
    public Critter(int energy, int direction, InterpreterVisitor interpreterVisitor) {
        this(0, 0, direction, energy, interpreterVisitor);
    }

    /**
     * Creates a critter with explicit position and a program-backed interpreter.
     *
     * @param x starting x coordinate
     * @param y starting y coordinate
     * @param direction initial facing direction index
     * @param energy initial energy amount
     * @param program AST program used for behavior
     */
    public Critter(int x, int y, int direction, int energy, Program program) {
        this(x, y, direction, energy, new ProgramCritterInterpreter(program));
    }

    /**
     * Creates a critter at the origin with a program-backed interpreter.
     *
     * @param energy initial energy amount
     * @param direction initial facing direction index
     * @param program AST program used for behavior
     */
    public Critter(int energy, int direction, Program program) {
        this(0, 0, direction, energy, program);
    }

    /**
     * Returns the behavior interpreter.
     *
     * @return interpreter bound to this critter
     */
    public InterpreterVisitor getInterpreterVisitor() {
        return interpreterVisitor;
    }

    /**
     * Updates the interpreter for this critter (used by mutation system).
     *
     * @param newInterpreter new interpreter to use
     */
    public void updateInterpreter(InterpreterVisitor newInterpreter) {
        this.interpreterVisitor = Objects.requireNonNull(newInterpreter, "newInterpreter");
    }

    /**
     * Returns the current x coordinate.
     *
     * @return x position
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the current y coordinate.
     *
     * @return y position
     */
    public int getY() {
        return y;
    }

    /**
     * Returns the current direction index in range 0..5.
     *
     * @return facing direction index
     */
    public int getDirection() {
        return direction;
    }

    /**
     * Returns the current energy value.
     *
     * @return critter energy
     */
    public int getEnergy() {
        return energy;
    }

    /**
     * Returns the current critter size.
     *
     * @return critter size (minimum 1)
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the memory size (number of memory slots).
     *
     * @return memory size (minimum 7)
     */
    public int getMemorySize() {
        return memorySize;
    }

    /**
     * Sets memory size with minimum 7 enforcement.
     *
     * @param newSize memory size to set
     */
    public void setMemorySize(int newSize) {
        memorySize = Math.max(MIN_MEMORY_SIZE, newSize);
    }

    /**
     * Returns offensive ability.
     *
     * @return offense value (minimum 1)
     */
    public int getOffense() {
        return offense;
    }

    /**
     * Sets offense with minimum 1 enforcement.
     *
     * @param newOffense offense value to set
     */
    public void setOffense(int newOffense) {
        offense = Math.max(MIN_OFFENSE, newOffense);
    }

    /**
     * Returns defensive ability.
     *
     * @return defense value (minimum 1)
     */
    public int getDefense() {
        return defense;
    }

    /**
     * Sets defense with minimum 1 enforcement.
     *
     * @param newDefense defense value to set
     */
    public void setDefense(int newDefense) {
        defense = Math.max(MIN_DEFENSE, newDefense);
    }

    /**
     * Returns the posture value.
     *
     * @return posture value (0-99)
     */
    public int getPosture() {
        return posture;
    }

    /**
     * Sets posture with bounds enforcement (0-99).
     *
     * @param newPosture posture value to set
     */
    public void setPosture(int newPosture) {
        posture = Math.max(MIN_POSTURE, Math.min(MAX_POSTURE, newPosture));
    }

    /**
     * Returns the critter's memory array.
     *
     * @return memory array owned by this critter
     */
    public int[] getMemory() {
        return memory;
    }

    /**
     * Returns a value from the critter's memory array.
     *
     * @param index memory index
     * @return stored value, or 0 for out-of-bounds indices
     */
    public int readMemory(int index) {
        if (index < 0 || index >= memory.length) {
            return 0;
        }
        return memory[index];
    }

    /**
     * Writes a value to the critter's memory array.
     * Special handling: memory[6] (posture) is bounded to 0-99.
     *
     * @param index memory index
     * @param value value to write
     */
    public void writeMemory(int index, int value) {
        if (index < 0 || index >= memory.length) {
            return;
        }
        
        // Special case: posture (memory[6]) is bounded to 0-99
        if (index == 6) {
            memory[6] = Math.max(MIN_POSTURE, Math.min(MAX_POSTURE, value));
            posture = memory[6];
        } else {
            memory[index] = value;
        }
    }

    /**
     * Syncs the memory array with derived critter state.
     * Called at the start of each turn and after state changes.
     *
     * memory[0] = array length
     * memory[1] = defense
     * memory[2] = offense
     * memory[3] = size
     * memory[4] = energy
     * memory[5] = pass (updated by interpreter)
     * memory[6] = posture (mutable, bounded 0-99)
     */
    public void syncMemoryArray() {
        memory[0] = memory.length;
        memory[1] = defense;
        memory[2] = offense;
        memory[3] = size;
        memory[4] = energy;
        // memory[5] is updated per-pass by the interpreter
        memory[6] = posture;
    }

    /**
     * Calculates the critter's appearance encoding as seen by an observer.
     * 
     * Formula: size * 1000 + posture * 10 + relative_direction
     * Where relative_direction = (this.direction - observer.direction + 6) % 6
     *
     * @param observerDirection the direction the observing critter is facing
     * @return appearance encoding integer
     */
    public int calculateAppearance(int observerDirection) {
        int relativeDirection = Math.floorMod(direction - observerDirection, 6);
        return size * 1000 + posture * 10 + relativeDirection;
    }

    /**
     * Computes critter complexity using the formula:
     * complexity = r * RULE_COST + (offense + defense) * ABILITY_COST
     *
     * @return computed complexity value
     */
    public int getComplexity() {
        ProgramCritterInterpreter interpreter = getInterpreterIfAvailable();
        if (interpreter == null) {
            return 0;
        }
        
        int ruleCount = interpreter.getRuleCount();
        int abilitySum = offense + defense;
        
        return ruleCount * Constants.RULE_COST + abilitySum * Constants.ABILITY_COST;
    }

    /**
     * Helper to safely cast the interpreter if it's a ProgramCritterInterpreter.
     *
     * @return the interpreter as ProgramCritterInterpreter, or null if not applicable
     */
    private ProgramCritterInterpreter getInterpreterIfAvailable() {
        if (interpreterVisitor instanceof ProgramCritterInterpreter) {
            return (ProgramCritterInterpreter) interpreterVisitor;
        }
        return null;
    }

    /**
     * Indicates whether the critter is alive.
     *
     * @return true when alive
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * Indicates whether the critter is dead.
     *
     * @return true when dead
     */
    public boolean isDead() {
        return !alive;
    }

    /**
     * Adds the given delta to energy and updates alive status.
     *
     * @param delta signed energy delta
     */
    public void adjustEnergy(int delta) {
        energy += delta;
        if (energy <= 0) {
            alive = false;
            energy = 0;
        }
        syncMemoryArray();
    }

    /**
     * Sets the direction using modular normalization into range 0..5.
     *
     * @param direction direction index to assign
     */
    public void setDirection(int direction) {
        this.direction = Math.floorMod(direction, 6);
    }

    /**
     * Increases critter size by one.
     */
    public void grow() {
        size++;
        syncMemoryArray();
    }

    /**
     * Creates a newborn critter inheriting behavior from this critter.
     *
     * @param initialEnergy energy assigned to the newborn
     * @return a new critter instance configured for offspring behavior
     */
    public Critter createOffspring(int initialEnergy) {
        Critter offspring = new Critter(0, 0, direction, initialEnergy, DEFAULT_SIZE, interpreterVisitor.offspringCopy());
        offspring.setMemorySize(MIN_MEMORY_SIZE);
        offspring.setOffense(MIN_OFFENSE);
        offspring.setDefense(MIN_DEFENSE);
        offspring.setPosture(MIN_POSTURE);
        offspring.syncMemoryArray();
        return offspring;
    }

    /**
     * Applies a random attribute mutation to this critter.
     * Chooses one of (memorySize, offense, defense) uniformly,
     * then increments or decrements it uniformly with boundaries enforced.
     *
     * @param random random source
     */
    public void mutateAttribute(java.util.Random random) {
        int attributeChoice = random.nextInt(3);
        boolean increment = random.nextBoolean();
        
        if (attributeChoice == 0) {
            int delta = increment ? 1 : -1;
            setMemorySize(memorySize + delta);
        } else if (attributeChoice == 1) {
            int delta = increment ? 1 : -1;
            setOffense(offense + delta);
        } else {
            int delta = increment ? 1 : -1;
            setDefense(defense + delta);
        }
    }

    void attachToWorld(int x, int y) {
        this.x = x;
        this.y = y;
        this.alive = true;
    }

    /**
     * Moves critter to a new location.
     *
     * @param x target x coordinate
     * @param y target y coordinate
     */
    public void moveTo(int x, int y) {
        this.x = x;
        this.y = y;
    }

    void kill() {
        alive = false;
        energy = 0;
    }
}