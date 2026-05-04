package compiler.simulation;

/**
 * The six doubled-coordinate directions used by the hex grid.
 */
public enum HexDirection {
    /** Direction index 0 with delta (0, +2). */
    NORTH(0, 0, 2),
    /** Direction index 1 with delta (+1, +1). */
    NORTHEAST(1, 1, 1),
    /** Direction index 2 with delta (+1, -1). */
    SOUTHEAST(2, 1, -1),
    /** Direction index 3 with delta (0, -2). */
    SOUTH(3, 0, -2),
    /** Direction index 4 with delta (-1, -1). */
    SOUTHWEST(4, -1, -1),
    /** Direction index 5 with delta (-1, +1). */
    NORTHWEST(5, -1, 1);

    private final int index;
    private final int dx;
    private final int dy;

    HexDirection(int index, int dx, int dy) {
        this.index = index;
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * Returns the canonical direction index.
     *
     * @return direction index in range 0..5
     */
    public int index() {
        return index;
    }

    /**
     * Returns x delta for one step in this direction.
     *
     * @return x-axis movement delta
     */
    public int dx() {
        return dx;
    }

    /**
     * Returns y delta for one step in this direction.
     *
     * @return y-axis movement delta
     */
    public int dy() {
        return dy;
    }

    /**
     * Rotates this direction left by one step.
     *
     * @return rotated direction
     */
    public HexDirection rotateLeft() {
        return fromIndex((index + 5) % 6);
    }

    /**
     * Rotates this direction right by one step.
     *
     * @return rotated direction
     */
    public HexDirection rotateRight() {
        return fromIndex((index + 1) % 6);
    }

    /**
     * Returns the opposite direction.
     *
     * @return direction 180 degrees from this one
     */
    public HexDirection opposite() {
        return fromIndex((index + 3) % 6);
    }

    /**
     * Converts an arbitrary integer index to a normalized direction.
     *
     * @param index direction index (any integer)
     * @return normalized enum constant
     */
    public static HexDirection fromIndex(int index) {
        int normalized = Math.floorMod(index, 6);
        return switch (normalized) {
            case 0 -> NORTH;
            case 1 -> NORTHEAST;
            case 2 -> SOUTHEAST;
            case 3 -> SOUTH;
            case 4 -> SOUTHWEST;
            case 5 -> NORTHWEST;
            default -> throw new IllegalStateException("Unexpected direction index: " + index);
        };
    }
}