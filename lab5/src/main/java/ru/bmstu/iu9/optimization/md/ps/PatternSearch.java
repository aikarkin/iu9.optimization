package ru.bmstu.iu9.optimization.md.ps;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;
import ru.bmstu.iu9.optimization.conf.dmc.DichotomyMethodConfig;
import ru.bmstu.iu9.optimization.conf.psc.PatternSearchConfig;
import ru.bmstu.iu9.optimization.md.OptimizationMethod;
import ru.bmstu.iu9.optimization.md.OptimizationResult;

import java.util.Arrays;
import java.util.function.Function;

import static ru.bmstu.iu9.optimization.od.DichotomyMethod.dichotomyMethod;

public class PatternSearch implements OptimizationMethod {

    private PatternSearchConfig c;
    private DichotomyMethodConfig dmc;
    private Function<RealVector, Double> objectiveFunc;

    public PatternSearch(Function<RealVector, Double> objectiveFunc, PatternSearchConfig c, DichotomyMethodConfig dmc) {
        this.objectiveFunc = objectiveFunc;
        this.c = c;
        this.dmc = dmc;
    }

    @Override
    public OptimizationResult optimize(RealVector x0) {
        RealVector x1 = MatrixUtils.createRealVector(x0.toArray()), x2;
        double[] steps = Arrays.copyOf(c.steps(), c.steps().length);
        boolean terminate;
        int k = 0;

        do {
            k++;
            terminate = true;
            ExploringSearchRes esRes = exploringSearch(x1, steps);
            x2 = esRes.x;
            if (esRes.succeed) {
                RealVector d = x2.subtract(x1);
                RealVector startVec = x1;
                double lambda = dichotomyMethod(
                        t -> objectiveFunc.apply(startVec.add(d.mapMultiply(t))),
                        dmc.start(),
                        dmc.end(),
                        dmc.eps()
                );

                if (lambda >= c.eps()) {
                    terminate = false;
                }
                x2 = x1.add(d.mapMultiply(lambda));
            } else {
                for (int i = 0; i < steps.length; i++) {
                    if (steps[i] >= c.eps()) {
                        terminate = false;
                        steps[i] *= c.beta();
                    }
                }
            }

            x1 = x2;
        } while (!terminate);

        System.out.printf("[info]\t\t-> Число итераций: %d%n", k);

        return new OptimizationResult(x1, objectiveFunc.apply(x1), k);
    }

    @Override
    public OptimizationResult optimize() {
        return optimize(MatrixUtils.createRealVector(new double[]{0.0, 0.0}));
    }

    private ExploringSearchRes exploringSearch(RealVector x, double[] steps) {
        int n = x.getDimension();
        RealVector x1 = MatrixUtils.createRealVector(x.toArray());

        for (int i = 0; i < n; i++) {
            x1.setEntry(i, x.getEntry(i) + steps[i]);
            if (objectiveFunc.apply(x1) < objectiveFunc.apply(x)) {
                continue;
            }

            x1.setEntry(i, x.getEntry(i) - steps[i]);
            if (objectiveFunc.apply(x1) < objectiveFunc.apply(x)) {
                continue;
            }

            x1.setEntry(i, x.getEntry(i));
        }

        ExploringSearchRes res = new ExploringSearchRes(x1);
        res.succeed = objectiveFunc.apply(x1) < objectiveFunc.apply(x);

        return res;
    }

    private static class ExploringSearchRes {

        RealVector x;
        boolean succeed;

        ExploringSearchRes(RealVector x) {
            this.x = x;
        }

    }

}
