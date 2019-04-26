package ru.bmstu.iu9.optimization.loader;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class PropertiesLoader<T> {

    private static Pattern GETTER_PATTERN = Pattern.compile("get([A-Z0-9].*?)");
    private Properties properties;
    private Class<T> configClass;

    public PropertiesLoader(File propertiesFile, Class<T> configClass) throws IOException {
        InputStream fileInputStream = new FileInputStream(propertiesFile);
        this.properties = new Properties();
        this.properties.load(fileInputStream);
        fileInputStream.close();
        this.configClass = configClass;
    }

    public T parse() {
        return this.parse(getDefaultMapping(configClass));
    }

    @SuppressWarnings("unchecked")
    public T parse(Map<String, String> configFieldsToKeysMapping) {
        return (T) Proxy.newProxyInstance(
                configClass.getClassLoader(),
                new Class[]{configClass},
                (proxy, method, methodArgs) -> {
                    Optional<String> propNameOpt = propertyNameFromGetter(method);

                    if (propNameOpt.isPresent()) {
                        String propertyName = propNameOpt.get();
                        if (configFieldsToKeysMapping.containsKey(propertyName)) {
                            return parseProperty(configFieldsToKeysMapping.get(propertyName), method.getReturnType());
                        }
                    }

                    return null;
                }
        );
    }

    @SuppressWarnings("unchecked")
    private <K> K parseProperty(String key, Class<K> keyType) {
        Object propValue = properties.get(key);

        if(propValue == null)
            return null;

        if (keyType == Double.TYPE) {
            return (K) Double.valueOf((String)propValue);
        } else if (keyType == Float.TYPE) {
            return (K) Float.valueOf((String)propValue);
        } else if (keyType == Integer.TYPE) {
            return (K) Integer.valueOf((String)propValue);
        } else if (keyType == Boolean.TYPE) {
            return (K) Boolean.valueOf((String)propValue);
        } else if (keyType == String.class) {
            return (K) properties.get(key);
        } else if (keyType == double[].class) {
            return (K) parseDoubleArray((String)propValue);
        } else if (inheritedFrom(keyType, RealVector.class)) {
            return (K) new ArrayRealVector(parseDoubleArray((String)propValue));
        } else {
            throw new UnsupportedOperationException("Unable to parse properties key for type: " + keyType);
        }
    }

    private static String toCamelCase(String str) {
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    private static Map<String, String> getDefaultMapping(Class<?> clazz) {
        Map<String, String> defaultMapping = new HashMap<>();
        String propPrefix = clazz.getPackage().getName() + ".";
        for (Method method : clazz.getDeclaredMethods()) {
            Optional<String> propNameOpt = propertyNameFromGetter(method);
            if (!propNameOpt.isPresent()) {
                continue;
            }
            String propName = propNameOpt.get();
            defaultMapping.put(propName, propPrefix + propName);
        }

        return defaultMapping;
    }

    private static boolean inheritedFrom(Class<?> targetClass, Class<?> testClass) {
        return (Arrays.stream(targetClass.getInterfaces()).anyMatch(curInt -> curInt == testClass))
                ||
                (targetClass.getSuperclass() == testClass)
                ||
                (targetClass == testClass);
    }

    private static double[] parseDoubleArray(String value) {
        Matcher listMatcher = Pattern.compile("\\[(.*?)]").matcher(value);

        if (!listMatcher.matches()) {
            throw new RuntimeException("Failed to parse property: " + value);
        }

        String listValuesStr = listMatcher.group(1);
        String[] strElements = listValuesStr.split(",");
        double[] array = new double[strElements.length];
        int[] iters = {0};

        Stream.of(strElements).map(Double::valueOf).forEach(val -> array[iters[0]++] = val);
        return array;
    }

    private static Optional<String> propertyNameFromGetter(Method method) {
        String methodName = method.getName();
        Matcher getterMatcher = GETTER_PATTERN.matcher(methodName);

        if (method.getReturnType().equals(Void.TYPE)) {
            return Optional.empty();
        }

        String propertyName = toCamelCase(getterMatcher.matches() ? getterMatcher.group(1) : methodName);
        return Optional.of(propertyName);
    }

}
