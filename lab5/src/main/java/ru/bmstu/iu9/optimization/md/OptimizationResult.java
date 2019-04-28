package ru.bmstu.iu9.optimization.md;

import org.apache.commons.math3.linear.RealVector;

public class OptimizationResult {
    private RealVector vector;
    private double functionValue;
    private int totalIterations = -1;

    public OptimizationResult(RealVector vector, double functionValue) {
        this.vector = vector;
        this.functionValue = functionValue;
    }

    public OptimizationResult(RealVector vector, double functionValue, int totalIterations) {
        this.vector = vector;
        this.functionValue = functionValue;
        this.totalIterations = totalIterations;
    }

    public RealVector getVector() {
        return vector;
    }

    public double getFunctionValue() {
        return functionValue;
    }

    public int getTotalIterations() {
        return totalIterations;
    }

}
