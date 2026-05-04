package compiler.parser;

import java.util.ArrayList;
import java.util.List;

import compiler.ast.*;
import compiler.ast.command.*;
import compiler.ast.condition.*;
import compiler.ast.expression.*;
import compiler.lexer.*;
import compiler.error.SyntaxException;

/**
 * Performs syntax analysis using recursive descent parsing.
 * 
 * This is the second stage of the compiler pipeline. The parser consumes tokens from the lexer,
 * validates that they conform to the grammar, and builds an abstract syntax tree (AST).
 * 
 * This parser uses <b>recursive descent</b> parsing, which means:
 * <ul>
 *   <li>Grammar rules map directly to methods (e.g., parseExpression() for expression production)</li>
 *   <li>Left recursion is eliminated via iteration (e.g., using while loops for operator chains)</li>
 *   <li>Precedence and associativity are encoded in method nesting (higher precedence = deeper nesting)</li>
 *   <li>Backtracking is minimal</li>
 * </ul>
 * 
 * <b>Grammar Overview:</b>
 * <pre>
 * program    ::= rule*
 * rule       ::= condition "-->" command ";"
 * condition  ::= conjunction ("or" conjunction)*
 * conjunction::= relation ("and" relation)*
 * relation   ::= expression relop expression
 * command    ::= update* (action | update)
 * update     ::= "mem" "[" expression "]" ":=" expression
 * action     ::= KEYWORD | "serve" "[" expression "]"
 * expression ::= term ((addop term)*)
 * term       ::= factor ((mulop factor)*)
 * factor     ::= NUMBER | "mem" "[" expression "]" | "(" expression ")" | "-" factor | sensor
 * sensor     ::= SENSOR_KEYWORD "[" expression "]"
 * </pre>
 * 
 * Throws {@link SyntaxException} when token stream violates grammar expectations.
 * 
 * @see Lexer
 * @see TokenType
 * @see Program
 */
public final class Parser {
    /** Ordered list of tokens from the lexer. Built once in constructor, then scanned sequentially. */
    private final List<Token> tokens;
    
    /** Current position in the token stream (0-based index). Advanced by match() and consume(). */
    private int currentIndex;

    /**
     * Initializes the parser and performs lexical analysis in one step.
     * 
     * The lexer is invoked immediately to tokenize the entire source, ensuring that
     * parsing logic works with stable token categories rather than re-scanning source text.
     *
     * @param source the complete source code to parse (non-null)
     * @throws SyntaxException if lexical errors are detected during tokenization
     */
    public Parser(String source) {
        this.tokens = new Lexer(source).tokenize();
        this.currentIndex = 0;
    }

    /**
     * Parses a complete program from the token stream.
     * 
     * This is the entry point for parsing. It consumes all tokens (except EOF) and builds
     * a Program AST node containing an ordered list of rules. The program is structurally
     * valid at this point and ready for analysis and interpretation.
     *
     * @return a Program node containing all parsed rules in source order
     * @throws SyntaxException if the input does not conform to the grammar
     */
    public Program parseProgram() {
        List<Rule> parsedRules = new ArrayList<>();
    
        while (!isAtEnd() && peek().getType() != TokenType.EOF) {
            parsedRules.add(parseRule());
        }
        return new Program(parsedRules, 0, 0);
    }

    /**
     * Parses a single rule from the current position.
     * 
     * A rule consists of: condition --> command ;
     * All three components are required. Position tracking is preserved from the first token.
     *
     * @return a Rule node pairing the parsed condition and command
     * @throws SyntaxException if the rule format is violated
     */
    private Rule parseRule() {
        Token firstToken = peek();
        Condition condition = parseCondition();
        consume(TokenType.ARROW, "Expected '-->' after condition");
        Command command = parseCommand();
        consume(TokenType.SEMICOLON, "Expected ';' after command");
        return new Rule(condition, command, firstToken.getLine(), firstToken.getColumn());
    }

    /**
     * Parses a condition (disjunction of conjunctions).
     * 
     * Handles "or" as the lowest-precedence logical operator. Returns a LogicNode tree
     * or a RelationNode leaf, depending on operators encountered.
     *
     * @return a Condition AST node
     * @throws SyntaxException if the condition syntax is invalid
     */
    private Condition parseCondition() {
        Condition condition = parseConjunction();

        while (match(TokenType.OR)) {
            Token operator = previous();
            Condition right = parseConjunction();
            condition = new LogicNode(condition, operator.getLexeme(), right, operator.getLine(), operator.getColumn());
        }

        return condition;
    }

    /**
     * Parses a conjunction (AND chain).
     * 
     * Handles "and" with higher precedence than "or". Returns a LogicNode tree or a
     * RelationNode leaf.
     *
     * @return a Condition AST node
     * @throws SyntaxException if the conjunction syntax is invalid
     */
    private Condition parseConjunction() {
        Condition condition = parseRelation();

        while (match(TokenType.AND)) {
            Token operator = previous();
            Condition right = parseRelation();
            condition = new LogicNode(condition, operator.getLexeme(), right, operator.getLine(), operator.getColumn());
        }

        return condition;
    }

    /**
     * Parses an atomic relation: expression relop expression.
     * 
     * A relation is either:
     * <ul>
     *   <li>A parenthesized condition: {condition}</li>
     *   <li>An expression comparison: expr relop expr</li>
     * </ul>
     *
     * @return a RelationNode or parenthesized Condition
     * @throws SyntaxException if the relation format is violated
     */
    private Condition parseRelation() {
        Token startToken = peek();

        if (match(TokenType.LBRACE)) {
            Condition condition = parseCondition();
            consume(TokenType.RBRACE, "Expected '}' after condition");
            return condition;
        }

        if (match(TokenType.LPAREN)) {
            Expression left = parseExpression();
            consume(TokenType.RPAREN, "Expected ')' after expression");

            if (isRelOp(peek().getType())) {
                Token operator = advance();
                Expression right = parseExpression();
                return new RelationNode(left, operator.getLexeme(), right, operator.getLine(), operator.getColumn());
            }

            throw new SyntaxException(
                "Expected a relation operator after parenthesized expression",
                peek().getLine(),
                peek().getColumn()
            );
        }

        Expression left = parseExpression();

        if (isRelOp(peek().getType())) {
            Token operator = advance();
            Expression right = parseExpression();
            return new RelationNode(left, operator.getLexeme(), right, operator.getLine(), operator.getColumn());
        }

        throw new SyntaxException("Expected a relation operator or a parenthesized condition", startToken.getLine(), startToken.getColumn());
    }

    /**
     * Parses a command: zero or more memory updates followed by a terminal action.
     * 
     * The command represents the effect to execute when a rule fires. It may update
     * memory slots before performing an action.
     *
     * @return a CommandList node
     * @throws SyntaxException if no valid action or update is found
     */
    private Command parseCommand() {
        Token startToken = peek();
        List<UpdateNode> updates = new ArrayList<>();
        
        while (check(TokenType.MEM)){
            updates.add(parseUpdate());
        }

        Command terminalAction;
        if (isActionKeyword(peek().getType())) {
            terminalAction = parseAction();
        } else if (!updates.isEmpty()) {
            terminalAction = updates.remove(updates.size() - 1);
        } else {
            throw new SyntaxException("Expected an action command", peek().getLine(), peek().getColumn());
        }
        return new CommandList(updates, terminalAction, startToken.getLine(), startToken.getColumn());
    }

    /**
     * Parses a memory update: mem[index] := value.
     * 
     * Validates that the target memory slot is writable (not read-only).
     *
     * @return an UpdateNode
     * @throws SyntaxException if the update format is invalid or target is read-only
     */
    private UpdateNode parseUpdate() {
        Token start = consume(TokenType.MEM, "Expected 'MEM' for an update command");
        consume(TokenType.LBRACKET, "Expected '[' after 'MEM'");
        Expression index = parseExpression();
        consume(TokenType.RBRACKET, "Expected ']' after index expression");

        if (index instanceof NumberNode) {
            NumberNode numberIndex = (NumberNode) index;
            int slot = numberIndex.getValue();
            if (!Sugar.isAssignableSugar(slot)) {
                throw new SyntaxException(
                    "Cannot assign to read-only memory slot mem[" + slot + "]",
                    numberIndex.getLine(),
                    numberIndex.getColumn()
                );
            }
        }

        consume(TokenType.ASSIGN, "Expected ':=' in update command");
        Expression value = parseExpression();

        return new UpdateNode(index, value, start.getLine(), start.getColumn());
    }

    /**
     * Parses an arithmetic expression with addition and subtraction.
     * 
     * Left-associative: a + b - c = (a + b) - c.
     * BinaryExpr nodes are built bottom-up.
     *
     * @return an Expression AST node
     * @throws SyntaxException if the expression is invalid
     */
    private Expression parseExpression() {
        Expression expr = parseTerm();
        while (match(TokenType.ADDOP)) {
                Token operator = previous();
                Expression right = parseTerm();
                expr = new BinaryExpr(expr, operator.getLexeme(), right, operator.getLine(), operator.getColumn());
         }
        return expr;
    }

    /**
     * Parses a multiplicative term (multiplication and division).
     * 
     * Higher precedence than addition. Left-associative: a * b / c = (a * b) / c.
     *
     * @return an Expression AST node
     * @throws SyntaxException if the term is invalid
     */
    private Expression parseTerm() {
        Expression expr = parseFactor();

        while (match(TokenType.MULOP)) {
                Token operator = previous();
                Expression right = parseFactor();
                expr = new BinaryExpr(expr, operator.getLexeme(), right, operator.getLine(), operator.getColumn());
         }
        return expr;
    }

    /**
     * Parses an atomic expression (leaf level in expression tree).
     * 
     * Recognizes:
     * <ul>
     *   <li>NUMBER literals</li>
     *   <li>Memory access: mem[expr]</li>
     *   <li>Parenthesized expressions: (expr)</li>
     *   <li>Unary negation: -expr</li>
     *   <li>Sensor queries: nearby[expr], ahead[expr], random[expr], smell</li>
     * </ul>
     *
     * @return an Expression AST node
     * @throws SyntaxException if no valid factor is found
     */
    private Expression parseFactor() {
        Token token = peek();
        if (match(TokenType.NUMBER)){
            return new NumberNode(token.getValue(), token.getLine(), token.getColumn());
        }

        if (match(TokenType.MEM)){
            consume(TokenType.LBRACKET, "Expected '[' after 'MEM'");
            Expression index = parseExpression();
            consume(TokenType.RBRACKET, "Expected ']' after index expression");
            return new MemoryNode(index, token.getLine(), token.getColumn());   
        }
        
        if (match(TokenType.LPAREN)){
            Expression expr = parseExpression();
            consume(TokenType.RPAREN, "Expected ')' after expression");
            return expr;
        }

        if (check(TokenType.ADDOP) && peek().getLexeme().equals("-")) {
            Token op = advance();
            Expression right = parseFactor();

            Expression zero = new NumberNode(0, op.getLine(), op.getColumn());
        
            return new BinaryExpr(zero, op.getLexeme(), right, op.getLine(), op.getColumn());
        }

        if (isSensorKeyword(token.getType())) {
            return parseSensor();
        }

        throw new SyntaxException("Expected a number, memory access, or parenthesized expression", token.getLine(), token.getColumn());
    }

    /**
     * Parses an action keyword command.
     * 
     * Most actions (wait, forward, etc.) take no arguments.
     * The SERVE action requires a memory slot argument: serve[slot].
     *
     * @return an ActionNode
     * @throws SyntaxException if action arguments are invalid
     */
    private Command parseAction() {
        Token actionToken = advance();
        TokenType type = actionToken.getType();

        if (type != TokenType.SERVE){
            return new ActionNode(type, actionToken.getLine(), actionToken.getColumn(), null);
        }

        consume(TokenType.LBRACKET, "Expected '[' after SERVE");
        Expression argument = parseExpression();
        consume(TokenType.RBRACKET, "Expected ']' after SERVE argument");

        return new ActionNode(type, actionToken.getLine(), actionToken.getColumn(), argument);
    }

    /**
     * Parses a sensor query (queries about environment or random state).
     * 
     * Most sensors require an index argument: nearby[dir], ahead[dir], random[max].
     * SMELL sensor requires no argument.
     *
     * @return a SensorNode
     * @throws SyntaxException if sensor arguments are invalid
     */
    private Expression parseSensor() {
        Token sensorToken = advance();
        TokenType type = sensorToken.getType();

        if (type == TokenType.SMELL){
            return new SensorNode(type, null, sensorToken.getLine(), sensorToken.getColumn());
        }

        consume(TokenType.LBRACKET, "Expected '[' after " + sensorToken.getLexeme() + " sensor");
        Expression argument = parseExpression();
        consume(TokenType.RBRACKET, "Expected ']' after " + sensorToken.getLexeme() + " sensor argument");

        return new SensorNode(type, argument, sensorToken.getLine(), sensorToken.getColumn());
    }

    // Helper methods for parsing
    /**
     * Checks if the given token type is a relational operator (<, >, <=, >=, =, !=).
     *
     * @param type token type to check
     * @return true if type is REL; false otherwise
     */
    private boolean isRelOp(TokenType type) {
        switch (type) {
            case REL:
                return true;
            default:
                return false;
        }
    }
    /**
     * Checks if the given token type is an action keyword (wait, forward, etc.).
     *
     * @param type token type to check
     * @return true if type is an action keyword; false otherwise
     */
    private boolean isActionKeyword(TokenType type) {
        switch (type) {
            case WAIT:
            case FORWARD:
            case BACKWARD:
            case LEFT:
            case RIGHT:
            case GROW:
            case EAT:
            case ATTACK:
            case SERVE:
            case BUD:
                return true;
            default:
                return false;
        }
    }

    /**
     * Checks if the given token type is a sensor keyword (nearby, ahead, random, smell).
     *
     * @param type token type to check
     * @return true if type is a sensor keyword; false otherwise
     */
    private boolean isSensorKeyword(TokenType type) {
        switch (type) {
            case NEARBY:
            case AHEAD:
            case RANDOM:
            case SMELL:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns the current token without consuming it.
     * Ensures token stream integrity through bounds checking.
     *
     * @return current token, guaranteed to be non-null (EOF token if at end)
     * @throws SyntaxException if token stream integrity is violated
     */
    private Token peek() {
        if (currentIndex < 0 || currentIndex >= tokens.size()) {
            throw new SyntaxException(
                "Token stream integrity violated: invalid token index " + currentIndex,
                -1, -1
            );
        }
        return tokens.get(currentIndex);
    }

    /**
     * Returns the previously consumed token.
     * Requires that advance() was called at least once.
     *
     * @return previous token
     * @throws SyntaxException if no previous token exists (index underflow)
     */
    private Token previous() {
        if (currentIndex <= 0 || currentIndex - 1 < 0 || currentIndex - 1 >= tokens.size()) {
            throw new SyntaxException(
                "Token stream integrity violated: no previous token at index " + (currentIndex - 1),
                -1, -1
            );
        }
        return tokens.get(currentIndex - 1);
    }

    private boolean isAtEnd() {
        // Check bounds BEFORE calling peek() to avoid NPE/AIOOB.
        if (currentIndex < 0 || currentIndex >= tokens.size()) {
            return true;
        }
        return peek().getType() == TokenType.EOF;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token advance() {
        if (!isAtEnd()) currentIndex++;
        return previous();
    }

    /**
     * Consumes and returns the expected token type, or throws a detailed error.
     *
     * @param type the expected token type
     * @param errorMessage human-readable message for error reporting
     * @return the consumed token
     * @throws SyntaxException if the current token does not match the expected type
     */
    private Token consume(TokenType type, String errorMessage) {
        if (check(type)) return advance();
        throw new SyntaxException(errorMessage, peek().getLine(), peek().getColumn());
    }
}
