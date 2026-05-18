package simulation;

/**
 * Immutable snapshot of a single hex cell.
 */
public final class HexSnapshot {
    public final boolean rock;
    public final int foodAmount;
    public final CritterSnapshot critter; // null when none

    public HexSnapshot(boolean rock, int foodAmount, CritterSnapshot critter) {
        this.rock = rock;
        this.foodAmount = foodAmount;
        this.critter = critter;
    }

    public static HexSnapshot fromHexState(HexState s) {
        if (s == null) return new HexSnapshot(true, 0, null);
        CritterSnapshot cs = s.hasCritter() ? new CritterSnapshot(s.getCritter()) : null;
        return new HexSnapshot(s.isRock(), s.getFoodAmount(), cs);
    }
}
