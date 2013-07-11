package de.tub.normalization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides methods to normalize the results of
 * the measurement of similarity.
 * 
 * @author Sebastian Oelke
 *
 */
public class SimilarityNormalizer {

	private static final Logger LOG = LoggerFactory.getLogger(SimilarityNormalizer.class);
	
	/**
	 * Normalizes all similarity values in the strictly upper triangular similarity matrix.
	 * 
	 * @param data the similarity matrix.
	 * @return the similarity matrix with all similarity scores normalized between 0 and 1.
	 */
	public static double[][] normalize(double[][] data) {
		if (data == null) {
			LOG.warn("You provided a null value for the required data array. Without any input data no normalization can be done.");
			return null;
		}
		
		double min = Double.MAX_VALUE;
    	double max = Double.MIN_VALUE;
    	
    	// Compute minimum and maximum of the similarity scores
    	for (int i = 0; i < data.length; i++) {
    		for (int j = i+1; j < data[i].length; j++) {
				double current = data[i][j];
				if (current > max) max = current;
				if (current < min) min = current;
			}
    	}
    	
    	// Compute normalized similarity score
    	for (int i = 0; i < data.length; i++) {
			for (int j = i+1; j < data[i].length; j++) {
				data[i][j] = (data[i][j] - min) / (max - min);
			}
		}
		
		return data;
	}
}
