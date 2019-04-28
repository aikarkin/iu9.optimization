package ru.bmstu.iu9.optimization.md;

import org.apache.commons.math3.linear.RealVector;

public interface OptimizationMethod {

    OptimizationResult optimize(RealVector x0);

    OptimizationResult optimize();

    default String name() {
        return this.getClass().getSimpleName();
    }

}
