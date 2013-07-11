package de.tub.util;

/**
 * This class provides methods that are used when computing
 * the similarity of two users.
 * 
 * @author Sebastian Oelke
 *
 */
public class SimilarityUtil {
	
	/**
	 * Computes the parameter alpha that is based on the level of the
	 * maximal length similar sequences. Alpha is used to weight a sequence
	 * depending on the level it was found. A higher level means a higher 
	 * weighting.
	 * <p />
	 * Alpha is computed with the following formula:
	 * <pre>alpha = 2^(level - 1)</pre>
	 * 
	 * @param level the level of a maximal length similar sequence.
	 * @return the parameter alpha.
	 */
	public static double alpha(int level) {
		return Math.pow(2, level - 1);
	}
	
	/**
	 * Computes the parameter beta that is based on the length of a
	 * maximal length similar sequence. Beta is used to weight a sequence
	 * depending on its length. A longer length means a higher 
	 * weighting.
	 * <p />
	 * Beta is computed with the following formula:
	 * <pre>beta = 2^(length - 1)</pre>
	 * 
	 * @param length the length of a maximal length similar sequence.
	 * @return the parameter beta.
	 */
	public static double beta(int length) {
		return Math.pow(2, length - 1);
	}
	
	/**
	 * Computes the logarithm of the division of the total number of users and 
	 * the number of users that visited a specific hg cluster.
	 * 
	 * @param totalNumberOfUsers the total number of users.
	 * @param usersInHgCluster the number of users that visited a specific hg cluster.
	 * @return the inverse document frequency (IDF) value.
	 */
	public static double idfOfHgCluster(long totalNumberOfUsers, long usersInHgCluster) {			
		return Math.log( ((double) totalNumberOfUsers) / ((double) usersInHgCluster));
	}

}
