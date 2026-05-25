package lexer;

/**
 * Named aliases for fixed critter memory slots.
 */
public enum SugarTokenType {
    /** Memory size slot. */
    MEMSIZE(0, false),
    /** Defense stat slot. */
    DEFENSE(1, false),
    /** Offense stat slot. */
    OFFENSE(2, false),
    /** Size stat slot. */
    SIZE(3, false),
    /** Energy stat slot. */
    ENERGY(4, false),
    /** Pass counter slot. */
    PASS(5, false),
    /** Posture slot (assignable). */
    POSTURE(6, true);

    private final int index;
    private final boolean assignable;

    SugarTokenType(int index, boolean assignable){
        this.index = index;
        this.assignable = assignable;
    }

    /**
     * Returns the backing memory index for this sugar name.
     *
     * @return non-negative memory index
     */
    public int getIndex(){
        return index;
    }

    /**
     * Indicates whether this slot is writable via assignment sugar.
     *
     * @return true when assignments to this slot are allowed
     */
    public boolean isAssignable(){
        return assignable;
    }

    /**
     * Resolves a sugar name to its memory index.
     *
     * @param name sugar identifier text
     * @return optional index for known names
     */
    public static Integer getIndexSugar(String name){
        try {
            return SugarTokenType.valueOf(name.toUpperCase()).getIndex();
        } catch (IllegalArgumentException exception){
            return null;
        }
    }

    /**
     * Checks whether a memory index is writable according to sugar rules.
     *
     * @param index memory index to test
     * @return true if assignments are allowed for that index
     */
    public static boolean isAssignableSugar(int index){
        for (SugarTokenType slot: values()){
            if (slot.getIndex() == index){
                return slot.isAssignable();
            }
        }
        return index >= values().length;
    }
}
