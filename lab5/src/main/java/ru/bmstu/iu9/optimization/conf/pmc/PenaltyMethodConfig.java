package ru.bmstu.iu9.optimization.conf.pmc;

import org.apache.commons.math3.linear.RealVector;

public interface PenaltyMethodConfig {

    double[] weights();

    RealVector internalPoint();

    RealVector externalPoint();

    double r0();

    double betaInternal();

    double betaExternal();

    double eps();

    RealVector combinedPenaltyPoint();

}
