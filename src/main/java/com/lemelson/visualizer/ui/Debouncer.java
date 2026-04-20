package com.lemelson.visualizer.ui;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;

public class Debouncer implements AutoCloseable {

    private final long delayMillis;
    private final ScheduledExecutorService scheduler;
    private final Object lock = new Object();
    private ScheduledFuture<?> pending;

    public Debouncer(long delayMillis) {
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis");
        }
        this.delayMillis = delayMillis;
        ThreadFactory factory = r -> {
            Thread thread = new Thread(r, "ui-debouncer");
            thread.setDaemon(true);
            return thread;
        };
        this.scheduler = Executors.newSingleThreadScheduledExecutor(factory);
    }

    public void submit(Runnable task) {
        Objects.requireNonNull(task, "task");
        synchronized (lock) {
            if (pending != null) {
                pending.cancel(false);
            }
            pending = scheduler.schedule(() -> Platform.runLater(task), delayMillis, TimeUnit.MILLISECONDS);
        }
    }

    public void flushAndRun(Runnable task) {
        Objects.requireNonNull(task, "task");
        synchronized (lock) {
            if (pending != null) {
                pending.cancel(false);
                pending = null;
            }
        }
        Platform.runLater(task);
    }

    @Override
    public void close() {
        scheduler.shutdownNow();
    }
}
