package compiler.ast;

import compiler.parser.*;

/**
 * Entry point and demo for the compiler pipeline.
 * 
 * This class demonstrates the complete compilation process:
 * <ol>
 *   <li>Parse source code using the {@link Parser}</li>
 *   <li>Produce an abstract syntax tree ({@link Program})</li>
 *   <li>Apply visitors to perform operations on the AST:
 *       <ul>
 *           <li>{@link PrettyPrinter}: Format the code nicely</li>
 *           <li>{@link AsciiTreePrinter}: Visualize the tree structure</li>
 *       </ul>
 *   </li>
 * </ol>
 * 
 * The main method includes a sample critter program demonstrating all grammar features.
 * Run this class to see how the compiler transforms source into formatted code and tree diagrams.
 * 
 * @see Parser
 * @see Program
 * @see PrettyPrinter
 * @see AsciiTreePrinter
 */
public class Main {
    /** Utility class; do not instantiate. */
    private Main() {
    }

    /**
     * Demonstrates parsing and printing for a built-in sample program.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        String source = "POSTURE != 17 --> POSTURE := 17; // we are species 17!\n" + //
                        "nearby[3] = 0 and ENERGY > 2500 --> bud;\n" + //
                        "{ENERGY > SIZE * 400 and SIZE < 7} --> grow;\n" + //
                        "ahead[1] < -1 and ENERGY < 500 * SIZE --> eat;\n" + //
                        "// next line attacks only other species\n" + //
                        "(ahead[1] / 10 mod 100) != 17 and ahead[1] > 0 --> attack;\n" + //
                        "{ahead[2] < -10 or random[20] = 0} and ahead[1] = 0 --> forward;\n" + //
                        "ahead[3] < -15 and ahead[1] = 0 --> forward;\n" + //
                        "ahead[4] < -20 and ahead[1] = 0 --> forward;\n" + //
                        "nearby[0] > 0 and nearby[3] = 0 --> backward;\n" + //
                        "// karma action: donate food if we are too full or large enough\n" + //
                        "ahead[1] < -1 and { ENERGY > 2500 or SIZE > 7 } --> serve[ENERGY / 42];\n" + //
                        "random[6] = 1 --> left;\n" + //
                        "random[5] = 1 --> right;\n" + //
                        "1 = 1 --> wait; // mostly soak up the rays\n" + //
                        "";


        Parser parser = new Parser(source);
        Program ast = parser.parseProgram();
        String formattedCode;
        
        PrettyPrinter prettyPrinter = new PrettyPrinter();
        formattedCode = ast.accept(prettyPrinter);

        System.out.println("=== Pretty Printed Code ===");
        System.out.println(formattedCode);
        System.out.println();
        
        AsciiTreePrinter printer = new AsciiTreePrinter();
        formattedCode = ast.accept(printer);
        
        System.out.println("=== Tree Structure ===");
        System.out.println(formattedCode);
    
    }
}