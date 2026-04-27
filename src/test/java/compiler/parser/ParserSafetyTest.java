package compiler.parser;

import compiler.error.SyntaxException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for Parser token stream safety and bounds checking.
 * These tests verify that the parser handles edge cases and malformed input
 * without throwing IndexOutOfBoundsException or NullPointerException.
 */
@DisplayName("Parser Token Stream Safety Tests")
public class ParserSafetyTest {

    @Test
    @DisplayName("Parser should handle empty input gracefully")
    void testParserHandlesEmptyInput() {
        String source = "";
        Parser parser = new Parser(source);
        
        // Should not throw NPE or AIOOB
        assertDoesNotThrow(() -> {
            var program = parser.parseProgram();
            assertNotNull(program);
            assertTrue(program.getRules().isEmpty());
        });
    }

    @Test
    @DisplayName("Parser should throw SyntaxException for incomplete rule")
    void testParserIncompleteRule() {
        // Missing arrow and command
        String source = "1 = 1";
        Parser parser = new Parser(source);
        
        assertThrows(SyntaxException.class, parser::parseProgram,
            "Parser should throw SyntaxException for incomplete rule, not AIOOB");
    }

    @Test
    @DisplayName("Parser should throw SyntaxException for missing semicolon")
    void testParserMissingSemicolon() {
        String source = "1 = 1 --> wait";
        Parser parser = new Parser(source);
        
        assertThrows(SyntaxException.class, parser::parseProgram,
            "Parser should throw SyntaxException for missing semicolon");
    }

    @Test
    @DisplayName("Parser should throw SyntaxException for malformed condition")
    void testParserMalformedCondition() {
        String source = "mem[4] = --> wait;";
        Parser parser = new Parser(source);
        
        assertThrows(SyntaxException.class, parser::parseProgram,
            "Parser should throw SyntaxException for malformed condition");
    }

    @Test
    @DisplayName("Parser should throw SyntaxException for unclosed parenthesis")
    void testParserUnclosedParenthesis() {
        String source = "(1 = 1 --> wait;";
        Parser parser = new Parser(source);
        
        assertThrows(SyntaxException.class, parser::parseProgram,
            "Parser should throw SyntaxException for unclosed parenthesis");
    }

    @Test
    @DisplayName("Parser should throw SyntaxException for invalid token sequence")
    void testParserInvalidTokenSequence() {
        String source = "1 = 1 --> --> wait;";
        Parser parser = new Parser(source);
        
        assertThrows(SyntaxException.class, parser::parseProgram,
            "Parser should throw SyntaxException for invalid token sequence");
    }

    @Test
    @DisplayName("Valid simple rule parses without error")
    void testParserValidSimpleRule() {
        String source = "1 = 1 --> wait;";
        Parser parser = new Parser(source);
        
        assertDoesNotThrow(() -> {
            var program = parser.parseProgram();
            assertNotNull(program);
            assertEquals(1, program.getRules().size());
        });
    }

    @Test
    @DisplayName("Valid complex rule with multiple conditions parses without error")
    void testParserValidComplexRule() {
        String source = "1 = 1 and 2 = 2 --> forward;";
        Parser parser = new Parser(source);
        
        assertDoesNotThrow(() -> {
            var program = parser.parseProgram();
            assertNotNull(program);
            assertEquals(1, program.getRules().size());
        });
    }

    @Test
    @DisplayName("Valid rule with OR conditions parses without error")
    void testParserValidOrCondition() {
        String source = "1 = 1 or 2 = 2 --> wait;";
        Parser parser = new Parser(source);
        
        assertDoesNotThrow(() -> {
            var program = parser.parseProgram();
            assertNotNull(program);
            assertEquals(1, program.getRules().size());
        });
    }

    @Test
    @DisplayName("Valid rule with SERVE action parses without error")
    void testParserValidServeAction() {
        String source = "1 = 1 --> serve[5];";
        Parser parser = new Parser(source);
        
        assertDoesNotThrow(() -> {
            var program = parser.parseProgram();
            assertNotNull(program);
            assertEquals(1, program.getRules().size());
        });
    }

    @Test
    @DisplayName("Valid rule with memory access parses without error")
    void testParserValidMemoryAccess() {
        String source = "mem[1] = 5 --> wait;";
        Parser parser = new Parser(source);
        
        assertDoesNotThrow(() -> {
            var program = parser.parseProgram();
            assertNotNull(program);
            assertEquals(1, program.getRules().size());
        });
    }

    @Test
    @DisplayName("Parser rejects assignment to read-only memory slot (existing test)")
    void testParserRejectsIllegalMemoryAssignment() {
        // Assigning to ENERGY (mem[4]) should be rejected.
        String source = "1 = 1 --> mem[4] := 9999;";
        Parser parser = new Parser(source);

        assertThrows(
            SyntaxException.class,
            parser::parseProgram,
            "Parser should reject assignment to read-only memory slot mem[4]."
        );
    }

    @Test
    @DisplayName("Valid rule with negative number parses without error")
    void testParserValidNegativeNumber() {
        String source = "1 = -5 --> wait;";
        Parser parser = new Parser(source);
        
        assertDoesNotThrow(() -> {
            var program = parser.parseProgram();
            assertNotNull(program);
            assertEquals(1, program.getRules().size());
        });
    }

    @Test
    @DisplayName("Valid rule with arithmetic expression parses without error")
    void testParserValidArithmeticExpression() {
        String source = "1 + 2 = 3 * 4 --> wait;";
        Parser parser = new Parser(source);
        
        assertDoesNotThrow(() -> {
            var program = parser.parseProgram();
            assertNotNull(program);
            assertEquals(1, program.getRules().size());
        });
    }

    @Test
    @DisplayName("Parser handles multiple rules without error")
    void testParserMultipleRules() {
        String source = "1 = 1 --> wait; 2 = 2 --> forward;";
        Parser parser = new Parser(source);
        
        assertDoesNotThrow(() -> {
            var program = parser.parseProgram();
            assertNotNull(program);
            assertEquals(2, program.getRules().size());
        });
    }
}
