package simulation;

import ast.Parser;
import ast.ProgramCritterInterpreter;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SmellSensorTest {

    private int getSmell(World world, Critter critter) {
        critter.setMemorySize(8);
        ProgramCritterInterpreter interpreter = new ProgramCritterInterpreter(
            new Parser("1=1-->wait and mem[7]:=smell;").parseProgram()
        );
        interpreter.interpret(critter, world);
        return critter.readMemory(7);
    }

    @Test
    void smellReturnsCorrectRelativeDirection() {
        World world = new World(20, 20);
        world.placeFood(10, 10, 10); // Center food point

        // Case 1: Food is straight ahead (Relative Action Code 0)
        Critter critter1 = new Critter(4, 10, 0, 100, new Parser("1=1-->wait;").parseProgram());
        world.placeCritter(critter1, 10, 8);
        int smell1 = getSmell(world, critter1);
        assertTrue(smell1 >= 0, "Scent should be detected");
        assertEquals(0, smell1 % 10, "Food should be straight ahead (0)");

        // Case 2: Food requires turning RIGHT to navigate (Relative Action Code 1)
        Critter critter2 = new Critter(4, 10, 0, 100, new Parser("1=1-->wait;").parseProgram());
        world.placeCritter(critter2, 9, 9);
        int smell2 = getSmell(world, critter2);
        assertTrue(smell2 >= 0, "Scent should be detected");
        assertEquals(1, smell2 % 10, "Path target should require turning left (1)");

        // Case 3: Food requires moving backward (Relative Action Code 3)
        Critter critter3 = new Critter(4, 10, 0, 100, new Parser("1=1-->wait;").parseProgram());
        world.placeCritter(critter3, 10, 12);
        int smell3 = getSmell(world, critter3);
        assertTrue(smell3 >= 0, "Scent should be detected");
        assertEquals(3, smell3 % 10, "Path target should require moving backward (3)");

        // Case 4: Food requires turning LEFT to navigate (Relative Action Code 5)
        Critter critter4 = new Critter(4, 10, 0, 100, new Parser("1=1-->wait;").parseProgram());
        world.placeCritter(critter4, 11, 9);
        int smell4 = getSmell(world, critter4);
        assertTrue(smell4 >= 0, "Scent should be detected");
        assertEquals(5, smell4 % 10, "Path target should require turning left (5)");
    }

    @Test
    void smellReturnsMinusOneWhenNoFoodInRadius() {
        World world = new World(30, 30);
        // Place a critter far away from any food source (> 10 hexes)
        Critter isolatedCritter = new Critter(4, 2, 0, 100, new Parser("1=1-->wait;").parseProgram());
        world.placeCritter(isolatedCritter, 2, 2);
        world.placeFood(25, 25, 10);

        int smellValue = getSmell(world, isolatedCritter);
        assertEquals(-1, smellValue, "Should return -1 when food is outside MAX_SMELL_DISTANCE");
    }
}