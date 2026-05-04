package compiler.simulation;

/**
 * Produces the next runtime action for a critter.
 */
@FunctionalInterface
public interface InterpreterVisitor {
    /**
     * Chooses the next action for a critter in the current world state.
     *
     * @param critter acting critter
     * @param world world snapshot/mutable context
     * @return action to execute this turn
     */
    Action interpret(Critter critter, World world);

    /**
     * Creates an interpreter copy suitable for an offspring critter.
     *
     * @return interpreter instance for offspring behavior
     */
    default InterpreterVisitor offspringCopy() {
        return this;
    }
}