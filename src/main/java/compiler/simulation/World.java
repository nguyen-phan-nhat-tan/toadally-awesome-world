package compiler.simulation;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe hex world snapshot.
 */
public class World {
    private final int width;
    private final int height;
    private final AtomicReference<HexState[]> cells;

    /**
     * Creates a world with the given rectangular bounds.
     *
     * @param width world width in cells
     * @param height world height in cells
     */
    public World(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("World dimensions must be positive");
        }
        this.width = width;
        this.height = height;
        this.cells = new AtomicReference<>(createInitialCells(width, height));
    }

    private static HexState[] createInitialCells(int width, int height) {
        HexState[] initial = new HexState[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                initial[indexOf(x, y, width)] = HexMath.isValidCoordinate(x, y)
                    ? HexState.empty()
                    : HexState.rock();
            }
        }
        return initial;
    }

    private static int indexOf(int x, int y, int width) {
        return y * width + x;
    }

    /**
     * Returns world width.
     *
     * @return number of columns
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns world height.
     *
     * @return number of rows
     */
    public int getHeight() {
        return height;
    }

    /**
     * Checks whether a coordinate lies inside rectangular world bounds.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return true when inside bounds
     */
    public boolean isInBounds(int x, int y) {
        return HexMath.isWithinBounds(x, y, width, height);
    }

    /**
     * Checks whether a coordinate is in bounds and valid for doubled-hex parity.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return true when coordinate is usable as a hex cell
     */
    public boolean isValidCoordinate(int x, int y) {
        return isInBounds(x, y) && HexMath.isValidCoordinate(x, y);
    }

    /**
     * Returns state at the given coordinate.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return cell state, or rock for out-of-bounds locations
     */
    public HexState getHex(int x, int y) {
        if (!isInBounds(x, y)) {
            return HexState.rock();
        }
        return cells.get()[indexOf(x, y, width)];
    }

    /**
     * Returns neighboring coordinate in the given direction.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param direction movement direction
     * @return neighbor coordinate
     */
    public HexCoordinate neighbor(int x, int y, HexDirection direction) {
        return HexMath.neighbor(x, y, direction);
    }

    /**
     * Computes hex distance between two coordinates.
     *
     * @param x1 first x coordinate
     * @param y1 first y coordinate
     * @param x2 second x coordinate
     * @param y2 second y coordinate
     * @return non-negative distance
     */
    public int distance(int x1, int y1, int x2, int y2) {
        return HexMath.distance(x1, y1, x2, y2);
    }

    /**
     * Places a critter on an empty valid hex.
     *
     * @param critter critter to place
     * @param x target x coordinate
     * @param y target y coordinate
     */
    public synchronized void placeCritter(Critter critter, int x, int y) {
        Objects.requireNonNull(critter, "critter");
        requireValidOpenHex(x, y);
        HexState[] snapshot = cells.get();
        int index = indexOf(x, y, width);
        if (!snapshot[index].isEmpty()) {
            throw new IllegalStateException("Target hex is not empty");
        }
        HexState[] updated = snapshot.clone();
        updated[index] = HexState.critter(critter);
        cells.set(updated);
        critter.attachToWorld(x, y);
    }

    /**
     * Attempts to move a critter one step in the given direction.
     *
     * @param critter critter to move
     * @param direction movement direction
     * @return true if movement succeeded
     */
    public synchronized boolean moveCritter(Critter critter, HexDirection direction) {
        Objects.requireNonNull(critter, "critter");
        Objects.requireNonNull(direction, "direction");
        if (!critter.isAlive()) {
            return false;
        }
        if (!isValidCoordinate(critter.getX(), critter.getY())) {
            return false;
        }
        HexCoordinate target = neighbor(critter.getX(), critter.getY(), direction);
        if (!isValidCoordinate(target.x(), target.y())) {
            return false;
        }
        HexState[] snapshot = cells.get();
        int fromIndex = indexOf(critter.getX(), critter.getY(), width);
        int toIndex = indexOf(target.x(), target.y(), width);
        if (!snapshot[fromIndex].hasCritter() || snapshot[fromIndex].getCritter() != critter) {
            return false;
        }
        if (!snapshot[toIndex].isEmpty()) {
            return false;
        }
        HexState[] updated = snapshot.clone();
        updated[fromIndex] = HexState.empty();
        updated[toIndex] = HexState.critter(critter);
        cells.set(updated);
        critter.moveTo(target.x(), target.y());
        return true;
    }

    /**
     * Places food on an empty valid hex.
     *
     * @param x target x coordinate
     * @param y target y coordinate
     * @param amount food amount to place
     */
    public synchronized void placeFood(int x, int y, int amount) {
        requireValidOpenHex(x, y);
        HexState[] snapshot = cells.get();
        int index = indexOf(x, y, width);
        if (!snapshot[index].isEmpty()) {
            throw new IllegalStateException("Target hex is not empty");
        }
        HexState[] updated = snapshot.clone();
        updated[index] = HexState.food(amount);
        cells.set(updated);
    }

    /**
     * Adds food to a valid non-rock, non-critter hex.
     *
     * @param x target x coordinate
     * @param y target y coordinate
     * @param amount food amount to add
     * @return true when food was added
     */
    public synchronized boolean addFood(int x, int y, int amount) {
        if (amount <= 0 || !isValidCoordinate(x, y)) {
            return false;
        }
        HexState[] snapshot = cells.get();
        int index = indexOf(x, y, width);
        HexState current = snapshot[index];
        if (current.isRock() || current.hasCritter()) {
            return false;
        }
        int updatedFood = current.getFoodAmount() + amount;
        HexState[] updated = snapshot.clone();
        updated[index] = HexState.food(updatedFood);
        cells.set(updated);
        return true;
    }

    /**
     * Removes and returns up to {@code amount} food from a hex.
     *
     * @param x source x coordinate
     * @param y source y coordinate
     * @param amount requested food amount
     * @return actual food removed
     */
    public synchronized int takeFood(int x, int y, int amount) {
        if (amount <= 0 || !isValidCoordinate(x, y)) {
            return 0;
        }
        HexState[] snapshot = cells.get();
        int index = indexOf(x, y, width);
        HexState current = snapshot[index];
        if (!current.hasFood()) {
            return 0;
        }
        int removed = Math.min(amount, current.getFoodAmount());
        int remainder = current.getFoodAmount() - removed;
        HexState[] updated = snapshot.clone();
        updated[index] = remainder > 0 ? HexState.food(remainder) : HexState.empty();
        cells.set(updated);
        return removed;
    }

    /**
     * Returns the critter at the given coordinate, if any.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return critter at location, or null
     */
    public Critter getCritterAt(int x, int y) {
        HexState hex = getHex(x, y);
        return hex.hasCritter() ? hex.getCritter() : null;
    }

    /**
     * Removes a critter and replaces its cell with food.
     *
     * @param critter critter to remove
     * @param foodAmount food value left behind
     */
    public synchronized void killCritter(Critter critter, int foodAmount) {
        Objects.requireNonNull(critter, "critter");
        if (!isValidCoordinate(critter.getX(), critter.getY())) {
            critter.kill();
            return;
        }
        HexState[] snapshot = cells.get();
        int index = indexOf(critter.getX(), critter.getY(), width);
        if (!snapshot[index].hasCritter() || snapshot[index].getCritter() != critter) {
            critter.kill();
            return;
        }
        HexState[] updated = snapshot.clone();
        updated[index] = HexState.food(Math.max(1, foodAmount));
        cells.set(updated);
        critter.kill();
    }

    private void requireValidOpenHex(int x, int y) {
        if (!isValidCoordinate(x, y)) {
            throw new IllegalArgumentException("Invalid or out-of-bounds hex: (" + x + ", " + y + ")");
        }
    }

    /**
     * Places a rock on an empty valid hex.
     *
     * @param x target x coordinate
     * @param y target y coordinate
     */
    public synchronized void placeRock(int x, int y) {
        requireValidOpenHex(x, y);
        HexState[] snapshot = cells.get();
        int index = indexOf(x, y, width);
        if (!snapshot[index].isEmpty()) {
            throw new IllegalStateException("Target hex is not empty");
        }
        HexState[] updated = snapshot.clone();
        updated[index] = HexState.rock();
        cells.set(updated);
    }
}