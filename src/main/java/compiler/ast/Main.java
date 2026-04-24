package compiler.ast;

import compiler.parser.*;

public class Main {
    public static void main(String[] args) {
        String source = "( 1 > 2 )  and { ( 3 <= 4 ) or (5 = 6) } --> mem[0] := 10 \r\n" + //
                        "\t\t\t\t\t\tmem[1] := 4 \r\n" + //
                        "\t\t\t\t\t\tmem[5] := 3 \r\n" + //
                        "\t\t\t\t\t\twait;\r\n" + //
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