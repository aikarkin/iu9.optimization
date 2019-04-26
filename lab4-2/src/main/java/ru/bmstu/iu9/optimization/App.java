package ru.bmstu.iu9.optimization;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import ru.bmstu.iu9.optimization.conf.*;
import ru.bmstu.iu9.optimization.conf.loader.PropertiesLoader;
import ru.bmstu.iu9.optimization.dfpm.DavidFletcherPaulMethod;
import ru.bmstu.iu9.optimization.gdm.GradientDescendMethod;
import ru.bmstu.iu9.optimization.lmm.LevenbergMarquardtMethod;
import ru.bmstu.iu9.optimization.ncgm.NonlinearConjugateGradientMethod;

import java.util.function.Function;

import static java.lang.Math.pow;

public class App {

    private static final Function<RealVector, Double> FUNC = (vec) -> {
        double x, y;
        x = vec.getEntry(0);
        y = vec.getEntry(1);
        return 50.0 * pow(pow(x, 2.0) - y, 2.0) + 2 * pow(x - 1.0, 2.0) + 300.0;
    };

    private static final Function<RealVector, RealVector> GRAD_FUNC = (vec) -> {
        double x, y;
        x = vec.getEntry(0);
        y = vec.getEntry(1);
        return MatrixUtils.createRealVector(new double[]{
                4.0 * (25.0 * (pow(x, 3.0) - x * y) + x - 1),
                -50.0 * (pow(x, 2.0) - y)
        });
    };

    private static final Function<RealVector, RealMatrix> HESSIAN_FUNC = (vec) -> {
        double x, y;
        x = vec.getEntry(0);
        y = vec.getEntry(1);

        return MatrixUtils.createRealMatrix(new double[][]{
                {100 * (pow(x, 2.0) - y) + 200 * pow(x, 2.0) + 4, -100 * x},
                {-100 * x, 50}
        });
    };

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                throw new IllegalArgumentException("No properties file provided");
            }

            var properties = args[0];
            long startTimeMs = 0;
            PropertiesLoader<GradientDescendConf> gdmLoader = new PropertiesLoader<>();
            PropertiesLoader<GoldenSectionConf> gsmLoader = new PropertiesLoader<>();
            PropertiesLoader<NonlinearConjugateGradientConf> ncgLoader = new PropertiesLoader<>();
            PropertiesLoader<DavidFletcherPaulConf> dfpLoader = new PropertiesLoader<>();
            PropertiesLoader<LevenbergMarquardtConf> lmmLoader = new PropertiesLoader<>();

            GradientDescendConf gdmConf = gdmLoader.load(GradientDescendConf.class, properties);
            GoldenSectionConf gsmConf = gsmLoader.load(GoldenSectionConf.class, properties);
            NonlinearConjugateGradientConf ncgConf = ncgLoader.load(NonlinearConjugateGradientConf.class, properties);
            DavidFletcherPaulConf dfpConf = dfpLoader.load(DavidFletcherPaulConf.class, properties);
            LevenbergMarquardtConf llmConf = lmmLoader.load(LevenbergMarquardtConf.class, properties);

            System.out.println("[info] Метод наискорейшего спуска:");
            startTimeMs = System.currentTimeMillis();
            RealVector sol = GradientDescendMethod.optimize(FUNC, GRAD_FUNC, gdmConf.startVector, gdmConf, gsmConf);
            printSolution(sol, startTimeMs);

            System.out.println("[info] Метод сопряженных градиентов:");
            startTimeMs = System.currentTimeMillis();
            sol = NonlinearConjugateGradientMethod.optimize(FUNC, GRAD_FUNC, ncgConf.startVector, ncgConf, gsmConf);
            printSolution(sol, startTimeMs);

            System.out.println("[info] Метод Дэфида-Флетчера-Паулла:");
            startTimeMs = System.currentTimeMillis();
            sol = DavidFletcherPaulMethod.optimize(FUNC, GRAD_FUNC, dfpConf.startVector, dfpConf, gsmConf);
            printSolution(sol, startTimeMs);

            startTimeMs = System.currentTimeMillis();
            System.out.println("[info] Метод Левенберга-Марквардта:");
            sol = LevenbergMarquardtMethod.optimize(FUNC, GRAD_FUNC, HESSIAN_FUNC, llmConf.startVector, llmConf);
            printSolution(sol, startTimeMs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printSolution(RealVector sol, long startTime) {
        System.out.printf("[info] \t\t-> Время выполенения: %dms%n", System.currentTimeMillis() - startTime);
        System.out.println("[info] \t\t-> Точка экстремума: " + sol);
        System.out.println("[info] \t\t-> Значение функции: " + FUNC.apply(sol));
    }

}
