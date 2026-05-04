package compiler.simulation;

import java.util.Objects;

/**
 * Immutable runtime action produced by a critter interpreter.
 */
public final class Action {
    private final ActionType type;
    private final Integer argument;

    /**
     * Creates an action without an argument.
     *
     * @param type action kind to execute
     */
    public Action(ActionType type) {
        this(type, null);
    }

    /**
     * Creates an action, optionally with an integer argument.
     *
     * @param type action kind to execute
     * @param argument optional argument value (for actions such as serve)
     */
    public Action(ActionType type, Integer argument) {
        this.type = Objects.requireNonNull(type, "type");
        this.argument = argument;
    }

    /**
     * Returns the action kind.
     *
     * @return action type enum value
     */
    public ActionType getType() {
        return type;
    }

    /**
     * Returns the optional argument.
     *
     * @return argument value, or null when absent
     */
    public Integer getArgument() {
        return argument;
    }

    /**
     * Indicates whether this action carries an argument.
     *
     * @return true if an argument is present
     */
    public boolean hasArgument() {
        return argument != null;
    }
}