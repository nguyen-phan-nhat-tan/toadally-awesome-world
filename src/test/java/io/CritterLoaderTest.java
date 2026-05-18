package io;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simulation.Critter;
import simulation.ProgramCritterInterpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CritterLoaderTest {

    @Test
    @DisplayName("Parses metadata and program separately with mixed header format")
    void parsesHeaderAndProgramSeparately() {
        String content = """
            Species : bouncer
            memsize: 8 // custom memory slot
            defense : 2
            OFFENSE: 3
            size: 2
            energy: 450
            posture: 5

            mem[7] < 2 --> right;
            1 = 1 --> forward;
            """;

        Critter critter = CritterLoader.parseContent(content);

        assertEquals(8, critter.getMemorySize());
        assertEquals(2, critter.getDefense());
        assertEquals(3, critter.getOffense());
        assertEquals(2, critter.getSize());
        assertEquals(450, critter.getEnergy());
        assertEquals(5, critter.getPosture());

        assertTrue(critter.getCritterInterpreter() instanceof ProgramCritterInterpreter);
        ProgramCritterInterpreter interpreter = (ProgramCritterInterpreter) critter.getCritterInterpreter();
        assertEquals(2, interpreter.getRuleCount());
    }
}
