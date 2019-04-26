package ru.bmstu.iu9.optimization.geometry;

import org.apache.commons.math3.linear.RealVector;

import java.util.function.DoublePredicate;
import java.util.stream.DoubleStream;

public class VectorUtils {

    private VectorUtils() {
    }

    public static boolean elementsAreNegative(RealVector vec) {
        return elementsSatisfyPredicate(vec, (x) -> x < 0);
    }

    public static boolean elementsAreGreaterOrEqualsZero(RealVector vec) {
        return elementsSatisfyPredicate(vec, (x) -> x >= 0);
    }

    public static boolean elementsAreLessOrEqualsZero(RealVector vec) {
        return elementsSatisfyPredicate(vec, (x) -> x <= 0);
    }

    public static boolean elementsAreZero(RealVector vec) {
        return elementsSatisfyPredicate(vec, x -> x == 0);
    }

    private static boolean elementsSatisfyPredicate(RealVector vec, DoublePredicate test) {
        return DoubleStream.of(vec.toArray())
                .allMatch(test);
    }

}
