package compiler.parser;

import compiler.error.SyntaxException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;


public class ParserTest {
    @Test
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
}
