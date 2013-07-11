package de.tub.processor.preprocessing;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.observer.Interests;
import de.tub.observer.Observer;
import de.tub.observer.Subject;
import de.tub.reader.directory.IterativeDirectoryReader;

/**
 * This specific processor extracts the information of
 * latitude, longitude, date and time out of text lines
 * of a text file that has the format 
 * <i>latitude,longitude,0,altitude,date(as #days passed since 12/30/1899),date,time</i>.
 * <p />
 * 
 * This processor handles data from the data set version 1.3 of the 
 * GeoLife project. See the following reference for more information:<br />
 * {@link "http://research.microsoft.com/en-us/downloads/b16d359d-d164-469e-9fd4-daa38f2b2e13/"}
 * 
 * @author Sebastian Oelke
 *
 */
public class GpsLogLineProcessor extends AbstractTextLineProcessor implements Observer {
	
	private static final Logger LOG = LoggerFactory.getLogger(GpsLogLineProcessor.class);
	
	private StringTokenizer tokens;
	private String userId, latitude, longitude, timeStamp;
	
	private PrintWriter writer;
	
	/**
	 * This constructor takes a file name parameter which defines
	 * the full path and name to the file to write the results to.
	 * 
	 * @param outFileName the file to which to write results.
	 */
	public GpsLogLineProcessor(String outFileName) {
		this.delimiter = ",";
		
		// Initialize a file writer
		try {
			writer = new PrintWriter(new FileWriter(outFileName, true));
		} catch (IOException e) {
			LOG.error("An error occurred initializing the file writer with the file {}:\n{}", outFileName, e);
		}
	}
	
	@Override
	public void newData(String data) {
		// Return if their is no data or the reference is null
		if (data == null || data.isEmpty()) return;
		
		// Process the given data
		processData(data);
	}
	
	/**
	 * This method has to be called to finish the working of this Processor.
	 * It finishes the file to write to and empties used resources. It should
	 * be called when no additional data is given to this processor.
	 */
	@Override
	public void finish() {
		// Close the writer.
		if (writer != null) { 
			writer.flush();
			writer.close();
		}
	}
	
	/**
	 * Processes the given data. This method writes the data
	 * as a new line in the specified output file.
	 * 
	 * @param data the data to process.
	 */
	private void processData(String data) {
		tokens = new StringTokenizer(data, delimiter);
		
		// Save information of new GPS log entry: latitude, longitude, date, time
		if (tokens.hasMoreTokens()) latitude = tokens.nextToken();
		if (tokens.hasMoreTokens()) longitude = tokens.nextToken();
		
		// Skip three tokens as they contain irrelevant information
		for (int i = 0; i < 3; i++)
			if (tokens.hasMoreTokens()) tokens.nextToken();
		
		String date = "", time = "";
		
		if (tokens.hasMoreTokens()) date = tokens.nextToken();
		if (tokens.hasMoreTokens()) time = tokens.nextToken();
		
		// Create time stamp from date and time tokens
		timeStamp = date + " " + time;
		
		writeLastLineToFile();
	}
	
	/**
	 * Writes the information of the last line to a file.
	 */
	private void writeLastLineToFile() {
		if (writer != null) {
			writer.println(userId + "\t" +  
					latitude + "\t" + 
					longitude + "\t" +
					timeStamp);
		}
		else LOG.error("The writer was not properly initialized. Could not write to the file.");
	}

	@Override
	public void update(Subject theSubject, Interests interest, Object arg) {
		if (theSubject instanceof IterativeDirectoryReader) {
			// An IterativeDirectoryReader notifies us about new child directories that it reads
			if (interest == Interests.NewChildDirectory) {
				// A new user should be processed
				userId = (String) arg;
				LOG.debug("New user {}.", userId);
			} else if (interest == Interests.HasFinished) {
				// Finish this processor
				finish();
			}
		}
	}
}
