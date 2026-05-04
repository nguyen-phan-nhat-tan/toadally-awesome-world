package compiler.io;

import compiler.ast.Program;
import compiler.parser.Parser;
import compiler.simulation.Critter;
import compiler.simulation.ProgramCritterInterpreter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * Loads critter specifications from text files.
 *
 * File format:
 * species: <name>
 * memsize: <memory size>
 * defense: <defensive ability>
 * offense: <offensive ability>
 * size: <size>
 * energy: <energy>
 * posture: <posture>
 * <program rules>
 */
public class CritterLoader {
    private static final String DEFAULT_SPECIES = "unknown";
    private static final int DEFAULT_MEMSIZE = 7;
    private static final int DEFAULT_DEFENSE = 1;
    private static final int DEFAULT_OFFENSE = 1;
    private static final int DEFAULT_SIZE = 1;
    private static final int DEFAULT_ENERGY = 100;
    private static final int DEFAULT_POSTURE = 0;

    /**
     * Loads a critter from a file at the given path.
     *
     * @param path path to the critter file
     * @return loaded critter with parsed program and attributes
     * @throws IOException if file cannot be read
     */
    public static Critter loadFromFile(Path path) throws IOException {
        String content = new String(Files.readAllBytes(path));
        return parseContent(content);
    }

    /**
     * Parses critter specification from text content.
     *
     * @param content text content of critter file
     * @return loaded critter with parsed attributes
     */
    public static Critter parseContent(String content) {
        CritterSpec spec = new CritterSpec();
        StringBuilder programText = new StringBuilder();
        
        Scanner scanner = new Scanner(content);
        boolean headerDone = false;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();

            // Skip blank lines and comments
            if (line.isEmpty() || line.startsWith("//")) {
                continue;
            }

            if (!headerDone) {
                if (line.startsWith("species:")) {
                    spec.species = parseString(line, "species:", DEFAULT_SPECIES);
                } else if (line.startsWith("memsize:")) {
                    spec.memsize = parseInt(line, "memsize:", DEFAULT_MEMSIZE);
                } else if (line.startsWith("defense:")) {
                    spec.defense = parseInt(line, "defense:", DEFAULT_DEFENSE);
                } else if (line.startsWith("offense:")) {
                    spec.offense = parseInt(line, "offense:", DEFAULT_OFFENSE);
                } else if (line.startsWith("size:")) {
                    spec.size = parseInt(line, "size:", DEFAULT_SIZE);
                } else if (line.startsWith("energy:")) {
                    spec.energy = parseInt(line, "energy:", DEFAULT_ENERGY);
                } else if (line.startsWith("posture:")) {
                    spec.posture = parseInt(line, "posture:", DEFAULT_POSTURE);
                } else {
                    // Start of program
                    headerDone = true;
                    programText.append(line).append("\n");
                }
            } else {
                programText.append(line).append("\n");
            }
        }
        scanner.close();

        // Parse the program
        Program program = parseProgram(programText.toString());
        
        // Create critter with parsed attributes
        Critter critter = new Critter(0, 0, 0, spec.energy, new ProgramCritterInterpreter(program));
        critter.setMemorySize(spec.memsize);
        critter.setDefense(spec.defense);
        critter.setOffense(spec.offense);
        
        // For size and posture, we set them directly but may need special handling
        for (int i = 0; i < spec.size - 1; i++) {
            critter.grow();
        }
        critter.setPosture(spec.posture);

        return critter;
    }

    private static String parseString(String line, String prefix, String defaultValue) {
        try {
            return line.substring(prefix.length()).trim();
        } catch (Exception e) {
            System.err.println("Warning: unable to parse " + prefix + " in critter file; using default: " + defaultValue);
            return defaultValue;
        }
    }

    private static int parseInt(String line, String prefix, int defaultValue) {
        try {
            String value = line.substring(prefix.length()).trim();
            int parsed = Integer.parseInt(value);
            if (parsed < 0) {
                System.err.println("Warning: negative value for " + prefix + "; using default: " + defaultValue);
                return defaultValue;
            }
            return parsed;
        } catch (Exception e) {
            System.err.println("Warning: unable to parse " + prefix + " in critter file; using default: " + defaultValue);
            return defaultValue;
        }
    }

    private static Program parseProgram(String programText) {
        try {
            Parser parser = new Parser(programText);
            return parser.parseProgram();
        } catch (Exception e) {
            System.err.println("Syntax error in critter program: " + e.getMessage());
            // Return an empty program (wait)
            Parser parser = new Parser("1 = 1 --> wait;");
            return parser.parseProgram();
        }
    }

    /**
     * Helper class to hold parsed critter specification.
     */
    private static class CritterSpec {
        String species = DEFAULT_SPECIES;
        int memsize = DEFAULT_MEMSIZE;
        int defense = DEFAULT_DEFENSE;
        int offense = DEFAULT_OFFENSE;
        int size = DEFAULT_SIZE;
        int energy = DEFAULT_ENERGY;
        int posture = DEFAULT_POSTURE;
    }
}
