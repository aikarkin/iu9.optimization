package ru.bmstu.iu9.optimization.md.gpm;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import ru.bmstu.iu9.optimization.conf.gpc.GradientProjectionConfig;
import ru.bmstu.iu9.optimization.geometry.VectorUtils;
import ru.bmstu.iu9.optimization.od.DichotomyMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.lang.Math.*;

public class GradientProjectionMethod {

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
        RealVector vecDir;
        double alphaOptimal;

        for (int k = 0; k < c.maxIterations(); k++) {
            RealVector gradF = gradient.apply(x);

            // если норма градиента стала слишком маленькой, выходим
            if (gradF.getNorm() < c.eps2()) {
                return x;
            }

            RealVector x1 = x;
            double alphaExt = DichotomyMethod.dichotomyMethod(
                    (alpha) -> objectiveFunc.apply(x1.subtract(gradF.mapMultiply(alpha))),
                    0.0,
                    c.alphaMax(),
                    c.alphaPrecision()
            );
            double alphaConstr = findMaxAlphaSatisfyingConstraints(x, gradF.mapMultiply(-1));

            // не вышли за границы, продолжаем движение в направлении антиградиента
            if (alphaExt <= alphaConstr) {
                alphaOptimal = alphaExt;
                vecDir = gradF.mapMultiply(-1);
            } else { // вышли за границу, проецируем градиент
                // перескакиваем на границу
                x = x.subtract(gradF.mapMultiply(alphaConstr));
                // определяем активные в точке x ограничения
                List<Integer> activeConstraintsIdxes = new ArrayList<>();
                for (int i = 0; i < constraints.size(); i++) {
                    Function<RealVector, Double> constr = constraints.get(i);
                    if (constr.apply(x) <= 0) {
                        activeConstraintsIdxes.add(i);
                    }
                }

                do {
                    RealMatrix matA = buildMatAWithConstraints(activeConstraintsIdxes, x);
                    vecDir = gradientProjection(matA, gradF);

                    if (vecDir.getNorm() > c.eps2()) {
                        break;
                    }

                    RealVector lambda = MatrixUtils.inverse(matA.multiply(matA.transpose()))
                            .multiply(matA)
                            .operate(gradF)
                            .mapMultiply(-1);

                    // Все элементы больше либо равны нулю - Ура!
                    // Похоже искомая точка найдена, следует проверить достаточные условия экстремума
                    if (VectorUtils.elementsAreGreaterOrEqualsZero(lambda)) {
                        return x;
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

                RealVector x2 = x;
                alphaConstr = findMaxAlphaSatisfyingConstraints(x, gradient.apply(x).mapMultiply(-1));
                alphaOptimal = DichotomyMethod.dichotomyMethod(
                        (alpha) -> objectiveFunc.apply(x2.subtract(gradient.apply(x2).mapMultiply(alpha))),
                        0.0,
                        alphaConstr,
                        c.alphaPrecision()
                );
            }

            x = x.add(vecDir.mapMultiply(alphaOptimal));
        }

        return x;
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
        double alpha = c.alpha0(), alphaPrev;

        RealVector x = x0.add(d.mapMultiply(alpha));

        if (satisfiesConstraints(x) > 0) {
            do {
                x = x0.add(d.mapMultiply(alpha));
                alpha *= 2.0;
            } while (satisfiesConstraints(x) > 0);
            alphaPrev = alpha / 4.0;
        } else {
            do {
                x = x0.add(d.mapMultiply(alpha));
                alpha /= 2.0;
            } while (satisfiesConstraints(x) < 0);
            alphaPrev = 4.0 * alpha;
        }

        double minAlpha = min(alpha, alphaPrev), maxAlpha = max(alpha, alphaPrev);

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

}
