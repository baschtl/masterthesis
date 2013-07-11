package de.tub.similarity.matching;

/**
 * Implementing classes provide means to match cluster sequences 
 * of two users to find maximal length similar sequences for levels
 * of the users hierarchical graphs.
 * 
 * @author Sebastian Oelke
 *
 */
public interface IMatcher<E> {

	/**
	 * Matches the found sequences on the levels of the hierarchical 
	 * graphs of two users. This method should return all maximal length
	 * similar sequences for the two users for the levels for their 
	 * hierarchical graphs.
	 *  
	 * @return all maximal length similar sequences for two users for 
	 * the levels for their hierarchical graphs. If no maximal length
	 * similar sequences could be found an empty result should be returned.
	 */
	E match();
}
