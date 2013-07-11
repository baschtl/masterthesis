package de.tub.reader.directory;

import de.tub.processor.IProcessor;
import de.tub.reader.IReader;

/**
 * This interface provides methods to access
 * a directory.
 * 
 * @author Sebastian Oelke
 *
 */
public interface IDirectoryReader extends IReader {

	/**
	 * Sets the folder to read.
	 * 
	 * @param directoryName the folder to read.
	 */
	void setDirectoryName(String directoryName);
	
	/**
	 * Returns the directory name.
	 * 
	 * @return the directory name.
	 */
	String getDirectoryName();
	
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
	 * Returns the reader of this reader.
	 * 
	 * @return the reader of this reader.
	 */
	IReader getReader();
	
	/**
	 * Sets the reader of this reader.
	 * 
	 * @param reader the reader to set.
	 */
	void setReader(IReader reader);
	
	/**
	 * Reads the given resource.
	 */
	void read();
}
