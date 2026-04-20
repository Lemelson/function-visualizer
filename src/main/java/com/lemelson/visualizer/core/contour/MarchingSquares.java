package com.lemelson.visualizer.core.contour;

import com.lemelson.visualizer.core.grid.GridSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MarchingSquares {

    public record Point(double x, double y) { }

    private static final int[][] EDGE_TABLE = {
            {},
            {3, 0},
            {0, 1},
            {3, 1},
            {1, 2},
            {3, 2, 0, 1},
            {0, 2},
            {3, 2},
            {2, 3},
            {0, 2},
            {0, 3, 1, 2},
            {1, 2},
            {1, 3},
            {0, 1},
            {3, 0},
            {}
    };

    public List<List<Point>> compute(double[][] data, double level, GridSpec spec) {
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(spec, "spec");
        if (data.length < 2 || data[0].length < 2) {
            return List.of();
        }
        int rows = data.length;
        int cols = data[0].length;
        for (double[] row : data) {
            if (row.length != cols) {
                throw new IllegalArgumentException("Матрица данных должна иметь одинаковую ширину строк");
            }
        }
        if (spec.nx() != cols || spec.ny() != rows) {
            throw new IllegalArgumentException("Размеры GridSpec не совпадают с матрицей данных");
        }

        List<List<Point>> segments = new ArrayList<>();

        for (int j = 0; j < rows - 1; j++) {
            double y0 = spec.yAt(j);
            double y1 = spec.yAt(j + 1);
            for (int i = 0; i < cols - 1; i++) {
                double x0 = spec.xAt(i);
                double x1 = spec.xAt(i + 1);

                double v00 = data[j][i];
                double v10 = data[j][i + 1];
                double v11 = data[j + 1][i + 1];
                double v01 = data[j + 1][i];

                if (!isFinite(v00, v10, v11, v01)) {
                    continue;
                }

                int cellCase = 0;
                if (isInside(v00, level)) cellCase |= 1;
                if (isInside(v10, level)) cellCase |= 2;
                if (isInside(v11, level)) cellCase |= 4;
                if (isInside(v01, level)) cellCase |= 8;

                if (cellCase == 0 || cellCase == 15) {
                    continue;
                }

                int[] edges;
                if (cellCase == 5) {
                    double cellMean = (v00 + v10 + v11 + v01) * 0.25;

                    edges = (cellMean >= level)
                            ? new int[]{3, 0, 1, 2}
                            : new int[]{3, 2, 0, 1};
                } else if (cellCase == 10) {
                    double cellMean = (v00 + v10 + v11 + v01) * 0.25;
                    edges = (cellMean >= level)
                            ? new int[]{0, 1, 2, 3}
                            : new int[]{0, 3, 1, 2};
                } else {
                    edges = EDGE_TABLE[cellCase];
                }

                for (int k = 0; k < edges.length; k += 2) {
                    Point p1 = interpolateEdge(edges[k], level, x0, x1, y0, y1, v00, v10, v11, v01);
                    Point p2 = interpolateEdge(edges[k + 1], level, x0, x1, y0, y1, v00, v10, v11, v01);
                    if (p1 != null && p2 != null) {
                        List<Point> segment = new ArrayList<>(2);
                        segment.add(p1);
                        segment.add(p2);
                        segments.add(segment);
                    }
                }
            }
        }
        return segments;
    }

    private boolean isFinite(double... values) {
        for (double v : values) {
            if (!Double.isFinite(v)) {
                return false;
            }
        }
        return true;
    }

    private boolean isInside(double value, double level) {
        return Double.compare(value, level) >= 0;
    }

    private Point interpolateEdge(int edge, double level, double x0, double x1, double y0, double y1,
                                  double v00, double v10, double v11, double v01) {
        return switch (edge) {
            case 0 -> interpolate(level, v00, v10, x0, y0, x1, y0);
            case 1 -> interpolate(level, v10, v11, x1, y0, x1, y1);
            case 2 -> interpolate(level, v11, v01, x1, y1, x0, y1);
            case 3 -> interpolate(level, v01, v00, x0, y1, x0, y0);
            default -> null;
        };
    }

    private Point interpolate(double level, double vStart, double vEnd,
                              double xStart, double yStart, double xEnd, double yEnd) {
        if (!Double.isFinite(vStart) || !Double.isFinite(vEnd)) {
            return null;
        }
        double delta = vEnd - vStart;
        if (Math.abs(delta) < 1e-12) {
            double midX = (xStart + xEnd) * 0.5;
            double midY = (yStart + yEnd) * 0.5;
            return new Point(midX, midY);
        }
        double t = (level - vStart) / delta;
        t = Math.max(0.0, Math.min(1.0, t));
        double x = xStart + t * (xEnd - xStart);
        double y = yStart + t * (yEnd - yStart);
        return new Point(x, y);
    }
}
