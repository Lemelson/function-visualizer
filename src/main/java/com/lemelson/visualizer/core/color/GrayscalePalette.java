package com.lemelson.visualizer.core.color;

public class GrayscalePalette implements Palette {

    @Override
    public int argb(double t) {
        double clamped = Math.min(1.0, Math.max(0.0, t));
        int channel = (int) Math.round(clamped * 255.0);
        return (0xFF << 24) | (channel << 16) | (channel << 8) | channel;
    }
}
