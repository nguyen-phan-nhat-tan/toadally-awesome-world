package compiler.ast;

import compiler.ast.ASTVisitor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for ASTNodeUtils - the consolidated utility for common AST operations.
 * Verifies that the DRY principle fix for null-check patterns works correctly.
 * //AI-Generated - DRY principle test suite
 */
@DisplayName("AST Node Utils Tests")
public class ASTNodeUtilsTest {

    /**
     * Minimal concrete ASTNode subclass for testing purposes.
     */
    private static class TestNode extends ASTNode {
        public TestNode(int line, int column) {
            super(line, column);
        }

        @Override
        public List<ASTNode> getChildren() {
            return List.of();
        }

        @Override
        public <T> T accept(ASTVisitor<T> visitor) {
            return null;
        }
    }

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
        ASTNode dummyNode = new TestNode(1, 1);

        List<ASTNode> result = ASTNodeUtils.toChildList(dummyNode);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(dummyNode, result.get(0));
    }

    @Test
    @DisplayName("toChildList returns immutable list")
    void testToChildListImmutable() {
        ASTNode dummyNode = new TestNode(1, 1);

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
            () -> result.add(new TestNode(1, 1)),
            "toChildList should return an immutable list even for null input");
    }
}
