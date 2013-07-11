package de.tub.processor;

/**
 * This interface defines methods that a processor
 * of data should offer.
 * 
 * @author Sebastian Oelke
 *
 */
public interface IProcessor<E> {

	/**
	 * Give a processor new data to process.
	 * 
	 * @param data the data to process.
	 */
	void newData(E data);
	
	/**
	 * Finish the processors work and free resources as needed.
	 */
	void finish();
	
}
