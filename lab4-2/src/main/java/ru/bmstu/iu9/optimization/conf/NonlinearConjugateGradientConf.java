package ru.bmstu.iu9.optimization.conf;

import org.apache.commons.math3.linear.RealVector;

public class NonlinearConjugateGradientConf implements OptimizationConfig {

    public RealVector startVector;
    public double eps;
    public double sigma;
    public int maxIterations;
    public double startAlpha;

}
