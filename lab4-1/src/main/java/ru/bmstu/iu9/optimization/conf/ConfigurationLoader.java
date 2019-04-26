package ru.bmstu.iu9.optimization.conf;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import ru.bmstu.iu9.optimization.hj.HookeJeevesConf;
import ru.bmstu.iu9.optimization.nm.NelderMeadConf;
import ru.bmstu.iu9.optimization.onedim.DichotomyMethod;
import ru.bmstu.iu9.optimization.onedim.FibonacciMethod;
import ru.bmstu.iu9.optimization.onedim.GoldenSectionMethod;
import ru.bmstu.iu9.optimization.onedim.OneDimOptimizationMethod;
import ru.bmstu.iu9.optimization.onedim.conf.BaseOneDimConfiguration;
import ru.bmstu.iu9.optimization.onedim.conf.DichotomyMethodConf;
import ru.bmstu.iu9.optimization.onedim.conf.FibonacciMethodConf;
import ru.bmstu.iu9.optimization.onedim.conf.GoldenSectionConf;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ConfigurationLoader {

    public static final List<Class<? extends OneDimOptimizationMethod>> AVAILABLE_ONE_DIM_OPTIMIZATIONS;

    static {
        AVAILABLE_ONE_DIM_OPTIMIZATIONS = Arrays.asList(
                DichotomyMethod.class,
                GoldenSectionMethod.class,
                FibonacciMethod.class
        );
    }

    private HookeJeevesConf hjConf;
    private NelderMeadConf nmConf;
    private Map<Class<? extends OneDimOptimizationMethod>, BaseOneDimConfiguration> oneDimConfigurations = new HashMap<>();
    private Properties prop;

    public ConfigurationLoader(String filePath) throws Exception {
        hjConf = new HookeJeevesConf();
        nmConf = new NelderMeadConf();
        load(filePath);
    }

    public HookeJeevesConf hookeJeevesConf() {
        return hjConf;
    }

    public NelderMeadConf nelderMeadConf() {
        return nmConf;
    }

    public void setOneDimOptimizationMethod(Class<? extends OneDimOptimizationMethod> clazz) throws Exception {
        hjConf.oneDimOptimization = clazz.getConstructor().newInstance();
        hjConf.oneDimOptimizationConf = oneDimConfigurations.get(clazz);
    }

    private void load(String filePath) throws Exception {
        File propertiesFile = new File(filePath);
        prop = new Properties();

        InputStream stream = new FileInputStream(propertiesFile);
        prop.load(stream);

        hjConf.lambda = getDouble("optimization.hookeJeeves.lambda", 2.0);
        hjConf.eps = getDouble("optimization.hookeJeeves.eps", 0.1);
        hjConf.beta = getDouble("optimization.hookeJeeves.beta", 0.1);
        hjConf.steps = getListProp("optimization.hookeJeeves.steps");
        hjConf.startVector = getRealVector("optimization.hookeJeeves.startVector");


        for (var clazz : AVAILABLE_ONE_DIM_OPTIMIZATIONS) {
            loadOneDimConfiguration(clazz);
        }

        nmConf.sigma = getDouble("optimization.nelderMead.sigma", 0.001);
        nmConf.eps = getDouble("optimization.nelderMead.eps", 0.01);
        nmConf.psi = getDouble("optimization.nelderMead.psi", 0.01);
        nmConf.mu = getDouble("optimization.nelderMead.mu", 0.01);
        nmConf.maxIterationsCount = getInt("optimization.nelderMead.maxIterationsCount", 1000);
        nmConf.repairStep = getInt("optimization.nelderMead.repairStep", 10);
        nmConf.edgeLen = getDouble("optimization.nelderMead.edgeLen", 10);
        nmConf.startVector = getRealVector("optimization.nelderMead.startVector");
    }

    private RealVector getRealVector(String key) {
        return new ArrayRealVector(getListProp(key));
    }

    private int getInt(String key, int defaultValue) {
        return Integer.valueOf((String) prop.getOrDefault(key, String.valueOf(defaultValue)));
    }

    private double getDouble(String key, double defaultValue) {
        return Double.valueOf((String) prop.getOrDefault(key, String.valueOf(defaultValue)));
    }


    private double[] getListProp(String s) {
        String valuesStr = (String) prop.get(s);

        Matcher listMatcher = Pattern.compile("\\[(.*?)]").matcher(valuesStr);

        if (!listMatcher.matches()) {
            throw new RuntimeException("Failed to parse property: " + s);
        }

        String listValuesStr = listMatcher.group(1);
        String[] strElements = listValuesStr.split(",");
        double[] steps = new double[strElements.length];
        int[] iters = {0};

        Stream.of(strElements).map(Double::valueOf).forEach(val -> steps[iters[0]++] = val);
        return steps;
    }

    private void loadOneDimConfiguration(Class<? extends OneDimOptimizationMethod> oneDimOptimizationClass) {
        if (oneDimConfigurations.containsKey(oneDimOptimizationClass)) {
            return;
        }
        BaseOneDimConfiguration oneDimConf;

        if (oneDimOptimizationClass == DichotomyMethod.class) {
            oneDimConf = new DichotomyMethodConf();
        } else if (oneDimOptimizationClass == GoldenSectionMethod.class) {
            oneDimConf = new GoldenSectionConf();
        } else if (oneDimOptimizationClass == FibonacciMethod.class) {
            oneDimConf = new FibonacciMethodConf();
            ((FibonacciMethodConf) oneDimConf).sigma = getDouble("optimization.onedim.fibonacci.sigma", 0.01);
        } else {
            throw new RuntimeException("Unknown one dim optimization method: " + oneDimOptimizationClass);
        }

        oneDimConf.start = getDouble("optimization.onedim.start", -100);
        oneDimConf.end = getDouble("optimization.onedim.end", 100);
        oneDimConf.eps = getDouble("optimization.onedim.eps", 0.01);

        oneDimConfigurations.put(oneDimOptimizationClass, oneDimConf);
    }

}
