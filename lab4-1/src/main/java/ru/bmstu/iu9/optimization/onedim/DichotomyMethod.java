package ru.bmstu.iu9.optimization.onedim;

import ru.bmstu.iu9.optimization.conf.OptimizationConfig;
import ru.bmstu.iu9.optimization.onedim.conf.DichotomyMethodConf;

import java.util.function.Function;

import static java.lang.Math.abs;

public class DichotomyMethod implements OneDimOptimizationMethod {

    @Override
    public double optimize(Function<Double, Double> objectiveFunc, double x, OptimizationConfig conf) {
        DichotomyMethodConf c = (DichotomyMethodConf) conf;
        double a = c.start, b = c.end,
                xMiddle = (a + b) / 2.0,
                l = abs(b - a),
                y, z;
        while(l > c.eps) {
            y = a + l / 4.0;
            z = b - l / 4.0;

            if(objectiveFunc.apply(y) < objectiveFunc.apply(xMiddle)) {
                b = xMiddle;
                xMiddle = y;
            } else {
                if(objectiveFunc.apply(z) < objectiveFunc.apply(xMiddle)) {
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
