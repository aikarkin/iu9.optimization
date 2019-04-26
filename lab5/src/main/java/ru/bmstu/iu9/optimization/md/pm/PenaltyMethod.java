package ru.bmstu.iu9.optimization.md.pm;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import ru.bmstu.iu9.optimization.conf.dmc.DichotomyMethodConfig;
import ru.bmstu.iu9.optimization.conf.pmc.PenaltyMethodConfig;
import ru.bmstu.iu9.optimization.conf.psc.PatternSearchConfig;

import java.util.List;
import java.util.function.Function;

import static java.lang.Math.*;
import static ru.bmstu.iu9.optimization.md.ps.PatternSearch.patternSearch;

public class PenaltyMethod {

    private static Function<Double, Double> POSITIVE_SQUARE_FUNC = (x) -> pow(max(0, x), 2.0);

    private static Function<Double, Double> HYPERBOLIC_FUNC = (x) -> -1 / x;

    private static Function<Double, Double> LOG_NEG_ARG_FUNC = (x) -> -log(-x);

    private static Function<Double, Double> COMPINED_PENALTY_FUNC = (x) ->
            (x <= 0) ? LOG_NEG_ARG_FUNC.apply(x) : POSITIVE_SQUARE_FUNC.apply(x);

    public static RealVector optimizeWithCombinedPenalty(
            Function<RealVector, Double> objectiveFunc,
            List<Function<RealVector, Double>> constraints,
            PatternSearchConfig psc,
            DichotomyMethodConfig dmc,
            PenaltyMethodConfig c
    ) {
        return optimizeWithPenaltyFunction(
                objectiveFunc,
                COMPINED_PENALTY_FUNC,
                constraints,
                c.combinedPenaltyPoint(),
                c.betaInternal(),
                psc,
                dmc,
                c
        );
    }

    public static RealVector optimizeWithExternalPenalty(
            Function<RealVector, Double> objectiveFunc,
            List<Function<RealVector, Double>> constraints,
            PatternSearchConfig psc,
            DichotomyMethodConfig dmc,
            PenaltyMethodConfig c
    ) {
        return optimizeWithPenaltyFunction(
                objectiveFunc,
                POSITIVE_SQUARE_FUNC,
                constraints,
                c.externalPoint(),
                c.betaExternal(),
                psc,
                dmc,
                c
        );
    }

    public static RealVector optimizeWithInternalHyperbolicPenalty(
            Function<RealVector, Double> objectiveFunc,
            List<Function<RealVector, Double>> constraints,
            PatternSearchConfig psc,
            DichotomyMethodConfig dmc,
            PenaltyMethodConfig c
    ) {
        return optimizeWithPenaltyFunction(
                objectiveFunc,
                HYPERBOLIC_FUNC,
                constraints,
                c.internalPoint(),
                c.betaInternal(),
                psc,
                dmc,
                c
        );
    }

    public static RealVector optimizeWithInternalLogPenalty(
            Function<RealVector, Double> objectiveFunc,
            List<Function<RealVector, Double>> constraints,
            PatternSearchConfig psc,
            DichotomyMethodConfig dmc,
            PenaltyMethodConfig c
    ) {
        return optimizeWithPenaltyFunction(
                objectiveFunc,
                LOG_NEG_ARG_FUNC,
                constraints,
                c.internalPoint(),
                c.betaInternal(),
                psc,
                dmc,
                c
        );
    }

    private static RealVector optimizeWithPenaltyFunction(
            Function<RealVector, Double> objectiveFunc,
            Function<Double, Double> constraintFunc,
            List<Function<RealVector, Double>> constraints,
            RealVector x0,
            double beta,
            PatternSearchConfig psc,
            DichotomyMethodConfig dmc,
            PenaltyMethodConfig c
    ) {
        Function<RealVector, Double> penaltyFunc = getPenaltyFunc(constraints, constraintFunc, c.weights());
        RealVector x = new ArrayRealVector(x0);
        int k = 0;
        double penalty, r = c.r0();

        try {
            do {
                double finalR = r;
                Function<RealVector, Double> func = (vec) -> {
                    double funcVal = objectiveFunc.apply(vec);
                    double penaltyFuncVal = penaltyFunc.apply(vec);
                    return funcVal + finalR * penaltyFuncVal;
                };

                x = patternSearch(func, x, psc, dmc);
                penalty = r * penaltyFunc.apply(x);
                r *= beta;
                k++;
            } while (abs(penalty) > c.eps());

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\t число итераций: " + k);

        return x;
    }

    private static Function<RealVector, Double> getPenaltyFunc(
            List<Function<RealVector, Double>> constraints,
            Function<Double, Double> constraintFunc,
            double[] weights) {
        return (x) -> {
            double penalty = 0.0;

            for (int i = 0; i < constraints.size(); i++) {
                penalty += weights[i] * constraints.get(i).andThen(constraintFunc).apply(x);
            }

            return penalty;
        };
    }

}
