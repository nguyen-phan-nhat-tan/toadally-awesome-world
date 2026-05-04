package compiler.simulation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ControllerTest {
    @Test
    void sequentialTurnsDeductEnergyEvenWhenSecondMoveFails() {
        World world = new World(5, 5);

        Critter first = new Critter(0, 0, 1, 10, (critter, runtimeWorld) -> new Action(ActionType.FORWARD));
        Critter second = new Critter(2, 2, 4, 10, (critter, runtimeWorld) -> new Action(ActionType.FORWARD));

        world.placeCritter(first, 0, 0);
        world.placeCritter(second, 2, 2);

        Controller controller = new Controller(world, java.util.List.of(first, second));
        controller.step();

        assertEquals(1, first.getX());
        assertEquals(1, first.getY());
        assertEquals(2, second.getX());
        assertEquals(2, second.getY());
        assertEquals(9, first.getEnergy());
        assertEquals(9, second.getEnergy());
    }

    @Test
    void waitGainsSolarEnergy() {
        World world = new World(5, 5);
        Critter critter = new Critter(0, 0, 0, 5, (c, w) -> new Action(ActionType.WAIT));
        world.placeCritter(critter, 0, 0);

        Controller controller = new Controller(world, java.util.List.of(critter));
        controller.step();

        assertEquals(6, critter.getEnergy());
    }

    @Test
    void eatConsumesFoodAhead() {
        World world = new World(5, 5);
        Critter critter = new Critter(0, 0, 0, 10, (c, w) -> new Action(ActionType.EAT));
        world.placeCritter(critter, 0, 0);

        HexCoordinate ahead = world.neighbor(0, 0, HexDirection.NORTH);
        world.placeFood(ahead.x(), ahead.y(), 4);

        Controller controller = new Controller(world, java.util.List.of(critter));
        controller.step();

        assertEquals(13, critter.getEnergy());
        assertFalse(world.getHex(ahead.x(), ahead.y()).hasFood());
    }

    @Test
    void serveTransfersEnergyIntoFoodAhead() {
        World world = new World(5, 5);
        Critter critter = new Critter(0, 0, 0, 10, (c, w) -> new Action(ActionType.SERVE, 3));
        world.placeCritter(critter, 0, 0);

        HexCoordinate ahead = world.neighbor(0, 0, HexDirection.NORTH);

        Controller controller = new Controller(world, java.util.List.of(critter));
        controller.step();

        assertEquals(6, critter.getEnergy());
        assertTrue(world.getHex(ahead.x(), ahead.y()).hasFood());
        assertEquals(3, world.getHex(ahead.x(), ahead.y()).getFoodAmount());
    }

    @Test
    void attackKillsCritterAhead() {
        World world = new World(5, 5);
        Critter attacker = new Critter(0, 0, 0, 10, (c, w) -> new Action(ActionType.ATTACK));
        world.placeCritter(attacker, 0, 0);

        HexCoordinate ahead = world.neighbor(0, 0, HexDirection.NORTH);
        Critter target = new Critter(ahead.x(), ahead.y(), 3, 10, (c, w) -> new Action(ActionType.WAIT));
        world.placeCritter(target, ahead.x(), ahead.y());

        Controller controller = new Controller(world, java.util.List.of(attacker, target));
        controller.step();

        assertTrue(target.isDead());
        assertTrue(world.getHex(ahead.x(), ahead.y()).hasFood());
    }

    @Test
    void growIncreasesSize() {
        World world = new World(5, 5);
        Critter critter = new Critter(0, 0, 0, 10, (c, w) -> new Action(ActionType.GROW));
        world.placeCritter(critter, 0, 0);

        Controller controller = new Controller(world, java.util.List.of(critter));
        controller.step();

        assertEquals(2, critter.getSize());
        assertEquals(9, critter.getEnergy());
    }

    @Test
    void budCreatesOffspringBehindAndSchedulesNextTurn() {
        World world = new World(7, 7);
        Critter parent = new Critter(2, 2, 0, 10, (c, w) -> new Action(ActionType.BUD));
        world.placeCritter(parent, 2, 2);

        HexCoordinate behind = world.neighbor(2, 2, HexDirection.NORTH.opposite());

        Controller controller = new Controller(world, java.util.List.of(parent));
        controller.step();

        Critter child = world.getCritterAt(behind.x(), behind.y());
        assertNotNull(child);
        assertTrue(child.isAlive());
        assertEquals(250, child.getEnergy());
        assertEquals(2, controller.getTurnOrder().size());
    }
}