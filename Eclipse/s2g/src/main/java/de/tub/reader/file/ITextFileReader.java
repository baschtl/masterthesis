package de.tub.reader.file;

import de.tub.processor.IProcessor;
import de.tub.reader.IReader;

/**
 * This interface provides methods to access
 * a text file.
 * 
 * @author Sebastian Oelke
 *
 */
public interface ITextFileReader extends IReader {

	/**
	 * Sets the resource to read.
	 * 
	 * @param resourceName the resource to read.
	 */
	void setResourceName(String resourceName);
	
	/**
	 * Returns the resource name.
	 * 
	 * @return the resource name.
	 */
	String getResourceName();
	
	/**
	 * Returns the processor of this reader.
	 * 
	 * @return the processor of this reader.
	 */
	IProcessor<String> getProcessor();
	
	/**
	 * Sets the processor of this reader.
	 * 
	 * @param processor the processor to set.
	 */
	void setProcessor(IProcessor<String> processor);
	
	/**
	 * Reads the given resource.
	 */
	void read();
}
