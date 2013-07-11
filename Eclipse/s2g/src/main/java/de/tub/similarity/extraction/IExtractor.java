package de.tub.similarity.extraction;

/**
 * Implementing classes provide means to extract cluster sequences 
 * of two users that are based on the users hierarchical graphs.
 * 
 * @author Sebastian Oelke
 *
 */
public interface IExtractor<E> {
	
	/**
	 * Extracts the cluster sequences of two users on each level 
	 * of their hierarchical graphs. If the two users do not share
	 * any common clusters this method should stop all further
	 * execution and inform about this finding.
	 * 
	 * @return the cluster sequences of two users.
	 */
	E extract();
}
