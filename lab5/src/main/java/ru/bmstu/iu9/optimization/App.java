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
import ru.bmstu.iu9.optimization.md.gpm.GradientProjectionMethod;

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
            // f(x, y) = 250 * (x^2 - y)^2 + 2 * (x - 1)^2 + 300
            Function<RealVector, Double> objectiveFunc = getRosenbrock2Function();
            GradientProjectionMethod gradientProjection = new GradientProjectionMethod(
                    objectiveFunc,
                    getRosenbrock2Gradient(),
                    getConstraints(),
                    getConstraintsDerivativeMatrix(),
                    gradientProjectionConfig
            );

            RealVector x0 = gradientProjection.optimize(gradientProjectionConfig.x0());
            System.out.println(x0);
//            RealVector sol = ModifiedLagrangianMethod.optimize(
//                    objectiveFunc,
//                    constraints,
//                    patternSearchConfig,
//                    dichotomyMethodConfig,
//                    modifiedLagrangianConfig
//            );
//            System.out.println(gradientProjectionConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private static Function<RealVector, RealMatrix> getRosenbrock2Hesian() {
        double a = globalConfig.rosenbrockA();
        double b = globalConfig.rosenbrockB();

        return (vec) -> {
            double x = vec.getEntry(0), y = vec.getEntry(1);

            return MatrixUtils.createRealMatrix(new double[][]{
                    {4 * a * (x * x - y) + 8 * a * x * x + 2 * b, -4 * a * x},
                    {- 4 * a * x, 2 * a}
            });
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
                (x) -> pow(x.getEntry(0), 2.0) + pow(x.getEntry(1), 2.0) - 40.0,
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
