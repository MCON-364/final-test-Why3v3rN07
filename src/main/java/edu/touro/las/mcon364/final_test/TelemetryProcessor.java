package edu.touro.las.mcon364.final_test;

import edu.touro.las.mcon364.final_test.TelemetryEvent;

import java.util.DoubleSummaryStatistics;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class TelemetryProcessor {

    private final BlockingQueue<TelemetryEvent> queue = new LinkedBlockingQueue<>();
    private final AtomicInteger totalProcessed = new AtomicInteger(0);
    private final AtomicReference<DoubleSummaryStatistics> stats =
            new AtomicReference<>(new DoubleSummaryStatistics());

    private volatile boolean running = false;
    private ExecutorService executor;

    public void submit(TelemetryEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }
        if (running) {
            queue.offer(event);
        }
    }

    public void start(int workerCount) {
        if (workerCount <= 0) {
            throw new IllegalArgumentException("workerCount must be positive");
        }
        if (running) {
            return;
        }
        running = true;
        executor = Executors.newFixedThreadPool(workerCount);
        for (int i = 0; i < workerCount; i++) {
            executor.submit(this::workerLoop);
        }
    }

    private void workerLoop() {
        while (running || !queue.isEmpty()) {
            try {
                TelemetryEvent event = queue.poll(100, TimeUnit.MILLISECONDS);
                if (event != null) {
                    process(event);
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void process(TelemetryEvent event) {
        totalProcessed.incrementAndGet();
        stats.updateAndGet(current -> {
            DoubleSummaryStatistics updated = new DoubleSummaryStatistics();
            updated.combine(current);
            updated.accept(event.metric());
            return updated;
        });
    }

    public void stop() throws InterruptedException {
        running = false;
        if (executor != null) {
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
        TelemetryEvent remaining;
        while ((remaining = queue.poll()) != null) {
            process(remaining);
        }
    }

    public int getTotalProcessed() {
        return totalProcessed.get();
    }

    public DoubleSummaryStatistics getStats() {
        DoubleSummaryStatistics snapshot = new DoubleSummaryStatistics();
        snapshot.combine(stats.get());
        return snapshot;
    }
}
