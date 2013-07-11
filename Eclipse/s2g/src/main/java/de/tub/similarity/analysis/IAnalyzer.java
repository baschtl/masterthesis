package de.tub.similarity.analysis;

/**
 * Implementing classes provide means to compute the spatial similarity 
 * between two users based on their maximal length similar sequences.
 * 
 * @author Sebastian Oelke
 *
 */
public interface IAnalyzer<E> {

	/**
	 * Computes the similarity between two users based on their 
	 * maximal length similar sequences.
	 * 
	 * @return the spatial similarity score between two users.
	 */
	E analyze();
}
