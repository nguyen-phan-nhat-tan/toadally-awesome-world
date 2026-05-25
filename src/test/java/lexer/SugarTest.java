package lexer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for Sugar enum null-safety and error handling improvements.
 * Verifies that the enum provides explicit Optional-based API instead of
 * swallowing exceptions with null returns.
 */
@DisplayName("Sugar Enum Safety Tests")
public class SugarTest {

    @Test
    @DisplayName("findSugarIndex returns correct index for valid sugar names")
    void testFindSugarIndexValidNames() {
        assertEquals(Optional.of(0), SugarTokenType.findSugarIndex("MEMSIZE"));
        assertEquals(Optional.of(1), SugarTokenType.findSugarIndex("DEFENSE"));
        assertEquals(Optional.of(2), SugarTokenType.findSugarIndex("OFFENSE"));
        assertEquals(Optional.of(3), SugarTokenType.findSugarIndex("SIZE"));
        assertEquals(Optional.of(4), SugarTokenType.findSugarIndex("ENERGY"));
        assertEquals(Optional.of(5), SugarTokenType.findSugarIndex("PASS"));
        assertEquals(Optional.of(6), SugarTokenType.findSugarIndex("POSTURE"));
    }

    @Test
    @DisplayName("findSugarIndex handles case-insensitive lookup")
    void testFindSugarIndexCaseInsensitive() {
        assertEquals(Optional.of(0), SugarTokenType.findSugarIndex("memsize"));
        assertEquals(Optional.of(1), SugarTokenType.findSugarIndex("Defense"));
        assertEquals(Optional.of(2), SugarTokenType.findSugarIndex("OFFENSE"));
        assertEquals(Optional.of(3), SugarTokenType.findSugarIndex("size"));
    }

    @Test
    @DisplayName("findSugarIndex returns empty Optional for non-sugar names")
    void testFindSugarIndexInvalidNames() {
        assertTrue(SugarTokenType.findSugarIndex("INVALID").isEmpty());
        assertTrue(SugarTokenType.findSugarIndex("NOTASUGAR").isEmpty());
        assertTrue(SugarTokenType.findSugarIndex("").isEmpty());
        assertTrue(SugarTokenType.findSugarIndex("123").isEmpty());
    }

    @Test
    @DisplayName("findSugarIndex throws on null input (explicit error)")
    void testFindSugarIndexNullInput() {
        assertThrows(Exception.class, 
            () -> SugarTokenType.findSugarIndex(null),
            "findSugarIndex should throw on null input (fail-loud)");
    }

    @Test
    @DisplayName("getIndexSugar backward compatibility method works")
    void testGetIndexSugarBackwardCompatibility() {
        assertEquals(Integer.valueOf(0), SugarTokenType.getIndexSugar("MEMSIZE"));
        assertEquals(Integer.valueOf(1), SugarTokenType.getIndexSugar("DEFENSE"));
        assertNull(SugarTokenType.getIndexSugar("INVALID"));
    }

    @Test
    @DisplayName("Sugar.MEMSIZE has correct properties")
    void testMemsizeProperties() {
        assertFalse(SugarTokenType.MEMSIZE.isAssignable());
        assertEquals(0, SugarTokenType.MEMSIZE.getIndex());
    }

    @Test
    @DisplayName("Sugar.POSTURE is the only assignable slot")
    void testPostureAssignable() {
        assertTrue(SugarTokenType.POSTURE.isAssignable());
        assertEquals(6, SugarTokenType.POSTURE.getIndex());
    }

    @Test
    @DisplayName("isAssignableSugar returns correct values for all slots")
    void testIsAssignableSugar() {
        assertFalse(SugarTokenType.isAssignableSugar(0)); // MEMSIZE
        assertFalse(SugarTokenType.isAssignableSugar(1)); // DEFENSE
        assertFalse(SugarTokenType.isAssignableSugar(2)); // OFFENSE
        assertFalse(SugarTokenType.isAssignableSugar(3)); // SIZE
        assertFalse(SugarTokenType.isAssignableSugar(4)); // ENERGY
        assertFalse(SugarTokenType.isAssignableSugar(5)); // PASS
        assertTrue(SugarTokenType.isAssignableSugar(6));  // POSTURE
        assertTrue(SugarTokenType.isAssignableSugar(7));  // Beyond sugar slots
        assertTrue(SugarTokenType.isAssignableSugar(100)); // Far beyond
    }
}
