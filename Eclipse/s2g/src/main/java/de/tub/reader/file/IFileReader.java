package de.tub.reader.file;

import java.io.File;

import de.tub.processor.IProcessor;
import de.tub.reader.IReader;

/**
 * This interface provides methods to access
 * a file.
 * 
 * @author Sebastian Oelke
 *
 */
public interface IFileReader extends IReader {

	/**
	 * Sets the file to read.
	 * 
	 * @param file the file to read.
	 */
	void setFile(File file);
	
	/**
	 * Returns the file name.
	 * 
	 * @return the file name.
	 */
	File getFile();
	
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
	 * Reads the given file.
	 */
	void read();
}
