package com.lemelson.visualizer.core.optimization;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OptimizationHistoryJsonWriterTest {

    @Test
    void serializesHistoryDocumentToJson() throws IOException {
        OptimizationHistoryDocument history = new OptimizationHistoryDocument(
                new OptimizationHistoryMetadata(
                        "DifferentialEvolution",
                        "T1",
                        "Test Function",
                        2,
                        4,
                        3,
                        Map.of("CR", 0.9, "F", 0.7),
                        List.of()),
                List.of(new IterationSnapshot(
                        0,
                        List.of(new AgentSnapshot(0, new double[] {1.0, 2.0}, 5.0, 5.0, 0.0, true)),
                        0,
                        5.0,
                        5.0,
                        0L)));

        String json = new OptimizationHistoryJsonWriter().toJson(history);

        assertTrue(json.contains("\"iterations\""));
        assertTrue(json.contains("\"agentIndex\""));
        assertTrue(json.contains("\"DifferentialEvolution\""));
    }
}
