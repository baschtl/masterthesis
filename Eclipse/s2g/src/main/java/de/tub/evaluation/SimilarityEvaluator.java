package de.tub.evaluation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides methods for the evaluation
 * of the similarity measurement results. 
 * 
 * @author Sebastian Oelke
 *
 */
public class SimilarityEvaluator {
	
	private static final Logger LOG = LoggerFactory.getLogger(SimilarityEvaluator.class);
	
	/**
	 * Evaluates the given data array that was created during the similarity measurement.
	 * The following measures are computed: minimal and maximal similarity values, the arithmetic
	 * mean of all similarity values, and the number of user pairs that are similar in any way.
	 * 
	 * @param data the double array that resulted from a similarity measurement run.
	 * @return an instance of the <code>SimilarityEvaluation</code> class that holds the
	 * values for all calculated metrics. This instance can be empty (i.e., all values
	 * are set to their default) if the given data array is <code>null</code>.
	 * 
	 * @see de.tub.evaluation.SimilarityEvaluation SimilarityEvaluation
	 */
	public static SimilarityEvaluation evaluate(double[][] data) {
		SimilarityEvaluation evaluation = new SimilarityEvaluation();
		
		if (data == null) {
			LOG.warn("You provided a null value for the required data array. Without any input data no evaluation data can be created.");
			return evaluation;
		}
		
		double mean = 0;
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		int dataItems = 0;
		
		for (int i = 0; i < data.length; i++) {
			for (int j = i + 1; j < data[i].length; j++) {
				double current = data[i][j];
				// Count the data items
				dataItems++;
				
				// Increase the number of user pairs that have a similarity value greater than zero
				if (current > 0) {
					evaluation.increaseSimilarUserPairs();
					mean += current;
				}
				
				// Calculate min and max values
				if (current < min) min = current;
				if (current > max) max = current;
			}
		}
		
		// Compute mean and set it
		mean = mean / dataItems;
		evaluation.setSimilarityMean(mean);
		
		// Set min and max values
		evaluation.setMin(min);
		evaluation.setMax(max);
		
		return evaluation;
	}
	
}
