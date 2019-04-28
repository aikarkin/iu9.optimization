package ru.bmstu.iu9.optimization.md.pm;

import org.apache.commons.math3.linear.RealVector;
import ru.bmstu.iu9.optimization.conf.dmc.DichotomyMethodConfig;
import ru.bmstu.iu9.optimization.conf.pmc.PenaltyMethodConfig;
import ru.bmstu.iu9.optimization.conf.psc.PatternSearchConfig;
import ru.bmstu.iu9.optimization.md.OptimizationMethod;
import ru.bmstu.iu9.optimization.md.OptimizationResult;

import java.util.List;
import java.util.function.Function;

public class InternalPenaltyMethod extends BasePenaltyMethod implements OptimizationMethod {

    private PenaltyType penaltyType;

    public InternalPenaltyMethod(
            Function<RealVector, Double> objectiveFunc,
            List<Function<RealVector, Double>> constraints,
            PatternSearchConfig psc,
            DichotomyMethodConfig dmc,
            PenaltyMethodConfig c,
            PenaltyType penaltyType
    ) {
        super(objectiveFunc, constraints, psc, dmc, c);
        this.penaltyType = penaltyType;
    }

    @Override
    public OptimizationResult optimize(RealVector x0) {
        Function<Double, Double> penaltyFunc = penaltyType == PenaltyType.HYPERBOLIC ? HYPERBOLIC_FUNC : LOG_NEG_ARG_FUNC;
        return super.optimize(penaltyFunc, x0, c.betaInternal());
    }

    @Override
    public OptimizationResult optimize() {
        return optimize(c.internalPoint());
    }

    @Override
    public String name() {
        return this.getClass().getSimpleName() + " [" + penaltyType.name() + "]";
    }

}
