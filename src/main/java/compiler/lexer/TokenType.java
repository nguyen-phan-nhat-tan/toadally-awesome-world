package compiler.lexer;

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

    public String getliteral() {
        return literal;
    }
}
