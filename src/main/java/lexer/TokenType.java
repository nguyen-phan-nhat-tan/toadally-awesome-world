    package lexer;

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
        
        // ========== Operators ==========
        /** Arithmetic addition operator (+). */
        PLUS("+"),

        /** Arithmetic subtraction operator (-). */
        MINUS("-"),
        
        /** Arithmetic multiplication operator (*) */
        MUL("*"),

        /** Arithmetic division operator (/) */
        DIV("/"),


        MOD("mod"),

        /** Category: relational operators (&lt;, &gt;, &lt;=, &gt;=, =, !=). */
        LT("<"),

        LE("<="),

        EQ("="),

        GT(">"),

        GE(">="),

        NEQ("!="),

        // ========== Literals ==========
        
        /** Literal: numeric value (integer). */
        NUMBER, 

        // ========== Special ==========
        
        /** Special: end of input marker. */
        EOF;

        private final String literal; 

        TokenType(){
            this.literal = null;
        }

        TokenType(String literal){
            this.literal = literal;
        }

        /** * Retrieves the string representation of the token
         * @return the literal string associated with the token, or null if there is no literal.
        */
        public String getLiteral(){
            return literal;
        }
    }
