package de.tub.reader.model;

import de.tub.data.model.User;
import de.tub.processor.IProcessor;
import de.tub.reader.IReader;

/**
 * This interface provides methods to access
 * users from a database.
 * 
 * @author Sebastian Oelke
 *
 */
public interface IUserReader extends IReader {

	/**
	 * Returns the processor of this reader.
	 * 
	 * @return the processor of this reader.
	 */
	IProcessor<User> getProcessor();
	
	/**
	 * Sets the processor of this reader.
	 * 
	 * @param processor the processor to set.
	 */
	void setProcessor(IProcessor<User> processor);
	
	/**
	 * Reads the given resource.
	 */
	void read();
}
