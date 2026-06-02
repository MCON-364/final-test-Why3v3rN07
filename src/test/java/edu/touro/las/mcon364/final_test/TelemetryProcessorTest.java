package edu.touro.las.mcon364.final_test;

import edu.touro.las.mcon364.final_test.TelemetryEvent;
import edu.touro.las.mcon364.final_test.TelemetryProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.DoubleSummaryStatistics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TelemetryProcessorTest {

    private TelemetryProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new TelemetryProcessor();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        processor.stop();
    }

    private TelemetryEvent event(double metric) {
        return new TelemetryEvent("device-1", metric, System.nanoTime());
    }

    @Test
    void totalProcessedIsZeroInitially() {
        assertEquals(0, processor.getTotalProcessed());
    }

    @Test
    void statsAreEmptyInitially() {
        DoubleSummaryStatistics s = processor.getStats();
        assertEquals(0L, s.getCount());
    }

    @Test
    void startWithZeroWorkersThrows() {
        assertThrows(IllegalArgumentException.class, () -> processor.start(0));
    }

    @Test
    void startWithNegativeWorkersThrows() {
        assertThrows(IllegalArgumentException.class, () -> processor.start(-2));
    }

    @Test
    void singleWorkerProcessesAllEvents() throws InterruptedException {
        processor.start(1);
        for (int i = 0; i < 20; i++) {
            processor.submit(event(i));
        }
        processor.stop();
        assertEquals(20, processor.getTotalProcessed());
    }

    @Test
    void multipleWorkersProcessAllEvents() throws InterruptedException {
        processor.start(4);
        for (int i = 0; i < 100; i++) {
            processor.submit(event(i));
        }
        processor.stop();
        assertEquals(100, processor.getTotalProcessed());
    }

    @Test
    void statsAverageIsCorrect() throws InterruptedException {
        processor.start(1);
        processor.submit(event(2.0));
        processor.submit(event(6.0));
        processor.stop();
        assertEquals(4.0, processor.getStats().getAverage(), 0.001);
    }

    @Test
    void concurrentSubmissionsAreAllProcessed() throws InterruptedException {
        processor.start(3);
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                processor.submit(event(1.0));
            }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                processor.submit(event(2.0));
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        processor.stop();
        assertEquals(100, processor.getTotalProcessed());
    }
}

