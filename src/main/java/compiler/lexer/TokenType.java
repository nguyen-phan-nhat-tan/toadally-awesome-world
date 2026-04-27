package compiler.lexer;

/**
 * Enumeration of all token types in the language grammar.
 * 
 * TokenType serves as the shared vocabulary between the lexer (which produces tokens)
 * and the parser (which consumes them). By using an enum instead of string-based type checks,
 * we avoid fragile coupling and enable compiler optimizations.
 * 
 * Token types include:
 * <ul>
 *   <li><b>Punctuation:</b> brackets, braces, parentheses, semicolons, arrows, assignment</li>
 *   <li><b>Action Keywords:</b> wait, forward, backward, left, right, eat, attack, grow, bud, serve</li>
 *   <li><b>Sensor Keywords:</b> mem (memory), nearby, ahead, random, smell</li>
 *   <li><b>Logical Operators:</b> and, or</li>
 *   <li><b>Arithmetic Operators:</b> +, -, *, / (addop, mulop)</li>
 *   <li><b>Relational Operators:</b> <, >, <=, >=, =, != (rel)</li>
 *   <li><b>Literals:</b> numeric values</li>
 *   <li><b>Special:</b> EOF (end of file)</li>
 * </ul>
 * 
 * @see Lexer
 * @see Token
 */
public enum TokenType {
    // ========== Punctuation & Delimiters ==========
    
    /** Punctuation: --> (used in rule syntax). */
    ARROW("-->"),
    
    /** Punctuation: ; (statement terminator). */
    SEMICOLON(";"),
    
    /** Punctuation: := (assignment operator). */
    ASSIGN(":="),
    
    /** Punctuation: { (left brace for command blocks). */
    LBRACE("{"),
    
    /** Punctuation: } (right brace for command blocks). */
    RBRACE("}"),
    
    /** Punctuation: [ (left bracket for arrays/lists). */
    LBRACKET("["),
    
    /** Punctuation: ] (right bracket for arrays/lists). */
    RBRACKET("]"),
    
    /** Punctuation: ( (left parenthesis for grouping/function calls). */
    LPAREN("("),
    
    /** Punctuation: ) (right parenthesis for grouping/function calls). */
    RPAREN(")"),

    // ========== Action Keywords ==========
    // These represent critter behaviors that can be executed as commands
    
    /** Keyword: wait (pause execution). */
    WAIT("wait"),
    
    /** Keyword: forward (move in current direction). */
    FORWARD("forward"),
    
    /** Keyword: backward (move opposite to current direction). */
    BACKWARD("backward"),
    
    /** Keyword: left (turn left). */
    LEFT("left"),
    
    /** Keyword: right (turn right). */
    RIGHT("right"),
    
    /** Keyword: eat (consume food). */
    EAT("eat"),
    
    /** Keyword: attack (attack adjacent enemy). */
    ATTACK("attack"),
    
    /** Keyword: grow (increase size). */
    GROW("grow"),
    
    /** Keyword: bud (reproduce). */
    BUD("bud"),
    
    /** Keyword: serve (serve food to allies). */
    SERVE("serve"),

    // ========== Sensor/Memory Keywords ==========
    // These represent queries about the critter's state or environment
    
    /** Keyword: mem (access memory location). */
    MEM("mem"),
    
    /** Keyword: nearby (query nearby cells). */
    NEARBY("nearby"),
    
    /** Keyword: ahead (query the cell ahead). */
    AHEAD("ahead"),
    
    /** Keyword: random (generate random value). */
    RANDOM("random"),
    
    /** Keyword: smell (detect via smell). */
    SMELL("smell"),

    // ========== Logical Operators ==========
    
    /** Keyword: and (logical conjunction). */
    AND("and"),
    
    /** Keyword: or (logical disjunction). */
    OR("or"),
    
    // ========== Operator Categories ==========
    // These are synthetic categories (+ or -, * or /)
    
    /** Category: additive operators (+ or -). */
    ADDOP,
    
    /** Category: multiplicative operators (* or /). */
    MULOP,
    
    /** Category: relational operators (<, >, <=, >=, =, !=). */
    REL,

    // ========== Literals ==========
    
    /** Literal: numeric value (integer). */
    NUMBER, 

    // ========== Special ==========
    
    /** Special: end of input marker. */
    EOF;

    private final String literal;

    TokenType() {
        this.literal = null;
    }

    TokenType(String literal) {
        this.literal = literal;
    }

    /**
     * Returns the canonical textual representation of this token type.
     * 
     * For keywords and operators with fixed spellings (WAIT, ASSIGN, etc.), this returns the text.
     * For synthetic categories (ADDOP, MULOP, REL) and literals (NUMBER, EOF), this returns null
     * because they don't have a single fixed representation.
     *
     * @return literal string (e.g., "and", ":="), or null for synthetic/literal token kinds
     */
    public String getliteral() {
        return literal;
    }
}
