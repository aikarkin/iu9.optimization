package ru.bmstu.iu9.optimization.conf;

import org.apache.commons.math3.linear.RealVector;

public class DavidFletcherPaulConf implements OptimizationConfig {

    public RealVector startVector;
    public double startAlpha;
    public double funcEps;
    public double gradEps;
    public double sigma;
    public int maxIterations;
    public int updateGMatrixIteration;

}
