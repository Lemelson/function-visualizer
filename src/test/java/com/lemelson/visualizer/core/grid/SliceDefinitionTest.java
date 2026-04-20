package com.lemelson.visualizer.core.grid;

import static org.junit.jupiter.api.Assertions.*;

import com.lemelson.visualizer.core.benchmark.BenchmarkFunction;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SliceDefinitionTest {

    @Test
    void createsValidSlice() {
        BenchmarkFunction f = testFunction(3);
        SliceDefinition slice = new SliceDefinition(f, 0, 1, new double[] { 1.0, 2.0, 3.0 });
        assertEquals(0, slice.xIndex());
        assertEquals(1, slice.yIndex());
    }

    @Test
    void createPointTemplateReturnsCopy() {
        double[] base = { 1.0, 2.0, 3.0 };
        SliceDefinition slice = new SliceDefinition(testFunction(3), 0, 1, base);

        double[] template1 = slice.createPointTemplate();
        double[] template2 = slice.createPointTemplate();

        assertNotSame(template1, template2, "Каждый вызов должен создавать новую копию");
        assertArrayEquals(template1, template2, 1e-10);
        assertArrayEquals(base, template1, 1e-10);
    }

    @Test
    void mutatingTemplateDoesNotAffectOriginal() {
        double[] base = { 1.0, 2.0, 3.0 };
        SliceDefinition slice = new SliceDefinition(testFunction(3), 0, 2, base);

        double[] template = slice.createPointTemplate();
        template[0] = 999.0;

        double[] fresh = slice.createPointTemplate();
        assertEquals(1.0, fresh[0], 1e-10, "Изменение шаблона не должно затрагивать оригинал");
    }

    @Test
    void rejectsNullFunction() {
        assertThrows(NullPointerException.class,
                () -> new SliceDefinition(null, 0, 1, new double[] { 0, 0 }));
    }

    @Test
    void rejectsNegativeXIndex() {
        assertThrows(IllegalArgumentException.class,
                () -> new SliceDefinition(testFunction(3), -1, 1, new double[] { 0, 0, 0 }));
    }

    @Test
    void rejectsXIndexOutOfBounds() {
        assertThrows(IllegalArgumentException.class,
                () -> new SliceDefinition(testFunction(3), 3, 1, new double[] { 0, 0, 0 }));
    }

    @Test
    void rejectsSameAxes() {
        assertThrows(IllegalArgumentException.class,
                () -> new SliceDefinition(testFunction(3), 1, 1, new double[] { 0, 0, 0 }));
    }

    @Test
    void rejectsWrongBasePointLength() {
        assertThrows(IllegalArgumentException.class,
                () -> new SliceDefinition(testFunction(3), 0, 1, new double[] { 0, 0 }));
    }

    @Test
    void rejectsNullBasePoint() {
        assertThrows(IllegalArgumentException.class,
                () -> new SliceDefinition(testFunction(3), 0, 1, null));
    }

    @Test
    void functionAccessorReturnsOriginal() {
        BenchmarkFunction f = testFunction(2);
        SliceDefinition slice = new SliceDefinition(f, 0, 1, new double[] { 0, 0 });
        assertSame(f, slice.function());
    }

    private BenchmarkFunction testFunction(int dim) {
        return new BenchmarkFunction() {
            @Override
            public String id() {
                return "TEST";
            }

            @Override
            public String displayName() {
                return "Test";
            }

            @Override
            public int dimension() {
                return dim;
            }

            @Override
            public double lowerBound(int index) {
                return -10;
            }

            @Override
            public double upperBound(int index) {
                return 10;
            }

            @Override
            public double globalMinimumValue() {
                return 0;
            }

            @Override
            public Optional<double[]> globalMinimumPoint() {
                return Optional.empty();
            }

            @Override
            public double evaluate(double[] x) {
                return 0;
            }
        };
    }
}
