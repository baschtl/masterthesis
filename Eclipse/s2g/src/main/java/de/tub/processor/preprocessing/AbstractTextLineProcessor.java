package de.tub.processor.preprocessing;

import de.tub.processor.IProcessor;


/**
 * The <code>AbstractTextLineProcessor</code> processes lines
 * of text with a standard delimiter of tab (i.e., '\t'). All 
 * implementing classes must override the <code>newData(String)</code>
 * method which implements the processing of data given to 
 * the Processor.
 * 
 * @author Sebastian Oelke
 *
 */
public abstract class AbstractTextLineProcessor implements IProcessor<String> {

	protected String delimiter = "\t";
	
	@Override
	public abstract void newData(String data);
	
	/**
	 * The default behaviour of this method is to do nothing. Override
	 * this method if a processor implementation needs to free resources 
	 * of any kind to finish its work.
	 */
	@Override
	public void finish() { }
	
	//###################################################################
	// Setters & Getters
	//###################################################################

	/**
	 * Returns the delimiter used to separate parts of a line of text.
	 * 
	 * @return the delimiter.
	 */
	public String getDelimiter() {
		return delimiter;
	}

	/**
	 * Sets the delimiter used to separate parts of a line of text. Call
	 * this method to change the standard delimiter before the processing
	 * of text.
	 * 
	 * @param delimiter the delimiter to set.
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
	
}
