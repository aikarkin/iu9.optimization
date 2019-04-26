package ru.bmstu.iu9.optimization.conf;

import org.apache.commons.math3.linear.RealVector;

public class LevenbergMarquardtConf implements OptimizationConfig {

    public RealVector startVector;
    public double startMu;
    public double gradEps;
    public double funcEps;
    public double sigma;
    public int maxIterations;

}
