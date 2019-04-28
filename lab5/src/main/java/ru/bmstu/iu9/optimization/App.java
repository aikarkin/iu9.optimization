package ru.bmstu.iu9.optimization;


import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import ru.bmstu.iu9.optimization.conf.dmc.DichotomyMethodConfig;
import ru.bmstu.iu9.optimization.conf.global.GlobalConfig;
import ru.bmstu.iu9.optimization.conf.gpc.GradientProjectionConfig;
import ru.bmstu.iu9.optimization.conf.mlc.ModifiedLagrangianConfig;
import ru.bmstu.iu9.optimization.conf.pmc.PenaltyMethodConfig;
import ru.bmstu.iu9.optimization.conf.psc.PatternSearchConfig;
import ru.bmstu.iu9.optimization.loader.PropertiesLoader;
import ru.bmstu.iu9.optimization.md.OptimizationMethod;
import ru.bmstu.iu9.optimization.md.OptimizationResult;
import ru.bmstu.iu9.optimization.md.gpm.GradientProjectionMethod;
import ru.bmstu.iu9.optimization.md.ml.ModifiedLagrangianMethod;
import ru.bmstu.iu9.optimization.md.pm.CombinedPenaltyMethod;
import ru.bmstu.iu9.optimization.md.pm.ExternalPenaltyMethod;
import ru.bmstu.iu9.optimization.md.pm.InternalPenaltyMethod;
import ru.bmstu.iu9.optimization.md.pm.PenaltyType;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.function.Function;

import static java.lang.Math.pow;
import static java.util.Arrays.asList;

public class App {

    private static final String CONFIG_FILE_NAME = "optimization.properties";

    private static GlobalConfig globalConfig;
    private static PenaltyMethodConfig penaltyMethodConfig;
    private static PatternSearchConfig patternSearchConfig;
    private static DichotomyMethodConfig dichotomyMethodConfig;
    private static ModifiedLagrangianConfig modifiedLagrangianConfig;
    private static GradientProjectionConfig gradientProjectionConfig;

    private static OptimizationMethod[] optimizationMethods;

    public static void main(String[] args) {
        ClassLoader classLoader = App.class.getClassLoader();
        URL configUrl = classLoader.getResource(CONFIG_FILE_NAME);

        if (configUrl == null) {
            System.err.println("Configuration file not found in classpath: " + CONFIG_FILE_NAME);
            return;
        }

        try {
            File configFile = new File(configUrl.getFile());
            loadConfiguration(configFile);
            initOptimizationMethods();

            for(OptimizationMethod optimization : optimizationMethods) {
                System.out.println("[info] Starting optimization: " + optimization.name());
                long start = System.currentTimeMillis();
                OptimizationResult res = optimization.optimize();
                long end = System.currentTimeMillis();
                System.out.printf("[info]\t x* = %s%n", res.getVector());
                System.out.printf("[info]\t f(x*) = %.3f%n", res.getFunctionValue());
                System.out.printf("[info]\t iterations: %d%n", res.getTotalIterations());
                System.out.printf("[info]\t execution time: %dms%n", end - start);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initOptimizationMethods() {
        Function<RealVector, Double> objectiveFunc = getRosenbrock2Function();
        Function<RealVector, RealVector> gradientFunc = getRosenbrock2Gradient();
        List<Function<RealVector, Double>> constraints = getConstraints();
        Function<RealVector, RealMatrix> activeConstraintsMatrix = getConstraintsDerivativeMatrix();

        optimizationMethods = new OptimizationMethod[]{
                new GradientProjectionMethod(objectiveFunc, gradientFunc, constraints, activeConstraintsMatrix, gradientProjectionConfig),
                new ModifiedLagrangianMethod(objectiveFunc, constraints, patternSearchConfig, dichotomyMethodConfig, modifiedLagrangianConfig),
                new ExternalPenaltyMethod(objectiveFunc, constraints, patternSearchConfig, dichotomyMethodConfig, penaltyMethodConfig),
                new InternalPenaltyMethod(objectiveFunc, constraints, patternSearchConfig, dichotomyMethodConfig, penaltyMethodConfig, PenaltyType.HYPERBOLIC),
                new InternalPenaltyMethod(objectiveFunc, constraints, patternSearchConfig, dichotomyMethodConfig, penaltyMethodConfig, PenaltyType.LOG_NEGATIVE),
                new CombinedPenaltyMethod(objectiveFunc, constraints, patternSearchConfig, dichotomyMethodConfig, penaltyMethodConfig)
        };
    }

    private static void loadConfiguration(File file) throws IOException {
        globalConfig = new PropertiesLoader<>(file, GlobalConfig.class).parse();
        penaltyMethodConfig = new PropertiesLoader<>(file, PenaltyMethodConfig.class).parse();
        patternSearchConfig = new PropertiesLoader<>(file, PatternSearchConfig.class).parse();
        dichotomyMethodConfig = new PropertiesLoader<>(file, DichotomyMethodConfig.class).parse();
        modifiedLagrangianConfig = new PropertiesLoader<>(file, ModifiedLagrangianConfig.class).parse();
        gradientProjectionConfig = new PropertiesLoader<>(file, GradientProjectionConfig.class).parse();
    }

    private static Function<RealVector, Double> getRosenbrock2Function() {
        double a = globalConfig.rosenbrockA();
        double b = globalConfig.rosenbrockB();
        double f0 = globalConfig.rosenbrockF0();

        return (vec) -> {
            double x = vec.getEntry(0), y = vec.getEntry(1);
            return a * pow(x * x - y, 2.0) + b * pow(x - 1, 2.0) + f0;
        };
    }

    private static Function<RealVector, RealVector> getRosenbrock2Gradient() {
        double a = globalConfig.rosenbrockA();
        double b = globalConfig.rosenbrockB();

        return (vec) -> {
            double x = vec.getEntry(0), y = vec.getEntry(1);
            return MatrixUtils.createRealVector(new double[] {
                    4 * a * x * (x * x - y) + 2 * b * (x - 1),
                    -2 * a * (x * x - y)
            });
        };
    }

    private static List<Function<RealVector, Double>> getConstraints() {
        return asList(
                (x) -> pow(x.getEntry(0), 2.0) + pow(x.getEntry(1), 2.0) - 0.8,
                (x) -> -x.getEntry(0),
                (x) -> -x.getEntry(1)
        );
    }

    private static Function<RealVector, RealMatrix> getConstraintsDerivativeMatrix() {
        return (vec) -> MatrixUtils.createRealMatrix(new double[][] {
                {2 * vec.getEntry(0), 2 * vec.getEntry(1)},
                {-1, 0},
                {0, -1}
        });
    }

}
