package de.tub.reader.file;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.observer.Interests;
import de.tub.observer.Subject;
import de.tub.processor.IProcessor;

/**
 * This reader reads a file with the given 
 * name from the file system line by line and 
 * delegates the processing of each line to the 
 * given processor. An <code>offset</code> can be 
 * specified to ignore a certain amount of lines 
 * from the beginning of the file.
 * <p />
 * The <code>TextFileLineReader</code> notifies its 
 * observers about its finishing of reading a file. 
 * 
 * @author Sebastian Oelke
 *
 */
public class TextFileLineReader extends Subject implements ITextFileReader, Cloneable {
	
	private static final Logger LOG = LoggerFactory.getLogger(TextFileLineReader.class);
	
	private String resourceName;
	private int offset;
	private IProcessor<String> processor;
	
	/**
	 * Standard constructor.
	 */
	public TextFileLineReader() {}
	
	/**
	 * Copy constructor for use with the Prototype design pattern.
	 * If you use <code>clone()</code> this constructor has to be used
	 * and all instance variables have to be copied.
	 *  
	 * @param textFileLineReader The <code>TextFileLineReader</code> which has to be copied.
	 * @see de.tub.reader.file.TextFileLineReader#clone()
	 */
	public TextFileLineReader(final TextFileLineReader textFileLineReader) {
		this.resourceName = textFileLineReader.getResourceName();
		this.offset = textFileLineReader.getOffset();
	}
	
	@Override
	public void read() {
		// No processor specified
		if (processor == null) {
			LOG.error("You have to specify a Processor (e.g., TextLineProcessor) for this Reader to work. A Reader is only " +
					"responsible for accessing a data resource. The processing is done by a Processor.");
			return;
		}
		// No file to read specified
		if (resourceName == null || resourceName.isEmpty()) {
			LOG.error("This TextFileLineReader requires the name of and the path to a text file.");
			return;
		}
		
		// Access the file specified by the file name and read it line by line
		try {
			BufferedReader br = new BufferedReader(new FileReader(resourceName));
		
			String line = "";
			
			// Ignore offset lines
			for (int i = 0; i < offset; i++)
				br.readLine();
			
			while( (line = br.readLine()) != null) {
				// Delegate the line data to the processor
				processor.newData(line);
			}
			
			// Notify all observers that the reader finished this resource
			notifyObservers(Interests.HasFinished, null);
		} catch (FileNotFoundException e) {
			LOG.error("The file {} could not be found:\n{}", resourceName, e);
		} catch (IOException e) {
			LOG.error("An error occurred while reading the file {}:\n{}", resourceName, e);
		}
	}
	
	//###################################################################
	// Setters & Getters
	//###################################################################
	
	/**
	 * Sets the file name.
	 */
	@Override
	public void setResourceName(String fileName) {
		this.resourceName = fileName;
	}
	
	/**
	 * Returns the file name.
	 */
	@Override
	public String getResourceName()	{
		return resourceName;
	}
	
	/**
	 * Returns the offset that defines from which line to start 
	 * reading the file.
	 * 
	 * @return the offset
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Sets the offset that defines from which line to start 
	 * reading the file.
	 * 
	 * @param offset the offset to set
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public IProcessor<String> getProcessor() {
		return processor;
	}

	@Override
	public void setProcessor(IProcessor<String> processor) {
		this.processor = processor;
	}
	
	//###################################################################
	// Other
	//###################################################################
	
	/**
	 * This method clones an instance of the <code>TextFileLineReader</code>
	 * class. Therefore it copies all of its properties to a new
	 * instance. 
	 *  
	 * @see java.lang.Object#clone()
	 */
	@Override
	public ITextFileReader clone() throws CloneNotSupportedException {
		return new TextFileLineReader(this);
	}
	
}
