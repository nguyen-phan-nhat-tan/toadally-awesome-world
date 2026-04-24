package compiler.lexer;

/**
 * Shared grammar vocabulary between lexer and parser, preventing fragile string-based
 * coupling across compiler stages.
 */
public enum TokenType {
    // Punctuation
    ARROW("-->"),
    SEMICOLON(";"),
    ASSIGN(":="),
    LBRACE("{"),
    RBRACE("}"),
    LBRACKET("["),
    RBRACKET("]"),
    LPAREN("("),
    RPAREN(")"),

    // Keywords (actions)
    WAIT("wait"),
    FORWARD("forward"),
    BACKWARD("backward"),
    LEFT("left"),
    RIGHT("right"),
    EAT("eat"),
    ATTACK("attack"),
    GROW("grow"),
    BUD("bud"),
    SERVE("serve"),

    // Keywords (others)
    MEM("mem"),
    NEARBY("nearby"),
    AHEAD("ahead"),
    RANDOM("random"),
    SMELL("smell"),

    // Logical operators
    AND("and"),
    OR("or"),
    ADDOP, MULOP, REL,

    // Literals
    NUMBER, 

    EOF;

    private final String literal;

    TokenType() {
        this.literal = null;
    }

    TokenType(String literal) {
        this.literal = literal;
    }

    /**
     * Exposes canonical text for operators/keywords when pretty-printing or reporting
     * errors; null is intentional for token kinds that are category-only.
     *
     * @return literal representation, or null for synthetic token categories
     */
    public String getliteral() {
        return literal;
    }
}
