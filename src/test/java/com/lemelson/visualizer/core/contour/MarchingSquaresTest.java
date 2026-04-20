package com.lemelson.visualizer.core.contour;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.lemelson.visualizer.core.grid.GridSpec;
import java.util.List;
import org.junit.jupiter.api.Test;

class MarchingSquaresTest {

    @Test
    void producesDiagonalSegmentsForPlane() {
        double[][] data = {
                {0.0, 1.0, 2.0},
                {1.0, 2.0, 3.0},
                {2.0, 3.0, 4.0}
        };
        GridSpec spec = new GridSpec(0.0, 2.0, 0.0, 2.0, 3, 3);
        MarchingSquares marchingSquares = new MarchingSquares();

        List<List<MarchingSquares.Point>> segments = marchingSquares.compute(data, 2.0, spec);

        assertFalse(segments.isEmpty(), "Ожидались изолинии");
        MarchingSquares.Point p0 = segments.get(0).get(0);
        MarchingSquares.Point p1 = segments.get(0).get(1);
        double sum0 = p0.x() + p0.y();
        double sum1 = p1.x() + p1.y();
        assertEquals(2.0, sum0, 1e-6);
        assertEquals(2.0, sum1, 1e-6);
    }
}
