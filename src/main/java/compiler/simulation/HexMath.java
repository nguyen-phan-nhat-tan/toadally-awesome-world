package compiler.simulation;

/**
 * Hex-grid utility methods.
 */
public final class HexMath {
    private HexMath() {
    }

    /**
     * Checks whether a doubled-coordinate pair is structurally valid.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return true when coordinate parity and non-negative bounds are valid
     */
    public static boolean isValidCoordinate(int x, int y) {
        return x >= 0 && y >= 0 && ((x + y) % 2 == 0);
    }

    /**
     * Checks whether a coordinate lies inside world bounds.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param width world width
     * @param height world height
     * @return true when coordinate is within bounds
     */
    public static boolean isWithinBounds(int x, int y, int width, int height) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /**
     * Computes distance between two doubled-coordinate hex positions.
     *
     * @param x1 first x coordinate
     * @param y1 first y coordinate
     * @param x2 second x coordinate
     * @param y2 second y coordinate
     * @return non-negative hex distance
     */
    public static int distance(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        return Math.max(
            Math.abs(dx),
            Math.max(Math.abs(dx + dy) / 2, Math.abs(dx - dy) / 2)
        );
    }

    /**
     * Returns adjacent coordinate in the provided direction.
     *
     * @param x starting x coordinate
     * @param y starting y coordinate
     * @param direction movement direction
     * @return neighbor coordinate
     */
    public static HexCoordinate neighbor(int x, int y, HexDirection direction) {
        return new HexCoordinate(x + direction.dx(), y + direction.dy());
    }
}