package ru.bmstu.iu9.optimization.loader;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.math3.linear.MatrixUtils.createRealVector;
import static org.junit.Assert.*;

public class PropertiesLoaderTest {

    @Test
    public void testParseWithCustomMapping() throws IOException {
        URL resUrl = PropertiesLoader.class.getClassLoader().getResource("test.properties");
        assertNotNull(resUrl);
        PropertiesLoader<TestConfInterface> loader = new PropertiesLoader<>(new File(resUrl.getFile()), TestConfInterface.class);

        Map<String, String> fieldsMapping = new HashMap<>();

        fieldsMapping.put("constraintsWeights", "ru.bmstu.iu9.optimization.loader.constraintsWeights");
        fieldsMapping.put("startPoint", "ru.bmstu.iu9.optimization.loader.startPoint");
        fieldsMapping.put("epsilon", "ru.bmstu.iu9.optimization.loader.epsilon");
        fieldsMapping.put("beta", "ru.bmstu.iu9.optimization.loader.beta");
        fieldsMapping.put("r0", "ru.bmstu.iu9.optimization.loader.r0");
        fieldsMapping.put("coordSteps", "ru.bmstu.iu9.optimization.loader.coordSteps");
        fieldsMapping.put("delta", "ru.bmstu.iu9.optimization.loader.delta");


        TestConfInterface conf = loader.parse(fieldsMapping);

        assertEquals(
                createRealVector(new double[]{1.0, 1.0, 1.0, 1.0}),
                conf.getConstraintsWeights()
        );

        assertEquals(
                createRealVector(new double[]{0.0, 0.0}),
                conf.getStartPoint()
        );

        assertArrayEquals(
                new double[]{0.5, 0.5},
                conf.getCoordSteps(),
                0.0001
        );

        assertEquals(
                0.001,
                conf.getEpsilon(),
                0.0001
        );

        assertEquals(
                1.5,
                conf.getBeta(),
                0.0001
        );

        assertEquals(
                2.0,
                conf.getR0(),
                0.0001
        );

        assertEquals(
                0.005,
                conf.delta(),
                0.0001
        );
    }

    @Test
    public void testParseWithDefaultMapping() throws IOException {
        URL resUrl = PropertiesLoader.class.getClassLoader().getResource("test.properties");
        assertNotNull(resUrl);
        PropertiesLoader<TestConfInterface> loader = new PropertiesLoader<>(new File(resUrl.getFile()), TestConfInterface.class);

        TestConfInterface conf = loader.parse();

        assertEquals(
                createRealVector(new double[]{1.0, 1.0, 1.0, 1.0}),
                conf.getConstraintsWeights()
        );

        assertArrayEquals(
                new double[]{0.5, 0.5},
                conf.getCoordSteps(),
                0.0001
        );

        assertEquals(
                createRealVector(new double[]{0.0, 0.0}),
                conf.getStartPoint()
        );

        assertEquals(
                0.001,
                conf.getEpsilon(),
                0.0001
        );

        assertEquals(
                1.5,
                conf.getBeta(),
                0.0001
        );

        assertEquals(
                2.0,
                conf.getR0(),
                0.0001
        );

        assertEquals(
                0.005,
                conf.delta(),
                0.0001
        );
    }

}