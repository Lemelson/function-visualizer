package com.lemelson.visualizer.core.grid;

public record GridSpec(double xmin, double xmax, double ymin, double ymax, int nx, int ny) {

    public GridSpec {
        if (!Double.isFinite(xmin) || !Double.isFinite(xmax) || !Double.isFinite(ymin) || !Double.isFinite(ymax)) {
            throw new IllegalArgumentException("Диапазоны по осям должны быть конечными числами");
        }
        if (xmax <= xmin) {
            throw new IllegalArgumentException("xmax должно быть больше xmin");
        }
        if (ymax <= ymin) {
            throw new IllegalArgumentException("ymax должно быть больше ymin");
        }
        if (nx < 2 || ny < 2) {
            throw new IllegalArgumentException("Размер сетки должен быть >= 2x2");
        }
    }

    public double xStep() {
        return (xmax - xmin) / (nx - 1);
    }

    public double yStep() {
        return (ymax - ymin) / (ny - 1);
    }

    public double xAt(int ix) {
        return xmin + ix * xStep();
    }

    public double yAt(int iy) {
        return ymin + iy * yStep();
    }
}
