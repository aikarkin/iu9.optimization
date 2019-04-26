package ru.bmstu.iu9.optimization.conf;

import org.apache.commons.math3.linear.RealVector;

public class GradientDescendConf implements OptimizationConfig {
    public double sigma;
    public double eps;
    public double startAlpha;
    public int maxIterations;
    public RealVector startVector;
}
