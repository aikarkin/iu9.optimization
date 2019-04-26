package ru.bmstu.iu9.optimization.ncgm;

import org.apache.commons.math3.linear.RealVector;
import ru.bmstu.iu9.optimization.conf.GoldenSectionConf;
import ru.bmstu.iu9.optimization.conf.NonlinearConjugateGradientConf;
import ru.bmstu.iu9.optimization.onedim.gsm.GoldenSectionMethod;

import java.util.function.Function;

import static java.lang.Math.pow;

public class NonlinearConjugateGradientMethod {

    @SuppressWarnings("Duplicates")
    public static RealVector optimize(
            Function<RealVector, Double> objectiveFunc,
            Function<RealVector, RealVector> gradientFunc,
            RealVector x0,
            NonlinearConjugateGradientConf c,
            GoldenSectionConf odConf
    ) {
        RealVector dCur = gradientFunc.apply(x0).mapMultiply(-1);
        RealVector xCur = x0;
        double w;
        int k = 0;

        for (; ; ) {
            RealVector xPrev = xCur;
            RealVector dPrev = dCur;

            double alpha = new GoldenSectionMethod().optimize(
                    a -> objectiveFunc.apply(xPrev.add(dPrev.mapMultiply(a))),
                    c.startAlpha,
                    odConf
            );

            xCur = xPrev.add(dPrev.mapMultiply(alpha));
            w = pow(gradientFunc.apply(xCur).getNorm(), 2.0) / pow(gradientFunc.apply(xPrev).getNorm(), 2.0);
            dCur = gradientFunc.apply(xCur).mapMultiply(-1).add(dPrev.mapMultiply(w));
            if (dCur.getNorm() < c.eps || xCur.subtract(xPrev).getNorm() < c.sigma || k >= c.maxIterations) {
                System.out.printf("[info]\t\t-> Число итераций: %d%n", k);
                return xCur;
            }
            k++;
        }
    }

}