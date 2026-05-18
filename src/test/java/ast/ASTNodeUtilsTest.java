package ast;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for ASTNodeUtils - the consolidated utility for common AST operations.
 * Verifies that the DRY principle fix for null-check patterns works correctly.
 */
@DisplayName("AST Node Utils Tests")
public class ASTNodeUtilsTest {

    @Test
    @DisplayName("toChildList returns empty list for null child")
    void testToChildListNullChild() {
        List<ASTNode> result = ASTNodeUtils.toChildList(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("toChildList returns single-element list for non-null child")
    void testToChildListNonNullChild() {
        ASTNode dummyNode = new NumberNode(1, 1, 1);

        List<ASTNode> result = ASTNodeUtils.toChildList(dummyNode);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(dummyNode, result.get(0));
    }

    @Test
    @DisplayName("toChildList returns immutable list")
    void testToChildListImmutable() {
        ASTNode dummyNode = new NumberNode(1, 1, 1);

        List<ASTNode> result = ASTNodeUtils.toChildList(dummyNode);
        
        // Attempting to modify should throw UnsupportedOperationException
        assertThrows(UnsupportedOperationException.class, 
            () -> result.add(dummyNode),
            "toChildList should return an immutable list");
    }

    @Test
    @DisplayName("toChildList empty result is immutable for null input")
    void testToChildListEmptyImmutable() {
        List<ASTNode> result = ASTNodeUtils.toChildList(null);
        
        assertThrows(UnsupportedOperationException.class, 
            () -> result.add(new NumberNode(1, 1, 1)),
            "toChildList should return an immutable list even for null input");
    }
}
