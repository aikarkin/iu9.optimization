package ru.bmstu.iu9.optimization.conf.loader;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import ru.bmstu.iu9.optimization.conf.*;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class PropertiesLoader<T extends OptimizationConfig> {

    private static final Map<Class<? extends OptimizationConfig>, Function<Properties, OptimizationConfig>> SUPPORTED_CONF;

    static {
        SUPPORTED_CONF = new HashMap<>();
        SUPPORTED_CONF.put(GradientDescendConf.class, PropertiesLoader::loadGradientDescendConfig);
        SUPPORTED_CONF.put(GoldenSectionConf.class, PropertiesLoader::loadGoldenSectionConfig);
        SUPPORTED_CONF.put(NonlinearConjugateGradientConf.class, PropertiesLoader::loadNonlinearConjugateGradientConf);
        SUPPORTED_CONF.put(DavidFletcherPaulConf.class, PropertiesLoader::loadDavidFletcherPaulConf);
        SUPPORTED_CONF.put(LevenbergMarquardtConf.class, PropertiesLoader::loadLevenbergMarquardtConf);
    }

    @SuppressWarnings("unchecked")
    public T load(Class<T> configClass, String filePath) throws IOException {
        if (!SUPPORTED_CONF.containsKey(configClass)) {
            throw new IllegalStateException("Unsupported optimization configuration class");
        }

        File propFile = new File(filePath);
        InputStream fileStream = new FileInputStream(propFile);

        Properties props = new Properties();
        props.load(fileStream);
        fileStream.close();

        return (T) SUPPORTED_CONF.get(configClass).apply(props);
    }

    private static OptimizationConfig loadLevenbergMarquardtConf(Properties properties) {
        LevenbergMarquardtConf conf = new LevenbergMarquardtConf();

        conf.startVector = parseVector(properties.get("optimization.order1.lmm.startVector"));
        conf.startMu = parseDouble(properties.get("optimization.order1.lmm.startMu"));
        conf.gradEps = parseDouble(properties.get("optimization.order1.lmm.gradEps"));
        conf.funcEps = parseDouble(properties.get("optimization.order1.lmm.funcEps"));
        conf.sigma = parseDouble(properties.get("optimization.order1.lmm.sigma"));
        conf.maxIterations = parseInt(properties.get("optimization.order1.lmm.maxIterations"));

        return conf;
    }

    private static OptimizationConfig loadDavidFletcherPaulConf(Properties props) {
        DavidFletcherPaulConf conf = new DavidFletcherPaulConf();

        conf.startVector = parseVector(props.get("optimization.order1.dfp.startVec"));
        conf.startAlpha = parseDouble(props.get("optimization.order1.dfp.startAlpha"));
        conf.funcEps = parseDouble(props.get("optimization.order1.dfp.funcEps"));
        conf.gradEps = parseDouble(props.get("optimization.order1.dfp.gradEps"));
        conf.sigma = parseDouble(props.get("optimization.order1.dfp.sigma"));
        conf.maxIterations = parseInt(props.get("optimization.order1.dfp.maxIterations"));
        conf.updateGMatrixIteration = parseInt(props.get("optimization.order1.dfp.updateGMatrixIter"));

        return conf;
    }

    private static OptimizationConfig loadGradientDescendConfig(Properties props) {
        GradientDescendConf gdmConf = new GradientDescendConf();

        gdmConf.eps = parseDouble(props.get("optimization.order1.gdm.eps"));
        gdmConf.sigma = parseDouble(props.get("optimization.order1.gdm.sigma"));
        gdmConf.maxIterations = parseInt(props.get("optimization.order1.gdm.maxIterations"));
        gdmConf.startVector = parseVector(props.get("optimization.order1.gdm.startPoint"));
        gdmConf.startAlpha = parseDouble(props.get("optimization.order1.gdm.alpha0"));

        return gdmConf;
    }

    private static OptimizationConfig loadGoldenSectionConfig(Properties props) {
        GoldenSectionConf gsmConf = new GoldenSectionConf();

        gsmConf.start = parseDouble(props.get("optimization.od.gsm.start"));
        gsmConf.end = parseDouble(props.get("optimization.od.gsm.end"));
        gsmConf.eps = parseDouble(props.get("optimization.od.gsm.eps"));

        return gsmConf;
    }

    private static OptimizationConfig loadNonlinearConjugateGradientConf(Properties props) {
        NonlinearConjugateGradientConf ncgConf = new NonlinearConjugateGradientConf();

        ncgConf.startVector = parseVector(props.get("optimization.order1.ncg.startPoint"));
        ncgConf.eps = parseDouble(props.get("optimization.order1.ncg.eps"));
        ncgConf.sigma = parseDouble(props.get("optimization.order1.ncg.sigma"));
        ncgConf.maxIterations = parseInt(props.get("optimization.order1.ncg.maxIterations"));
        ncgConf.startAlpha = parseDouble(props.get("optimization.order1.ncg.startAlpha"));

        return ncgConf;
    }

    private static double parseDouble(Object propValue) {
        return Double.valueOf((String) propValue);
    }

    private static int parseInt(Object propValue) {
        return Integer.valueOf((String) propValue);
    }

    private static RealVector parseVector(Object value) {
        String valuesStr = (String) value;

        Matcher listMatcher = Pattern.compile("\\[(.*?)]").matcher(valuesStr);

        if (!listMatcher.matches()) {
            throw new RuntimeException("Failed to parse property: " + value);
        }

        String listValuesStr = listMatcher.group(1);
        String[] strElements = listValuesStr.split(",");
        double[] steps = new double[strElements.length];
        int[] iters = {0};

        Stream.of(strElements).map(Double::valueOf).forEach(val -> steps[iters[0]++] = val);
        return new ArrayRealVector(steps);
    }

}
