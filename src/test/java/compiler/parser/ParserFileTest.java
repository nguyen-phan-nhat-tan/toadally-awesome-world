package compiler.parser;

import compiler.ast.Program;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParserFileTest {

    @Test
    void parsesUserProvidedTxtFile() throws IOException {
        String filePath = System.getProperty("parser.inputFile");
        if (filePath == null || filePath.isBlank()) {
            filePath = System.getenv("PARSER_INPUT_FILE");
        }

        Assumptions.assumeTrue(
            filePath != null && !filePath.isBlank(),
            "Provide parser.inputFile JVM property or PARSER_INPUT_FILE environment variable to run this test"
        );

        Path inputPath = Path.of(filePath);
        System.out.println("[ParserFileTest] Reading input from: " + inputPath.toAbsolutePath());
        assertTrue(Files.exists(inputPath), "Input file does not exist: " + inputPath);
        assertTrue(Files.isRegularFile(inputPath), "Input path is not a regular file: " + inputPath);

        String source = Files.readString(inputPath, StandardCharsets.UTF_8);
        System.out.println("[ParserFileTest] Source length: " + source.length() + " characters");
        Program program = assertDoesNotThrow(() -> new Parser(source).parseProgram());
        assertNotNull(program);
        System.out.println("[ParserFileTest] Parse completed successfully.");
    }
}
