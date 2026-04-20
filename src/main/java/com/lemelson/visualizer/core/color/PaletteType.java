package com.lemelson.visualizer.core.color;

import java.util.function.Supplier;

public enum PaletteType implements Supplier<Palette> {
    GRAYSCALE("Grayscale", new GrayscalePalette()),
    VIRIDIS("Viridis", new ViridisPalette());

    private final String displayName;
    private final Palette palette;

    PaletteType(String displayName, Palette palette) {
        this.displayName = displayName;
        this.palette = palette;
    }

    @Override
    public Palette get() {
        return palette;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
