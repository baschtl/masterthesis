package de.tub.normalization;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SimilarityNormalizerTest {

	@Test
	public void testNormalize() {
		double[][] data = new double[][] 
				{	new double[] {0, 0.2, 0.3, 0.4},
					new double[] {0, 0, 0.6, 0.7},
					new double[] {0, 0, 0, 0.8},
					new double[] {0, 0, 0, 0}};
		
		double[][] dataNormalized = SimilarityNormalizer.normalize(data);
		
		// Max is 0.9, min is 0.1
		// Expected result (note: only the strictly upper triangular matrix is normalized):
		// 0	0		0.1667	0.5
		// 0	0		0.6667	0.833
		// 0	0		0		1
		// 0	0		0		0
		
		assertEquals(0.0, dataNormalized[0][1], 0.01);
		assertEquals(0.1667, dataNormalized[0][2], 0.01);
		assertEquals(0.333, dataNormalized[0][3], 0.01);
		assertEquals(0.6667, dataNormalized[1][2], 0.01);
		assertEquals(0.833, dataNormalized[1][3], 0.01);
		assertEquals(1.0, dataNormalized[2][3], 0.01);
	}
}
