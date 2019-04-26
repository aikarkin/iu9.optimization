package ru.bmstu.iu9.optimization.core.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.Math.*;

public final class OneDimAlgorithms {
    private static final double CONST_GS = (3 - sqrt(5)) / 2.0;
    public static int NO_OF_STEPS = -1;

    private OneDimAlgorithms() { }

    public static Optional<Interval> findUnimodalIntervalWithSvennMethod(Function<Double, Double> func, double x0, double t) {
        if( (func.apply(x0 - t) <= func.apply(x0)) && (func.apply(x0) >= func.apply(x0 + t)) )
            return Optional.empty();

        if( (func.apply(x0 - t) >= func.apply(x0)) && (func.apply(x0) <= func.apply(x0 + t)) )
            return Optional.of(new Interval(x0 - t, x0 + t));

        double x1, x2, delta, a = x0 - t, b = x0 + t;
        boolean moveRight = true;
        int k = 1;

        if(func.apply(x0 - t) >= func.apply(x0) && func.apply(x0) >= func.apply(x0 + t)) {
            delta = t;
            a = x0;
            x1 = x0 + t;
        } else {
            delta = -t;
            moveRight = false;
            b = x0;
            x1 = x0 - t;
        }

        do {
            x2 = x1 + pow(2, k) * delta;

            if(delta == t && func.apply(x2) < func.apply(x1)) {
                a = x1;
            } else if(delta == -t && func.apply(x2) < func.apply(x1)) {
                b = x1;
            }

            if(func.apply(x2) >= func.apply(x1))
                break;

            x1 = x2;
            k++;
        } while (true);

        if(moveRight)
            b = x1;
        else
            a = x1;

        NO_OF_STEPS = k;

        return Optional.of(new Interval(a, b));
    }

    public static double extremaWithDichotomyMethod(Function<Double, Double> func, Interval searchInt, double precision) {
        double a = searchInt.start, b = searchInt.end,
                xMiddle = (a + b) / 2.0,
                l = abs(b - a),
                y, z;
        int k = 0;
        while(l > precision) {
            y = a + l / 4.0;
            z = b - l / 4.0;

            if(func.apply(y) < func.apply(xMiddle)) {
                b = xMiddle;
                xMiddle = y;
            } else {
                if(func.apply(z) < func.apply(xMiddle)) {
                    a = xMiddle;
                    xMiddle = z;
                } else {
                    a = y;
                    b = z;
                }
            }

            l = abs(b - a);
            k++;
        }

        NO_OF_STEPS = k;

        return (a + b) / 2.0;
    }

    public static double extremaWithGoldenSectionMethod(Function<Double, Double> func, Interval searchInt, double precision) {
        double y, z,
                a = searchInt.start, b = searchInt.end,
                delta = abs(b - a);

        int k = 0;
        while(delta > precision) {
            y = a + CONST_GS * (b - a);
            z = a + b - y;

            if(func.apply(y) <= func.apply(z)) {
                b = z;
            } else {
                a = y;
            }

            delta = abs(b - a);
            k++;
        }

        NO_OF_STEPS = k;

        return (a + b) / 2.0;
    }

    public static double extremaWithFibonacciMethod(Function<Double, Double> func, Interval searchInt, double eps, double sigma) {
        double a0 = searchInt.start, b0 = searchInt.end;
        List<Double> fibonacciList = findFibonacciNumbers(a0, b0, eps);
        int n = fibonacciList.size(), k = 0;

        if(n <= 3) {
            return (a0 + b0) / 2.0;
        }

        double a = a0, b = b0,
                y = a0 + fibonacciList.get(n - 3) / fibonacciList.get(n - 1) * (b0 - a0),
                z = a0 + fibonacciList.get(n - 2) / fibonacciList.get(n - 1) * (b0 - a0);

        while(abs(b - a) > sigma || abs(func.apply(b) - func.apply(a)) > eps) {
            if(func.apply(y) <= func.apply(z)) {
                b = z;
                z = y;
                y = a + fibonacciList.get(n - k - 3) / fibonacciList.get(n - k - 1) * (b - a);
            } else {
                a = y;
                y = z;
                z = a + fibonacciList.get(n - k -2) / fibonacciList.get(n - k - 1) * (b - a);
            }

            if(k == n - 3) {
                y = (a + b) / 2.0;
                z = y + eps;
                if(func.apply(y) <= func.apply(z)) {
                    b = z;
                } else {
                    a = y;
                }
                break;
            } else {
                k++;
            }
        }

        NO_OF_STEPS = k;

        return (a + b) / 2.0;
    }

    private static List<Double> findFibonacciNumbers(double a0, double b0, double precision) {
        double l = abs(a0 + b0), prev = 1, cur = 1, t;
        ArrayList<Double> numbers = new ArrayList<>(Arrays.asList(prev, cur));

        while(cur <  l / precision) {
            t = cur;
            cur += prev;
            prev = t;

            numbers.add(cur);
        }

        return numbers;
    }

    public static class Interval {
        double start, end;

        public Interval(double start, double end) {
            this.start = min(start, end);
            this.end = max(start, end);
        }

        public double getStart() {
            return start;
        }

        public double getEnd() {
            return end;
        }

        @Override
        public String toString() {
            return String.format("(%.6f; %.6f)", start, end);
        }

    }
}
