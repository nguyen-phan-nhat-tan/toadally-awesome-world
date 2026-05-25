package ast;

import java.util.List;

/**
 * Utility methods for common AST node operations.
 * Consolidates duplicated patterns like null-safe child list construction.
 */
public final class ASTNodeUtils {
    private ASTNodeUtils(){

    }

    /**
     * Safely constructs a child list from an optional node.
     * Used by nodes that have zero or one optional child.
     *
     * @param child the optional child node, may be null
     * @return immutable list containing the child if non-null, otherwise empty list
     */
    public static List<ASTNode> toChildList(ASTNode child){
        return child == null ? List.of() : List.of(child);
    }

    /**
     * Safely constructs an immutable child list from multiple nodes, filtering out nulls.
     *
     * @param children varargs of child nodes, may contain nulls
     * @return immutable list containing only the non-null children
     */
    public static List<ASTNode> toChildList(ASTNode... children) {
        if (children == null || children.length == 0) {
            return List.of();
        }
        return java.util.Arrays.stream(children)
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}
