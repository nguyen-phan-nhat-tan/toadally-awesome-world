package compiler.lexer;

import java.util.List;
import org.junit.jupiter.api.Test;
import compiler.error.SyntaxException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LexerTest {

    @Test
    void testSimple() {
        Lexer lexer = new Lexer("1 = 1 --> wait; // mostly soak up the rays");
        List<Token> tokens = lexer.tokenize();

        assertTokenTypes(tokens,
            TokenType.NUMBER,
            TokenType.REL,
            TokenType.NUMBER,
            TokenType.ARROW,
            TokenType.WAIT,
            TokenType.SEMICOLON,
            TokenType.EOF
        );

        assertEquals(1, tokens.get(0).getValue());
        assertEquals(1, tokens.get(2).getValue());
    }

    @Test
    void testUnknownIdentifierThrowsSyntaxException() {
        Lexer lexer = new Lexer("unknown");
        assertThrows(SyntaxException.class, lexer::tokenize);
    }

    @Test
    void testSpecies17ProgramTokenizes() {
        String program = """
            POSTURE != 17 --> POSTURE := 17; // we are species 17!
            nearby[3] = 0 and ENERGY > 2500 --> bud;
            {ENERGY > SIZE * 400 and SIZE < 7} --> grow;
            ahead[1] < -1 and ENERGY < 500 * SIZE --> eat;
            // next line attacks only other species
            (ahead[1] / 10 mod 100) != 17 and ahead[1] > 0 --> attack;
            {ahead[2] < -10 or random[20] = 0} and ahead[1] = 0 --> forward;
            ahead[3] < -15 and ahead[1] = 0 --> forward;
            ahead[4] < -20 and ahead[1] = 0 --> forward;
            nearby[0] > 0 and nearby[3] = 0 --> backward;
            // karma action: donate food if we are too full or large enough
            ahead[1] < -1 and { ENERGY > 2500 or SIZE > 7 } --> serve[ENERGY / 42];
            random[6] = 1 --> left;
            random[5] = 1 --> right;
            1 = 1 --> wait; // mostly soak up the rays
            """;

        Lexer lexer = new Lexer(program);
        List<Token> tokens = assertDoesNotThrow(lexer::tokenize);

        assertEquals(TokenType.EOF, tokens.get(tokens.size() - 1).getType());
    }

    private void assertTokenTypes(List<Token> tokens, TokenType... expected) {
        assertEquals(expected.length, tokens.size(), "Token count mismatch");
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], tokens.get(i).getType(), "Unexpected token type at index " + i);
        }
    }
}
