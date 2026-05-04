package compiler.simulation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorldTest {
    @Test
    void outOfBoundsCoordinatesActLikeRock() {
        World world = new World(5, 5);

        assertTrue(world.getHex(-1, 0).isRock());
        assertTrue(world.getHex(0, -1).isRock());
        assertTrue(world.getHex(5, 0).isRock());
        assertTrue(world.getHex(1, 0).isRock());
    }

    @Test
    void placingFoodOnInvalidCoordinatesIsRejected() {
        World world = new World(5, 5);

        assertThrows(IllegalArgumentException.class, () -> world.placeFood(1, 0, 3));
    }
}