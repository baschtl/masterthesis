package de.tub.evaluation;

/**
 * This class provides resources to save the results of
 * evaluation metrics.
 * 
 * @author Sebastian Oelke
 *
 */
public class SimilarityEvaluation {

	public static final String SIMILARITY_MEAN = "similarity mean";
	public static final String SIMILARITY_MIN = "similarity min";
	public static final String SIMILARITY_MAX = "similarity max";
	public static final String SIMILAR_USER_PAIRS = "similar user pairs";
	
	private double similarityMean;
	private double min, max;
	private int similarUserPairs;
	
	//###################################################################
	// Instance methods
	//###################################################################
	
	public void increaseSimilarUserPairs() {
		this.similarUserPairs++;
	}
	
	//###################################################################
	// Setter & Getter
	//###################################################################
	
	/**
	 * @return the mean similarity value.
	 */
	public double getSimilarityMean() {
		return similarityMean;
	}
	/**
	 * @param similarityMean the mean similarity value to set.
	 */
	public void setSimilarityMean(double similarityMean) {
		this.similarityMean = similarityMean;
	}
	/**
	 * @return the number of pairs of users that have a similarity value greater than zero.
	 */
	public int getSimilarUserPairs() {
		return similarUserPairs;
	}
	/**
	 * @param similarUserPairs number of pairs of users that have a similarity value greater than zero to set.
	 */
	public void setSimilarUserPairs(int similarUserPairs) {
		this.similarUserPairs = similarUserPairs;
	}

	/**
	 * @return the minimal value.
	 */
	public double getMin() {
		return min;
	}

	/**
	 * @param min the minimal value to set.
	 */
	public void setMin(double min) {
		this.min = min;
	}

	/**
	 * @return the maximal value.
	 */
	public double getMax() {
		return max;
	}

	/**
	 * @param max the maximal value to set.
	 */
	public void setMax(double max) {
		this.max = max;
	}
	
}
