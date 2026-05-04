package compiler.io;

import compiler.simulation.Controller;
import compiler.simulation.Critter;
import compiler.simulation.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Loads world definitions from text files.
 *
 * File format:
 * name <world name>
 * size <width> <height>
 * rock <column> <row>
 * food <column> <row> <amount>
 * critter <critter file> <column> <row> <direction>
 */
public class WorldLoader {
    private static final int DEFAULT_WIDTH = 50;
    private static final int DEFAULT_HEIGHT = 50;
    private static final String DEFAULT_NAME = "Unnamed World";
    private static final Random random = new Random();

    /**
     * Loads a world from a file at the given path.
     *
     * @param path path to the world file
     * @return loaded world with controller
     * @throws IOException if file cannot be read
     */
    public static WorldAndController loadFromFile(Path path) throws IOException {
        String content = new String(Files.readAllBytes(path));
        return parseContent(content, path.getParent());
    }

    /**
     * Parses world specification from text content.
     *
     * @param content text content of world file
     * @param baseDir base directory for resolving critter files
     * @return world and controller
     */
    public static WorldAndController parseContent(String content, Path baseDir) {
        String worldName = DEFAULT_NAME;
        int width = DEFAULT_WIDTH;
        int height = DEFAULT_HEIGHT;
        List<Critter> critters = new ArrayList<>();

        Scanner scanner = new Scanner(content);
        
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();

            // Skip blank lines and comments
            if (line.isEmpty() || line.startsWith("//")) {
                continue;
            }

            if (line.startsWith("name ")) {
                worldName = line.substring(5).trim();
            } else if (line.startsWith("size ")) {
                String[] parts = line.substring(5).trim().split("\\s+");
                try {
                    if (parts.length >= 2) {
                        width = Integer.parseInt(parts[0]);
                        height = Integer.parseInt(parts[1]);
                        if (width <= 0 || height <= 0) {
                            System.err.println("Warning: invalid world size; using defaults");
                            width = DEFAULT_WIDTH;
                            height = DEFAULT_HEIGHT;
                        }
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Warning: unable to parse world size; using defaults");
                    width = DEFAULT_WIDTH;
                    height = DEFAULT_HEIGHT;
                }
            } else if (line.startsWith("critter ")) {
                Critter critter = parseCritter(line, baseDir);
                if (critter != null) {
                    critters.add(critter);
                }
            }
        }
        scanner.close();

        // Create world and add rocks/food by re-parsing
        World world = new World(width, height);
        scanner = new Scanner(content);
        
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();

            // Skip blank lines and comments
            if (line.isEmpty() || line.startsWith("//")) {
                continue;
            }

            if (line.startsWith("rock ")) {
                parseRock(line, world);
            } else if (line.startsWith("food ")) {
                parseFood(line, world);
            }
        }
        scanner.close();
        
        // Place critters on the world
        for (Critter critter : critters) {
            world.placeCritter(critter, critter.getX(), critter.getY());
        }
        
        Controller controller = new Controller(world, critters);
        
        System.out.println("Loaded world: " + worldName);
        System.out.println("  Size: " + width + " x " + height);
        System.out.println("  Critters: " + critters.size());

        return new WorldAndController(worldName, world, controller);
    }

    private static void parseRock(String line, World world) {
        String[] parts = line.substring(5).trim().split("\\s+");
        try {
            if (parts.length >= 2) {
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                if (world.isValidCoordinate(x, y)) {
                    world.placeRock(x, y);
                } else {
                    System.err.println("Warning: rock at invalid coordinate (" + x + ", " + y + ")");
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("Warning: unable to parse rock coordinates");
        }
    }

    private static void parseFood(String line, World world) {
        String[] parts = line.substring(5).trim().split("\\s+");
        try {
            if (parts.length >= 3) {
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                int amount = Integer.parseInt(parts[2]);
                if (world.isValidCoordinate(x, y) && amount > 0) {
                    world.placeFood(x, y, amount);
                } else {
                    System.err.println("Warning: invalid food placement at (" + x + ", " + y + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: unable to parse food line");
        }
    }

    private static Critter parseCritter(String line, Path baseDir) {
        String[] parts = line.substring(8).trim().split("\\s+");
        try {
            if (parts.length >= 4) {
                String filename = parts[0];
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                int direction = Integer.parseInt(parts[3]);

                // Load critter from file
                Path critterFile = baseDir != null ? baseDir.resolve(filename) : Path.of(filename);
                Critter critter = CritterLoader.loadFromFile(critterFile);
                critter.moveTo(x, y);
                critter.setDirection(direction);
                
                return critter;
            }
        } catch (Exception e) {
            System.err.println("Warning: unable to load critter: " + e.getMessage());
        }
        return null;
    }

    /**
     * Result of loading a world: name, world, and controller.
     */
    public static class WorldAndController {
        public final String name;
        public final World world;
        public final Controller controller;

        WorldAndController(String name, World world, Controller controller) {
            this.name = name;
            this.world = world;
            this.controller = controller;
        }
    }
}
