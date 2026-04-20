package com.lemelson.visualizer.ui;

import com.lemelson.visualizer.core.color.Palette;
import com.lemelson.visualizer.core.grid.GridComputer;
import com.lemelson.visualizer.core.grid.GridSpec;
import com.lemelson.visualizer.core.grid.SliceDefinition;
import com.lemelson.visualizer.render.HeatmapRenderResult;
import com.lemelson.visualizer.render.HeatmapRenderer;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;

public class ComputeService {

    private final GridComputer gridComputer = new GridComputer();
    private final HeatmapRenderer renderer = new HeatmapRenderer();

    public record ComputeResult(
            SliceDefinition slice,
            GridSpec spec,
            double[][] grid,
            HeatmapRenderResult render,
            long elapsedMs) {
    }

    public ComputeResult computeAndRender(SliceDefinition slice, GridSpec spec,
            Palette palette, ExecutorService pool) {
        long startNanos = System.nanoTime();
        double[][] grid = gridComputer.compute(slice, spec, pool);
        if (Thread.currentThread().isInterrupted()) {
            throw new CancellationException("Расчёт был отменён");
        }
        HeatmapRenderResult render = renderer.render(grid, palette);
        if (Thread.currentThread().isInterrupted()) {
            throw new CancellationException("Рендер был отменён");
        }
        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
        return new ComputeResult(slice, spec, grid, render, elapsedMs);
    }

    public HeatmapRenderResult rerenderWithPalette(double[][] grid, Palette palette) {
        return renderer.render(grid, palette);
    }
}
