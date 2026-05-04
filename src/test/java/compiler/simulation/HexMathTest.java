package compiler.simulation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HexMathTest {
    @Test
    void coordinateValidationRequiresEvenParity() {
        assertTrue(HexMath.isValidCoordinate(2, 2));
        assertFalse(HexMath.isValidCoordinate(1, 2));
        assertFalse(HexMath.isValidCoordinate(-2, 2));
    }

    @Test
    void distanceMatchesDoubledCoordinateFormula() {
        assertEquals(2, HexMath.distance(0, 0, 2, 0));
        assertEquals(1, HexMath.distance(0, 0, 1, 1));
        assertEquals(3, HexMath.distance(0, 0, 3, 3));
    }

    @Test
    void directionRotationsWrapAroundTheSixHexFacets() {
        assertEquals(HexDirection.NORTHEAST, HexDirection.NORTH.rotateRight());
        assertEquals(HexDirection.NORTHWEST, HexDirection.NORTH.rotateLeft());
        assertEquals(HexDirection.SOUTH, HexDirection.NORTH.opposite());
        assertEquals(HexDirection.NORTH, HexDirection.fromIndex(6));
    }
}