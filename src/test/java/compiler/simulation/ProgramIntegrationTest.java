package compiler.simulation;

import compiler.ast.Program;
import compiler.parser.Parser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProgramIntegrationTest {
    @Test
    void noActionProgramHaltsAfter999Passes() {
        Program program = new Parser("1 = 1 --> mem[6] := 1;").parseProgram();
        World world = new World(5, 5);
        ProgramCritterInterpreter interpreter = new ProgramCritterInterpreter(program);
        Critter critter = new Critter(0, 0, 1, 10, interpreter);

        world.placeCritter(critter, 0, 0);

        Action action = interpreter.interpret(critter, world);

        assertEquals(ActionType.WAIT, action.getType());
        assertEquals(999, interpreter.getMemoryValue(5));
    }

    @Test
    void deadCritterIsRemovedAndReplacedWithFoodOnNextStep() {
        Program program = new Parser("1 = 1 --> left;").parseProgram();
        World world = new World(5, 5);
        Critter critter = new Critter(0, 0, 1, 1, program);

        world.placeCritter(critter, 0, 0);

        Controller controller = new Controller(world, List.of(critter));
        controller.step();

        assertTrue(controller.getTurnOrder().isEmpty());
        assertTrue(world.getHex(0, 0).hasFood());
        assertTrue(world.getHex(0, 0).getFoodAmount() > 0);
    }
}