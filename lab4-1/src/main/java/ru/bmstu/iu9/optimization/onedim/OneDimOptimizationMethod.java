package ru.bmstu.iu9.optimization.onedim;

import ru.bmstu.iu9.optimization.conf.OptimizationConfig;

import java.util.function.Function;

public interface OneDimOptimizationMethod {
    double optimize(Function<Double, Double> objectiveFunc, double x, OptimizationConfig c);
}
