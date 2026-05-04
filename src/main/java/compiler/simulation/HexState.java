package compiler.simulation;

import java.util.Objects;

/**
 * Immutable state of a single world hex.
 */
public final class HexState {
    private static final HexState ROCK = new HexState(true, 0, null);
    private static final HexState EMPTY = new HexState(false, 0, null);

    private final boolean rock;
    private final int foodAmount;
    private final Critter critter;

    private HexState(boolean rock, int foodAmount, Critter critter) {
        this.rock = rock;
        this.foodAmount = foodAmount;
        this.critter = critter;
    }

    /**
     * Returns the canonical rock state.
     *
     * @return immutable rock hex state
     */
    public static HexState rock() {
        return ROCK;
    }

    /**
     * Returns the canonical empty state.
     *
     * @return immutable empty hex state
     */
    public static HexState empty() {
        return EMPTY;
    }

    /**
     * Creates a food-containing hex state.
     *
     * @param amount food amount (must be non-negative)
     * @return state containing the requested amount of food
     */
    public static HexState food(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Food amount cannot be negative");
        }
        if (amount == 0) {
            return empty();
        }
        return new HexState(false, amount, null);
    }

    /**
     * Creates a critter-containing hex state.
     *
     * @param critter critter occupant
     * @return state containing the given critter
     */
    public static HexState critter(Critter critter) {
        return new HexState(false, 0, Objects.requireNonNull(critter, "critter"));
    }

    /**
     * Indicates whether this hex is a rock.
     *
     * @return true when rock terrain
     */
    public boolean isRock() {
        return rock;
    }

    /**
     * Indicates whether this hex contains food.
     *
     * @return true when food amount is positive
     */
    public boolean hasFood() {
        return !rock && foodAmount > 0;
    }

    /**
     * Returns food amount.
     *
     * @return food amount, zero when none
     */
    public int getFoodAmount() {
        return foodAmount;
    }

    /**
     * Indicates whether this hex contains a critter.
     *
     * @return true when a critter is present
     */
    public boolean hasCritter() {
        return critter != null;
    }

    /**
     * Returns the critter occupant.
     *
     * @return critter instance, or null if none
     */
    public Critter getCritter() {
        return critter;
    }

    /**
     * Indicates whether this hex is empty (no rock, no food, no critter).
     *
     * @return true when empty
     */
    public boolean isEmpty() {
        return !rock && foodAmount == 0 && critter == null;
    }

    /**
     * Returns a state with updated food amount.
     *
     * @param amount food amount to set
     * @return updated hex state
     */
    public HexState withFood(int amount) {
        if (rock) {
            return this;
        }
        if (amount <= 0) {
            return empty();
        }
        if (critter != null) {
            throw new IllegalStateException("A hex cannot contain both food and a critter");
        }
        return food(amount);
    }

    /**
     * Returns a state with the given critter occupant.
     *
     * @param critter critter to place
     * @return updated hex state
     */
    public HexState withCritter(Critter critter) {
        if (rock) {
            return this;
        }
        if (foodAmount > 0) {
            throw new IllegalStateException("A hex cannot contain both food and a critter");
        }
        return critter(critter);
    }

    /**
     * Returns a state without any critter occupant.
     *
     * @return updated hex state
     */
    public HexState withoutCritter() {
        if (rock) {
            return this;
        }
        return foodAmount > 0 ? food(foodAmount) : empty();
    }
}