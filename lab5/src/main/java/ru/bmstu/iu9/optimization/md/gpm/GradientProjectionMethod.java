package ru.bmstu.iu9.optimization.md.gpm;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import ru.bmstu.iu9.optimization.conf.gpc.GradientProjectionConfig;
import ru.bmstu.iu9.optimization.geometry.VectorUtils;
import ru.bmstu.iu9.optimization.md.OptimizationMethod;
import ru.bmstu.iu9.optimization.md.OptimizationResult;
import ru.bmstu.iu9.optimization.od.DichotomyMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.lang.Math.*;

public class GradientProjectionMethod implements OptimizationMethod {

    private Function<RealVector, Double> objectiveFunc;
    private Function<RealVector, RealVector> gradient;
    private List<Function<RealVector, Double>> constraints;
    private Function<RealVector, RealMatrix> matAFullFunc;
    private GradientProjectionConfig c;

    public GradientProjectionMethod(
            Function<RealVector, Double> objectiveFunc,
            Function<RealVector, RealVector> gradient,
            List<Function<RealVector, Double>> constraints,
            Function<RealVector, RealMatrix> constraintsDerivativesMatrix,
            GradientProjectionConfig c) {
        this.objectiveFunc = objectiveFunc;
        this.gradient = gradient;
        this.constraints = constraints;
        this.matAFullFunc = constraintsDerivativesMatrix;
        this.c = c;
    }

    @Override
    public OptimizationResult optimize(RealVector x0) {
        RealVector x = new ArrayRealVector(x0);
        RealVector vecDir = null;
        boolean shouldUseGradDir;
        int k;

        for (k = 0; k < c.maxIterations(); k++) {
            RealVector gradF = gradient.apply(x);
            shouldUseGradDir = true;

            // если норма градиента стала слишком маленькой, выходим
            if ((gradF.getNorm() < c.eps2())) {
                return new OptimizationResult(x, objectiveFunc.apply(x), k);
            }

            gradF.unitize();

            List<Integer> activeConstraintsIdxes = getActiveConstraints(x);

            // Попали на границу
            // антиградиент направлен за пределы допустимой области, проецируем градиент
            if (activeConstraintsIdxes.size() > 0 && satisfiesConstraints(x.subtract(gradF)) < 0) {
                RealMatrix matA = buildMatAWithConstraints(activeConstraintsIdxes, x);
                do {
                    RealVector deltaX = gradientProjection(matA, gradF);
                    if (deltaX.getNorm() > c.eps2()) {
                        vecDir = deltaX;
                        shouldUseGradDir = false;
                        break;
                    }

                    RealVector lambda = MatrixUtils.inverse(matA.multiply(matA.transpose()))
                            .multiply(matA)
                            .operate(gradF)
                            .mapMultiply(-1);

                    // Все элементы больше либо равны нулю - Ура!
                    // Похоже искомая точка найдена, следует проверить достаточные условия экстремума
                    if (VectorUtils.elementsAreGreaterOrEqualsZero(lambda)) {
                        return new OptimizationResult(x, objectiveFunc.apply(x), k);
                    }

                    // найдем индекс с минимальным лямбда
                    int minLambdaIdx = 0;
                    for (int i = 1; i < lambda.getDimension(); i++) {
                        if (lambda.getEntry(i) < lambda.getEntry(minLambdaIdx)) {
                            minLambdaIdx = i;
                        }
                    }

                    // удалим ограничение с найденным индексом из числа активных
                    activeConstraintsIdxes.remove(minLambdaIdx);
                } while (activeConstraintsIdxes.size() > 0);

                shouldUseGradDir = activeConstraintsIdxes.size() == 0;
            }

            if (shouldUseGradDir) {
                vecDir = gradF.mapMultiply(-1);
            }

            RealVector vecDirFinal = vecDir;
            RealVector xFinal = x;

            double alphaConstr = findMaxAlphaSatisfyingConstraints(x, vecDir);
            double alphaOptimal = DichotomyMethod.dichotomyMethod(
                    (alpha) -> objectiveFunc.apply(xFinal.add(vecDirFinal.mapMultiply(alpha))),
                    c.alpha0(),
                    alphaConstr,
                    c.sigma()
            );
            x = x.add(vecDirFinal.mapMultiply(alphaOptimal));

            if(xFinal.subtract(x).getNorm() < c.sigma() || abs(objectiveFunc.apply(x) - objectiveFunc.apply(xFinal)) < c.eps2())
                return new OptimizationResult(x, objectiveFunc.apply(x), k);

        }

        return new OptimizationResult(x, objectiveFunc.apply(x), k);
    }

    private List<Integer> getActiveConstraints(RealVector x) {
        List<Integer> activeConstraintsIdxes = new ArrayList<>();

        for (int i = 0; i < constraints.size(); i++) {
            double constraintValue = constraints.get(i).apply(x);
            if (c.eps1() <= constraintValue && constraintValue <= 0) {
                activeConstraintsIdxes.add(i);
            }
        }

        return activeConstraintsIdxes;
    }

    private RealVector gradientProjection(RealMatrix matA, RealVector gradF) {
        RealMatrix identity = MatrixUtils.createRealIdentityMatrix(gradF.getDimension());
        return identity
                .subtract(
                        matA.transpose()
                                .multiply(
                                        MatrixUtils.inverse(matA.multiply(matA.transpose()))
                                )
                                .multiply(matA)
                )
                .operate(gradF)
                .mapMultiply(-1);
    }

    private double findMaxAlphaSatisfyingConstraints(RealVector x0, RealVector d) {
        double alpha = 0.0, alphaPrev;
        int k = 0;
        RealVector x;

        do {
            alphaPrev = alpha;
            alpha = pow(2.0, k);
            x = x0.add(d.mapMultiply(alpha));
            k++;
        } while (satisfiesConstraints(x) > 0);

        double minAlpha = alphaPrev, maxAlpha = alpha;

        while (abs(minAlpha - maxAlpha) > c.alphaPrecision()) {
            double mid = (minAlpha + maxAlpha) / 2.0;
            if (satisfiesConstraints(x0.add(d.mapMultiply(minAlpha))) * satisfiesConstraints(x0.add(d.mapMultiply(mid))) < 0) {
                maxAlpha = mid;
            } else {
                minAlpha = mid;
            }
        }

        return (minAlpha + maxAlpha) / 2.0;
    }

    private RealMatrix buildMatAWithConstraints(List<Integer> activeIdxes, RealVector x) {
        RealMatrix matA = MatrixUtils.createRealMatrix(activeIdxes.size(), x.getDimension());

        for (int i = 0; i < activeIdxes.size(); i++) {
            matA.setRowVector(i, matAFullFunc.apply(x).getRowVector(activeIdxes.get(i)));
        }

        return matA;
    }

    private int satisfiesConstraints(RealVector x) {
        return constraints.stream()
                .map(g -> g.apply(x))
                .allMatch(val -> val <= 0) ? 1 : -1;
    }

    @Override
    public OptimizationResult optimize() {
        return optimize(c.x0());
    }

}
