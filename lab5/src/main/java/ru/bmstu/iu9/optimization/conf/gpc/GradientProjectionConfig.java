package ru.bmstu.iu9.optimization.conf.gpc;

import org.apache.commons.math3.linear.RealVector;

public interface GradientProjectionConfig {

    RealVector x0();

    double eps1();

    double eps2();

    double alphaPrecision();

    int maxIterations();

    double alpha0();

}
