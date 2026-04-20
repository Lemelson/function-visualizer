package com.lemelson.visualizer.core.color;

public class ViridisPalette implements Palette {

    @Override
    public int argb(double t) {
        double x = Math.min(1.0, Math.max(0.0, t));
        double x2 = x * x;
        double x3 = x2 * x;
        double x4 = x3 * x;
        double x5 = x4 * x;

        double r =  0.280268003 - 0.143510503 * x  + 2.225793877 * x2
                 - 14.815088879 * x3 + 25.212752309 * x4 - 11.772589584 * x5;
        double g = -0.002117546 + 1.617109353 * x  - 1.909305070 * x2
                 +  2.701152864 * x3 -  1.685288385 * x4 +  0.178738871 * x5;
        double b =  0.300805501 + 2.614650302 * x  - 12.019139090 * x2
                 + 28.933559110 * x3 - 33.491294770 * x4 + 13.762053843 * x5;

        return (0xFF << 24)
                | (toChannel(r) << 16)
                | (toChannel(g) << 8)
                | toChannel(b);
    }

    private int toChannel(double value) {
        return (int) Math.round(Math.min(1.0, Math.max(0.0, value)) * 255.0);
    }
}
