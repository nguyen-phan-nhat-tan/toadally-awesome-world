package compiler.lexer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for Sugar enum null-safety and error handling improvements.
 * Verifies that the enum provides explicit Optional-based API instead of
 * swallowing exceptions with null returns.
 * //AI-Generated - Error handling test suite
 */
@DisplayName("Sugar Enum Safety Tests")
public class SugarTest {

    @Test
    @DisplayName("findSugarIndex returns correct index for valid sugar names")
    void testFindSugarIndexValidNames() {
        assertEquals(Optional.of(0), Sugar.findSugarIndex("MEMSIZE"));
        assertEquals(Optional.of(1), Sugar.findSugarIndex("DEFENSE"));
        assertEquals(Optional.of(2), Sugar.findSugarIndex("OFFENSE"));
        assertEquals(Optional.of(3), Sugar.findSugarIndex("SIZE"));
        assertEquals(Optional.of(4), Sugar.findSugarIndex("ENERGY"));
        assertEquals(Optional.of(5), Sugar.findSugarIndex("PASS"));
        assertEquals(Optional.of(6), Sugar.findSugarIndex("POSTURE"));
    }

    @Test
    @DisplayName("findSugarIndex handles case-insensitive lookup")
    void testFindSugarIndexCaseInsensitive() {
        assertEquals(Optional.of(0), Sugar.findSugarIndex("memsize"));
        assertEquals(Optional.of(1), Sugar.findSugarIndex("Defense"));
        assertEquals(Optional.of(2), Sugar.findSugarIndex("OFFENSE"));
        assertEquals(Optional.of(3), Sugar.findSugarIndex("size"));
    }

    @Test
    @DisplayName("findSugarIndex returns empty Optional for non-sugar names")
    void testFindSugarIndexInvalidNames() {
        assertTrue(Sugar.findSugarIndex("INVALID").isEmpty());
        assertTrue(Sugar.findSugarIndex("NOTASUGAR").isEmpty());
        assertTrue(Sugar.findSugarIndex("").isEmpty());
        assertTrue(Sugar.findSugarIndex("123").isEmpty());
    }

    @Test
    @DisplayName("findSugarIndex throws on null input (explicit error)")
    void testFindSugarIndexNullInput() {
        assertThrows(Exception.class, 
            () -> Sugar.findSugarIndex(null),
            "findSugarIndex should throw on null input (fail-loud)");
    }

    @Test
    @DisplayName("getIndexSugar backward compatibility method works")
    void testGetIndexSugarBackwardCompatibility() {
        assertEquals(Integer.valueOf(0), Sugar.getIndexSugar("MEMSIZE"));
        assertEquals(Integer.valueOf(1), Sugar.getIndexSugar("DEFENSE"));
        assertNull(Sugar.getIndexSugar("INVALID"));
    }

    @Test
    @DisplayName("Sugar.MEMSIZE has correct properties")
    void testMemsizeProperties() {
        assertFalse(Sugar.MEMSIZE.isAssignable());
        assertEquals(0, Sugar.MEMSIZE.getIndex());
    }

    @Test
    @DisplayName("Sugar.POSTURE is the only assignable slot")
    void testPostureAssignable() {
        assertTrue(Sugar.POSTURE.isAssignable());
        assertEquals(6, Sugar.POSTURE.getIndex());
    }

    @Test
    @DisplayName("isAssignableSugar returns correct values for all slots")
    void testIsAssignableSugar() {
        assertFalse(Sugar.isAssignableSugar(0)); // MEMSIZE
        assertFalse(Sugar.isAssignableSugar(1)); // DEFENSE
        assertFalse(Sugar.isAssignableSugar(2)); // OFFENSE
        assertFalse(Sugar.isAssignableSugar(3)); // SIZE
        assertFalse(Sugar.isAssignableSugar(4)); // ENERGY
        assertFalse(Sugar.isAssignableSugar(5)); // PASS
        assertTrue(Sugar.isAssignableSugar(6));  // POSTURE
        assertTrue(Sugar.isAssignableSugar(7));  // Beyond sugar slots
        assertTrue(Sugar.isAssignableSugar(100)); // Far beyond
    }
}
