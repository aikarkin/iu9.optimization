import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class MatrixMul {

    @Test
    public void testMatrixMul() {
        RealMatrix matA = MatrixUtils.createRealMatrix(new double[][] {
                {1.0, 2.0},
                {4.0, 3.0}
        });

        RealMatrix matB = MatrixUtils.createRealMatrix(new double[][] {
                {3.0, 5.0},
                {1.0, 6.0}
        });

        RealMatrix matC = MatrixUtils.createRealMatrix(new double[][] {
                {1.0, 8.0},
                {7.0, 4.0}
        });

        assertEquals(
                matA.operate(MatrixUtils.createRealVector(new double[] {2.0, 3.0})),
                MatrixUtils.createRealVector(new double[] {8, 17})
        );

        assertEquals(
                MatrixUtils.createRealMatrix(new double[][] {
                        {124, 108},
                        {281, 272}
                }),
                matA.multiply(matB).multiply(matC)
        );

    }

}
