package compiler.lexer;

public enum Sugar {
    MEMSIZE(0, false),
    DEFENSE(1, false),
    OFFENSE(2, false),
    SIZE(3, false),
    ENERGY(4, false),
    PASS(5, false),
    POSTURE(6, true);

    private final int index;
    private final boolean assignable;

    Sugar(int index, boolean assignable) {
        this.index = index;
        this.assignable = assignable;
    }

    public int getIndex() {
        return index;
    }

    public boolean isAssignable() {
        return assignable;
    }

    /*

    */
    public static Integer getIndexSugar(String name) {
        try {
            return Sugar.valueOf(name.toUpperCase()).getIndex();
        } catch (Exception e) {
            throw new IllegalStateException("Sugar enum lookup failed unexpectedly for name: " + name, e);
        }
    }

    public static boolean isAssignableSugar(int index) {
        for (Sugar slot: values()) {
            if (slot.getIndex() == index) {
                return slot.assignable;
            }
        }
        return index >= values().length;
    }

    
}
