package com.lemelson.visualizer.core.grid;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GridSpecTest {

    @Test
    void validSpecCreation() {
        GridSpec spec = new GridSpec(0, 10, 0, 5, 100, 50);
        assertEquals(0, spec.xmin(), 1e-10);
        assertEquals(10, spec.xmax(), 1e-10);
        assertEquals(0, spec.ymin(), 1e-10);
        assertEquals(5, spec.ymax(), 1e-10);
        assertEquals(100, spec.nx());
        assertEquals(50, spec.ny());
    }

    @Test
    void xAtReturnsCorrectValues() {
        GridSpec spec = new GridSpec(0, 10, 0, 5, 11, 6);
        assertEquals(0.0, spec.xAt(0), 1e-10);
        assertEquals(10.0, spec.xAt(10), 1e-10);
        assertEquals(5.0, spec.xAt(5), 1e-10);
    }

    @Test
    void yAtReturnsCorrectValues() {
        GridSpec spec = new GridSpec(0, 10, 0, 5, 11, 6);
        assertEquals(0.0, spec.yAt(0), 1e-10);
        assertEquals(5.0, spec.yAt(5), 1e-10);
        assertEquals(2.5, spec.yAt(2), 0.5, "y value at index 2 should be ~2.5");
    }

    @Test
    void rejectsInvertedXRange() {
        assertThrows(IllegalArgumentException.class,
                () -> new GridSpec(10, 0, 0, 5, 100, 50));
    }

    @Test
    void rejectsInvertedYRange() {
        assertThrows(IllegalArgumentException.class,
                () -> new GridSpec(0, 10, 5, 0, 100, 50));
    }

    @Test
    void rejectsZeroNx() {
        assertThrows(IllegalArgumentException.class,
                () -> new GridSpec(0, 10, 0, 5, 0, 50));
    }

    @Test
    void rejectsNegativeNy() {
        assertThrows(IllegalArgumentException.class,
                () -> new GridSpec(0, 10, 0, 5, 100, -1));
    }

    @Test
    void negativeBoundsWork() {
        GridSpec spec = new GridSpec(-100, 100, -50, 50, 201, 101);
        assertEquals(-100.0, spec.xAt(0), 1e-10);
        assertEquals(100.0, spec.xAt(200), 1e-10);
        assertEquals(0.0, spec.xAt(100), 1e-10);
    }
}
