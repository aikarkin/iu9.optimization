package ru.bmstu.iu9.optimization.md.gpm;

import org.apache.commons.math3.linear.*;
import ru.bmstu.iu9.optimization.conf.gpc.GradientProjectionConfig;
import ru.bmstu.iu9.optimization.gemo.VectorUtils;
import ru.bmstu.iu9.optimization.od.DichotomyMethod;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.*;

public class GradientProjectionMethod {

    // Configuration:
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

    public RealVector optimize(RealVector x0) {
        RealVector x = new ArrayRealVector(x0);
        boolean shouldCheckDeltaX;

        for (int k = 0; k < c.maxIterations(); k++) {
            ActiveConstraintsParams activeConstraintsParams = findActiveConstraints(x);
            x = activeConstraintsParams.x;
            List<Integer> activeIdxes = activeConstraintsParams.indexes;
            RealMatrix matA = buildMatAWithConstraints(activeIdxes, x);
            RealVector lambda;
            RealVector gradF = gradient.apply(x);
            shouldCheckDeltaX = !VectorUtils.elementsAreZero(gradF);

            for (; ; ) {
                if (shouldCheckDeltaX) {
                    RealVector deltaX = calcDeltaX(matA, gradF);
                    if (deltaX.getNorm() > c.eps2()) {
                        double alpha = findOptimalAlphaSatisfyingConstraints(passiveConstraintsIdxes(activeIdxes), x, deltaX);
                        x = x.add(deltaX.mapMultiply(alpha));
                        break;
                    }
                    shouldCheckDeltaX = true;
                }

                lambda = MatrixUtils.inverse(matA.multiply(matA.transpose()))
                        .multiply(matA)
                        .operate(gradF)
                        .mapMultiply(-1);


                if (VectorUtils.elementsAreGreaterOrEqualsZero(lambda)) {
                    return x;
                } else {
                    int minLambdaIdx = 0;
                    for (int i = 1; i < lambda.getDimension(); i++) {
                        if (lambda.getEntry(i) < lambda.getEntry(minLambdaIdx)) {
                            minLambdaIdx = i;
                        }
                    }

                    activeIdxes.remove(minLambdaIdx);

                    if (activeIdxes.size() == 0) {
                        break;
                    }

                    matA = buildMatAWithConstraints(activeIdxes, x);
                }
            }

        }

        return x;
    }

    private RealVector calcDeltaX(RealMatrix matA, RealVector gradF) {
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

    private List<Integer> passiveConstraintsIdxes(List<Integer> activeConstraints) {
        Set<Integer> activeIdxesSet = new HashSet<>(activeConstraints);
        return IntStream.range(0, constraints.size())
                .filter(i -> !activeIdxesSet.contains(i))
                .boxed()
                .collect(Collectors.toList());
    }

    private ActiveConstraintsParams findActiveConstraints(RealVector x0) {
        List<Integer> activeIdxes = new ArrayList<>();
        int n = x0.getDimension(), m = constraints.size();
        boolean hasActiveConstraints;
        RealVector x = x0;
        RealVector vecTau;
        RealMatrix matA;

        for (; ; ) {
            hasActiveConstraints = false;

            // находим значения ограничений в точке x
            final RealVector xFinal = x;
            List<Double> constraintsValues = constraints
                    .stream()
                    .map(func -> func.apply(xFinal))
                    .collect(Collectors.toList());

            for (int i = 0; i < m; i++) {
                if (constraintsValues.get(i) >= c.eps1()) {
                    activeIdxes.add(i);
                    hasActiveConstraints = true;
                }
            }


            if (hasActiveConstraints) {
                activeIdxes.sort(Comparator.comparingDouble(i -> abs(constraintsValues.get(i))));
                activeIdxes = activeIdxes.subList(0, min(n, activeIdxes.size()));
                break;
            }

            for (int i = 0; i < m; i++) {
                activeIdxes.add(i);
            }
            activeIdxes.sort(Comparator.comparingDouble(i -> abs(constraintsValues.get(i))));
            activeIdxes = activeIdxes.subList(0, n);

            vecTau = new ArrayRealVector(n);
            for (int i = 0; i < n; i++) {
                vecTau.setEntry(i, -constraintsValues.get(activeIdxes.get(i)));
            }

            matA = buildMatAWithConstraints(activeIdxes, x);

            x = x.add(
                    matA.transpose()
                            .multiply(
                                    MatrixUtils.inverse(matA.multiply(matA.transpose()))
                            ).operate(vecTau)
            );

        }

        return new ActiveConstraintsParams(x, activeIdxes);
    }

    private double findOptimalAlphaSatisfyingConstraints(List<Integer> constraintsIdxes, RealVector x0, RealVector d) {
        double maxAlpha = findMaxAlphaSatisfyingConstraints(constraintsIdxes, x0, d);
        return DichotomyMethod.dichotomyMethod(
                (alpha) -> objectiveFunc.apply(x0.add(d.mapMultiply(alpha))),
                0.0,
                maxAlpha,
                c.alphaPrecision()
        );
    }

    private double findMaxAlphaSatisfyingConstraints(List<Integer> constraintIndexes, RealVector x0, RealVector d) {
        double alpha = c.alpha0(), alphaPrev;

        RealVector x = x0.add(d.mapMultiply(alpha));

        if (satisfiesConstraints(constraintIndexes, x) > 0) {
            do {
                x = x0.add(d.mapMultiply(alpha));
                alpha *= 2.0;
            } while (satisfiesConstraints(constraintIndexes, x) > 0);
            alphaPrev = alpha / 4.0;
        } else {
            do {
                x = x0.add(d.mapMultiply(alpha));
                alpha /= 2.0;
            } while (satisfiesConstraints(constraintIndexes, x) < 0);
            alphaPrev = 4.0 * alpha;
        }

        double minAlpha = min(alpha, alphaPrev), maxAlpha = max(alpha, alphaPrev);

        while (abs(minAlpha - maxAlpha) > c.alphaPrecision()) {
            double mid = (minAlpha + maxAlpha) / 2.0;
            if (
                    satisfiesConstraints(constraintIndexes, x0.add(d.mapMultiply(minAlpha)))
                            * satisfiesConstraints(constraintIndexes, x0.add(d.mapMultiply(mid))) < 0
            ) {
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

    private int satisfiesConstraints(List<Integer> activeIdxes, RealVector x) {
        return activeIdxes
                .stream()
                .map(i -> constraints.get(i).apply(x))
                .allMatch(val -> val <= 0) ? 1 : -1;
    }

    private static class ActiveConstraintsParams {

        RealVector x;
        List<Integer> indexes;

        ActiveConstraintsParams(RealVector x, List<Integer> indexes) {
            this.x = x;
            this.indexes = indexes;
        }

    }

}
