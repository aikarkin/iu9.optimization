package ru.bmstu.iu9.optimization.nm;

import org.apache.commons.math3.linear.RealVector;
import ru.bmstu.iu9.optimization.conf.OptimizationConfig;

public class NelderMeadConf implements OptimizationConfig {

    public double edgeLen;
    public double alpha;
    public double gamma;
    public double beta;
    public double mu;
    public double psi;
    public double sigma;
    public double eps;
    public int repairStep;
    public int maxIterationsCount;

    public RealVector startVector;

}
