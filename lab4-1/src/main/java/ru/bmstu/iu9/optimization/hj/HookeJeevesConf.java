package ru.bmstu.iu9.optimization.hj;

import org.apache.commons.math3.linear.RealVector;
import ru.bmstu.iu9.optimization.conf.OptimizationConfig;
import ru.bmstu.iu9.optimization.onedim.OneDimOptimizationMethod;

public class HookeJeevesConf implements OptimizationConfig {

    public double eps;
    public double lambda;
    public double beta;

    public double[] steps;

    public RealVector startVector;
    public OneDimOptimizationMethod oneDimOptimization;
    public OptimizationConfig oneDimOptimizationConf;

}
