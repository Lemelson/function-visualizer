package com.lemelson.visualizer.render;

import com.lemelson.visualizer.core.color.Palette;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class HeatmapRenderer {

    public HeatmapRenderResult render(double[][] data, Palette palette) {
        if (data.length == 0 || data[0].length == 0) {
            throw new IllegalArgumentException("Пустая матрица данных");
        }
        int height = data.length;
        int width = data[0].length;

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (double[] row : data) {
            if (row.length != width) {
                throw new IllegalArgumentException("Все строки матрицы должны иметь одинаковую длину");
            }
            for (double value : row) {
                if (Double.isFinite(value)) {
                    if (value < min) {
                        min = value;
                    }
                    if (value > max) {
                        max = value;
                    }
                }
            }
        }

        boolean hasFinite = Double.isFinite(min) && Double.isFinite(max);
        double range = hasFinite ? max - min : 1.0;
        if (range == 0.0) {
            range = 1.0;
        }

        WritableImage image = new WritableImage(width, height);
        PixelWriter writer = image.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double value = data[y][x];
                double normalized = 0.5;
                if (Double.isFinite(value) && hasFinite) {
                    normalized = (value - min) / range;
                }
                int argb = palette.argb(normalized);

                writer.setArgb(x, height - 1 - y, argb);
            }
        }
        double effectiveMin = hasFinite ? min : Double.NaN;
        double effectiveMax = hasFinite ? max : Double.NaN;
        return new HeatmapRenderResult(image, effectiveMin, effectiveMax);
    }
}
