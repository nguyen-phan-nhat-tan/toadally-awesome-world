package compiler.lexer;

import compiler.error.SyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Performs lexical analysis on source code, converting raw text into a sequence of tokens.
 * 
 * This is the first stage of the compiler pipeline. The lexer reads source text character-by-character,
 * recognizes keywords, operators, identifiers, and numeric literals, and produces an ordered list of tokens
 * that the parser can easily work with. Position information (line and column) is preserved at each token
 * to enable precise error reporting.
 * 
 * The lexer handles:
 * <ul>
 *   <li>Single-character tokens: brackets, braces, parentheses, semicolons</li>
 *   <li>Multi-character operators: := (assign), --> (arrow), <= >= != (relations)</li>
 *   <li>Keywords: wait, forward, backward, left, right, eat, attack, grow, bud, serve, mem, nearby, ahead, random, smell, and, or</li>
 *   <li>Numeric literals: integers (e.g., 42)</li>
 *   <li>Identifiers: variable and rule names</li>
 *   <li>Comments: line comments starting with //</li>
 *   <li>Whitespace: spaces, tabs, newlines (skipped but tracked for position)</li>
 * </ul>
 * 
 * Throws {@link SyntaxException} when encountering invalid characters or malformed tokens.
 * 
 * @see Lexer#tokenize()
 * @see Token
 * @see TokenType
 */
public class Lexer {
    /** Complete source text being tokenized. */
    private final String source;
    
    /** Current position in source (0-based absolute offset). */
    private int currentIndex;
    
    /** Current line in source (1-based); incremented on each newline. */
    private int line;
    
    /** Current column in source (1-based); reset to 1 after each newline. */
    private int column;
    
    /** Start position of the current token being scanned. */
    private int start;
    
    /** Column number where the current token started. */
    private int tokenColumn;
    
    /** Accumulator for tokens found during scanning; finalized by {@link #tokenize()}. */
    private final List<Token> tokens;

    /**
     * Initializes the lexer with source code to tokenize.
     * 
     * Position tracking begins at line 1, column 1. All internal state is reset for fresh scanning.
     * 
     * @param source the complete source code to lex (non-null)
     * @throws NullPointerException if source is null
     */
    public Lexer(String source) {
        this.source = source;
        this.currentIndex = 0;
        this.line = 1;
        this.column = 1;
        this.tokenColumn = 1;
        this.tokens = new ArrayList<>();
    }

    /**
     * Tokenizes the entire source code and returns a list of tokens in source order.
     * 
     * This method processes the entire source text from start to finish, recognizing all token types
     * and recording their positions. An EOF token is appended to signal end-of-input to the parser.
     * After this method completes, the lexer state is frozen and cannot be reused.
     * 
     * @return immutable token list in source order (always ends with EOF token)
     * @throws SyntaxException when a lexical error is detected (e.g., invalid character, malformed token)
     */
    public List<Token> tokenize() {
        while (!isAtEnd()) {
           start = currentIndex;
           tokenColumn = column;
           scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", line, column));
        return tokens;
    }

    /**
     * Scans the next token from the source and adds it to the token list.
     * Handles single-character tokens, multi-character operators, literals, identifiers as well as custom "sugar" syntax, 
     * and skips whitespace and comments.
     * 
     * @throws SyntaxException for any lexical errors encountered during scanning
     */
    private void scanToken() {
        char c = advance();
        switch (c) {
            case ' ':
            case '\r':
            case '\t':
            case '\n':
                return;
            case '[':
                addToken(TokenType.LBRACKET, "[");
                return;
            case ']':
                addToken(TokenType.RBRACKET, "]");
                return;
            case '(':
                addToken(TokenType.LPAREN, "(");
                return;
            case ')':
                addToken(TokenType.RPAREN, ")");
                return;
            case '{':
                addToken(TokenType.LBRACE, "{");
                return;
            case '}':
                addToken(TokenType.RBRACE, "}");
                return;
            case ';':
                addToken(TokenType.SEMICOLON, ";");
                return;
            case ':':
                if (match('=')){
                    addToken(TokenType.ASSIGN, ":=");
                } else {
                    throw new SyntaxException("Unexpected character: " + c, line, column);
                }
                return;
            case '+':
                addToken(TokenType.ADDOP, "+");
                return;
            case '*':
                addToken(TokenType.MULOP, "*");
                return;
            case '-':
                if (peek() == '-' && peekNext() == '>') {
                    advance();
                    advance();
                    addToken(TokenType.ARROW, "-->");
                } else {
                    addToken(TokenType.ADDOP, "-");
                }
                return;
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                } else {
                    addToken(TokenType.MULOP, "/");
                }
                return;
            case '<':
                if (match('=')){
                    addToken(TokenType.REL, "<=");
                } else {
                    addToken(TokenType.REL, "<");
                }
                return;
            case '>':
                if (match('=')){
                    addToken(TokenType.REL, ">=");
                } else {
                    addToken(TokenType.REL, ">");
                }
                return;
            case '=':
                addToken(TokenType.REL, "=");
                return;
            case '!':
                if (match('=')){
                    addToken(TokenType.REL, "!=");
                } else {
                    throw new SyntaxException("Unexpected character: " + c, line, column);
                }
                return;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    throw new SyntaxException("Unexpected character: " + c, line, column);
                }
                return;
        }
    }

    /**
     * Scans a number literal from the source, validates it, and adds it as a token.
     */
    private void number(){
        while (isDigit(peek())) {
            advance();
        }
        String lexeme = source.substring(start, currentIndex);
        int value;
        try {
            value = Integer.parseInt(lexeme);
        } catch (NumberFormatException e) {
            throw new SyntaxException("Invalid number: " + lexeme, line, column);
        }
        addToken(TokenType.NUMBER, lexeme, value);
    }


    /**
     * Scans an identifier or keyword from the source, checks for reserved words and "sugar" syntax, 
     * and adds the appropriate token(s).
     */
    private void identifier() {
        while (isAlpha(peek()) || isDigit(peek())) {
            advance();
        }
        String lexeme = source.substring(start, currentIndex);
        Integer sugarIndex = Sugar.getIndexSugar(lexeme);
        if (sugarIndex != null) {
            injectSugarToken(sugarIndex);
            return;
        }
        TokenType type = keyword(lexeme);
        addToken(type, lexeme);
    }

    /**
     * Checks if the given lexeme matches a reserved keyword and returns the corresponding token type.
     * @param lexeme the identifier string to check against reserved keywords
     * @return the TokenType corresponding to the keyword, or 
     * @throws SyntaxException if it's not a recognized keyword
     */
    private TokenType keyword(String lexeme){
        switch (lexeme.toLowerCase()) {
            case "and":
                return TokenType.AND;
            case "or":
                return TokenType.OR;
            case "mem":
                return TokenType.MEM;
            case "wait":
                return TokenType.WAIT;
            case "forward":
                return TokenType.FORWARD;
            case "backward":
                return TokenType.BACKWARD;
            case "left":
                return TokenType.LEFT;
            case "right":
                return TokenType.RIGHT;
            case "eat":
                return TokenType.EAT;
            case "attack":
                return TokenType.ATTACK;
            case "grow":
                return TokenType.GROW;
            case "bud":
                return TokenType.BUD;
            case "serve":
                return TokenType.SERVE;
            case "nearby":
                return TokenType.NEARBY;
            case "ahead":
                return TokenType.AHEAD;
            case "random":
                return TokenType.RANDOM;
            case "smell":
                return TokenType.SMELL;
            case "mod":
                return TokenType.MULOP;
            default:
                throw new SyntaxException("Unknown identifier: " + lexeme, line, column);
        }
    }
    
    /**
     * Injects a sequence of tokens representing a sugar access for the given sugar index.
     * 
     * @param index the sugar index to inject (e.g., 0 for MEMSIZE, 1 for DEFENSE, etc.)
     */
    private void injectSugarToken(int index) {
        addToken(TokenType.MEM, "mem");
        addToken(TokenType.LBRACKET, "[");
        addToken(TokenType.NUMBER, String.valueOf(index), index);
        addToken(TokenType.RBRACKET, "]");
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    // Overloading addToken 
    private void addToken(TokenType type, String lexeme) {
        addToken(type, lexeme, 0);
    }

    private void addToken(TokenType type, String lexeme, int value) {
        tokens.add(new Token(type, lexeme, line, tokenColumn, value));
    }

    private boolean match(char expected){
        if (isAtEnd()) {
            return false;
        }
        if (source.charAt(currentIndex) != expected) {
            return false;
        }
        advance();
        return true;
    }

    private char advance() {
        char c = source.charAt(currentIndex++);
        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return c;
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(currentIndex);
    }

    private char peekNext() {
        if (currentIndex + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(currentIndex + 1);
    }

    private boolean isAtEnd() {
        return currentIndex >= source.length();
    }
}
