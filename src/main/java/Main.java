
import compiler.io.WorldLoader;
import compiler.simulation.World;
import compiler.simulation.Controller;

import java.nio.file.Path;

/**
 * Entry point for the Toadally Awesome World Simulator.
 * 
 * This program loads a world and optionally adds critters, then runs the
 * simulation with real-time rendering of the world state every second.
 * 
 * Usage:
 *   java Main [world-file] [critter-file] [num-critters]
 * 
 * If no arguments are provided, creates a default empty world.
 */
public class Main {
    private static final int RENDER_INTERVAL_MS = 1000; // 1 second
    private static final int MS_PER_STEP = 1; // 100ms between steps = ~10 steps/sec = ~1 command/critter/sec for 10 critters

    private Main() {
    }

    /**
     * Loads world and critters, then runs a real-time simulation.
     *
     * @param args optional [world-file] [critter-file] [num-critters]
     */
    public static void main(String[] args) {
        World world;
        Controller controller;
        String worldName = "Unnamed World";

        try {
            if (args.length > 0) {
                // Load world from file
                WorldLoader.WorldAndController wac = WorldLoader.loadFromFile(Path.of(args[0]));
                world = wac.world;
                controller = wac.controller;
                worldName = wac.name;

                // Optionally add critters from file
                if (args.length > 1) {
                    int numCritters = args.length > 2 ? Integer.parseInt(args[2]) : 1;
                    System.out.println("Adding " + numCritters + " critters from " + args[1]);
                    // Note: adding critters dynamically would require controller enhancement
                }
            } else {
                // Create default empty world
                System.out.println("No world file specified. Creating default 50x50 world.");
                world = new World(50, 50);
                controller = new Controller(world, new java.util.ArrayList<>());
                worldName = "Default Empty World";
            }

            // Run simulation with real-time rendering
            runSimulation(world, controller, worldName);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Runs the simulation, rendering every second until stopped.
     *
     * @param world the simulation world
     * @param controller the simulation controller
     * @param worldName name of the world
     */
    private static void runSimulation(World world, Controller controller, String worldName) {
        System.out.println("Toadally Awesome World Simulator");
        System.out.println("World: " + worldName);
        System.out.println("Size: " + world.getWidth() + " x " + world.getHeight());
        System.out.println("Critters: " + controller.getTurnOrder().size());
        System.out.println("Press Ctrl+C to stop\n");

        long stepCount = 0;
        long startTime = System.currentTimeMillis();
        long lastRenderTime = startTime;

        while (true) {
            // Advance one simulation step
            controller.step();
            stepCount++;

            // Sleep to control simulation speed
            try {
                Thread.sleep(MS_PER_STEP);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            // Render every RENDER_INTERVAL_MS
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastRenderTime >= RENDER_INTERVAL_MS) {
                lastRenderTime = currentTime;
                clearScreen();
                renderWorldState(world, controller, worldName, stepCount, currentTime - startTime);
            }
        }
    }

    /**
     * Renders the current world state as ASCII art.
     */
    private static void renderWorldState(World world, Controller controller, String worldName,
                                        long stepCount, long elapsedMs) {
        System.out.println("Time steps: " + stepCount);
        System.out.println("Critters alive: " + controller.getTurnOrder().size());
        System.out.println();
        
        printAsciiMap(world);
    }

    /**
     * Prints ASCII hex map with proper staggering according to hex grid layout.
     * Even rows (y even): indent 2 spaces, display cells at x=0,2,4,6...
     * Odd rows (y odd): indent 4 spaces, display cells at x=1,3,5,7...
     */
    private static void printAsciiMap(World world) {
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
                    printHexCell(world, x, y);
                }
            } else {
                // Odd row: print cells at x = 1, 3, 5, 7, ...
                for (int x = 1; x < displayWidth; x += 2) {
                    if (!first) System.out.print("   ");  // 3 spaces between cells
                    first = false;
                    printHexCell(world, x, y);
                }
            }
            System.out.println();
        }
    }

    /**
     * Prints a single hex cell content.
     */
    private static void printHexCell(World world, int x, int y) {
        if (!world.isValidCoordinate(x, y)) {
            System.out.print("#");
        } else {
            var hex = world.getHex(x, y);
            if (hex.isRock()) {
                System.out.print("#");
            } else if (hex.hasCritter()) {
                var c = world.getCritterAt(x, y);
                System.out.print(c.getDirection());
            } else if (hex.hasFood()) {
                System.out.print("F");
            } else {
                System.out.print("-");
            }
        }
    }

    /**
     * Formats elapsed time in ms to a readable string.
     */
    private static String formatTime(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }

    /**
     * Clears the console screen (works on most terminals).
     */
    private static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // If clear fails, just print separator lines
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }
}