package ru.bmstu.iu9.optimization.od;

import ru.bmstu.iu9.optimization.conf.dmc.DichotomyMethodConfig;

import java.util.function.Function;

import static java.lang.Math.abs;

public class DichotomyMethod {

    public static double dichotomyMethod(Function<Double, Double> objectiveFunc, double start, double end, double eps) {
        double a = start, b = end,
                xMiddle = (a + b) / 2.0,
                l = abs(b - a),
                y, z;
        while (l > eps) {
            y = a + l / 4.0;
            z = b - l / 4.0;

            if (objectiveFunc.apply(y) < objectiveFunc.apply(xMiddle)) {
                b = xMiddle;
                xMiddle = y;
            } else {
                if (objectiveFunc.apply(z) < objectiveFunc.apply(xMiddle)) {
                    a = xMiddle;
                    xMiddle = z;
                } else {
                    a = y;
                    b = z;
                }
            }

            l = abs(b - a);
        }

        return (a + b) / 2.0;
    }

}
