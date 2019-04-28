package ru.bmstu.iu9.optimization.md.pm;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import ru.bmstu.iu9.optimization.conf.dmc.DichotomyMethodConfig;
import ru.bmstu.iu9.optimization.conf.pmc.PenaltyMethodConfig;
import ru.bmstu.iu9.optimization.conf.psc.PatternSearchConfig;
import ru.bmstu.iu9.optimization.md.OptimizationResult;
import ru.bmstu.iu9.optimization.md.ps.PatternSearch;

import java.util.List;
import java.util.function.Function;

import static java.lang.Math.*;

public class BasePenaltyMethod {

    protected static Function<Double, Double> POSITIVE_SQUARE_FUNC = (x) -> pow(max(0, x), 2.0);

    protected static Function<Double, Double> HYPERBOLIC_FUNC = (x) -> -1 / x;

    protected static Function<Double, Double> LOG_NEG_ARG_FUNC = (x) -> -log(-x);

    protected static Function<Double, Double> COMBINED_PENALTY_FUNC = (x) ->
            (x <= 0) ? LOG_NEG_ARG_FUNC.apply(x) : POSITIVE_SQUARE_FUNC.apply(x);

    protected Function<RealVector, Double> objectiveFunc;
    protected List<Function<RealVector, Double>> constraints;
    protected PatternSearchConfig psc;
    protected DichotomyMethodConfig dmc;
    protected PenaltyMethodConfig c;

    public BasePenaltyMethod(
            Function<RealVector, Double> objectiveFunc,
            List<Function<RealVector, Double>> constraints,
            PatternSearchConfig psc,
            DichotomyMethodConfig dmc,
            PenaltyMethodConfig c
    ) {
        this.objectiveFunc = objectiveFunc;
        this.constraints = constraints;
        this.psc = psc;
        this.dmc = dmc;
        this.c = c;
    }

    protected OptimizationResult optimize(
            Function<Double, Double> constraintFunc,
            RealVector x0,
            double beta
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

                x = new PatternSearch(func, psc, dmc).optimize(x).getVector();
                penalty = r * penaltyFunc.apply(x);
                r *= beta;
                k++;
            } while (abs(penalty) > c.eps());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new OptimizationResult(x, objectiveFunc.apply(x), k);
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
