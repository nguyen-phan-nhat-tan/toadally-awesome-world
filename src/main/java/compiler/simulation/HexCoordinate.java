package compiler.simulation;

/**
 * Immutable doubled-coordinate position in the hex grid.
 *
 * @param x horizontal coordinate
 * @param y vertical coordinate
 */
public record HexCoordinate(int x, int y) {
    /**
     * Returns a new coordinate one step in the provided direction.
     *
     * @param direction movement direction
     * @return neighbor coordinate in that direction
     */
    public HexCoordinate step(HexDirection direction) {
        return new HexCoordinate(x + direction.dx(), y + direction.dy());
    }
}