package com.lemelson.visualizer.core.benchmark;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ExpressionBenchmarkFunctionTest {

    @Test
    void simpleAddition() {
        ExpressionBenchmarkFunction f = new ExpressionBenchmarkFunction("x1 + x2", 2, -10, 10);
        assertEquals(3.0, f.evaluate(new double[] { 1.0, 2.0 }), 1e-10);
    }

    @Test
    void sinCosExpression() {
        ExpressionBenchmarkFunction f = new ExpressionBenchmarkFunction("sin(x1) * cos(x2)", 2, -10, 10);
        assertEquals(0.0, f.evaluate(new double[] { 0.0, 0.0 }), 1e-10);
        double expected = Math.sin(1.0) * Math.cos(2.0);
        assertEquals(expected, f.evaluate(new double[] { 1.0, 2.0 }), 1e-10);
    }

    @Test
    void powerExpression() {
        ExpressionBenchmarkFunction f = new ExpressionBenchmarkFunction("x1^2 + x2^2 + x3^2", 3, -5, 5);
        assertEquals(14.0, f.evaluate(new double[] { 1.0, 2.0, 3.0 }), 1e-10);
    }

    @Test
    void singleVariable() {
        ExpressionBenchmarkFunction f = new ExpressionBenchmarkFunction("x1 * x1", 1, -5, 5);
        assertEquals(4.0, f.evaluate(new double[] { 2.0 }), 1e-10);
    }

    @Test
    void complexExpression() {
        ExpressionBenchmarkFunction f = new ExpressionBenchmarkFunction("sqrt(x1^2 + x2^2)", 2, 0, 10);
        assertEquals(5.0, f.evaluate(new double[] { 3.0, 4.0 }), 1e-10);
    }

    @Test
    void rejectsEmptyFormula() {
        assertThrows(IllegalArgumentException.class,
                () -> new ExpressionBenchmarkFunction("", 2, -1, 1));
    }

    @Test
    void rejectsBlankFormula() {
        assertThrows(IllegalArgumentException.class,
                () -> new ExpressionBenchmarkFunction("   ", 2, -1, 1));
    }

    @Test
    void rejectsInvalidBounds() {
        assertThrows(IllegalArgumentException.class,
                () -> new ExpressionBenchmarkFunction("x1 + x2", 2, 10, -10));
    }

    @Test
    void rejectsEqualBounds() {
        assertThrows(IllegalArgumentException.class,
                () -> new ExpressionBenchmarkFunction("x1 + x2", 2, 5, 5));
    }

    @Test
    void rejectsUnknownVariable() {
        assertThrows(IllegalArgumentException.class,
                () -> new ExpressionBenchmarkFunction("x1 + y", 2, -1, 1));
    }

    @Test
    void rejectsInvalidSyntax() {
        assertThrows(Exception.class,
                () -> new ExpressionBenchmarkFunction("x1 * / x2", 2, -1, 1));
    }

    @Test
    void handlesNaNResult() {
        ExpressionBenchmarkFunction f = new ExpressionBenchmarkFunction("log(x1)", 1, -10, 10);
        double result = f.evaluate(new double[] { -1.0 });
        assertTrue(Double.isNaN(result), "log(-1) должен вернуть NaN");
    }

    @Test
    void dimensionIsCorrect() {
        ExpressionBenchmarkFunction f = new ExpressionBenchmarkFunction("x1 + x2 + x3", 3, -1, 1);
        assertEquals(3, f.dimension());
        assertEquals("CUSTOM", f.id());
    }

    @Test
    void boundsAreCorrect() {
        ExpressionBenchmarkFunction f = new ExpressionBenchmarkFunction("x1", 2, -5.0, 10.0);
        assertEquals(-5.0, f.lowerBound(0), 1e-10);
        assertEquals(10.0, f.upperBound(0), 1e-10);
        assertEquals(-5.0, f.lowerBound(1), 1e-10);
        assertEquals(10.0, f.upperBound(1), 1e-10);
    }

    @Test
    void globalMinimumIsNaN() {
        ExpressionBenchmarkFunction f = new ExpressionBenchmarkFunction("x1 + x2", 2, -1, 1);
        assertTrue(Double.isNaN(f.globalMinimumValue()),
                "Пользовательская функция не имеет известного глобального минимума");
    }

    @Test
    void threadSafeEvaluation() throws InterruptedException {
        ExpressionBenchmarkFunction f = new ExpressionBenchmarkFunction("x1 * x2", 2, -10, 10);
        Thread[] threads = new Thread[8];
        boolean[] errors = new boolean[1];

        for (int t = 0; t < threads.length; t++) {
            final int offset = t;
            threads[t] = new Thread(() -> {
                for (int i = 0; i < 100; i++) {
                    double val = offset + i;
                    double result = f.evaluate(new double[] { val, 2.0 });
                    if (Math.abs(result - val * 2.0) > 1e-10) {
                        errors[0] = true;
                    }
                }
            });
        }

        for (Thread thread : threads)
            thread.start();
        for (Thread thread : threads)
            thread.join();

        assertFalse(errors[0], "Потокобезопасность нарушена");
    }
}
