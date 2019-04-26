package ru.bmstu.iu9.optimization;

import org.apache.commons.math3.linear.RealVector;
import ru.bmstu.iu9.optimization.conf.ConfigurationLoader;
import ru.bmstu.iu9.optimization.hj.HookeJeevesMethod;
import ru.bmstu.iu9.optimization.nm.NelderMeadMethod;

import java.util.function.Function;

import static java.lang.Math.pow;


public class App {

    private static final Function<RealVector, Double> FUNC = (vec) -> {
        double x, y;
        x = vec.getEntry(0);
        y = vec.getEntry(1);
        return 50.0 * pow(pow(x, 2.0) - y, 2.0) + 2 * pow(x - 1.0, 2.0) + 300.0;
    };

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                throw new IllegalArgumentException("No properties file provided");
            }

            var properties = args[0];
            var confLoader = new ConfigurationLoader(properties);
            RealVector sol;

            System.out.println("[info] Запускаем метод Нелдера - Мида");
            long startTimeMs = System.currentTimeMillis();
            sol = NelderMeadMethod.optimize(FUNC, confLoader.nelderMeadConf().startVector, confLoader.nelderMeadConf());
            System.out.printf("[info]\t\t-> Время выполнения: %dms%n", System.currentTimeMillis() - startTimeMs);
            printSolution(sol);

            System.out.println("[info] Запускаем метод Хука - Дживса");
            for (var optimizationClass : ConfigurationLoader.AVAILABLE_ONE_DIM_OPTIMIZATIONS) {
                System.out.printf("[info] \t Одномерная оптимизация - %s:%n", optimizationClass.getSimpleName());
                confLoader.setOneDimOptimizationMethod(optimizationClass);
                startTimeMs = System.currentTimeMillis();
                sol = HookeJeevesMethod.optimize(FUNC, confLoader.hookeJeevesConf().startVector, confLoader.hookeJeevesConf());
                System.out.printf("[info]\t\t-> Время выполнения: %dms%n", System.currentTimeMillis() - startTimeMs);
                printSolution(sol);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printSolution(RealVector sol) {
        System.out.println("[info] \t\t-> Точка экстремума: " + sol);
        System.out.println("[info] \t\t-> Значение функции: " + FUNC.apply(sol));
    }

}
