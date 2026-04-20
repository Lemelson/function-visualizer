package com.lemelson.visualizer.core.benchmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;

public final class BenchmarkLibrary {

    private static final List<BenchmarkFunctionDefinition> DEFINITIONS = buildDefinitions();

    private BenchmarkLibrary() {
    }

    public static List<BenchmarkFunctionDefinition> definitions() {
        return DEFINITIONS;
    }

    private static List<BenchmarkFunctionDefinition> buildDefinitions() {
        List<BenchmarkFunctionDefinition> list = new ArrayList<>();

        list.add(definition("F1", "Sphere function", FunctionCategory.UNIMODAL, 1, 1000, 30,
                dim -> new AbstractBenchmarkFunction(
                        "F1", "Sphere function", dim,
                        BenchmarkUtils.uniform(dim, -100.0),
                        BenchmarkUtils.uniform(dim, 100.0),
                        0.0,
                        BenchmarkUtils.zeros(dim)) {
                    @Override
                    public double evaluate(double[] x) {
                        double sum = 0.0;
                        for (double v : x) {
                            sum += v * v;
                        }
                        return sum;
                    }
                }));

        list.add(definition("F2", "Schwefel 2.22", FunctionCategory.UNIMODAL, 1, 1000, 30,
                dim -> new AbstractBenchmarkFunction(
                        "F2", "Schwefel 2.22", dim,
                        BenchmarkUtils.uniform(dim, -10.0),
                        BenchmarkUtils.uniform(dim, 10.0),
                        0.0,
                        BenchmarkUtils.zeros(dim)) {
                    @Override
                    public double evaluate(double[] x) {
                        double sum = 0.0;
                        double product = 1.0;
                        for (double v : x) {
                            double abs = Math.abs(v);
                            sum += abs;
                            product *= abs;
                        }
                        return sum + product;
                    }
                }));

        list.add(definition("F3", "Schwefel 1.2", FunctionCategory.UNIMODAL, 1, 1000, 30,
                dim -> new AbstractBenchmarkFunction(
                        "F3", "Schwefel 1.2", dim,
                        BenchmarkUtils.uniform(dim, -100.0),
                        BenchmarkUtils.uniform(dim, 100.0),
                        0.0,
                        BenchmarkUtils.zeros(dim)) {
                    @Override
                    public double evaluate(double[] x) {
                        double sum = 0.0;
                        double cumulative = 0.0;
                        for (double v : x) {
                            cumulative += v;
                            sum += cumulative * cumulative;
                        }
                        return sum;
                    }
                }));

        list.add(definition("F4", "Schwefel 2.21", FunctionCategory.UNIMODAL, 1, 1000, 30,
                dim -> new AbstractBenchmarkFunction(
                        "F4", "Schwefel 2.21", dim,
                        BenchmarkUtils.uniform(dim, -100.0),
                        BenchmarkUtils.uniform(dim, 100.0),
                        0.0,
                        BenchmarkUtils.zeros(dim)) {
                    @Override
                    public double evaluate(double[] x) {
                        double max = Math.abs(x[0]);
                        for (int i = 1; i < x.length; i++) {
                            double abs = Math.abs(x[i]);
                            if (abs > max) {
                                max = abs;
                            }
                        }
                        return max;
                    }
                }));

        list.add(definition("F5", "Rosenbrock", FunctionCategory.UNIMODAL, 2, 1000, 30,
                dim -> new AbstractBenchmarkFunction(
                        "F5", "Rosenbrock", dim,
                        BenchmarkUtils.uniform(dim, -30.0),
                        BenchmarkUtils.uniform(dim, 30.0),
                        0.0,
                        BenchmarkUtils.uniform(dim, 1.0)) {
                    @Override
                    public double evaluate(double[] x) {
                        double sum = 0.0;
                        for (int i = 0; i < x.length - 1; i++) {
                            double xi = x[i];
                            double next = x[i + 1];
                            double term1 = next - xi * xi;
                            double term2 = xi - 1.0;
                            sum += 100.0 * term1 * term1 + term2 * term2;
                        }
                        return sum;
                    }
                }));

        list.add(definition("F6", "Step function", FunctionCategory.UNIMODAL, 1, 1000, 30,
                dim -> new AbstractBenchmarkFunction(
                        "F6", "Step function", dim,
                        BenchmarkUtils.uniform(dim, -100.0),
                        BenchmarkUtils.uniform(dim, 100.0),
                        0.0,
                        BenchmarkUtils.zeros(dim)) {
                    @Override
                    public double evaluate(double[] x) {
                        double sum = 0.0;
                        for (double v : x) {
                            double floor = Math.floor(v + 0.5);
                            sum += floor * floor;
                        }
                        return sum;
                    }
                }));

        list.add(definition("F7", "Quartic with noise", FunctionCategory.UNIMODAL, 1, 1000, 30,
                dim -> new AbstractBenchmarkFunction(
                        "F7", "Quartic with noise", dim,
                        BenchmarkUtils.uniform(dim, -1.28),
                        BenchmarkUtils.uniform(dim, 1.28),
                        0.0,
                        BenchmarkUtils.zeros(dim)) {
                    @Override
                    public double evaluate(double[] x) {
                        double sum = 0.0;
                        for (int i = 0; i < x.length; i++) {
                            double term = (i + 1) * Math.pow(x[i], 4);
                            sum += term;
                        }

                        return sum;
                    }
                }));

        list.add(definition("F8", "Schwefel", FunctionCategory.MULTIMODAL, 1, 1000, 30,
                dim -> new AbstractBenchmarkFunction(
                        "F8", "Schwefel function", dim,
                        BenchmarkUtils.uniform(dim, -500.0),
                        BenchmarkUtils.uniform(dim, 500.0),
                        -418.9829 * dim,
                        BenchmarkUtils.uniform(dim, 420.9687)) {
                    @Override
                    public double evaluate(double[] x) {
                        double sum = 0.0;
                        for (double v : x) {
                            double abs = Math.abs(v);
                            sum += -v * Math.sin(Math.sqrt(abs));
                        }
                        return sum;
                    }
                }));

        list.add(definition("F9", "Rastrigin", FunctionCategory.MULTIMODAL, 1, 1000, 30,
                dim -> new AbstractBenchmarkFunction(
                        "F9", "Rastrigin", dim,
                        BenchmarkUtils.uniform(dim, -5.12),
                        BenchmarkUtils.uniform(dim, 5.12),
                        0.0,
                        BenchmarkUtils.zeros(dim)) {
                    @Override
                    public double evaluate(double[] x) {
                        double sum = 0.0;
                        for (double v : x) {
                            sum += v * v - 10.0 * Math.cos(2 * Math.PI * v);
                        }
                        return 10.0 * x.length + sum;
                    }
                }));

        list.add(definition("F10", "Ackley", FunctionCategory.MULTIMODAL, 1, 1000, 30,
                dim -> new AbstractBenchmarkFunction(
                        "F10", "Ackley", dim,
                        BenchmarkUtils.uniform(dim, -32.0),
                        BenchmarkUtils.uniform(dim, 32.0),
                        0.0,
                        BenchmarkUtils.zeros(dim)) {
                    @Override
                    public double evaluate(double[] x) {
                        int n = x.length;
                        double sumSq = 0.0;
                        double sumCos = 0.0;
                        for (double v : x) {
                            sumSq += v * v;
                            sumCos += Math.cos(2 * Math.PI * v);
                        }
                        double term1 = -20.0 * Math.exp(-0.2 * Math.sqrt(sumSq / n));
                        double term2 = -Math.exp(sumCos / n);
                        return term1 + term2 + 20.0 + Math.E;
                    }
                }));

        list.add(definition("F11", "Griewank", FunctionCategory.MULTIMODAL, 1, 1000, 30,
                dim -> new AbstractBenchmarkFunction(
                        "F11", "Griewank", dim,
                        BenchmarkUtils.uniform(dim, -600.0),
                        BenchmarkUtils.uniform(dim, 600.0),
                        0.0,
                        BenchmarkUtils.zeros(dim)) {
                    @Override
                    public double evaluate(double[] x) {
                        double sum = 0.0;
                        double product = 1.0;
                        for (int i = 0; i < x.length; i++) {
                            double v = x[i];
                            sum += (v * v) / 4000.0;
                            product *= Math.cos(v / Math.sqrt(i + 1));
                        }
                        return sum - product + 1.0;
                    }
                }));

        list.add(definition("F12", "Penalized function I", FunctionCategory.MULTIMODAL, 1, 1000, 30,
                dim -> new AbstractBenchmarkFunction(
                        "F12", "Penalized function I", dim,
                        BenchmarkUtils.uniform(dim, -50.0),
                        BenchmarkUtils.uniform(dim, 50.0),
                        0.0,
                        BenchmarkUtils.zeros(dim)) {
                    @Override
                    public double evaluate(double[] x) {
                        int n = x.length;
                        double[] y = new double[n];
                        for (int i = 0; i < n; i++) {
                            y[i] = 1.0 + (x[i] + 1.0) / 4.0;
                        }

                        double sum = 0.0;
                        for (int i = 0; i < n - 1; i++) {
                            double term = y[i] - 1.0;
                            double next = y[i + 1] - 1.0;
                            sum += term * term * (1.0 + 10.0 * next * next);
                        }
                        double lastTerm = y[n - 1] - 1.0;
                        double term0 = (Math.sin(Math.PI * y[0])) * (Math.sin(Math.PI * y[0]));
                        double termN = lastTerm * lastTerm;
                        sum = Math.PI / n * (term0 + sum + termN);

                        double penalty = 0.0;
                        for (double v : x) {
                            penalty += u(v, 10.0, 100.0, 4.0);
                        }
                        return sum + penalty;
                    }

                    private double u(double x, double a, double k, double m) {
                        if (x > a) {
                            return k * Math.pow(x - a, m);
                        } else if (x < -a) {
                            return k * Math.pow(-x - a, m);
                        }
                        return 0.0;
                    }
                }));

        list.add(definition("F13", "Penalized function II", FunctionCategory.MULTIMODAL, 1, 1000, 30,
                dim -> new AbstractBenchmarkFunction(
                        "F13", "Penalized function II", dim,
                        BenchmarkUtils.uniform(dim, -50.0),
                        BenchmarkUtils.uniform(dim, 50.0),
                        0.0,
                        BenchmarkUtils.uniform(dim, 1.0)) {
                    @Override
                    public double evaluate(double[] x) {
                        int n = x.length;
                        double sum = Math.pow(Math.sin(3 * Math.PI * x[0]), 2);
                        for (int i = 0; i < n - 1; i++) {
                            double term = x[i] - 1.0;
                            double next = x[i + 1] - 1.0;
                            sum += (term * term) * (1.0 + Math.pow(Math.sin(3 * Math.PI * next), 2));
                        }
                        double last = x[n - 1] - 1.0;
                        sum += last * last * (1.0 + Math.pow(Math.sin(2 * Math.PI * x[n - 1]), 2));

                        double penalty = 0.0;
                        for (double v : x) {
                            penalty += u(v, 5.0, 100.0, 4.0);
                        }
                        return 0.1 * sum + penalty;
                    }

                    private double u(double x, double a, double k, double m) {
                        if (x > a) {
                            return k * Math.pow(x - a, m);
                        } else if (x < -a) {
                            return k * Math.pow(-x - a, m);
                        }
                        return 0.0;
                    }
                }));

        list.add(definition("F14", "Shekel's Foxholes", FunctionCategory.FIXED_DIMENSION_MULTIMODAL, 2, 2, 2,
                dim -> new AbstractBenchmarkFunction(
                        "F14", "Shekel's Foxholes", 2,
                        new double[]{-65.0, -65.0},
                        new double[]{65.0, 65.0},
                        0.998004,
                        new double[]{-32.0, -32.0}) {

                    private final double[][] a = {
                        {-32, -16, 0, 16, 32, -32, -16, 0, 16, 32, -32, -16, 0, 16, 32, -32, -16, 0, 16, 32, -32, -16, 0, 16, 32},
                        {-32, -32, -32, -32, -32, -16, -16, -16, -16, -16, 0, 0, 0, 0, 0, 16, 16, 16, 16, 16, 32, 32, 32, 32, 32}
                    };

                    @Override
                    public double evaluate(double[] x) {
                        double sum = 0.002;
                        for (int j = 0; j < 25; j++) {
                            double inner = (j + 1);
                            for (int i = 0; i < 2; i++) {
                                inner += Math.pow(x[i] - a[i][j], 6);
                            }
                            sum += 1.0 / inner;
                        }
                        return 1.0 / sum;
                    }
                }));

        list.add(definition("F15", "Kowalik", FunctionCategory.FIXED_DIMENSION_MULTIMODAL, 4, 4, 4,
                dim -> new AbstractBenchmarkFunction(
                        "F15", "Kowalik", 4,
                        new double[]{-5.0, -5.0, -5.0, -5.0},
                        new double[]{5.0, 5.0, 5.0, 5.0},
                        0.0003075,
                        new double[]{0.1928, 0.1908, 0.1231, 0.1358}) {

                    private final double[] a = {0.1957, 0.1947, 0.1735, 0.16, 0.0844, 0.0627, 0.0456, 0.0342, 0.0323, 0.0235, 0.0246};
                    private final double[] b = {1.0/0.25, 1.0/0.5, 1.0/1.0, 1.0/2.0, 1.0/4.0, 1.0/6.0, 1.0/8.0, 1.0/10.0, 1.0/12.0, 1.0/14.0, 1.0/16.0};

                    @Override
                    public double evaluate(double[] x) {
                        double sum = 0.0;
                        for (int i = 0; i < 11; i++) {
                            double bi = b[i];
                            double numerator = x[0] * (bi * bi + bi * x[1]);
                            double denominator = bi * bi + bi * x[2] + x[3];
                            double diff = a[i] - numerator / denominator;
                            sum += diff * diff;
                        }
                        return sum;
                    }
                }));

        list.add(definition("F16", "Six-hump camel", FunctionCategory.FIXED_DIMENSION_MULTIMODAL, 2, 2, 2,
                dim -> new AbstractBenchmarkFunction(
                        "F16", "Six-hump camel", 2,
                        new double[]{-3.0, -2.0},
                        new double[]{3.0, 2.0},
                        -1.031628453489877,
                        null) {
                    @Override
                    public double evaluate(double[] x) {
                        double x1 = x[0];
                        double x2 = x[1];
                        return (4.0 - 2.1 * x1 * x1 + (Math.pow(x1, 4)) / 3.0) * x1 * x1
                                + x1 * x2
                                + (-4.0 + 4.0 * x2 * x2) * x2 * x2;
                    }
                }));

        list.add(definition("F17", "Branin", FunctionCategory.FIXED_DIMENSION_MULTIMODAL, 2, 2, 2,
                dim -> new AbstractBenchmarkFunction(
                        "F17", "Branin", 2,
                        new double[]{-5.0, 0.0},
                        new double[]{10.0, 15.0},
                        0.39788735772973816,
                        null) {
                    @Override
                    public double evaluate(double[] x) {
                        double x1 = x[0];
                        double x2 = x[1];
                        double a = 1.0;
                        double b = 5.1 / (4 * Math.PI * Math.PI);
                        double c = 5.0 / Math.PI;
                        double r = 6.0;
                        double s = 10.0;
                        double t = 1.0 / (8 * Math.PI);
                        double term = x2 - b * x1 * x1 + c * x1 - r;
                        return a * term * term + s * (1 - t) * Math.cos(x1) + s;
                    }
                }));

        list.add(definition("F18", "Goldstein-Price", FunctionCategory.FIXED_DIMENSION_MULTIMODAL, 2, 2, 2,
                dim -> new AbstractBenchmarkFunction(
                        "F18", "Goldstein-Price", 2,
                        new double[]{-2.0, -2.0},
                        new double[]{2.0, 2.0},
                        3.0,
                        new double[]{0.0, -1.0}) {
                    @Override
                    public double evaluate(double[] x) {
                        double x1 = x[0];
                        double x2 = x[1];
                        double term1 = 1.0 + Math.pow(x1 + x2 + 1.0, 2)
                                * (19.0 - 14.0 * x1 + 3.0 * x1 * x1 - 14.0 * x2 + 6.0 * x1 * x2 + 3.0 * x2 * x2);
                        double term2 = 30.0 + Math.pow(2.0 * x1 - 3.0 * x2, 2)
                                * (18.0 - 32.0 * x1 + 12.0 * x1 * x1 + 48.0 * x2 - 36.0 * x1 * x2 + 27.0 * x2 * x2);
                        return term1 * term2;
                    }
                }));

        list.add(definition("F19", "Hartmann 3D", FunctionCategory.FIXED_DIMENSION_MULTIMODAL, 3, 3, 3,
                dim -> new AbstractBenchmarkFunction(
                        "F19", "Hartmann 3D", 3,
                        new double[]{0.0, 0.0, 0.0},
                        new double[]{1.0, 1.0, 1.0},
                        -3.86278,
                        new double[]{0.114614, 0.555649, 0.852547}) {

                    private final double[][] A = {
                        {3.0, 10.0, 30.0},
                        {0.1, 10.0, 35.0},
                        {3.0, 10.0, 30.0},
                        {0.1, 10.0, 35.0}
                    };
                    private final double[] C = {1.0, 1.2, 3.0, 3.2};
                    private final double[][] P = {
                        {0.3689, 0.1170, 0.2673},
                        {0.4699, 0.4387, 0.7470},
                        {0.1091, 0.8732, 0.5547},
                        {0.0381, 0.5743, 0.8828}
                    };

                    @Override
                    public double evaluate(double[] x) {
                        double sum = 0.0;
                        for (int i = 0; i < 4; i++) {
                            double inner = 0.0;
                            for (int j = 0; j < 3; j++) {
                                double diff = x[j] - P[i][j];
                                inner += A[i][j] * diff * diff;
                            }
                            sum += C[i] * Math.exp(-inner);
                        }
                        return -sum;
                    }
                }));

        list.add(definition("F20", "Hartmann 6D", FunctionCategory.FIXED_DIMENSION_MULTIMODAL, 6, 6, 6,
                dim -> new AbstractBenchmarkFunction(
                        "F20", "Hartmann 6D", 6,
                        new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                        new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
                        -3.32237,
                        new double[]{0.20169, 0.150011, 0.476874, 0.275332, 0.311652, 0.6573}) {

                    private final double[][] A = {
                        {10.0, 3.0, 17.0, 3.5, 1.7, 8.0},
                        {0.05, 10.0, 17.0, 0.1, 8.0, 14.0},
                        {3.0, 3.5, 1.7, 10.0, 17.0, 8.0},
                        {17.0, 8.0, 0.05, 10.0, 0.1, 14.0}
                    };
                    private final double[] C = {1.0, 1.2, 3.0, 3.2};
                    private final double[][] P = {
                        {0.1312, 0.1696, 0.5569, 0.0124, 0.8283, 0.5886},
                        {0.2329, 0.4135, 0.8307, 0.3736, 0.1004, 0.9991},
                        {0.2348, 0.1451, 0.3522, 0.2883, 0.3047, 0.6650},
                        {0.4047, 0.8828, 0.8732, 0.5743, 0.1091, 0.0381}
                    };

                    @Override
                    public double evaluate(double[] x) {
                        double sum = 0.0;
                        for (int i = 0; i < 4; i++) {
                            double inner = 0.0;
                            for (int j = 0; j < 6; j++) {
                                double diff = x[j] - P[i][j];
                                inner += A[i][j] * diff * diff;
                            }
                            sum += C[i] * Math.exp(-inner);
                        }
                        return -sum;
                    }
                }));

        list.add(definition("F21", "Shekel 5", FunctionCategory.FIXED_DIMENSION_MULTIMODAL, 4, 4, 4,
                dim -> new ShekelFunction("F21", "Shekel 5", 5, -10.1532)));

        list.add(definition("F22", "Shekel 7", FunctionCategory.FIXED_DIMENSION_MULTIMODAL, 4, 4, 4,
                dim -> new ShekelFunction("F22", "Shekel 7", 7, -10.4028)));

        list.add(definition("F23", "Shekel 10", FunctionCategory.FIXED_DIMENSION_MULTIMODAL, 4, 4, 4,
                dim -> new ShekelFunction("F23", "Shekel 10", 10, -10.5363)));

        return Collections.unmodifiableList(list);
    }

    private static class ShekelFunction extends AbstractBenchmarkFunction {
        private static final double[][] A = {
            {4.0, 4.0, 4.0, 4.0},
            {1.0, 1.0, 1.0, 1.0},
            {8.0, 8.0, 8.0, 8.0},
            {6.0, 6.0, 6.0, 6.0},
            {3.0, 7.0, 3.0, 7.0},
            {2.0, 9.0, 2.0, 9.0},
            {5.0, 5.0, 3.0, 3.0},
            {8.0, 1.0, 8.0, 1.0},
            {6.0, 2.0, 6.0, 2.0},
            {7.0, 3.6, 7.0, 3.6}
        };

        private static final double[] C = {0.1, 0.2, 0.2, 0.4, 0.4, 0.6, 0.3, 0.7, 0.5, 0.5};

        private final int m;

        ShekelFunction(String id, String name, int m, double fmin) {
            super(id, name, 4,
                  new double[]{0.0, 0.0, 0.0, 0.0},
                  new double[]{10.0, 10.0, 10.0, 10.0},
                  fmin,
                  new double[]{4.0, 4.0, 4.0, 4.0});
            this.m = m;
        }

        @Override
        public double evaluate(double[] x) {
            double sum = 0.0;
            for (int i = 0; i < m; i++) {
                double inner = C[i];
                for (int j = 0; j < 4; j++) {
                    double diff = x[j] - A[i][j];
                    inner += diff * diff;
                }
                sum += 1.0 / inner;
            }
            return -sum;
        }
    }

    private static BenchmarkFunctionDefinition definition(String id,
                                                          String displayName,
                                                          FunctionCategory category,
                                                          int minDimension,
                                                          int maxDimension,
                                                          int defaultDimension,
                                                          IntFunction<BenchmarkFunction> factory) {
        return new BenchmarkFunctionDefinition(id, displayName, category, minDimension, maxDimension, defaultDimension, factory);
    }
}
