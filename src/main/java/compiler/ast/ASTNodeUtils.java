package compiler.ast;

import java.util.List;

/**
 * Utility methods for common AST node operations.
 * Consolidates duplicated patterns like null-safe child list construction.
 */
public final class ASTNodeUtils {
    private ASTNodeUtils() {
        // Utility class - no instances
    }

    /**
     * Safely constructs a child list from an optional node.
     * Used by nodes that have zero or one optional child.
     *
     * @param child the optional child node, may be null
     * @return immutable list containing the child if non-null, otherwise empty list
     */
    public static List<ASTNode> toChildList(ASTNode child) {
        return child == null ? List.of() : List.of(child);
    }
}
