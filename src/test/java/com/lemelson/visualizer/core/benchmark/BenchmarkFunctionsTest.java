package com.lemelson.visualizer.core.benchmark;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BenchmarkFunctionsTest {

    private final List<BenchmarkFunctionDefinition> definitions = BenchmarkLibrary.definitions();

    @Test
    void libraryContainsAllExpectedFunctions() {
        assertTrue(definitions.size() >= 23,
                "Библиотека должна содержать не менее 23 функций, найдено: " + definitions.size());
    }

    @Test
    void f1SphereAtZero() {
        BenchmarkFunction f = findFunction("F1").create(30);
        double result = f.evaluate(new double[30]);
        assertEquals(0.0, result, 1e-10, "F1(0,...,0) = 0");
    }

    @Test
    void f1SphereAtOnes() {
        BenchmarkFunction f = findFunction("F1").create(3);
        double result = f.evaluate(new double[] { 1.0, 1.0, 1.0 });
        assertEquals(3.0, result, 1e-10, "F1(1,1,1) = 3");
    }

    @Test
    void f2SchwefelAtZero() {
        BenchmarkFunction f = findFunction("F2").create(5);
        assertEquals(0.0, f.evaluate(new double[5]), 1e-10);
    }

    @Test
    void f2SchwefelAtOnes() {
        BenchmarkFunction f = findFunction("F2").create(3);

        assertEquals(4.0, f.evaluate(new double[] { 1.0, 1.0, 1.0 }), 1e-10);
    }

    @Test
    void f3SchwefelAtZero() {
        BenchmarkFunction f = findFunction("F3").create(10);
        assertEquals(0.0, f.evaluate(new double[10]), 1e-10);
    }

    @Test
    void f4SchwefelAtZero() {
        BenchmarkFunction f = findFunction("F4").create(5);
        assertEquals(0.0, f.evaluate(new double[5]), 1e-10);
    }

    @Test
    void f4SchwefelMaxAbs() {
        BenchmarkFunction f = findFunction("F4").create(3);
        assertEquals(5.0, f.evaluate(new double[] { 1.0, -5.0, 3.0 }), 1e-10);
    }

    @Test
    void f5RosenbrockAtMinimum() {
        BenchmarkFunction f = findFunction("F5").create(4);
        assertEquals(0.0, f.evaluate(new double[] { 1.0, 1.0, 1.0, 1.0 }), 1e-10);
    }

    @Test
    void f6StepAtZero() {
        BenchmarkFunction f = findFunction("F6").create(5);
        assertEquals(0.0, f.evaluate(new double[5]), 1e-10);
    }

    @Test
    void f7QuarticAtZero() {
        BenchmarkFunction f = findFunction("F7").create(10);
        assertEquals(0.0, f.evaluate(new double[10]), 1e-10);
    }

    @Test
    void f9RastriginAtZero() {
        BenchmarkFunction f = findFunction("F9").create(5);
        assertEquals(0.0, f.evaluate(new double[5]), 1e-10);
    }

    @Test
    void f10AckleyAtZero() {
        BenchmarkFunction f = findFunction("F10").create(5);
        assertEquals(0.0, f.evaluate(new double[5]), 1e-6, "F10 Ackley → 0 при нулевом входе");
    }

    @Test
    void f11GriewankAtZero() {
        BenchmarkFunction f = findFunction("F11").create(5);
        assertEquals(0.0, f.evaluate(new double[5]), 1e-10);
    }

    @Test
    void f16SixHumpCamelMinimumValue() {
        BenchmarkFunction f = findFunction("F16").create(2);
        double globalMin = f.globalMinimumValue();
        assertEquals(-1.0316, globalMin, 0.001, "Глобальный минимум Six-hump camel ≈ -1.0316");
    }

    @Test
    void f17BraninMinimumValue() {
        BenchmarkFunction f = findFunction("F17").create(2);
        double globalMin = f.globalMinimumValue();
        assertEquals(0.3979, globalMin, 0.001, "Глобальный минимум Branin ≈ 0.3979");
    }

    @Test
    void f18GoldsteinPriceAtMinimum() {
        BenchmarkFunction f = findFunction("F18").create(2);
        double result = f.evaluate(new double[] { 0.0, -1.0 });
        assertEquals(3.0, result, 1e-6, "Goldstein-Price(0, -1) = 3");
    }

    @ParameterizedTest
    @CsvSource({ "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F9", "F10", "F11" })
    void globalMinimumAtOriginOrKnownPoint(String id) {
        BenchmarkFunctionDefinition def = findFunction(id);
        BenchmarkFunction f = def.create(def.defaultDimension());
        f.globalMinimumPoint().ifPresent(point -> {
            double value = f.evaluate(point);
            assertEquals(f.globalMinimumValue(), value, 1e-4,
                    id + ": значение в точке минимума должно совпадать с globalMinimumValue()");
        });
    }

    @Test
    void allFunctionsHaveValidBounds() {
        for (BenchmarkFunctionDefinition def : definitions) {
            BenchmarkFunction f = def.create(def.defaultDimension());
            for (int i = 0; i < f.dimension(); i++) {
                assertTrue(f.lowerBound(i) < f.upperBound(i),
                        def.id() + ": нижняя граница должна быть меньше верхней для измерения " + i);
            }
        }
    }

    @Test
    void allFunctionsEvaluateWithoutException() {
        for (BenchmarkFunctionDefinition def : definitions) {
            BenchmarkFunction f = def.create(def.defaultDimension());
            double[] center = new double[f.dimension()];
            for (int i = 0; i < f.dimension(); i++) {
                center[i] = (f.lowerBound(i) + f.upperBound(i)) / 2.0;
            }
            assertDoesNotThrow(() -> f.evaluate(center),
                    def.id() + ": вычисление в центре диапазона не должно бросать исключение");
        }
    }

    @Test
    void dimensionLimitsRespected() {
        for (BenchmarkFunctionDefinition def : definitions) {
            assertTrue(def.minDimension() <= def.maxDimension(),
                    def.id() + ": minDimension <= maxDimension");
            assertTrue(def.defaultDimension() >= def.minDimension(),
                    def.id() + ": defaultDimension >= minDimension");
            assertTrue(def.defaultDimension() <= def.maxDimension(),
                    def.id() + ": defaultDimension <= maxDimension");
        }
    }

    private BenchmarkFunctionDefinition findFunction(String id) {
        return definitions.stream()
                .filter(d -> d.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Функция " + id + " не найдена в библиотеке"));
    }
}
