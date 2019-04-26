package ru.bmstu.iu9.optimization.lab3.first;

import ru.bmstu.iu9.optimization.core.algo.OneDimAlgorithms;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.Math.*;
import static ru.bmstu.iu9.optimization.core.algo.OneDimAlgorithms.Interval;

@SuppressWarnings("Duplicates")
public class RunApplication {
    private static final double PRECISION = 0.001;
    private static final double START_POINT = -10;
    private static final double DELTA = 0.01;
    private static final int NO_OF_TESTS = 100_000;

    private static final Function<Double, Double> TARGET_FUNC;
    static {
//        TARGET_FUNC = (x) -> 10.0 * exp(-x*x) + 3 * x*x;
        TARGET_FUNC = (x) -> 40.0 * pow(pow(x, 2.0) - 6.0, 2.0) + pow(x - 1.0, 2.0) + abs(10 - x);
    }

    public static void main(String[] args) {
        Optional<Interval> startIntOpt = OneDimAlgorithms.findUnimodalIntervalWithSvennMethod(TARGET_FUNC, START_POINT, DELTA);

        if(!startIntOpt.isPresent()) {
            System.out.println("Данная функция не является унимадальной. Попробуйте другую функцию.");
            return;
        }

        Interval startInt = startIntOpt.get();
        System.out.printf("Алгоритм Свенна: %n");
        System.out.printf("\tсреднее время выполнения (алгоритм Свенна): %dns%n", measureSvenMethod());
        System.out.printf("\tначальный интервал неопределенности: %s%n", startInt);
        System.out.printf("\tчисло итераций: %d%n", OneDimAlgorithms.NO_OF_STEPS);


        System.out.printf("%n%s%n%n", new String(new char[27]).replaceAll("\0", "#"));


        System.out.println("1) Метод деления пополам: ");
        double xDichotomy = OneDimAlgorithms.extremaWithDichotomyMethod(TARGET_FUNC, startInt, PRECISION);
        System.out.printf("\tсреднее время выполнения: %dns%n", measureDichotomyMethodExecutionTime(startInt));
        System.out.printf("\tx=%.6f, y=%.6f;%n", xDichotomy, TARGET_FUNC.apply(xDichotomy));
        System.out.printf("\tчисло итераций: %d%n", OneDimAlgorithms.NO_OF_STEPS);

        System.out.printf("%s%n%n", new String(new char[27]).replaceAll("\0", "-"));

        System.out.println("2) Метод золотого сечения: ");
        double xGS = OneDimAlgorithms.extremaWithGoldenSectionMethod(TARGET_FUNC, startInt, PRECISION);
        System.out.printf("\tсреднее время выполнения: %dns%n", measureGoldenSectionMethodExecutionTime(startInt));
        System.out.printf("\tx=%.6f, y=%.6f;%n", xGS, TARGET_FUNC.apply(xGS));
        System.out.printf("\tчисло итераций: %d%n", OneDimAlgorithms.NO_OF_STEPS);

        System.out.printf("%s%n%n", new String(new char[27]).replaceAll("\0", "-"));


        System.out.println("3) Метод Фибоначи: ");
        double xFib = OneDimAlgorithms.extremaWithFibonacciMethod(TARGET_FUNC, startInt, 0.01, 0.01);
        System.out.printf("\tсреднее время выполнения: %dns%n", measureFibonacciMethodExecutionTime(startInt));
        System.out.printf("\tx=%.6f, y=%.6f;%n", xFib, TARGET_FUNC.apply(xFib));
        System.out.printf("\tчисло итераций: %d%n", OneDimAlgorithms.NO_OF_STEPS);
    }

    private static long measureDichotomyMethodExecutionTime(Interval startInt) {
        long startTime = System.nanoTime();
        double[] values = new double[NO_OF_TESTS];
        for (int i = 0; i < NO_OF_TESTS; i++) {
            values[i] = OneDimAlgorithms.extremaWithDichotomyMethod(RunApplication.TARGET_FUNC, startInt, PRECISION);
        }
        long time = (System.nanoTime() - startTime) / NO_OF_TESTS;
        double val = Arrays.stream(values).reduce(0.0, (prev, cur) -> prev + cur);
        System.out.println(val);
        return time;
    }

    private static long measureGoldenSectionMethodExecutionTime(Interval startInt) {
        long startTime = System.nanoTime();
        double[] values = new double[NO_OF_TESTS];
        for (int i = 0; i < NO_OF_TESTS; i++) {
            values[i] = OneDimAlgorithms.extremaWithGoldenSectionMethod(RunApplication.TARGET_FUNC, startInt, PRECISION);
        }
        long time = (System.nanoTime() - startTime) / NO_OF_TESTS;
        double val = Arrays.stream(values).reduce(0.0, (prev, cur) -> prev + cur);
        System.out.println(val);
        return time;
    }

    private static long measureFibonacciMethodExecutionTime(Interval startInt) {
        long startTime = System.nanoTime();
        double[] values = new double[NO_OF_TESTS];
        for (int i = 0; i < NO_OF_TESTS; i++) {
            values[i] = OneDimAlgorithms.extremaWithFibonacciMethod(RunApplication.TARGET_FUNC, startInt, PRECISION, PRECISION);
        }
        long time = (System.nanoTime() - startTime) / NO_OF_TESTS;
        double val = Arrays.stream(values).reduce(0.0, (prev, cur) -> prev + cur);
        System.out.println(val);
        return time;
    }

    private static long measureSvenMethod() {
        long startTime = System.nanoTime();
        Interval[] values = new Interval[NO_OF_TESTS];
        for (int i = 0; i < NO_OF_TESTS; i++) {
            values[i] = OneDimAlgorithms.findUnimodalIntervalWithSvennMethod(RunApplication.TARGET_FUNC, RunApplication.START_POINT, RunApplication.DELTA).get();
        }
        long time = (System.nanoTime() - startTime) / NO_OF_TESTS;
        Optional<Interval> intervalOptional = Arrays.stream(values)
                .reduce((prev, cur) -> new Interval(prev.getStart() + cur.getStart(), cur.getEnd() + prev.getEnd()));
        intervalOptional.ifPresent(System.out::println);

        return time;
    }

}
