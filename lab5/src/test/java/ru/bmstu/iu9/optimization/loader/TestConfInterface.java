package ru.bmstu.iu9.optimization.loader;

import org.apache.commons.math3.linear.RealVector;

public interface TestConfInterface {
    RealVector getConstraintsWeights();
    RealVector getStartPoint();
    double getEpsilon();
    double getBeta();
    double getR0();
    double[] getCoordSteps();
    double delta();
}
