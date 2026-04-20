package com.lemelson.visualizer.core.grid;

import java.util.ArrayList;
import java.util.concurrent.CancellationException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class GridComputer {

    public double[][] compute(SliceDefinition slice, GridSpec spec, ExecutorService pool) {
        Objects.requireNonNull(slice, "slice");
        Objects.requireNonNull(spec, "spec");
        Objects.requireNonNull(pool, "pool");

        double[][] data = new double[spec.ny()][spec.nx()];
        int taskCount = Math.min(spec.ny(), Math.max(1, Runtime.getRuntime().availableProcessors()));
        int rowsPerTask = Math.max(1, (spec.ny() + taskCount - 1) / taskCount);
        List<Future<?>> tasks = new ArrayList<>(taskCount);
        for (int startRow = 0; startRow < spec.ny(); startRow += rowsPerTask) {
            final int fromRow = startRow;
            final int toRow = Math.min(spec.ny(), fromRow + rowsPerTask);
            tasks.add(pool.submit(() -> fillRows(data, fromRow, toRow, slice, spec)));
        }

        for (Future<?> task : tasks) {
            try {
                task.get();
            } catch (InterruptedException ex) {
                cancelTasks(tasks);
                Thread.currentThread().interrupt();
                throw new CancellationException("Расчёт сетки был прерван");
            } catch (ExecutionException ex) {
                cancelTasks(tasks);
                if (ex.getCause() instanceof CancellationException cancellation) {
                    throw cancellation;
                }
                throw new IllegalStateException("Ошибка при вычислении функции", ex.getCause());
            }
        }
        return data;
    }

    private void fillRows(double[][] data, int startRow, int endRow, SliceDefinition slice, GridSpec spec) {
        double[] point = slice.createPointTemplate();
        double xmin = spec.xmin();
        double xStep = spec.xStep();
        double ymin = spec.ymin();
        double yStep = spec.yStep();
        int xIndex = slice.xIndex();
        int yIndex = slice.yIndex();

        for (int rowIndex = startRow; rowIndex < endRow; rowIndex++) {
            if (Thread.currentThread().isInterrupted()) {
                throw new CancellationException("Расчёт сетки был отменён");
            }

            double[] targetRow = data[rowIndex];
            point[yIndex] = ymin + rowIndex * yStep;

            for (int colIndex = 0; colIndex < targetRow.length; colIndex++) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new CancellationException("Расчёт сетки был отменён");
                }

                point[xIndex] = xmin + colIndex * xStep;
                try {
                    double value = slice.function().evaluate(point);
                    targetRow[colIndex] = Double.isFinite(value) ? value : Double.NaN;
                } catch (RuntimeException ex) {
                    targetRow[colIndex] = Double.NaN;
                }
            }
        }
    }

    private void cancelTasks(List<Future<?>> tasks) {
        for (Future<?> task : tasks) {
            task.cancel(true);
        }
    }
}
