package ru.bmstu.iu9.optimization.md.ml;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import ru.bmstu.iu9.optimization.conf.dmc.DichotomyMethodConfig;
import ru.bmstu.iu9.optimization.conf.mlc.ModifiedLagrangianConfig;
import ru.bmstu.iu9.optimization.conf.psc.PatternSearchConfig;
import ru.bmstu.iu9.optimization.md.OptimizationMethod;
import ru.bmstu.iu9.optimization.md.OptimizationResult;
import ru.bmstu.iu9.optimization.md.ps.PatternSearch;

import java.util.List;
import java.util.function.Function;

import static java.lang.Math.*;

public class ModifiedLagrangianMethod implements OptimizationMethod {

    private Function<RealVector, Double> objectiveFunc;
    private List<Function<RealVector, Double>> constraints;
    private PatternSearchConfig psc;
    private DichotomyMethodConfig dmc;
    private ModifiedLagrangianConfig c;

    public ModifiedLagrangianMethod(
            Function<RealVector, Double> objectiveFunc,
            List<Function<RealVector, Double>> constraints,
            PatternSearchConfig psc,
            DichotomyMethodConfig dmc,
            ModifiedLagrangianConfig c
    ) {
        this.objectiveFunc = objectiveFunc;
        this.constraints = constraints;
        this.psc = psc;
        this.dmc = dmc;
        this.c = c;
    }

    @Override
    public OptimizationResult optimize(RealVector x0) {
        int k = 0;
        double r = c.r0(), penalty;
        var mu = c.muVector();
        var xOptimal = x0;

        do {
            var penaltyFunc = getPenaltyFunc(mu, r);
            Function<RealVector, Double> lagrangian = (x) ->
                    objectiveFunc.apply(x) + penaltyFunc.apply(x);

            xOptimal = new PatternSearch(lagrangian, psc, dmc).optimize(xOptimal).getVector();
            penalty = penaltyFunc.apply(xOptimal);
            r *= c.beta();
            mu = calcMuVector(xOptimal, mu, r);
            k++;
        } while (abs(penalty) >= c.eps());

        return new OptimizationResult(xOptimal, objectiveFunc.apply(xOptimal), k);
    }

    private RealVector calcMuVector(RealVector x, RealVector mu, double r) {
        RealVector newMuVector = new ArrayRealVector(mu.getDimension());

        for (int i = 0; i < constraints.size(); i++) {
            newMuVector.setEntry(i, max(0, mu.getEntry(i) + r * constraints.get(i).apply(x)));
        }

        return newMuVector;
    }

    private Function<RealVector, Double> getPenaltyFunc(RealVector muVector, double r) {
        return (x) -> {
            double penalty = 0.0;

            for (int i = 0; i < constraints.size(); i++) {
                penalty += max(0, muVector.getEntry(i) + r * c.weights().getEntry(i) * constraints.get(i).apply(x))
                        - pow(muVector.getEntry(i), 2.0);
            }

            return r * penalty / 2.0;
        };
    }

    @Override
    public OptimizationResult optimize() {
        return optimize(c.x0());
    }

}
