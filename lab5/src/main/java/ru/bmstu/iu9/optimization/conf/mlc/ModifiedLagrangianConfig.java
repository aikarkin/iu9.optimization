package ru.bmstu.iu9.optimization.conf.mlc;

import org.apache.commons.math3.linear.RealVector;

public interface ModifiedLagrangianConfig {
    RealVector muVector();
    RealVector x0();
    RealVector weights();
    double r0();
    double beta();
    double eps();
}