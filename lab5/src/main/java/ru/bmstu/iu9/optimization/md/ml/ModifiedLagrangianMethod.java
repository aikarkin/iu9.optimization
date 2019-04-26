package ru.bmstu.iu9.optimization.md.ml;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import ru.bmstu.iu9.optimization.conf.dmc.DichotomyMethodConfig;
import ru.bmstu.iu9.optimization.conf.mlc.ModifiedLagrangianConfig;
import ru.bmstu.iu9.optimization.conf.psc.PatternSearchConfig;
import ru.bmstu.iu9.optimization.md.ps.PatternSearch;

import java.util.List;
import java.util.function.Function;

import static java.lang.Math.*;

public class ModifiedLagrangianMethod {

    public static RealVector optimize(
            Function<RealVector, Double> objectiveFunc,
            List<Function<RealVector, Double>> constraints,
            PatternSearchConfig psc,
            DichotomyMethodConfig dmc,
            ModifiedLagrangianConfig c
    ) {
        int k = 0;
        double r = c.r0(), penalty;
        var mu = c.muVector();
        var xOptimal = c.x0();

        do {
            var penaltyFunc = getPenaltyFunc(constraints, mu, c.weights(), r);
            Function<RealVector, Double> lagrangian = (x) ->
                    objectiveFunc.apply(x) + penaltyFunc.apply(x);

            xOptimal = PatternSearch.patternSearch(lagrangian, xOptimal, psc, dmc);
            penalty = penaltyFunc.apply(xOptimal);
            r *= c.beta();
            mu = calcMuVector(constraints, xOptimal, mu, r);
            k++;
        } while (abs(penalty) >= c.eps());

        System.out.printf("[info] Число итераций: %d%n", k);

        return xOptimal;
    }

    private static RealVector calcMuVector(List<Function<RealVector, Double>> constraints, RealVector x, RealVector mu, double r) {
        RealVector newMuVector = new ArrayRealVector(mu.getDimension());

        for (int i = 0; i < constraints.size(); i++) {
            newMuVector.setEntry(i, max(0, mu.getEntry(i) + r * constraints.get(i).apply(x)));
        }

        return newMuVector;
    }

    private static Function<RealVector, Double> getPenaltyFunc(
            List<Function<RealVector, Double>> constraints,
            RealVector muVector,
            RealVector weights,
            double r) {
        return (x) -> {
            double penalty = 0.0;

            for (int i = 0; i < constraints.size(); i++) {
                penalty += max(0, muVector.getEntry(i) + r * weights.getEntry(i) * constraints.get(i).apply(x))
                        - pow(muVector.getEntry(i), 2.0);
            }

            return r * penalty / 2.0;
        };
    }

}
