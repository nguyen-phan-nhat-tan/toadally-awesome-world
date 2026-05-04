package compiler.io;

import compiler.ast.PrettyPrinter;
import compiler.simulation.Controller;
import compiler.simulation.Critter;
import compiler.simulation.World;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Command-line interface for the critter simulation.
 *
 * Supported commands:
 * - new: Start a new simulation with a world populated by randomly placed rocks
 * - load <world file>: Start a new simulation with the specified world file
 * - critters <critter file> <n>: Add n critters from the specified file at random locations
 * - step <n>: Advance the world for n time steps
 * - info: Display world information and ASCII-art map
 * - hex <column> <row>: Display information about a specific hex
 * - quit: Exit the program
 */
public class Console {
    private static final Random random = new Random();
    private World world;
    private Controller controller;
    private String worldName;
    private long stepCount = 0;

    public Console() {
        this.world = null;
        this.controller = null;
        this.worldName = "None";
    }

    /**
     * Run the interactive console.
     */
    public void run() {
        System.out.println("Toadally Awesome World Simulator");
        System.out.println("Commands: new, load <file>, critters <file> <n>, step <n>, info, hex <x> <y>, quit");
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while (true) {
                System.out.print("> ");
                line = reader.readLine();
                if (line == null) break;
                
                line = line.trim();
                if (line.isEmpty()) continue;
                
                processCommand(line);
            }
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }

    private void processCommand(String line) {
        String[] parts = line.split("\\s+", 3);
        String command = parts[0].toLowerCase();

        try {
            switch (command) {
                case "new" -> commandNew();
                case "load" -> {
                    if (parts.length < 2) {
                        System.out.println("Usage: load <world file>");
                    } else {
                        commandLoad(parts[1]);
                    }
                }
                case "critters" -> {
                    if (parts.length < 3) {
                        System.out.println("Usage: critters <critter file> <n>");
                    } else {
                        try {
                            int n = Integer.parseInt(parts[2]);
                            commandCritters(parts[1], n);
                        } catch (NumberFormatException e) {
                            System.out.println("Error: n must be an integer");
                        }
                    }
                }
                case "step" -> {
                    if (parts.length < 2) {
                        System.out.println("Usage: step <n>");
                    } else {
                        try {
                            int n = Integer.parseInt(parts[1]);
                            commandStep(n);
                        } catch (NumberFormatException e) {
                            System.out.println("Error: n must be an integer");
                        }
                    }
                }
                case "info" -> commandInfo();
                case "hex" -> {
                    if (parts.length < 3) {
                        System.out.println("Usage: hex <column> <row>");
                    } else {
                        try {
                            int x = Integer.parseInt(parts[1]);
                            int y = Integer.parseInt(parts[2]);
                            commandHex(x, y);
                        } catch (NumberFormatException e) {
                            System.out.println("Error: coordinates must be integers");
                        }
                    }
                }
                case "quit", "exit" -> System.exit(0);
                default -> System.out.println("Unknown command: " + command);
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void commandNew() {
        this.world = new World(50, 50);
        this.controller = new Controller(world, new ArrayList<>());
        this.worldName = "Default World";
        this.stepCount = 0;
        System.out.println("Created new 50x50 world");
    }

    private void commandLoad(String filename) throws IOException {
        try {
            WorldLoader.WorldAndController loaded = WorldLoader.loadFromFile(Path.of(filename));
            this.world = loaded.world;
            this.controller = loaded.controller;
            this.worldName = loaded.name;
            this.stepCount = 0;
        } catch (IOException e) {
            System.err.println("Error loading world: " + e.getMessage());
        }
    }

    private void commandCritters(String filename, int count) throws IOException {
        if (world == null) {
            System.out.println("Error: no world loaded");
            return;
        }

        List<Critter> newCritters = new ArrayList<>();
        Path critterPath = Path.of(filename);

        for (int i = 0; i < count; i++) {
            try {
                Critter critter = CritterLoader.loadFromFile(critterPath);
                
                // Find random valid location
                boolean placed = false;
                for (int attempt = 0; attempt < 100; attempt++) {
                    int x = random.nextInt(world.getWidth());
                    int y = random.nextInt(world.getHeight());
                    
                    if (world.isValidCoordinate(x, y) && world.getHex(x, y).isEmpty()) {
                        controller.addCritter(critter, x, y);
                        newCritters.add(critter);
                        placed = true;
                        break;
                    }
                }
                
                if (!placed) {
                    System.err.println("Warning: could not place critter " + (i + 1) + " (no free space)");
                }
            } catch (IOException e) {
                System.err.println("Warning: could not load critter " + (i + 1) + ": " + e.getMessage());
            }
        }

        System.out.println("Added " + newCritters.size() + " critters");
    }

    private void commandStep(int n) {
        if (controller == null) {
            System.out.println("Error: no world loaded");
            return;
        }

        for (int i = 0; i < n; i++) {
            controller.step();
            stepCount++;
        }
        System.out.println("Advanced " + n + " steps (total: " + stepCount + ")");
    }

    private void commandInfo() {
        if (world == null) {
            System.out.println("Error: no world loaded");
            return;
        }

        System.out.println("World: " + worldName);
        System.out.println("Size: " + world.getWidth() + " x " + world.getHeight());
        System.out.println("Steps: " + stepCount);
        System.out.println("Critters alive: " + controller.getTurnOrder().size());
        System.out.println();
        
        printAsciiMap();
    }

    private void commandHex(int x, int y) {
        if (world == null) {
            System.out.println("Error: no world loaded");
            return;
        }

        if (!world.isInBounds(x, y)) {
            System.out.println("Hex (" + x + ", " + y + ") is outside world bounds");
            return;
        }

        var hex = world.getHex(x, y);
        System.out.println("Hex (" + x + ", " + y + "):");
        
        if (hex.isRock()) {
            System.out.println("  Contains: Rock");
        } else if (hex.hasCritter()) {
            Critter critter = world.getCritterAt(x, y);
            System.out.println("  Contains: Critter");
            System.out.println("  Species: Unknown");
            
            // Print first 7 memory locations
            int[] memory = critter.getMemory();
            System.out.print("  Memory[0-6]: ");
            for (int i = 0; i < Math.min(7, memory.length); i++) {
                System.out.print(memory[i]);
                if (i < 6) System.out.print(" ");
            }
            System.out.println();
            
            System.out.println("  Defense: " + critter.getDefense());
            System.out.println("  Offense: " + critter.getOffense());
            System.out.println("  Size: " + critter.getSize());
            System.out.println("  Energy: " + critter.getEnergy());
            System.out.println("  Posture: " + critter.getPosture());
            System.out.println("  Direction: " + critter.getDirection());
            
            var interpreter = critter.getInterpreterVisitor();
            if (interpreter instanceof compiler.simulation.ProgramCritterInterpreter pi) {
                System.out.println("  Rules:");
                String program = new PrettyPrinter().visit(pi.getProgram());
                for (String ruleLine : program.split("\n")) {
                    System.out.println("    " + ruleLine);
                }
            }
        } else if (hex.hasFood()) {
            System.out.println("  Contains: Food (" + hex.getFoodAmount() + ")");
        } else {
            System.out.println("  Contains: Empty");
        }
    }

    private void printAsciiMap() {
        int width = world.getWidth();
        int height = world.getHeight();
        int displayWidth = Math.min(width, 40);
        int displayHeight = Math.min(height, 20);

        // Print rows from top to bottom
        for (int y = displayHeight - 1; y >= 0; y--) {
            // Indent based on row parity
            if (y % 2 == 0) {
                System.out.print("  ");  // Even rows: 2 spaces
            } else {
                System.out.print("    ");  // Odd rows: 4 spaces
            }

            // Print cells at appropriate x coordinates
            boolean first = true;
            if (y % 2 == 0) {
                // Even row: print cells at x = 0, 2, 4, 6, ...
                for (int x = 0; x < displayWidth; x += 2) {
                    if (!first) System.out.print("   ");  // 3 spaces between cells
                    first = false;
                    printHexCell(x, y);
                }
            } else {
                // Odd row: print cells at x = 1, 3, 5, 7, ...
                for (int x = 1; x < displayWidth; x += 2) {
                    if (!first) System.out.print("   ");  // 3 spaces between cells
                    first = false;
                    printHexCell(x, y);
                }
            }
            System.out.println();
        }
    }

    private void printHexCell(int x, int y) {
        if (!world.isValidCoordinate(x, y)) {
            System.out.print("#");
        } else {
            var hex = world.getHex(x, y);
            if (hex.isRock()) {
                System.out.print("#");
            } else if (hex.hasCritter()) {
                Critter c = world.getCritterAt(x, y);
                System.out.print(c.getDirection());
            } else if (hex.hasFood()) {
                System.out.print("F");
            } else {
                System.out.print("-");
            }
        }
    }

    public static void main(String[] args) {
        Console console = new Console();
        console.run();
    }
}
