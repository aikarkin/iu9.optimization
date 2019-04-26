package ru.bmstu.iu9.optimization.onedim;

import ru.bmstu.iu9.optimization.conf.OptimizationConfig;
import ru.bmstu.iu9.optimization.onedim.conf.GoldenSectionConf;

import java.util.function.Function;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

public class GoldenSectionMethod implements OneDimOptimizationMethod {

    private static final double CONST_GS = (3 - sqrt(5)) / 2.0;

    @Override
    public double optimize(Function<Double, Double> objectiveFunc, double x, OptimizationConfig conf) {
        GoldenSectionConf c = (GoldenSectionConf) conf;
        double y, z,
                a = c.start, b = c.end,
                delta = abs(b - a);

        while(delta > c.eps) {
            y = a + CONST_GS * (b - a);
            z = a + b - y;

            if(objectiveFunc.apply(y) <= objectiveFunc.apply(z)) {
                b = z;
            } else {
                a = y;
            }

            delta = abs(b - a);
        }


        return (a + b) / 2.0;
    }

}
