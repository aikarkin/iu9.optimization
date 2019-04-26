package ru.bmstu.iu9.optimization.lmm;

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import ru.bmstu.iu9.optimization.conf.LevenbergMarquardtConf;

import java.util.function.Function;

import static java.lang.Math.abs;

public class LevenbergMarquardtMethod {

    public static RealVector optimize(
            Function<RealVector, Double> func,
            Function<RealVector, RealVector> gradFunc,
            Function<RealVector, RealMatrix> hessianFunc,
            RealVector startVector,
            LevenbergMarquardtConf c
    ) {
        RealMatrix identityMatrix = MatrixUtils.createRealIdentityMatrix(startVector.getDimension());
        RealVector xPrev, xCur = startVector;
        double muPrev, muCur = c.startMu;
        int k = 0;

        do {

            do {
                xPrev = xCur;
                muPrev = muCur;
                RealVector d = inverseOf(hessianFunc.apply(xPrev).add(identityMatrix.scalarMultiply(muPrev)))
                        .preMultiply(gradFunc.apply(xPrev));

                xCur = xPrev.subtract(d);
                muCur = 2.0 * muPrev;
            } while (func.apply(xCur) >= func.apply(xPrev));

            muCur /= 4.0;

            k++;

        } while(
                xCur.subtract(xPrev).getNorm() >= c.sigma
                && abs(func.apply(xCur) - func.apply(xPrev)) >= c.funcEps
                && gradFunc.apply(xCur).getNorm() >= c.gradEps
                && k < c.maxIterations
        );

        System.out.println("[info]\t\t-> Число итераций: " + k);

        return xCur;
    }


    private static RealMatrix inverseOf(RealMatrix matrix) {
        return new LUDecomposition(matrix).getSolver().getInverse();
    }

}
