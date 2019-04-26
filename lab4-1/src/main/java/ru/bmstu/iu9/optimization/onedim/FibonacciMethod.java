package ru.bmstu.iu9.optimization.onedim;

import ru.bmstu.iu9.optimization.conf.OptimizationConfig;
import ru.bmstu.iu9.optimization.onedim.conf.FibonacciMethodConf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static java.lang.Math.abs;

public class FibonacciMethod implements OneDimOptimizationMethod {

    @Override
    public double optimize(Function<Double, Double> objectiveFunc, double x, OptimizationConfig conf) {
        FibonacciMethodConf c = (FibonacciMethodConf) conf;
        double a0 = c.start, b0 = c.end;
        List<Double> fibonacciList = findFibonacciNumbers(a0, b0, c.eps);
        int n = fibonacciList.size(), k = 0;

        if(n <= 3) {
            return (a0 + b0) / 2.0;
        }

        double a = a0, b = b0,
                y = a0 + fibonacciList.get(n - 3) / fibonacciList.get(n - 1) * (b0 - a0),
                z = a0 + fibonacciList.get(n - 2) / fibonacciList.get(n - 1) * (b0 - a0);

        while(abs(b - a) > c.sigma || abs(objectiveFunc.apply(b) - objectiveFunc.apply(a)) > c.eps) {
            if(objectiveFunc.apply(y) <= objectiveFunc.apply(z)) {
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
                z = y + c.eps;
                if(objectiveFunc.apply(y) <= objectiveFunc.apply(z)) {
                    b = z;
                } else {
                    a = y;
                }
                break;
            } else {
                k++;
            }
        }

        return (a + b) / 2.0;
    }

    private static List<Double> findFibonacciNumbers(double a0, double b0, double precision) {
        double l = abs(a0 - b0), prev = 1, cur = 1, t;
        ArrayList<Double> numbers = new ArrayList<>(Arrays.asList(prev, cur));

        while(cur <  l / precision) {
            t = cur;
            cur += prev;
            prev = t;

            numbers.add(cur);
        }

        return numbers;
    }

}
