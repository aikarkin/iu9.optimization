package ru.bmstu.iu9.optimization.gdm;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import ru.bmstu.iu9.optimization.conf.GoldenSectionConf;
import ru.bmstu.iu9.optimization.conf.GradientDescendConf;
import ru.bmstu.iu9.optimization.onedim.gsm.GoldenSectionMethod;

import java.util.function.BiPredicate;
import java.util.function.Function;

import static java.lang.Math.abs;

public class GradientDescendMethod {

    public static RealVector optimize(
            Function<RealVector, Double> objectiveFunc,
            Function<RealVector, RealVector> gradientFunc,
            RealVector x0,
            GradientDescendConf c,
            GoldenSectionConf odConf
    ) {
        boolean mustExit;
        int k = 0;
        RealVector curVec = new ArrayRealVector(x0.toArray());
        BiPredicate<RealVector, RealVector> terminatePredicate = (prev, cur) ->
                (cur.subtract(prev).getNorm() < c.sigma
                        && abs(objectiveFunc.apply(cur) - objectiveFunc.apply(prev)) < c.eps)
                        || (gradientFunc.apply(cur).getNorm() < c.sigma);

        do {
            RealVector prevVec = curVec;
            RealVector grad = gradientFunc.apply(curVec);
            double optimalAlpha = new GoldenSectionMethod().optimize(
                    alpha -> objectiveFunc.apply(prevVec.subtract(grad.mapMultiply(alpha))),
                    c.startAlpha,
                    odConf
            );
            curVec = prevVec.subtract(grad.mapMultiply(optimalAlpha));
            k++;
            mustExit = terminatePredicate.test(prevVec, curVec) || k >= c.maxIterations;
        } while (!mustExit);

        System.out.printf("[info]\t\t-> Число итераций: %d%n", k);

        return curVec;
    }

}
