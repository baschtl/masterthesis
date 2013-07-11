package de.tub.graph.generator;

/**
 * This interface declares methods that have to be provided by
 * generator classes.
 * 
 * @author Sebastian Oelke
 *
 */
public interface IGenerator<E> {

	/**
	 * Generates a specific property for the given input entity
	 * or any other part of the given input entity and attaches it
	 * to the entity.
	 * 
	 * @param input the entity to change a property for.
	 */
	void generate(E input);
}
