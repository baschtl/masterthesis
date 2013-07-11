package de.tub.writer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.util.DateTimeUtil;
import de.tub.util.FileUtil;
import de.tub.util.NumberUtil;
import de.tub.util.StringUtil;

/**
 * This class provides means to write array data to CSV files.
 * 
 * @author Sebastian Oelke
 *
 */
public class ArrayToCsvWriter {

	private static final Logger LOG = LoggerFactory.getLogger(ArrayToCsvWriter.class);
	
	public static final String COMMENT_LINE_START = "# ";
	public static final char CSV_SEPARATOR = ',';
	public static final String CSV_ENDING = ".csv";
	public static final String DATE_FORMAT = "yyyy-MM-dd_HH-mm-ss-SSS";
	
	/**
	 * A convenience method which uses <code>writeDoubles(double[][], String, String[])</code>
	 * and provides a <code>null</code> value for the third parameter.
	 * 
	 * @param data the data to be written as an array of an array of double values.
	 * @param toDir the directory to write the file to. If the directory does not exist it is created.
	 * 
	 * @see de.tub.writer.ArrayToCsvWriter#writeDoubles(double[][], String, String[])
	 */
	public static void writeDoubles(double[][] data, String toDir) {
		writeDoubles(data, toDir, null);
	}
	
	/**
	 * Writes the given array of array of double values to the given 
	 * directory. Each file starts with a time stamp of the creation 
	 * time of the file. Additional information can be given and is written 
	 * to the beginning of the file. Each line of the additional 
	 * information starts with a '#' character.
	 * <p />
	 * The additional information has to be provided as a string array.
	 * This array has to have an even number of elements. Hence, each 
	 * information has to be stored as a key value pair. Consider the
	 * two consecutive array elements <code>key</code> and <code>value</code>.
	 * The resulting line in the file would be <code># key: value</code>.
	 * <p />
	 * The data is separated by the ',' character.
	 * 
	 * @param data the data to be written as an array of an array of double values.
	 * @param toDir the directory to write the file to. If the directory does not exist it is created.
	 * @param other additional information which is written to the beginning of the file.
	 * This is omitted if a <code>null</code> value is provided.
	 * 
	 * @throws NullPointerException if the name of the output directory is <code>null</code>.
	 * @throws IllegalArgumentException if the name of the output directory is empty.
	 */
	public static void writeDoubles(double[][] data, String toDir, String[] other) throws NullPointerException, IllegalArgumentException {
		if (toDir == null)
			throw new NullPointerException(
				"You provided a null value for the output directory. " +
				"The writing to a file cannot be performed without a specified output directory.");
		else if (toDir.isEmpty())
			throw new IllegalArgumentException(
				"You provided an empty string for the output directory. " +
				"The writing to a file cannot be performed without a specified output directory.");
		
		if (data == null)
			LOG.warn("You provided a null value for the required data array. Without any input data the resulting file will be empty.");
		
		// Build file name
		String currentDateString = DateTimeUtil.currentDate(DATE_FORMAT);
		String fileName = StringUtil.concatWithFileSeparator(
								new String[] {
											StringUtil.validateFileSeparator(toDir), 
											currentDateString + CSV_ENDING
						});
		
		LOG.info("Writing to file: {}.", fileName);
		
		// Test if output directory exists
		if (!FileUtil.ifNotExistCreateDir(toDir)) {
			LOG.error("The output directory {} could not be created. The file could not be written.", toDir);
			return;
		}
		
		// Initialize file writer
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(fileName, true));
		} catch (IOException ioe) {
			LOG.error("An error occurred initializing the file writer with the file {}:\n{}", fileName, ioe);
			return;
		}
		
		// Write the data
		// Write date
		writer.append(COMMENT_LINE_START);
		writer.append("Created: ");
		writer.append(currentDateString);
		writer.println();
		
		// Write other given information
		if (other != null && other.length % 2 == 0) {
			for (int i = 0; i < other.length; i+=2) {
				writer.append(COMMENT_LINE_START);
				writer.append(other[i]);
				writer.append(": ");
				writer.append(other[i+1]);
				writer.println();
			}
			writer.println();
		}
		
		// Write data
		StringBuilder lineBuilder = new StringBuilder();
		DecimalFormat df = NumberUtil.decimalFormat();
		
		for (int i = 0; i < data.length; i++) {
			// Build one whole line
			for (int j = 0; j < data[i].length; j++) {
				lineBuilder.append(df.format(data[i][j]));
				// Only add item separator if we are not at the end of the line
				if (j != data[i].length - 1) lineBuilder.append(CSV_SEPARATOR);
			}
			// End the line by writing it
			writer.println(lineBuilder.toString());
			// Clear line builder
			lineBuilder.setLength(0);
		}
		
		// Flush and close the writer
		writer.flush();
		writer.close();
		
		LOG.debug("Finished writing to file.");
	}
}
