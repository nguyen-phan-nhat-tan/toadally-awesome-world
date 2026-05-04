package compiler.simulation;

/**
 * Runtime action types supported by critters.
 */
public enum ActionType {
    /** Do nothing for this turn. */
    WAIT,
    /** Move one hex forward. */
    FORWARD,
    /** Move one hex backward. */
    BACKWARD,
    /** Rotate left by one step. */
    LEFT,
    /** Rotate right by one step. */
    RIGHT,
    /** Consume food from the current location. */
    EAT,
    /** Attack an adjacent target. */
    ATTACK,
    /** Spend energy to increase size. */
    GROW,
    /** Attempt to reproduce by budding. */
    BUD,
    /** Serve energy/food amount as specified by argument. */
    SERVE
}