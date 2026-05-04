package compiler.simulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Global simulation constants loaded from constants.txt.
 *
 * The file format is one constant per line:
 * NAME VALUE {description}
 */
public final class Constants {
    // Combat and energy mechanics
    public static final double BASE_DAMAGE;
    public static final double DAMAGE_INC;
    public static final int ENERGY_PER_SIZE;
    public static final int FOOD_PER_SIZE;

    // World parameters
    public static final int MAX_SMELL_DISTANCE;
    public static final int ROCK_VALUE;
    public static final int WIDTH;
    public static final int HEIGHT;

    // Action limits
    public static final int MAX_RULES_PER_TURN;
    public static final int SOLAR_FLUX;
    public static final int MOVE_COST;
    public static final int ATTACK_COST;
    public static final int GROW_COST;
    public static final int BUD_COST;

    // Complexity calculation
    public static final int RULE_COST;
    public static final int ABILITY_COST;

    // Critter defaults
    public static final int INITIAL_ENERGY;
    public static final int MIN_MEMORY;

    // Manna spawning
    public static final int MANNA_COUNT;
    public static final int MANNA_AMOUNT;

    // Mutations
    public static final double MUTATION_PROBABILITY;

    static {
        try {
            Map<String, String> values = loadConstants();
            BASE_DAMAGE = Double.parseDouble(values.getOrDefault("BASE_DAMAGE", "100"));
            DAMAGE_INC = Double.parseDouble(values.getOrDefault("DAMAGE_INC", "0.2"));
            ENERGY_PER_SIZE = Integer.parseInt(values.getOrDefault("ENERGY_PER_SIZE", "500"));
            FOOD_PER_SIZE = Integer.parseInt(values.getOrDefault("FOOD_PER_SIZE", "200"));
            MAX_SMELL_DISTANCE = Integer.parseInt(values.getOrDefault("MAX_SMELL_DISTANCE", "10"));
            ROCK_VALUE = Integer.parseInt(values.getOrDefault("ROCK_VALUE", "-1"));
            WIDTH = Integer.parseInt(values.getOrDefault("WIDTH", "50"));
            HEIGHT = Integer.parseInt(values.getOrDefault("HEIGHT", "87"));
            MAX_RULES_PER_TURN = Integer.parseInt(values.getOrDefault("MAX_RULES_PER_TURN", "999"));
            SOLAR_FLUX = Integer.parseInt(values.getOrDefault("SOLAR_FLUX", "1"));
            MOVE_COST = Integer.parseInt(values.getOrDefault("MOVE_COST", "3"));
            ATTACK_COST = Integer.parseInt(values.getOrDefault("ATTACK_COST", "5"));
            GROW_COST = Integer.parseInt(values.getOrDefault("GROW_COST", "1"));
            BUD_COST = Integer.parseInt(values.getOrDefault("BUD_COST", "9"));
            RULE_COST = Integer.parseInt(values.getOrDefault("RULE_COST", "2"));
            ABILITY_COST = Integer.parseInt(values.getOrDefault("ABILITY_COST", "25"));
            INITIAL_ENERGY = Integer.parseInt(values.getOrDefault("INITIAL_ENERGY", "250"));
            MIN_MEMORY = Integer.parseInt(values.getOrDefault("MIN_MEMORY", "7"));
            MANNA_COUNT = Integer.parseInt(values.getOrDefault("MANNA_COUNT", "1"));
            MANNA_AMOUNT = Integer.parseInt(values.getOrDefault("MANNA_AMOUNT", "10"));
            MUTATION_PROBABILITY = Double.parseDouble(values.getOrDefault("MUTATION_PROBABILITY", "0.25"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load constants.txt", e);
        }
    }

    private static Map<String, String> loadConstants() throws IOException {
        Map<String, String> constants = new HashMap<>();
        
        Path path = Paths.get("constants.txt");
        if (!Files.exists(path)) {
            throw new IOException("constants.txt not found at " + path.toAbsolutePath());
        }
        
        for (String line : Files.readAllLines(path)) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            
            int firstSpace = line.indexOf(' ');
            if (firstSpace <= 0) {
                continue;
            }
            
            String name = line.substring(0, firstSpace).trim();
            String rest = line.substring(firstSpace + 1).trim();
            
            int bracketIndex = rest.indexOf('{');
            String value;
            if (bracketIndex > 0) {
                value = rest.substring(0, bracketIndex).trim();
            } else {
                value = rest;
            }
            
            if (!value.isEmpty()) {
                constants.put(name, value);
            }
        }
        
        return constants;
    }

    private Constants() {
        // Utility class
    }
}
