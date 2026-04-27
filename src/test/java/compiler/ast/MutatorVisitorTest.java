package compiler.ast;

import compiler.parser.Parser;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

//AI-Generated
public class MutatorVisitorTest {
    @Test
    void mutationCreatesNewRootAndSharesUnchangedBranches() {
        String source = "1 = 1 --> wait; " +
            "nearby[1] = 0 --> forward; " +
            "ahead[1] > 3 --> left;";

        Program originalRoot = new Parser(source).parseProgram();
        Program mutatedRoot = mutateUntilChanged(originalRoot);

        assertNotSame(originalRoot, mutatedRoot, "Mutation must produce a new root instance.");

        List<Rule> originalRules = originalRoot.getRules();
        List<Rule> mutatedRules = mutatedRoot.getRules();

        boolean foundSharedBranch = false;
        boolean foundChangedBranch = false;
        int commonPrefix = Math.min(originalRules.size(), mutatedRules.size());

        for (int i = 0; i < commonPrefix; i++) {
            if (originalRules.get(i) == mutatedRules.get(i)) {
                foundSharedBranch = true;
            } else {
                foundChangedBranch = true;
            }
        }

        if (originalRules.size() != mutatedRules.size()) {
            foundChangedBranch = true;
        }

        assertTrue(foundSharedBranch, "Unmodified branches should retain identical references.");
        assertTrue(foundChangedBranch, "At least one branch should differ after mutation.");
    }

    //AI-Generated
    private Program mutateUntilChanged(Program root) {
        Random random = new Random(0); //AI-Generated
        for (int attempt = 0; attempt < 2000; attempt++) {
            Program mutated = MutatorVisitor.mutate(root, random);
            if (mutated != root) {
                return mutated;
            }
        }
        throw new AssertionError("Failed to produce a mutation after bounded attempts.");
    }
}
