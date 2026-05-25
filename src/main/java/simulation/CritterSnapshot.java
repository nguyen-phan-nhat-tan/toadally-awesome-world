package simulation;

import java.util.Arrays;
import ast.PrettyPrinter;
import ast.ProgramCritterInterpreter;

/**
 * Lightweight immutable snapshot of a critter for rendering/inspection.
 */
public final class CritterSnapshot {
    public final int x;
    public final int y;
    public final int direction;
    public final int size;
    public final int energy;
    public final int offense;
    public final int defense;
    public final int posture;
    public final int[] memory; // copy
    public final String speciesKey;

    public final String programText;
    public final String lastRule;

    public CritterSnapshot(Critter c) {
        this.x = c.getX();
        this.y = c.getY();
        this.direction = c.getDirection();
        this.size = c.getSize();
        this.energy = c.getEnergy();
        this.offense = c.getOffense();
        this.defense = c.getDefense();
        this.posture = c.getPosture();
        int[] src = c.getMemory();
        this.memory = src == null ? new int[0] : Arrays.copyOf(src, src.length);
        this.speciesKey = c.getSpecies() == null ? "unknown" : c.getSpecies();
        // capture program text if available
        String pt = "";
        if (c.getCritterInterpreter() instanceof ProgramCritterInterpreter pci) {
            try {
                pt = new PrettyPrinter().format(pci.getProgram());
            } catch (Exception ex) {
                pt = "<program unavailable>";
            }
        }
        this.programText = pt;
        this.lastRule = c.getLastRule();
    }
}
