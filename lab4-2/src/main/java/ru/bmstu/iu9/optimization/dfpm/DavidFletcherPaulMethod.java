package ru.bmstu.iu9.optimization.dfpm;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import ru.bmstu.iu9.optimization.conf.DavidFletcherPaulConf;
import ru.bmstu.iu9.optimization.conf.GoldenSectionConf;
import ru.bmstu.iu9.optimization.onedim.gsm.GoldenSectionMethod;

import java.util.function.Function;

import static java.lang.Math.abs;

public class DavidFletcherPaulMethod {


    @SuppressWarnings("Duplicates")
    public static RealVector optimize(
            Function<RealVector, Double> objectiveFunc,
            Function<RealVector, RealVector> gradientFunc,
            RealVector x0,
            DavidFletcherPaulConf c,
            GoldenSectionConf odConf
    ) {
        RealMatrix gCur = MatrixUtils.createRealIdentityMatrix(x0.getDimension());
        RealVector dCur = gCur.preMultiply(gradientFunc.apply(x0).mapMultiply(-1));
        RealVector xCur = x0;
        int k = 0;

        for (; ; ) {
            RealVector xPrev = xCur;
            RealMatrix gPrev = (k % c.updateGMatrixIteration == 0) ? MatrixUtils.createRealIdentityMatrix(x0.getDimension()) : gCur;
            RealVector dPrev = dCur;

            double alpha = new GoldenSectionMethod().optimize(
                    a -> objectiveFunc.apply(xPrev.add(dPrev.mapMultiply(a))),
                    c.startAlpha,
                    odConf
            );

            xCur = xPrev.add(dPrev.mapMultiply(alpha));
            RealVector deltaX = xCur.subtract(xPrev);
            RealVector deltaGrad = gradientFunc.apply(xCur).subtract(gradientFunc.apply(xPrev));
            if(deltaX.getNorm() < c.sigma || gradientFunc.apply(xCur).getNorm() < c.gradEps) {
                return xCur;
            }


            double deltaG = (deltaX.dotProduct(deltaX) / deltaX.dotProduct(deltaGrad)) -
                    ((gPrev.preMultiply(deltaGrad).dotProduct(gPrev.preMultiply(deltaGrad))) / (deltaGrad.dotProduct(gPrev.preMultiply(deltaGrad))));
            gCur = gPrev.scalarAdd(deltaG);
            dCur = gCur.scalarMultiply(-1).preMultiply(gradientFunc.apply(xCur));

            if(abs(objectiveFunc.apply(xCur) - objectiveFunc.apply(xPrev)) < c.funcEps || k >= c.maxIterations) {
                System.out.printf("[info]\t\t-> Число итераций: %d%n", k);
                return xCur;
            }

            k++;
        }
    }

}
