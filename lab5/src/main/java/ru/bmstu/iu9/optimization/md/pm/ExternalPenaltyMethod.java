package ru.bmstu.iu9.optimization.md.pm;

import org.apache.commons.math3.linear.RealVector;
import ru.bmstu.iu9.optimization.conf.dmc.DichotomyMethodConfig;
import ru.bmstu.iu9.optimization.conf.pmc.PenaltyMethodConfig;
import ru.bmstu.iu9.optimization.conf.psc.PatternSearchConfig;
import ru.bmstu.iu9.optimization.md.OptimizationMethod;
import ru.bmstu.iu9.optimization.md.OptimizationResult;

import java.util.List;
import java.util.function.Function;

public class ExternalPenaltyMethod extends BasePenaltyMethod implements OptimizationMethod {

    public ExternalPenaltyMethod(
            Function<RealVector, Double> objectiveFunc,
            List<Function<RealVector, Double>> constraints,
            PatternSearchConfig psc,
            DichotomyMethodConfig dmc,
            PenaltyMethodConfig c
    ) {
        super(objectiveFunc, constraints, psc, dmc, c);
    }

    @Override
    public OptimizationResult optimize(RealVector x0) {
        return super.optimize(POSITIVE_SQUARE_FUNC, x0, c.betaExternal());
    }

    @Override
    public OptimizationResult optimize() {
        return optimize(c.externalPoint());
    }

}
