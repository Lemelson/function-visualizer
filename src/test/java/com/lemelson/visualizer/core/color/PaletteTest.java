package com.lemelson.visualizer.core.color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PaletteTest {

    @Test
    void grayscaleCoversExtremes() {
        Palette palette = new GrayscalePalette();
        assertEquals(0xFF000000, palette.argb(0.0));
        assertEquals(0xFFFFFFFF, palette.argb(1.0));
        assertEquals(0xFF808080, palette.argb(0.5));
    }

    @Test
    void viridisMonotonicOnGreenChannel() {
        Palette palette = new ViridisPalette();
        int low = palette.argb(0.0);
        int mid = palette.argb(0.5);
        int high = palette.argb(1.0);
        int gLow = (low >> 8) & 0xFF;
        int gMid = (mid >> 8) & 0xFF;
        int gHigh = (high >> 8) & 0xFF;
        assertTrue(gLow < gMid);
        assertTrue(gMid <= gHigh);
    }
}
