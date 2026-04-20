package com.lemelson.visualizer.core.optimization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ExpressionConstraintParserTest {

    @Test
    void parsesMixedConstraints() {
        ExpressionConstraintParser parser = new ExpressionConstraintParser();

        List<Constraint> constraints = parser.parse("""
                x1 + x2 <= 1
                x1 - x2 >= 0
                x2 = 0.5
                """, 2, 1e-3);

        assertEquals(3, constraints.size());
        assertEquals(ConstraintType.INEQUALITY, constraints.get(0).type());
        assertEquals(ConstraintType.INEQUALITY, constraints.get(1).type());
        assertEquals(ConstraintType.EQUALITY, constraints.get(2).type());
    }

    @Test
    void computesViolationsCorrectly() {
        ExpressionConstraintParser parser = new ExpressionConstraintParser();
        List<Constraint> constraints = parser.parse("x1 + x2 <= 1; x1 - x2 >= 0", 2, 1e-4);

        assertTrue(constraints.get(0).isSatisfied(new double[] {0.2, 0.3}));
        assertFalse(constraints.get(0).isSatisfied(new double[] {0.8, 0.6}));
        assertTrue(constraints.get(1).isSatisfied(new double[] {0.8, 0.2}));
        assertFalse(constraints.get(1).isSatisfied(new double[] {0.1, 0.7}));
    }

    @Test
    void rejectsUnknownVariables() {
        ExpressionConstraintParser parser = new ExpressionConstraintParser();
        assertThrows(IllegalArgumentException.class, () -> parser.parse("x3 <= 1", 2, 1e-4));
    }
}
